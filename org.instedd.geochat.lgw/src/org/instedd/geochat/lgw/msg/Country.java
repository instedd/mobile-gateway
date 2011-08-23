package org.instedd.geochat.lgw.msg;

public class Country {

	private String name;
	private String phonePrefix;
	
	Country(String aName, String aPhonePrefix) {
		name = aName;
		phonePrefix = aPhonePrefix;
	}
	
	public String getPhonePrefix() {
		return phonePrefix;
	}
	
	public String getName() {
		return name;
	}
}
