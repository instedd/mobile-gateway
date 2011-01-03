package org.instedd.geochat.lgw;

import java.io.IOException;
import java.util.UUID;

import org.instedd.geochat.lgw.msg.IRestClient;

import android.text.format.DateFormat;

public class ExternalLog {
	
	private IRestClient client;
	
	public ExternalLog(IRestClient client) {
		this.client = client;
	}
	
	public void send(String message) throws IOException {
		String guid = UUID.randomUUID().toString();
		CharSequence date = DateFormat.format("yyyyMMddhhmmss", new java.util.Date());
		client.put("http://instedd-logs.s3.amazonaws.com/android-lgw-" + date + "-" + guid, message, "text/plain");
	}

}
