package org.instedd.geochat.lgw.msg;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.instedd.geochat.lgw.UnauthorizedException;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import android.util.Xml.Encoding;

public class QstClient {
	
	//private final static String base = "https://nuntium.instedd.org/instedd";
	// private final static String base = "http://nuntium.manas.com.ar/geochat/qst";
	
	private final static SimpleDateFormat DATE_FORMAT;
	static {
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));	
	}
	
	private final IRestClient client;
	private final String httpBase;
	
	public QstClient(String httpBase, String name, String password) {
		this(httpBase, name, password, new RestClient());
	}

	public QstClient(String httpBase, String name, String password, IRestClient restClient) {
		this.httpBase = httpBase;
		this.client = restClient;
		this.client.setAuth(name, password);
	}

	public void sendAddress(String address) throws QstClientException {
		try {
			HttpResponse response = this.client.get(httpBase + "/setaddress?address=" + encode(address));
			try {
				check(response);
			} finally {
				close(response);
			}
		} catch (IOException e) {
			throw new QstClientException(e);
		}
	}
	
	public String sendMessages(Message[] messages) throws QstClientException {
		if (messages == null || messages.length == 0)
			return null;
		
		StringWriter writer = new StringWriter();
		
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "messages");
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				
				serializer.startTag("", "message");				
				serializer.attribute("", "id", msg.guid);
				serializer.attribute("", "from", addProtocol(msg.from));
				serializer.attribute("", "to", addProtocol(msg.to));
				serializer.attribute("", "when", DATE_FORMAT.format(new Date(msg.when)));
				serializer.startTag("", "text");
				serializer.text(msg.text);
				serializer.endTag("", "text");
				serializer.endTag("", "message");
			}
			serializer.endTag("", "messages");
			serializer.flush();
			
			HttpResponse response = this.client.post(httpBase + "/incoming", writer.toString(), "application/xml");
			try {
				check(response);
				
				Header header = response.getFirstHeader("ETag");
				if (header != null)
					return header.getValue();
			} finally {
				close(response);
			}
		} catch (Exception e) {
			throw new QstClientException(e);
		}
		return null;
	}

	public Message[] getMessages(String lastReceivedMessageId) throws QstClientException {
		try {
			List<NameValuePair> headers = new ArrayList<NameValuePair>(1);
			if (lastReceivedMessageId != null) {
				headers.add(new BasicNameValuePair("If-None-Match", lastReceivedMessageId));
			}
			
			HttpResponse response = this.client.get(httpBase + "/outgoing", headers);
			check(response);
			
			InputStream content = response.getEntity().getContent();
			try {
				MessageHandler handler = new MessageHandler();
				Xml.parse(content, Encoding.UTF_8, handler);
				return handler.getMessages();
			} finally {
				content.close();
			}
		} catch (IOException e) {
			throw new QstClientException(e);
		} catch (SAXException e) {
			throw new QstClientException(e);
		}
	}
	
	public String getLastSentMessageId() throws QstClientException {
		try {
			HttpResponse response = this.client.head(httpBase + "/incoming");
			try {
				check(response);
				
				Header header = response.getFirstHeader("ETag");
				if (header != null)
					return header.getValue();
			} finally {
				close(response);
			}
		} catch (IOException e) {
			throw new QstClientException(e);
		}
		return null;
	}
	
	private void check(HttpResponse response) throws QstClientException {
		switch(response.getStatusLine().getStatusCode()) {
		case 200:
		case 304:
			return;
		case 401:
			throw new UnauthorizedException();
		default:
			throw new QstClientException("Received HTTP status code " + response.getStatusLine().getStatusCode());
		}
	}
	
	private void close(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity == null) return;
		
		InputStream content = entity.getContent();
		if (content == null) return;
		
		content.close();
	}
	
	private String addProtocol(String address) {
		if (address != null && !address.startsWith("sms://")) {
			return "sms://" + address;
		}
		return address;
	}
	
	private String encode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}

}
