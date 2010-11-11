package org.instedd.geochat.lgw.trans;

import java.util.ArrayList;
import java.util.UUID;

import org.instedd.geochat.lgw.Connectivity;
import org.instedd.geochat.lgw.GeoChatLgwSettings;
import org.instedd.geochat.lgw.Notifier;
import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;
import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.QstClientException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class Transceiver {
	
	private final static String SMS_SENT_ACTION = "org.instedd.geochat.lgw.SMS_SENT_ACTION";
	private final static String INTENT_EXTRA_GUID = "org.instedd.geochat.lgw.Guid";
	
	private final static Uri NOT_SENDING_URI = Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "not_sending");
	private final static ContentValues SENDING_CONTENT_VALUES = new ContentValues();
	static {
		SENDING_CONTENT_VALUES.put(OutgoingMessages.SENDING, 1);
	}
	private final static ContentValues NOT_SENDING_CONTENT_VALUES = new ContentValues();
	static {
		NOT_SENDING_CONTENT_VALUES.put(OutgoingMessages.SENDING, 0);
	}
	
	private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SMS_SENT_ACTION.equals(intent.getAction())) {
				String guid = intent.getExtras().getString(INTENT_EXTRA_GUID);
				Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "guid"), guid);
				
				switch(getResultCode()) {
				case Activity.RESULT_OK:
					Transceiver.this.context.getContentResolver().delete(uri, null, null);
					break;
				default:
					synchronized(notSendingLock) {
						Transceiver.this.context.getContentResolver().update(uri, NOT_SENDING_CONTENT_VALUES, null, null);
					}
					break;
				}
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
	        for (int i = 0; i < pdus.length; i++) {
	            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
	            
	            String guid = UUID.randomUUID().toString();
	            String from = message.getOriginatingAddress();
	            String to = settings.getNumber();
	            String text = message.getMessageBody();
	            long when = message.getTimestampMillis();
	            
	            Transceiver.this.context.getContentResolver().insert(IncomingMessages.CONTENT_URI, 
	            		Message.toContentValues(guid, from, to, text, when));
	        }
	        
	        resync();
		}
	};

	final Context context;
	final Handler handler;
	final Notifier notifier;
	GeoChatLgwSettings settings;
	QstClient client;
	SyncThread syncThread;
	boolean connectivityChanged;
	boolean running;
	boolean resync;
	Object sleepLock;
	Object notSendingLock = new Object();

	public Transceiver(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.notifier = new Notifier(context);
		this.settings = new GeoChatLgwSettings(context);
		this.client = this.settings.newQstClient();
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
		
		@Override
		public void run() {
			boolean hasConnectivity = false;
			
			while(running) {
				try {
					notifier.startTranscieving();
					hasConnectivity = Connectivity.hasConnectivity(context);
					resync = false;
					
					if (hasConnectivity) {
						try {
							Cursor c;
							
							// Send incoming messages (to be sent to the app)
							c = context.getContentResolver().query(IncomingMessages.CONTENT_URI, Message.PROJECTION, null, null, null);
							try {
								if (c.getCount() > 0) {
									Message[] incoming = new Message[c.getCount()];
									for (int i = 0; c.moveToNext(); i++)
										incoming[i] = Message.readFrom(c);
									
									String receivedId = client.sendMessages(incoming);
									context.getContentResolver().delete(Uri.withAppendedPath(IncomingMessages.CONTENT_URI, receivedId), null, null);
								}
							} finally {
								c.close();
							}
							
							// Get pending messages
							synchronized(notSendingLock) {
								c = context.getContentResolver().query(NOT_SENDING_URI, Message.PROJECTION, null, null, null);
								
								// Mark them as sending
								if (c.getCount() > 0)
									context.getContentResolver().update(NOT_SENDING_URI, SENDING_CONTENT_VALUES, null, null);
							}
							
							// Send them via phone
							try {
								for (int i = 0; c.moveToNext(); i++)
									sendMessage(Message.readFrom(c));
							} finally {
								c.close();
							}
							
							
							// Get outgoing messages (to be sent to users)
							Message[] outgoing = client.getMessages(settings.getLastReceivedMessageId());
							if (outgoing.length != 0) {
								// Persist as "sending"
								ContentValues[] values = Message.toContentValues(outgoing);
								for(ContentValues v : values)
									v.put(OutgoingMessages.SENDING, 1);
								context.getContentResolver().bulkInsert(OutgoingMessages.CONTENT_URI, values);
								
								// Send them via phone
								for (int i = 0; i < outgoing.length; i++)
									sendMessage(outgoing[i]);
								
								// Remember last id
								settings.setLastReceivedMessageId(outgoing[outgoing.length - 1].guid);
							}
						} catch (QstClientException e) {
							// TODO handle this exception
							e.printStackTrace();
						} catch (Throwable t) {
							t.printStackTrace();
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
