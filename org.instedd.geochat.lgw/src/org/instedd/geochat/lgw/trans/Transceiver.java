package org.instedd.geochat.lgw.trans;

import java.util.ArrayList;

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
import android.os.Handler;
import android.telephony.SmsManager;

public class Transceiver {
	
	private final static String SMS_SENT_ACTION = "org.instedd.geochat.lgw.SMS_SENT_ACTION";
	private final static String INTENT_EXTRA_GUID = "org.instedd.geochat.lgw.Guid";
	
	private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SMS_SENT_ACTION.equals(intent.getAction())) {
				switch(getResultCode()) {
				case Activity.RESULT_OK:
					String guid = intent.getExtras().getString(INTENT_EXTRA_GUID);
					Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "guid"), guid);
					context.getContentResolver().delete(uri, null, null);
				}
			}
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
	Object lock;

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
		
		syncThread = new SyncThread();
		syncThread.start();
	}

	public void stop() {
		context.unregisterReceiver(smsSentReceiver);
		
		running = false;
		this.resync();
	}

	public void connectivityChanged() {
		this.connectivityChanged = true;
	}

	public void resync() {
		if (lock != null) {
			synchronized(lock) {
				lock.notify();
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
							// Get outgoing messages (to be sent to users)
							Message[] outgoing = client.getMessages(settings.getLastReceivedMessageId());
							if (outgoing.length != 0) {
								// Persist
								ContentValues[] values = Message.toContentValues(outgoing);
								context.getContentResolver().bulkInsert(OutgoingMessages.CONTENT_URI, values);
								
								// Send messages
								for (int i = 0; i < outgoing.length; i++)
									sendMessage(outgoing[i]);
								
								// Remember last id
								settings.setLastReceivedMessageId(outgoing[outgoing.length - 1].guid);
							}
							
							// Send incoming messages (to be sent to the app)
							Cursor c = context.getContentResolver().query(IncomingMessages.CONTENT_URI, Message.PROJECTION, null, null, null);
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
				lock = new Object();
				synchronized (lock) {
					try {
						lock.wait(1000 * 60 * 1);
					} catch (InterruptedException e) {
					}
				}
				lock = null;
			}
		}
		
	}

}
