package org.instedd.geochat.lgw.msg;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;

import android.content.Context;

public class RestClient implements IRestClient {
	
	private HttpClient client;
	private String auth;
	
	public RestClient(Context context) {
		this.client = initClient(context);
	}

	private HttpClient initClient(Context context) {
		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 20);
		
		// 30 seconds timeout
		HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
		HttpConnectionParams.setSoTimeout(params, 30 * 1000);
		
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		
	    SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        // Register for port 443 our SSLSocketFactory with our keystore
        // to the ConnectionManager
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager conman = new ThreadSafeClientConnManager(params, registry);
	    
		return new DefaultHttpClient(conman, params);
	}
	
	@Override
	public HttpResponse get(String url) throws IOException {
		return get(url, null);
	}

	public HttpResponse get(String url, List<NameValuePair> headers) throws IOException {
		HttpGet get = auth(new HttpGet(url));
		if (headers != null)
			for(NameValuePair header : headers)
				get.addHeader(header.getName(), header.getValue());
		return this.client.execute(get, new BasicHttpContext());
	}
	
	@Override
	public HttpResponse head(String url) throws IOException {
		HttpHead head = auth(new HttpHead(url));
		return this.client.execute(head, new BasicHttpContext());
	}
	
	public HttpResponse post(String url, String data, String contentType) throws IOException {
		return post(url, data, contentType, null);
	}

	public HttpResponse post(String url, String data, String contentType, List<NameValuePair> headers) throws IOException {
		HttpPost post = auth(new HttpPost(url));

		StringEntity entity = new StringEntity(data, "UTF-8");
		entity.setContentType(contentType);

		post.setEntity(entity);
		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		if (headers != null)
			for(NameValuePair header : headers)
				post.addHeader(header.getName(), header.getValue());

		return this.client.execute(post, new BasicHttpContext());
	}
	
	public HttpResponse put(String url, String data, String contentType) throws IOException {
		HttpPut post = auth(new HttpPut(url));
		
		StringEntity entity = new StringEntity(data, "UTF-8");
		entity.setContentType(contentType);

		post.setEntity(entity);
		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		return this.client.execute(post, new BasicHttpContext());
	}

	public void setAuth(String user, String password) {
		this.auth = "Basic " + Base64.encodeBytes((user + ":" + password).getBytes());
	}
	
	private <T extends HttpMessage> T auth(T request) {
		if (auth != null) {
			request.addHeader("Authorization", auth);
		}
		return request;
	}

}
