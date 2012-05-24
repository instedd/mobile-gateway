package org.instedd.geochat.lgw.test;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.MessageHandler;
import org.instedd.geochat.lgw.msg.QstClient;

import android.util.Xml;
import android.util.Xml.Encoding;

public class QstClientApiTest extends TestCase {
	
	public void testCredentialsFalse() throws Exception {
		MockRestClient restClient = new MockRestClient("");
		QstClient client = createQstClient(restClient, "123", new MockSettings(false));
		client.sendAddress("lala");
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("http://example.com/setaddress?address=lala", restClient.getUrl());
	}
	
	public void testGetMessages() throws Exception {
		MockRestClient restClient = new MockRestClient("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
				"<messages>\n" + 
				"  <message id=\"2b483424-2c2a-4702-b391-651f2a21da9d\" from=\"twitter://edjez\" to=\"twitter://kzu\" when=\"2008-09-24T17:12:57-03:00\">\n" + 
				"    <text>You're fired!</text>\n" + 
				"  </message>\n" + 
				"  <message id=\"2b483424-2c2a-4702-b391-651f2a21da9e\" from=\"twitter://edjez2\" to=\"twitter://kzu2\" when=\"2009-09-24T17:12:57-03:00\">\n" + 
				"    <text>You're fired!!</text>\n" + 
				"  </message>\n" + 
				"</messages>");
		
		QstClient client = createQstClient(restClient, "123", new MockSettings(false));
		Message[] messages = client.getMessages("lastone");
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("get", restClient.getMethod());
		assertEquals("http://example.com/outgoing", restClient.getUrl());
		assertEquals("lastone", restClient.getGetHeader("If-None-Match"));
		
		assertEquals(2, messages.length);
		
		assertEquals("2b483424-2c2a-4702-b391-651f2a21da9d", messages[0].guid);
		assertEquals("twitter://edjez", messages[0].from);
		assertEquals("twitter://kzu", messages[0].to);
		assertEquals("You're fired!", messages[0].text);
		
		assertEquals("2b483424-2c2a-4702-b391-651f2a21da9e", messages[1].guid);
		assertEquals("twitter://edjez2", messages[1].from);
		assertEquals("twitter://kzu2", messages[1].to);
		assertEquals("You're fired!!", messages[1].text);
	}
	
	public void testSendMessages() throws Exception {
		MockRestClient restClient = mockRestClient();
		
		QstClient client = createQstClient(restClient, "123", new MockSettings(false));
		
		Message[] messages = stubMessages(2);
		
		String lastMessageId = client.sendMessages(messages);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("post", restClient.getMethod());
		assertEquals("http://example.com/incoming", restClient.getUrl());
		assertEquals("etagg", lastMessageId);
		
		assertEquals("application/xml", restClient.getPostContentType());
		String data = restClient.getPostData();
		
		MessageHandler handler = new MessageHandler(new MockSettings(false));
		Xml.parse(new ByteArrayInputStream(data.getBytes()), Encoding.UTF_8, handler);
		
		Message[] actualMessages = handler.getMessages();
		
		assertEquals(messages.length, actualMessages.length);
		for (int i = 0; i < messages.length; i++) {
			assertEquals(messages[i].guid, actualMessages[i].guid);
			assertEquals(messages[i].from, actualMessages[i].from);
			assertEquals(messages[i].to, actualMessages[i].to);
			assertEquals(messages[i].text, actualMessages[i].text);
		}
	}

	private Message[] stubMessages(int howMany) {
		Message[] messages = new Message[howMany];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = new Message();
			messages[i].guid = String.valueOf(i);
			messages[i].from = "123" + i;
			messages[i].to = "to" + i;
			messages[i].text = "text" + i;
			messages[i].when = System.currentTimeMillis() + i;	
		}
		return messages;
	}
	
	public void testNormalizationWithNullCountryCode() throws Exception {
		MockRestClient restClient = mockRestClient();
		
		QstClient client = createQstClient(restClient, null, new MockSettings(false));
		
		Message[] messages = stubMessages(10);
		
		messages[0].from = "+98765";
		messages[1].from = "+123456";
		messages[2].from = "+0123456";
		messages[3].from = "+0456";
		messages[4].from = "+00456";
		messages[5].from = "0123456";
		messages[6].from = "0456";
		messages[7].from = "00456";
		messages[8].from = "123456";
		messages[9].from = "456";
		
		client.sendMessages(messages);
		
		String data = restClient.getPostData();
		
		MessageHandler handler = new MessageHandler(new MockSettings(false));
		Xml.parse(new ByteArrayInputStream(data.getBytes()), Encoding.UTF_8, handler);
		
		Message[] actualMessages = handler.getMessages();
		
		assertAfterSendInvariants(messages, actualMessages);
		
		assertEquals("98765", actualMessages[0].from);
		assertEquals("123456", actualMessages[1].from);
		assertEquals("123456", actualMessages[2].from);
		assertEquals("456", actualMessages[3].from);
		assertEquals("0456", actualMessages[4].from);
		assertEquals("123456", actualMessages[5].from);
		assertEquals("456", actualMessages[6].from);
		assertEquals("0456", actualMessages[7].from);
		assertEquals("123456", actualMessages[8].from);
		assertEquals("456", actualMessages[9].from);
	}
	
	public void testFromNormalization()throws Exception {
		MockRestClient restClient = mockRestClient();
		
		QstClient client = createQstClient(restClient, "123", new MockSettings(false));
		
		Message[] messages = stubMessages(10);
		
		messages[0].from = "+98765";
		messages[1].from = "+123456";
		messages[2].from = "+0123456";
		messages[3].from = "+0456";
		messages[4].from = "+00456";
		messages[5].from = "0123456";
		messages[6].from = "0456";
		messages[7].from = "00456";
		messages[8].from = "123456";
		messages[9].from = "456";
		
		client.sendMessages(messages);
		
		String data = restClient.getPostData();
		
		MessageHandler handler = new MessageHandler(new MockSettings(false));
		Xml.parse(new ByteArrayInputStream(data.getBytes()), Encoding.UTF_8, handler);
		
		Message[] actualMessages = handler.getMessages();
		
		assertAfterSendInvariants(messages, actualMessages);
		
		assertEquals("12398765", actualMessages[0].from);
		assertEquals("123456", actualMessages[1].from);
		assertEquals("123456", actualMessages[2].from);
		assertEquals("123456", actualMessages[3].from);
		assertEquals("0456", actualMessages[4].from);
		assertEquals("123456", actualMessages[5].from);
		assertEquals("123456", actualMessages[6].from);
		assertEquals("0456", actualMessages[7].from);
		assertEquals("123456", actualMessages[8].from);
		assertEquals("123456", actualMessages[9].from);
	}

	private void assertAfterSendInvariants(Message[] messages,
			Message[] actualMessages) {
		assertEquals(messages.length, actualMessages.length);
		for (int i = 0; i < messages.length; i++) {
			assertEquals(messages[i].guid, actualMessages[i].guid);
			assertEquals(messages[i].to, actualMessages[i].to);
			assertEquals(messages[i].text, actualMessages[i].text);
		}
	}

	private QstClient createQstClient(MockRestClient restClient, String countryCode, MockSettings settings) {
		QstClient client = new QstClient(null, "http://example.com", "foo", "bar", restClient, countryCode, settings);
		return client;
	}

	private MockRestClient mockRestClient() {
		MockRestClient restClient = new MockRestClient("");
		restClient.addResponseHeader("ETag", "etagg");
		return restClient;
	}
	
	public void testGetLastSentMessageId() throws Exception {
		MockRestClient restClient = new MockRestClient("");
		restClient.addResponseHeader("ETag", "123");
		
		QstClient client = createQstClient(restClient, "123", new MockSettings(false));
		String lastId = client.getLastSentMessageId();
		assertEquals("123", lastId);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("head", restClient.getMethod());
		assertEquals("http://example.com/incoming", restClient.getUrl());
	}

}
