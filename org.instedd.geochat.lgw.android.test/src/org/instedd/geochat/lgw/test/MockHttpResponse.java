package org.instedd.geochat.lgw.test;

import java.io.InputStream;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

public class MockHttpResponse implements HttpResponse {
	
	private final InputStream stream;

	public MockHttpResponse(InputStream stream) {
		this.stream = stream;
	}

	public HttpEntity getEntity() {
		return new MockHttpEntity(stream);
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public StatusLine getStatusLine() {
		return new StatusLine() {
			public int getStatusCode() {
				return 200;
			}
			public String getReasonPhrase() {
				return null;
			}
			
			public ProtocolVersion getProtocolVersion() {
				return null;
			}
		};
	}

	public void setEntity(HttpEntity entity) {
		// TODO Auto-generated method stub
		
	}

	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		
	}

	public void setReasonPhrase(String reason) throws IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	public void setStatusCode(int code) throws IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	public void setStatusLine(StatusLine statusline) {
		// TODO Auto-generated method stub
		
	}

	public void setStatusLine(ProtocolVersion ver, int code) {
		// TODO Auto-generated method stub
		
	}

	public void setStatusLine(ProtocolVersion ver, int code, String reason) {
		// TODO Auto-generated method stub
		
	}

	public void addHeader(Header header) {
		// TODO Auto-generated method stub
		
	}

	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public Header[] getAllHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	public Header getFirstHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Header[] getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Header getLastHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	public ProtocolVersion getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public HeaderIterator headerIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public HeaderIterator headerIterator(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeHeader(Header header) {
		// TODO Auto-generated method stub
		
	}

	public void removeHeaders(String name) {
		// TODO Auto-generated method stub
		
	}

	public void setHeader(Header header) {
		// TODO Auto-generated method stub
		
	}

	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	public void setHeaders(Header[] headers) {
		// TODO Auto-generated method stub
		
	}

	public void setParams(HttpParams params) {
		// TODO Auto-generated method stub
		
	}

}
