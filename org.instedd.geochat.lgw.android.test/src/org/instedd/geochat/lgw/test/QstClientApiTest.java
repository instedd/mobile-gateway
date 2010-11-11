package org.instedd.geochat.lgw.test;

import junit.framework.TestCase;

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

}
