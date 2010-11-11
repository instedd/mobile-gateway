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
		QstClient client = new QstClient("foo", "bar", restClient);
		client.sendAddress("lala");
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://nuntium.instedd.org/instedd/qst/setaddress?address=lala", restClient.getUrl());
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
		
		QstClient client = new QstClient("foo", "bar", restClient);
		Message[] messages = client.getMessages("lastone");
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://nuntium.instedd.org/instedd/qst/outgoing", restClient.getUrl());
		assertEquals("lastone", restClient.getGetHeader("If-None-Match"));
		
		assertEquals(2, messages.length);
		
		assertEquals("2b483424-2c2a-4702-b391-651f2a21da9d", messages[0].id);
		assertEquals("twitter://edjez", messages[0].from);
		assertEquals("twitter://kzu", messages[0].to);
		assertEquals("You're fired!", messages[0].text);
		
		assertEquals("2b483424-2c2a-4702-b391-651f2a21da9e", messages[1].id);
		assertEquals("twitter://edjez2", messages[1].from);
		assertEquals("twitter://kzu2", messages[1].to);
		assertEquals("You're fired!!", messages[1].text);
	}
	
	public void testSendMessages() throws Exception {
		MockRestClient restClient = new MockRestClient("");
		restClient.addResponseHeader("ETag", "etagg");
		
		QstClient client = new QstClient("foo", "bar", restClient);
		
		Message[] messages = new Message[2];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = new Message();
			messages[i].id = String.valueOf(i);
			messages[i].from = "from" + i;
			messages[i].to = "to" + i;
			messages[i].text = "text" + i;
			messages[i].when = System.currentTimeMillis() + i;	
		}
		
		String lastMessageId = client.sendMessages(messages);
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://nuntium.instedd.org/instedd/qst/incoming", restClient.getUrl());
		assertEquals("etagg", lastMessageId);
		
		assertEquals("application/xml", restClient.getPostContentType());
		String data = restClient.getPostData();
		
		MessageHandler handler = new MessageHandler();
		Xml.parse(new ByteArrayInputStream(data.getBytes()), Encoding.UTF_8, handler);
		
		Message[] actualMessages = handler.getMessages();
		
		assertEquals(messages.length, actualMessages.length);
		for (int i = 0; i < messages.length; i++) {
			assertEquals(messages[i].id, actualMessages[i].id);
			assertEquals(messages[i].from, actualMessages[i].from);
			assertEquals(messages[i].to, actualMessages[i].to);
			assertEquals(messages[i].text, actualMessages[i].text);
		}
	}

}
