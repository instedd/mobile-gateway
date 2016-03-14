package org.instedd.geochat.lgw.data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.instedd.geochat.lgw.Uris;
import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Statuses;
import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.Status;

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
	
	private final static ContentValues TRIES_ZERO = new ContentValues();
	static {
		TRIES_ZERO.put(OutgoingMessages.TRIES, 0);
	}
	
	private final ContentResolver content;
	private final Object notSendingLock = new Object();	

	public GeoChatLgwData(Context context) {
		this.content = context.getContentResolver();		
	}

	/**
	 * Set the number of remaining parts to be sent on an outgoing message
	 *
	 * @param guid           message to update
	 * @param remainingParts the number of remaining parts to send
	 */
	public int updateOutgoingMessageRemainingParts(String guid, int remainingParts) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(OutgoingMessages.REMAINING_PARTS, remainingParts);
		return content.update(Uris.outgoingMessage(guid), updateValues, null, null);
	}

	public int deleteOutgoingMessage(String guid) {
		return content.delete(Uris.outgoingMessage(guid), null, null);
	}
	
	public int deleteOutgoingMessage(int id) {
		return content.delete(Uris.outgoingMessage(id), null, null);
	}
	
	public int deleteAllOutgoingMessages() {
		return content.delete(OutgoingMessages.CONTENT_URI, null, null);
	}
	
	public int deleteAllIncomingMessages() {
		return content.delete(IncomingMessages.CONTENT_URI, null, null);
	}
	
	public int deleteIncomingMessage(int id) {
		return content.delete(Uris.incomingMessage(id), null, null);
	}
	
	public int resetOutgoingMessageTries(int id) {
		return content.update(Uris.outgoingMessage(id), TRIES_ZERO, null, null);
	}
	
	public int resetAllOutgoingMessageTries() {
		return content.update(OutgoingMessages.CONTENT_URI, TRIES_ZERO, null, null);
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
			return content.update(Uris.outgoingMessage(guid), values, OutgoingMessages.SENDING + " = 1", null);
		}
	}
	
	public int markOutgoingMessagesAsNotBeingSent() {
		synchronized(notSendingLock) {
			return content.update(OutgoingMessages.CONTENT_URI, NOT_BEING_SENT, null, null);
		}
	}
	
	public void createIncomingMessage(SmsMessage[] message, String toNumber) {
		String guid = UUID.randomUUID().toString();
        String from = message[0].getOriginatingAddress();
        String to = toNumber;
        long when = message[0].getTimestampMillis();
        
        StringBuilder textBuffer = new StringBuilder();
        for (SmsMessage msg : message) {
			textBuffer.append(msg.getMessageBody());
		}
		
		content.insert(IncomingMessages.CONTENT_URI, 
        		Message.toContentValues(guid, from, to, textBuffer.toString(), when));
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
		log(message, null);
	}
	
	public void log(String message, Throwable t) {
		ContentValues values = new ContentValues();
		values.put(Logs.WHEN, System.currentTimeMillis());
		values.put(Logs.TEXT, message);
		if (t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			sw.flush();
			values.put(Logs.STACK_TRACE, sw.getBuffer().toString());
		}
		content.insert(Logs.CONTENT_URI, values);
		
		content.delete(Uris.OldLogs, null, null);
	}

	public void markOutgoingMessageAsSent(String guid) {
		ContentValues values = new ContentValues();
		values.put(Statuses.GUID, guid);
		values.put(Statuses.SENT, 1);
		content.insert(Statuses.CONTENT_URI, values);
	}

	public Status getStatus() {
		Cursor c = content.query(Statuses.CONTENT_URI, Statuses.PROJECTION, null, null, null);
		try {
			int count = c.getCount();
			if (count == 0)
				return null;

			List<String> confirmed = new ArrayList<String>();
			List<String> failed = new ArrayList<String>();

			while(c.moveToNext()) {
				String guid = c.getString(1);
				int sent = c.getInt(2);
				if (sent == 0) {
					failed.add(guid);
				} else {
					confirmed.add(guid);
				}
			}

			return new Status(confirmed, failed);
		} finally {
			c.close();
		}
	}

	public void deleteStatus(Status status) {
		if (status == null) return;

		for(String guid : status.confirmed) {
			content.delete(Uris.status(guid), null, null);
		}
		for(String guid : status.failed) {
			content.delete(Uris.status(guid), null, null);
		}
	}
}
