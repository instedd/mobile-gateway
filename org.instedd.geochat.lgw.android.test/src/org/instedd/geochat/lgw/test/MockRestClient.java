package org.instedd.geochat.lgw.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeader;
import org.instedd.geochat.lgw.msg.IRestClient;

public class MockRestClient implements IRestClient {
	
	private String user;
	private String password;
	private String response;
	private String getUrl;
	private List<Header> headers = new ArrayList<Header>();
	
	public MockRestClient(String response) {
		this.response = response;
	}
	
	public void addHeader(String name, String value) {
		this.headers.add(new BasicHeader(name, value));
	}

	public HttpResponse get(String url) {
		this.getUrl = url;
		MockHttpResponse response = new MockHttpResponse(new ByteArrayInputStream(this.response.getBytes()));
		response.setHeaders(headers);
		return response;
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

}
