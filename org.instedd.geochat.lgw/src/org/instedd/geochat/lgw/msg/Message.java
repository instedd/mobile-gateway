package org.instedd.geochat.lgw.msg;

import org.instedd.geochat.lgw.data.GeoChatLgw.Messages;

import android.content.ContentValues;


public class Message {
	
	public String guid;
	public String from;
	public String to;
	public String text;
	public long when;
	
	public static ContentValues[] toContentValues(Message[] messages) {
		ContentValues[] values = new ContentValues[messages.length];
		for (int i = 0; i < messages.length; i++) {
			values[i] = messages[i].toContentValues();
		}
		return values;
	}
	
	public ContentValues toContentValues() {
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

}
