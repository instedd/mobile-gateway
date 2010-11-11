package org.instedd.geochat.lgw.trans;

import org.instedd.geochat.lgw.Connectivity;
import org.instedd.geochat.lgw.GeoChatLgwSettings;
import org.instedd.geochat.lgw.Notifier;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;
import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.QstClientException;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;

public class Transceiver {

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
		
		syncThread = new SyncThread();
		syncThread.start();
	}

	public void stop() {
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
							// Get messages
							Message[] messages = client.getMessages(settings.getLastReceivedMessageId());
							if (messages.length != 0) {
								// Persist
								ContentValues[] values = Message.toContentValues(messages);
								context.getContentResolver().bulkInsert(OutgoingMessages.CONTENT_URI, values);
								
								// Remember last id
								settings.setLastReceivedMessageId(messages[messages.length - 1].guid);
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
