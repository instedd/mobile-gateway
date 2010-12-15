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
	private final Object notSendingLock = new Object();	

	public GeoChatLgwData(Context context) {
		this.content = context.getContentResolver();		
	}
	
	public int deleteOutgoingMessage(String guid) {
		return content.delete(Uris.outgoingMessage(guid), null, null);
	}
	
	public int deleteOutgoingMessage(int id) {
		return content.delete(Uris.outgoingMessage(id), null, null);
	}
	
	public int deleteIncomingMessage(int id) {
		return content.delete(Uris.incomingMessage(id), null, null);
	}
	
	public int markOutgoingMessageAsNotBeingSent(String guid) {
		synchronized(notSendingLock) {
			return content.update(Uris.outgoingMessage(guid), NOT_BEING_SENT, null, null);
		}
	}
	
	public int markOutgoingMessageAsNotBeingSent(String guid, int tries) {
		synchronized(notSendingLock) {
			ContentValues values = new ContentValues();
			values.put(OutgoingMessages.SENDING, 0);
			values.put(OutgoingMessages.TRIES, tries);
			return content.update(Uris.outgoingMessage(guid), values, null, null);
		}
	}
	
	public int markOutgoingMessagesAsNotBeingSent() {
		synchronized(notSendingLock) {
			return content.update(OutgoingMessages.CONTENT_URI, NOT_BEING_SENT, null, null);
		}
	}
	
	public void createIncomingMessage(SmsMessage message, String toNumber) {
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
	
	public Message getOutgoingMessage(String guid) {
		Cursor c = content.query(Uris.outgoingMessage(guid), OutgoingMessages.PROJECTION, null, null, null);
		try {
			if (c.moveToNext()) {
				return Message.readFrom(c);
			} else {
				return null;
			}
		} finally {
			c.close();
		}
	}
	
	public void log(String message) {
		ContentValues values = new ContentValues();
		values.put(Logs.WHEN, System.currentTimeMillis());
		values.put(Logs.TEXT, message);
		content.insert(Logs.CONTENT_URI, values);
		
		content.delete(Uris.OldLogs, null, null);
	}	

}
