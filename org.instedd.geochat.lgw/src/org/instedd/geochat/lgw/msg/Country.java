package org.instedd.geochat.lgw.msg;

public class Country {

	private String name;
	private String phonePrefix;
	private String iso2;
	private String iso3;
	
	Country(String aName, String aPhonePrefix, String aIso2, String aIso3) {
		name = aName;
		phonePrefix = aPhonePrefix;
		iso2 = aIso2;
		iso3 = aIso3;
	}
	
	public String getPhonePrefix() {
		return phonePrefix;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIso2() {
		return iso2;
	}
	
	public String getIso3() {
		return iso3;
	}
}
