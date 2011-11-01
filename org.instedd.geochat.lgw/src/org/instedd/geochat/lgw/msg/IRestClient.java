package org.instedd.geochat.lgw.msg;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

public interface IRestClient {
	
	void setAuth(String user, String password);
	
	HttpResponse get(String url) throws IOException;
	
	HttpResponse get(String url, List<NameValuePair> headers) throws IOException;
	
	HttpResponse post(String url, String data, String contentType) throws IOException;
	
	HttpResponse put(String url, String data, String contentType) throws IOException;

	HttpResponse head(String url) throws IOException;

}
