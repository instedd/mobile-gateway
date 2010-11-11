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
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import android.util.Xml.Encoding;

public class QstClient {
	
	//private final static String base = "https://nuntium.instedd.org/instedd";
	private final static String base = "http://nuntium.manas.com.ar/geochat";
	
	private final static SimpleDateFormat DATE_FORMAT;
	static {
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));	
	}
	
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
			HttpResponse response = this.client.get(base + "/qst/setaddress?address=" + encode(address));
			if (response == null) throw new QstClientException("Status not HTTP_OK (200) on sendAddress");
			
			response.getEntity().getContent().close();
		} catch (IOException e) {
			throw new QstClientException(e);
		}
	}
	
	public String sendMessages(Message[] messages) throws QstClientException {
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
				serializer.attribute("", "from", msg.from);
				serializer.attribute("", "to", msg.to);
				serializer.attribute("", "when", DATE_FORMAT.format(new Date(msg.when)));
				serializer.startTag("", "text");
				serializer.text(msg.text);
				serializer.endTag("", "text");
				serializer.endTag("", "message");
			}
			serializer.endTag("", "messages");
			serializer.flush();
			
			HttpResponse response = this.client.post(base + "/qst/incoming", writer.toString(), "application/xml");
			if (response == null) throw new QstClientException("Status not HTTP_OK (200) on sendMessages");
			
			try {
				Header header = response.getFirstHeader("ETag");
				if (header != null)
					return header.getValue();
			} finally {
				response.getEntity().getContent().close();
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
			
			HttpResponse response = this.client.get(base + "/qst/outgoing", headers);
			if (response == null) throw new QstClientException("Status not HTTP_OK (200) on getMessages");
			
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
			HttpResponse response = this.client.head(base + "/qst/incoming");
			if (response == null) throw new QstClientException("Status not HTTP_OK (200) on getLastSentMessageId");
			
			try {
				Header header = response.getFirstHeader("ETag");
				if (header != null)
					return header.getValue();
			} finally {
				response.getEntity().getContent().close();
			}
		} catch (IOException e) {
			throw new QstClientException(e);
		}
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
