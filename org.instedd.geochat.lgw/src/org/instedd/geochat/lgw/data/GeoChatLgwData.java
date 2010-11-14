package org.instedd.geochat.lgw.data;

import java.util.UUID;

import org.instedd.geochat.lgw.Uris;
import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;
import org.instedd.geochat.lgw.msg.Message;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.SmsMessage;

public class GeoChatLgwData {
	
	private final static ContentValues NOT_BEING_SENT = new ContentValues();
	static {
		NOT_BEING_SENT.put(OutgoingMessages.SENDING, 0);
	}
	
	private final static ContentValues BEING_SENT = new ContentValues();
	static {
		BEING_SENT.put(OutgoingMessages.SENDING, 1);
	}
	
	private final ContentResolver content;
	private final String toNumber;
	private final Object notSendingLock = new Object();	

	public GeoChatLgwData(Context context, String fromNumber) {
		this.toNumber = fromNumber;
		this.content = context.getContentResolver();		
	}
	
	public void deleteOutgoingMessage(String guid) {
		content.delete(Uris.outgoingMessage(guid), null, null);
	}
	
	public void markOutgoingMessageAsBeingSent(String guid) {
		synchronized(notSendingLock) {
			content.update(Uris.outgoingMessage(guid), NOT_BEING_SENT, null, null);
		}
	}
	
	public void createIncomingMessage(SmsMessage message) {
		String guid = UUID.randomUUID().toString();
        String from = message.getOriginatingAddress();
        String to = toNumber;
        String text = message.getMessageBody();
        long when = message.getTimestampMillis();
		
		content.insert(IncomingMessages.CONTENT_URI, 
        		Message.toContentValues(guid, from, to, text, when));
	}
	
	public Message[] getIncomingMessages() {
		Cursor c = content.query(IncomingMessages.CONTENT_URI, IncomingMessages.PROJECTION, null, null, null);
		try {
			int count = c.getCount();
			if (count == 0)
				return null;
			
			Message[] incoming = new Message[count];
			for (int i = 0; c.moveToNext(); i++)
				incoming[i] = Message.readFrom(c);
			return incoming;
		} finally {
			c.close();
		}
	}
	
	public void deleteIncomingMessageUpTo(String guid) {
		content.delete(Uris.incomingMessageBefore(guid), null, null);
	}
	
	public Message[] getOutgoingMessagesNotBeingSentAndMarkAsBeingSent() {
		synchronized(notSendingLock) {
			Cursor c = content.query(Uris.OutgoingMessagesNotBeingSent, OutgoingMessages.PROJECTION, null, null, null);
			try {
				int count = c.getCount();
				if (count == 0)
					return null;
				
				content.update(Uris.OutgoingMessagesNotBeingSent, BEING_SENT, null, null);
				
				Message[] outgoing = new Message[count];
				for (int i = 0; c.moveToNext(); i++) {
					outgoing[i] = Message.readFrom(c);
				}
				return outgoing;
			} finally {
				c.close();
			}
		}
	}
	
	public String createOutgoingMessagesAsBeingSent(Message[] outgoing) {
		if (outgoing == null || outgoing.length == 0)
			return null;
			
		ContentValues[] values = new ContentValues[outgoing.length];
		for (int i = 0; i < outgoing.length; i++) {
			values[i] = outgoing[i].toContentValues();
			values[i].put(OutgoingMessages.SENDING, 1);
		}
		content.bulkInsert(OutgoingMessages.CONTENT_URI, values);
		return outgoing[outgoing.length - 1].guid;
	}
	
	public void log(String message) {
		ContentValues values = new ContentValues();
		values.put(Logs.WHEN, System.currentTimeMillis());
		values.put(Logs.TEXT, message);
		content.insert(Logs.CONTENT_URI, values);
		
		content.delete(Uris.OldLogs, null, null);
	}

}
