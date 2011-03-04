package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.RestClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GeoChatLgwSettings {
	
	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.lgw.settings";
	
	public final static String NAME = "name";
	public final static String PASSWORD = "password";
	public final static String COUNTRY_CODE = "country_code";
	public final static String NUMBER = "number";
	public final static String ENDPOINT_URL = "endpointUrl";
	public final static String REFRESH_RATE = "refreshRate";
	public final static String LAST_RECEIVED_MESSAGE_ID = "lastSentMessageId";
	
	private final Context context;
	
	public GeoChatLgwSettings(Context context) {
		this.context = context;
	}
	
	public String getName() {
		return openRead().getString(NAME, null);
	}
	
	public String getPassword() {
		return openRead().getString(PASSWORD, null);
	}
	
	public String getNumber() {
		return openRead().getString(NUMBER, null);
	}
	
	public void setCredentials(String endpointUrl, String name, String password, String number, String countryCode) {
		Editor editor = openWrite();
		editor.putString(ENDPOINT_URL, endpointUrl);
		editor.putString(NAME, name);
		editor.putString(PASSWORD, password);
		editor.putString(NUMBER, number);
		editor.putString(COUNTRY_CODE, countryCode);
		editor.commit();
	}
	
	public void setLastReceivedMessageId(String id) {
		Editor editor = openWrite();
		editor.putString(LAST_RECEIVED_MESSAGE_ID, id);
		editor.commit();
	}
	
	public String getLastReceivedMessageId() {
		return openRead().getString(LAST_RECEIVED_MESSAGE_ID, null);
	}
	
	public String getEndpointUrl() {
		return openRead().getString(ENDPOINT_URL, "https://nuntium.instedd.org/instedd/qst");
	}
	
	public int getRefreshRateInMinutes() {
		return Integer.parseInt(openRead().getString(REFRESH_RATE, "1"));
	}
	
	public int getRefreshRateInMilliseconds() {
		return 1000 * 60 * getRefreshRateInMinutes();
	}
	
	private SharedPreferences openRead() {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	private Editor openWrite() {
		return openRead().edit();
	}

	public QstClient newQstClient() {
		return new QstClient(getEndpointUrl(), getName(), getPassword(), new RestClient(context), getCountryCode());
	}

	public String getCountryCode() {
		return openRead().getString(COUNTRY_CODE, null);
	}

}
