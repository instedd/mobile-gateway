package org.instedd.geochat.lgw.msg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;

public class QstClient {
	
	private final IRestClient client;
	
	public QstClient(String name, String password) {
		this(name, password, new RestClient());
	}

	public QstClient(String name, String password, IRestClient restClient) {
		this.client = restClient;
		this.client.setAuth(name, password);
	}

	public void sendAddress(String address) throws QstClientException {
		try {
			HttpResponse response = this.client.get("https://nuntium.instedd.org/instedd/qst/setaddress?address=" + encode(address));
			if (response == null) throw new QstClientException("Status not HTTP_OK (200) on credentialsAreValid");
			
			response.getEntity().getContent().close();
		} catch (IOException e) {
			throw new QstClientException(e);
		}	
	}
	
	public void sendMessages(Message[] messages) throws IOException {
		
	}
	
	public Message[] getMessages() throws IOException {
		return null;
	}
	
	private String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

}
