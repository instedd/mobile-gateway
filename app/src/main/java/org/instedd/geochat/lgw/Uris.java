package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Statuses;

import android.net.Uri;

public class Uris {
	
	public final static Uri OutgoingMessagesToBeSent = Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "not_sending");
	public final static Uri OldLogs = Uri.withAppendedPath(Logs.CONTENT_URI, "old");

	public static Uri outgoingMessage(int id) {
		return Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, String.valueOf(id));
	}
	
	public static Uri outgoingMessage(String guid) {
		return Uri.withAppendedPath(Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "guid"), guid);
	}
	
	public static Uri incomingMessage(int id) {
		return Uri.withAppendedPath(Uri.withAppendedPath(IncomingMessages.CONTENT_URI, "id"), String.valueOf(id));
	}
	
	public static Uri incomingMessageBefore(String guid) {
		return Uri.withAppendedPath(IncomingMessages.CONTENT_URI, guid);
	}

	public static Uri status(String guid) {
		return Uri.withAppendedPath(Uri.withAppendedPath(Statuses.CONTENT_URI, "guid"), guid);
	}

}
