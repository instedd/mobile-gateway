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
import org.apache.http.conn.ClientConnectionManager;
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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;

public class RestClient implements IRestClient {
	
	private HttpClient client;
	private String auth;
	
	public RestClient() {
		this.client = initClient();
	}

	private HttpClient initClient() {
		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 20);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		           
		// Create and initialize scheme registry 
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		        
		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(cm, params);
	}
	
	@Override
	public HttpResponse get(String url) throws IOException {
		return get(url, null);
	}

	public HttpResponse get(String url, List<NameValuePair> headers) throws IOException {
		HttpGet get = auth(new HttpGet(url));
			for(NameValuePair header : headers)
				get.addHeader(header.getName(), header.getValue());
		auth(get);
		return handleResponse(this.client.execute(get, new BasicHttpContext()));
	}
	
	@Override
	public HttpResponse head(String url) throws IOException {
		HttpHead head = auth(new HttpHead(url));
		return handleResponse(this.client.execute(head, new BasicHttpContext()));
	}
	
	public HttpResponse post(String url, String data, String contentType) throws IOException {
		HttpPost post = auth(new HttpPost(url));
		
		StringEntity entity = new StringEntity(data, "UTF-8");
		entity.setContentType(contentType);

		post.setEntity(entity);
		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		return handleResponse(this.client.execute(post, new BasicHttpContext()));
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
	
	private HttpResponse handleResponse(HttpResponse response) throws IOException {
		int code = response.getStatusLine().getStatusCode();
		if (code != 200 && code != 304) {
			response.getEntity().getContent().close();
			return null;
		}
		return response;
	}

}
