package org.instedd.geochat.lgw.trans;

import java.util.ArrayList;

import org.instedd.geochat.lgw.Connectivity;
import org.instedd.geochat.lgw.GeoChatLgwSettings;
import org.instedd.geochat.lgw.Notifier;
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
			switch(getResultCode()) {
			case Activity.RESULT_OK:
				data.deleteOutgoingMessage(guid);
				break;
			default:
				data.markOutgoingMessageAsBeingSent(guid);
				data.log("A message could not be sent to phone");
				break;
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
	            data.createIncomingMessage(
	            		SmsMessage.createFromPdu((byte[]) pdus[i]));
	        
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
	Object sleepLock;
	

	public Transceiver(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.notifier = new Notifier(context);
		this.settings = new GeoChatLgwSettings(context);
		this.data = new GeoChatLgwData(context, settings.getNumber());
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
		if (sleepLock != null) {
			synchronized(sleepLock) {
				sleepLock.notify();
			}
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
			sms.sendTextMessage(message.to, null, parts.get(i), 
					PendingIntent.getBroadcast(context, 0, 
							new Intent(SMS_SENT_ACTION)
								.putExtra(INTENT_EXTRA_GUID, message.guid), 0), 
					null);
		}
	}
	
	class SyncThread extends Thread {
		
		boolean firstRun = true;
		
		@Override
		public void run() {
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
									log.append("Sent address: ").append(number).append("\n");
								} catch (QstClientException e) {
									log.append("Couldn't send address: ").append(e.getMessage()).append("\n");
								}
								firstRun = false;
							}
							
							// 1.a. Get incoming messages
							Message[] incoming = data.getIncomingMessages();
							
							// 1.b. Send them to the application
							String receivedId = client.sendMessages(incoming);
							if (incoming != null) {
								switch(incoming.length) {
								case 0:
									break;
								case 1:
									log.append("Sent 1 message to application.\n");
									break;
								default:
									log.append("Sent ").append(incoming.length).append(" message to application.\n");
									break;
								}
							}
							
							// 1.c. Delete previous incoming messages
							if (receivedId != null)
								data.deleteIncomingMessageUpTo(receivedId);
							
							if (resync)	continue;
							
							// 2. Send pending messages (those that were sent at least once and failed)
							Message[] pending = data.getOutgoingMessagesNotBeingSentAndMarkAsBeingSent();
							sendMessages(pending);
							
							if (pending != null) {
								switch(pending.length) {
								case 0:
									break;
								case 1:
									log.append("Sent 1 previously failed message to phone");
									break;
								default:
									log.append("Sent ").append(pending.length).append(" previously failed messages to phone.\n");
									break;
								}
							}
							
							if (resync)	continue;
							
							// 3.a. Get outgoing messages
							Message[] outgoing = client.getMessages(settings.getLastReceivedMessageId());
							
							// 3.b. Persist them and mark them as being sent
							String lastReceivedMessageId = data.createOutgoingMessagesAsBeingSent(outgoing);
							
							// 3.c. Send them via phone
							if (outgoing != null) {
								switch(outgoing.length) {
								case 0:
									break;
								case 1:
									log.append("Sent 1 message to phone");
									break;
								default:
									log.append("Sent ").append(pending.length).append(" messages to phone.\n");
									break;
								}
							}
							sendMessages(outgoing);
							
							// 3.d. Remember last id
							if (lastReceivedMessageId != null)
								settings.setLastReceivedMessageId(lastReceivedMessageId);
						} catch (Throwable t) {
							log.append("Fatal error: " + t.getMessage());
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
					sleepLock = new Object();
					synchronized (sleepLock) {
						try {
							sleepLock.wait(1000 * 60 * 1);
						} catch (InterruptedException e) {
						}
					}
					sleepLock = null;
				}
			}
		}
		
	}

}
