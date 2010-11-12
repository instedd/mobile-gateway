package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;

import android.net.Uri;

public class Uris {
	
	public final static Uri OutgoingMessagesNotBeingSent = Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "not_sending");
	
	public static Uri outgoingMessage(String guid) {
		return Uri.withAppendedPath(Uri.withAppendedPath(OutgoingMessages.CONTENT_URI, "guid"), guid);
	}
	
	public static Uri incomingMessageBefore(String guid) {
		return Uri.withAppendedPath(IncomingMessages.CONTENT_URI, guid);
	}

}
