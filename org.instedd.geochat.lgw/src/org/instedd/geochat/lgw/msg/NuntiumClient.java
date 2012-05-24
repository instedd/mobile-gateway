package org.instedd.geochat.lgw.msg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.instedd.geochat.lgw.R;
import org.instedd.geochat.lgw.UnauthorizedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class NuntiumClient {

	private final Context context;
	private final IRestClient restClient;
	private final String nuntiumUrl;
	private Country[] countries;

	public NuntiumClient(Context context, IRestClient client, String endpointUrl) {
		this.context = context;
		this.restClient = client;
		this.nuntiumUrl = endpointUrl;
	}

	public String[] countryNames() throws NuntiumClientException, WrongHostException {

		String countryNames[] = new String[countries().length];

		for (int index = 0; index < countries().length; index++) {
			countryNames[index] = countries()[index].getName();
		}

		return countryNames;

	}

	public String[] countryPhonePrefixes() throws NuntiumClientException, WrongHostException {

		String countryCodes[] = new String[countries().length];

		for (int index = 0; index < countries().length; index++) {
			countryCodes[index] = countries()[index].getPhonePrefix();
		}

		return countryCodes;
	}

	public Country[] countries() throws NuntiumClientException, WrongHostException {
		if (countries == null)
			initializeCountries();

		return countries;
	}

	private void initializeCountries() throws NuntiumClientException, WrongHostException {
		try {
			JSONArray jsonArray = this.jsonCountries();
			countries = new Country[jsonArray.length()];

			for (int index = 0; index < jsonArray.length(); index++) {
				countries[index] = new Country(
						(String) ((JSONObject) jsonArray.get(index)).get("name"),
						(String) ((JSONObject) jsonArray.get(index)).get("phone_prefix"),
						(String) ((JSONObject) jsonArray.get(index)).get("iso2"),
						(String) ((JSONObject) jsonArray.get(index)).get("iso3")
						);
			}
		} catch (JSONException e) {
			countries = null;
			throw new NuntiumClientException(e);
		}
	}

	private JSONArray jsonCountries() throws NuntiumClientException, WrongHostException {
		try {
			HttpResponse response = restClient.get("http://" + nuntiumUrl
					+ "/api/countries.json");
			check(response);

			InputStream content = response.getEntity().getContent();

			try {
				return new JSONArray(convertStreamToString(content));
			} finally {
				content.close();
			}

		} catch (IOException e) {
			throw new NuntiumClientException(e);
		} catch (JSONException e) {
			throw new NuntiumClientException(e);
		} catch (UnauthorizedException e) {
			throw new NuntiumClientException(e);
		} catch (IllegalArgumentException e) {
			throw new WrongHostException(e);
		}
	}

	private void check(HttpResponse response) throws NuntiumClientException,
			UnauthorizedException {
		switch (response.getStatusLine().getStatusCode()) {
		case 200:
		case 304:
			return;
		case 401:
			throw new UnauthorizedException(context.getString(R.string.invalid_channel_name_password_combination));
		default:
			throw new NuntiumClientException(context.getString(R.string.received_http_status_code, response.getStatusLine().getStatusCode()));
		}
	}

	private static String convertStreamToString(InputStream is)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		return sb.toString();
	}

	public NuntiumTicket requestTicketFor(String telephoneNumber)
			throws NuntiumClientException {
		try {
			HttpResponse response = restClient.post("http://" + nuntiumUrl
					+ "/tickets.json?", "address=" + telephoneNumber, null);

			check(response);
			return buildTicketFrom(response);

		} catch (IOException e) {
			throw new NuntiumClientException(e);
		} catch (JSONException e) {
			throw new NuntiumClientException(e);
		} catch (UnauthorizedException e) {
			throw new NuntiumClientException(e);
		}
	}

	public NuntiumTicket askForUpdateAbout(NuntiumTicket nuntiumTicket)
			throws NuntiumClientException, WrongHostException {
		try {
			HttpResponse response = restClient.get("http://" + nuntiumUrl
					+ "/tickets/" + nuntiumTicket.code() + ".json?secret_key="
					+ nuntiumTicket.secretKey());

			check(response);
			return buildTicketFrom(response);

		} catch (IOException e) {
			throw new NuntiumClientException(e);
		} catch (JSONException e) {
			throw new NuntiumClientException(e);
		} catch (UnauthorizedException e) {
			throw new NuntiumClientException(e);
		} catch (IllegalArgumentException e) {
			throw new WrongHostException(e);
		}
	}

	private NuntiumTicket buildTicketFrom(HttpResponse response)
			throws IOException, JSONException {

		JSONObject jsonObject;
		InputStream content = response.getEntity().getContent();

		try {
			jsonObject = new JSONObject(convertStreamToString(content));
		} finally {
			content.close();
		}
		return new NuntiumTicket((String) jsonObject.get("code"),
				(String) jsonObject.get("secret_key"),
				(String) jsonObject.get("status"),
				asHashMap((JSONObject) jsonObject.get("data")));
	}

	private HashMap<String, String> asHashMap(JSONObject jsonObject)
			throws JSONException {
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<?> keysIterator = jsonObject.keys();
		while (keysIterator.hasNext()) {
			String key = (String) keysIterator.next();
			if (jsonObject.isNull(key)) {
				map.put(key, null);
			} else {
				map.put(key, (String) jsonObject.get(key));
			}
		}
		return map;
	}
}
