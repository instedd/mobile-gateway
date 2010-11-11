package org.instedd.geochat.lgw.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.instedd.geochat.lgw.msg.IRestClient;

public class MockRestClient implements IRestClient {
	
	private String user;
	private String password;
	private String response;
	private String getUrl;
	private List<NameValuePair> headers;
	
	public MockRestClient(String response) {
		this.response = response;
	}
	
	public HttpResponse get(String url) {
		return get(url, null);
	}

	public HttpResponse get(String url, List<NameValuePair> headers) {
		this.getUrl = url;
		this.headers = headers;
		return new MockHttpResponse(new ByteArrayInputStream(this.response.getBytes()));
	}
	
	public void post(String url, List<NameValuePair> params) throws IOException {
		
	}

	public void setAuth(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getGetUrl() {
		return getUrl;
	}

	public String getHeader(String name) {
		for(NameValuePair header : this.headers) {
			if (header.getName().equals(name))
				return header.getValue();
		}
		return null;
	}

}
