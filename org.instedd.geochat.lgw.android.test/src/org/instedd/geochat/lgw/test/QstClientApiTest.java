package org.instedd.geochat.lgw.test;

import junit.framework.TestCase;

import org.instedd.geochat.lgw.msg.Message;
import org.instedd.geochat.lgw.msg.QstClient;

public class QstClientApiTest extends TestCase {
	
	public void testCredentialsFalse() throws Exception {
		MockRestClient restClient = new MockRestClient("");
		QstClient client = new QstClient("foo", "bar", restClient);
		client.sendAddress("lala");
		
		assertEquals("foo", restClient.getUser());
		assertEquals("bar", restClient.getPassword());
		assertEquals("https://nuntium.instedd.org/instedd/qst/setaddress?address=lala", restClient.getGetUrl());
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
		assertEquals("https://nuntium.instedd.org/instedd/qst/outgoing", restClient.getGetUrl());
		assertEquals("lastone", restClient.getHeader("If-None-Match"));
		
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
		 
	}

}
