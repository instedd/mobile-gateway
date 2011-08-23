package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.Country;
import org.instedd.geochat.lgw.msg.NuntiumClient;
import org.instedd.geochat.lgw.msg.NuntiumClientException;
import org.instedd.geochat.lgw.msg.NuntiumTicket;
import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.RestClient;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {

	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.lgw.settings";
	public final static String REFRESH_RATE = "refreshRate";
	public final static String ENDPOINT_URL = "endpointUrl";
	public final static String NAME = "userName";
	public final static String PASSWORD = "userPassword";
	public final static String COUNTRY_CODE = "countryCode";
	public final static String NUMBER = "telephoneNumber";
	public final static String LAST_RECEIVED_MESSAGE_ID = "lastSentMessageId";

	private final Context context;
	private NuntiumClient nuntiumClient;

	public Settings(Context context) {
		this.context = context;
	}

	public String storedUserName() {
		return openRead().getString(NAME, null);
	}

	public String storedPassword() {
		return openRead().getString(PASSWORD, null);
	}

	public String storedTelephoneNumber() {
		return openRead().getString(NUMBER, null);
	}

	public String storedLastReceivedMessageId() {
		return openRead().getString(LAST_RECEIVED_MESSAGE_ID, null);
	}

	public String storedEndpointUrl() {
		return openRead().getString(ENDPOINT_URL, defaultEndpointUrl());
	}

	private String defaultEndpointUrl() {
		return "http://nuntium.instedd.org/";
	}

	public int storedRefreshRateInMinutes() {
		return Integer.parseInt(openRead().getString(REFRESH_RATE, "1"));
	}

	public int storedRefreshRateInMilliseconds() {
		return 1000 * 60 * storedRefreshRateInMinutes();
	}

	public String storedCountryCode() {
		return openRead().getString(COUNTRY_CODE, null);
	}

	public void saveLastReceivedMessageId(String id) {
		Editor editor = openWrite();
		editor.putString(LAST_RECEIVED_MESSAGE_ID, id);
		editor.commit();
	}

	public void saveCredentials(String endpointUrl, String name, String password) {
		Editor editor = openWrite();
		editor.putString(ENDPOINT_URL, endpointUrl);
		editor.putString(NAME, name);
		editor.putString(PASSWORD, password);
		editor.commit();
	}

	public void saveCountryCode(String countryCode) {
		Editor editor = openWrite();
		editor.putString(COUNTRY_CODE, countryCode);
		editor.commit();
	}

	public void saveTelephoneNumber(String number) {
		Editor editor = openWrite();
		editor.putString(NUMBER, number);
		editor.commit();
	}

	private SharedPreferences openRead() {
		return context.getSharedPreferences(SHARED_PREFS_NAME,
				Context.MODE_PRIVATE);
	}

	private Editor openWrite() {
		return openRead().edit();
	}

	public QstClient qstClient() {
		return new QstClient(storedEndpointUrl(), storedUserName(),
				storedPassword(), restClient(), storedCountryCode());
	}

	public NuntiumClient nuntiumClient() {
		if (nuntiumClient == null) {
			nuntiumClient = new NuntiumClient(restClient(),
					storedEndpointBaseUrl());
		}

		return nuntiumClient;
	}

	private RestClient restClient() {
		return new RestClient(context);
	}

	public String storedEndpointBaseUrl() {
		return trimToHost(storedEndpointUrl());
	}

	private String trimToHost(String url) {
		String[] splittedUrl = url.split("/");
		if (splittedUrl.length >= 3) {
			return splittedUrl[2];
		} else {
			return "";
		}
	}

	public boolean areIncomplete() {
		return isEndpointUrlUnset() || isUserNameUnset() || isPasswordUnset()
				|| isTelephoneNumberUnset() || isCountryCodeUnset();
	}

	private boolean isCountryCodeUnset() {
		return storedCountryCode() == null || storedCountryCode().length() == 0;
	}

	private boolean isTelephoneNumberUnset() {
		return storedTelephoneNumber() == null
				|| storedTelephoneNumber().length() == 0;
	}

	private boolean isPasswordUnset() {
		return storedPassword() == null || storedPassword().length() == 0;
	}

	private boolean isUserNameUnset() {
		return storedUserName() == null || storedUserName().length() == 0;
	}

	private boolean isEndpointUrlUnset() {
		return storedEndpointUrl() == null || storedEndpointUrl().length() == 0;
	}

	public int storedCountryCodeIndex() throws NuntiumClientException,
			WrongHostException {
		String storedCountryCode = storedCountryCode();
		Country[] countries = nuntiumClient().countries();

		for (int i = 0; i < countries.length; i++) {
			if (countries[i].getPhonePrefix().equals(storedCountryCode))
				return i;
		}
		return 0;
	}

	public void saveCountryCodeAtIndex(int position)
			throws NuntiumClientException, WrongHostException {
		saveCountryCode(nuntiumClient().countries()[position].getPhonePrefix());
	}

	public void saveCredentialsFrom(NuntiumTicket nuntiumTicket) {
		saveCredentials(endpointUrlFrom(nuntiumTicket), nuntiumTicket.data()
				.get("channel"), nuntiumTicket.data().get("password"));
	}

	private String endpointUrlFrom(NuntiumTicket nuntiumTicket) {
		return "http://" + storedEndpointBaseUrl() + "/"
				+ nuntiumTicket.data().get("account") + "/qst";
	}

	public void resetAdvancedConfiguration() {
		saveCredentials(defaultEndpointUrl(), "", "");
	}
}
