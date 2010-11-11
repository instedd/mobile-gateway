package org.instedd.geochat.lgw.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.instedd.geochat.lgw.msg.IRestClient;

public class MockRestClient implements IRestClient {
	
	private String user;
	private String password;
	private String response;
	private String url;
	private List<NameValuePair> getHeaders;
	private String postData;
	private String postContentType;;
	private List<Header> responseHeaders = new ArrayList<Header>();
	
	public MockRestClient(String response) {
		this.response = response;
	}
	
	public HttpResponse get(String url) {
		return get(url, null);
	}

	public HttpResponse get(String url, List<NameValuePair> headers) {
		this.url = url;
		this.getHeaders = headers;
		MockHttpResponse resp = new MockHttpResponse(new ByteArrayInputStream(this.response.getBytes()));
		resp.setHeaders(responseHeaders);
		return resp;
	}
	
	public HttpResponse post(String url, String data, String contentType) throws IOException {
		this.url = url;
		this.postData = data;
		this.postContentType = contentType;
		
		MockHttpResponse resp = new MockHttpResponse(new ByteArrayInputStream(this.response.getBytes()));
		resp.setHeaders(responseHeaders);
		return resp;
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
	
	public String getUrl() {
		return url;
	}
	
	public String getPostData() {
		return postData;
	}
	
	public String getPostContentType() {
		return postContentType;
	}

	public String getGetHeader(String name) {
		for(NameValuePair header : this.getHeaders) {
			if (header.getName().equals(name))
				return header.getValue();
		}
		return null;
	}

	public void addResponseHeader(String name, String value) {
		this.responseHeaders.add(new BasicHeader(name, value));
	}

}
