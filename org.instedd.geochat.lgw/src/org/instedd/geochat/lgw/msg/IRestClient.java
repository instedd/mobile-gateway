package org.instedd.geochat.lgw.msg;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

public interface IRestClient {
	
	void setAuth(String user, String password);
	
	HttpResponse get(String url) throws IOException;
	
	void post(String url, List<NameValuePair> params) throws IOException;

}
