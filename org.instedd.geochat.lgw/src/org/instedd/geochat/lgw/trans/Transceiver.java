package org.instedd.geochat.lgw.trans;

import java.util.ArrayList;

import org.instedd.geochat.lgw.Connectivity;
import org.instedd.geochat.lgw.GeoChatLgwSettings;
import org.instedd.geochat.lgw.Notifier;
import org.instedd.geochat.lgw.R;
import org.instedd.geochat.lgw.data.GeoChatLgwData;
import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.QstClientException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

public class Transceiver {
	
	private final static String SMS_SENT_ACTION = "org.instedd.geochat.lgw.SMS_SENT_ACTION";
	private final static String INTENT_EXTRA_GUID = "org.instedd.geochat.lgw.Guid";
	
	private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String guid = intent.getExtras().getString(INTENT_EXTRA_GUID);
			Message msg = data.getOutgoingMessage(guid);
			switch(getResultCode()) {
			case Activity.RESULT_OK:
				data.deleteOutgoingMessage(guid);
				if (msg != null) {
					data.log(context.getResources().getString(R.string.sent_message_to_phone, msg.text, msg.to));
				}
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				data.markOutgoingMessageAsNotBeingSent(guid);
				if (msg != null) {
					data.log(context.getResources().getString(R.string.message_could_not_be_sent_no_service, msg.text, msg.to));
				}
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				data.markOutgoingMessageAsNotBeingSent(guid);
				if (msg != null) {
					data.log(context.getResources().getString(R.string.message_could_not_be_sent_radio_off, msg.text, msg.to, msg.tries));
				}
				break;
			default:
				if (msg != null) {
					msg.tries++;
					if (msg.tries >= 3) {
						data.log(context.getResources().getString(R.string.message_could_not_be_sent_tries_deleting, msg.text, msg.to, msg.tries));
						data.deleteOutgoingMessage(guid);
					} else {
						data.log(context.getResources().getString(R.string.message_could_not_be_sent_tries, msg.text, msg.to, msg.tries));
						data.markOutgoingMessageAsNotBeingSent(guid, msg.tries);
					}
				}
				break;
			}
			
			synchronized (sendLock) {
				sendLock.notify();
			}
		}
	};
	
	private BroadcastReceiver smsReceivedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
	        if (extras == null)
	            return;
	        
	        Object[] pdus = (Object[]) extras.get("pdus");
	        for (int i = 0; i < pdus.length; i++)
	            data.createIncomingMessage(SmsMessage.createFromPdu((byte[]) pdus[i]), settings.getNumber());
	        
	        resync();
		}
	};

	final Context context;
	final Handler handler;
	final Notifier notifier;
	final GeoChatLgwData data;
	final GeoChatLgwSettings settings;
	QstClient client;
	SyncThread syncThread;
	boolean connectivityChanged;
	boolean running;
	boolean resync;
	Object sleepLock = new Object();
	Object sendLock = new Object();

	public Transceiver(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.notifier = new Notifier(context);
		this.settings = new GeoChatLgwSettings(context);
		this.data = new GeoChatLgwData(context);
		recreateQstClient();		
	}
	
	public void recreateQstClient() {
		this.client = settings.newQstClient();
	}

	public void start() {
		if (running) return;
		
		running = true;
		
		context.registerReceiver(smsSentReceiver, new IntentFilter(SMS_SENT_ACTION));
		context.registerReceiver(smsReceivedReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		
		syncThread = new SyncThread();
		syncThread.start();
	}

	public void stop() {
		context.unregisterReceiver(smsSentReceiver);
		context.unregisterReceiver(smsReceivedReceiver);
		
		running = false;
		this.resync();
	}

	public void connectivityChanged() {
		this.connectivityChanged = true;
	}

	public void resync() {
		synchronized(sleepLock) {
			sleepLock.notify();
		}
		this.resync = true;
	}
	
	void sendMessages(Message[] messages) {
		if (messages == null || messages.length == 0)
			return;
			
		for(Message msg : messages)
			sendMessage(msg);
	}
	
	void sendMessage(Message message) {
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message.text);
		for (int i = 0; i < parts.size(); i++) {
			Intent intent = new Intent(SMS_SENT_ACTION).putExtra(INTENT_EXTRA_GUID, message.guid);
			sms.sendTextMessage(message.to, null, parts.get(i), 
					PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT), 
					null);
		}
		
		synchronized (sendLock) {
			try {
				sendLock.wait(10000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	class SyncThread extends Thread {
		
		boolean firstRun = true;
		
		@Override
		public void run() {
			Resources r = context.getResources();
			
			// Since maybe some messages were marked as being sent and the application
			// was exited or crashed, mark all of them as not being sent so we can
			// send them again
			data.markOutgoingMessagesAsNotBeingSent();
			
			boolean hasConnectivity = false;
			while(running) {
				try {
					notifier.startTranscieving();
					hasConnectivity = Connectivity.hasConnectivity(context);
					resync = false;
					
					if (hasConnectivity) {
						StringBuilder log = new StringBuilder();
						
						try {
							// 0. Send address
							if (firstRun) {
								String number = settings.getNumber();
								try {
									client.sendAddress(number);
									log.append(r.getString(R.string.sent_your_number, number)).append("\n");
								} catch (QstClientException e) {
									log.append(r.getString(R.string.couldnt_send_your_number, e.getMessage())).append("\n");
								}
								firstRun = false;
							}
							
							// 1.a. Get incoming messages
							Message[] incoming = data.getIncomingMessages();
							
							// 1.b. Send them to the application
							String receivedId = client.sendMessages(incoming);
							if (incoming != null) {
								for(Message msg : incoming) {
									log.append(r.getString(R.string.sent_message_to_application, msg.text, msg.from)).append("\n");
								}
							}
							
							// 1.c. Delete previous incoming messages
							if (receivedId != null)
								data.deleteIncomingMessageUpTo(receivedId);
							
							if (resync)	continue;
							
							// 2. Send pending messages (those that were sent at least once and failed)
							Message[] pending = data.getOutgoingMessagesNotBeingSentAndMarkAsBeingSent();
							sendMessages(pending);
							
							if (resync)	continue;
							
							// 3.a. Get outgoing messages
							Message[] outgoing = client.getMessages(settings.getLastReceivedMessageId());
							
							// 3.b. Persist them and mark them as being sent
							String lastReceivedMessageId = data.createOutgoingMessagesAsBeingSent(outgoing);
							
							// 3.c. Send them via phone
							sendMessages(outgoing);
							
							// 3.d. Remember last id
							if (lastReceivedMessageId != null)
								settings.setLastReceivedMessageId(lastReceivedMessageId);
						} catch (Throwable t) {
							log.append(r.getString(R.string.fatal_error, t.getMessage())).append("\n");
						} finally {
							if (!TextUtils.isEmpty(log)) {
								data.log(log.toString().trim());
							}
						}
					}
				} finally {
					if (hasConnectivity) { 
						notifier.stopTransceiving();
					} else {
						notifier.offline();
					}
				}
				
				// Wait 1 minutes
				if (!resync) {
					try {
						synchronized (sleepLock) {
							sleepLock.wait(settings.getRefreshRateInMilliseconds());
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
	}

}
