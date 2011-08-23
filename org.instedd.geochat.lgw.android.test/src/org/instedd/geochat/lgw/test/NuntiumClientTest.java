package org.instedd.geochat.lgw.test;

import junit.framework.TestCase;

import org.instedd.geochat.lgw.msg.NuntiumClient;

public class NuntiumClientTest extends TestCase {

	public final static String JSON_RESPONSE = "[{\"name\":\"Afghanistan\",\"iso2\":\"AF\",\"phone_prefix\":\"93\",\"iso3\":\"AFG\"},{\"name\":\"Albania\",\"iso2\":\"AL\",\"phone_prefix\":\"355\",\"iso3\":\"ALB\"}]";

	public void testCountryNames() throws Exception {
		NuntiumClient client = new NuntiumClient(new MockRestClient(
				JSON_RESPONSE), "nuntium.instedd.org");
		String[] result = { "Afghanistan", "Albania" };
		assertEquals(2, client.countryNames().length);
		assertEquals(result[0], client.countryNames()[0]);
		assertEquals(result[1], client.countryNames()[1]);
	}

	public void testCountryValues() throws Exception {
		NuntiumClient client = new NuntiumClient(new MockRestClient(
				JSON_RESPONSE), "nuntium.instedd.org");
		String[] result = { "93", "355" };
		assertEquals(2, client.countryPhonePrefixes().length);
		assertEquals(result[0], client.countryPhonePrefixes()[0]);
		assertEquals(result[1], client.countryPhonePrefixes()[1]);
	}

}
