package org.instedd.geochat.lgw.msg;

import org.instedd.geochat.lgw.data.GeoChatLgw.Messages;

import android.content.ContentValues;
import android.database.Cursor;


public class Message {
	
	public String guid;
	public String from;
	public String to;
	public String text;
	public long when;
	public int tries;
	public int remainingParts;
	
	public static Message readFrom(Cursor c) {
		// Check the projections in GeoChatLgw
		Message msg = new Message();
		msg.guid = c.getString(1);
		msg.from = c.getString(2);
		msg.to = c.getString(3);
		msg.text = c.getString(4);
		msg.when = c.getLong(5);
		// msg.sending = c.getInt(6);
		if (c.getColumnCount() > 7) {
			msg.tries = c.getInt(7);
			msg.remainingParts = c.getInt(8);
		}
		return msg;
	}
	
	public static ContentValues toContentValues(String guid, String from, String to, String text, long when) {
		ContentValues values = new ContentValues();
		values.put(Messages.GUID, guid);
		values.put(Messages.FROM, from);
		values.put(Messages.TO, to);
		values.put(Messages.TEXT, text);
		if (when != 0) {
			values.put(Messages.WHEN, when);
		}
		return values;
	}
	
	public ContentValues toContentValues() {
		return toContentValues(guid, from, to, text, when);
	}

}
