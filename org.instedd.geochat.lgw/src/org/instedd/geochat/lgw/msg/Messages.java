package org.instedd.geochat.lgw.msg;

public class Messages {
	
	public final Message[] messages;
	public final String etag;
	
	public Messages(Message[] messages, String etag) {
		this.messages = messages;
		this.etag = etag;
	}

}
