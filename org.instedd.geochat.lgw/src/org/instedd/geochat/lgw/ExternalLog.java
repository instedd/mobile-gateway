package org.instedd.geochat.lgw;

import java.io.IOException;
import java.util.UUID;

import org.instedd.geochat.lgw.msg.IRestClient;
import org.instedd.geochat.lgw.msg.RestClient;

import android.content.Context;
import android.text.format.DateFormat;

public class ExternalLog {
	
	private IRestClient client;
	private Settings settings;
	
	public ExternalLog(Context context) {
		this.client = new RestClient(context);
		this.settings = new Settings(context);
	}
	
	public void send(String message) throws IOException {
		String url = settings.storedEndpointUrl();
		String channel = settings.storedUserName();
		String guid = UUID.randomUUID().toString();
		
		message = "URL: " + url + "\n" + message;
		
		CharSequence date = DateFormat.format("yyyyMMddhhmmss", new java.util.Date());
		client.put("http://instedd-logs.s3.amazonaws.com/android-lgw-" + channel + "-" + date + "-" + guid, message, "text/plain");
	}

}
