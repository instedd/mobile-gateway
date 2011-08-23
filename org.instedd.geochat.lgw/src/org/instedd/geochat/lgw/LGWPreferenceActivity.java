package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.NuntiumClientException;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class LGWPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private Settings settings;
	private final static int ERROR_DIALOG = 1;
	private Exception exception;
	private Handler handler = new Handler();
	
	public Settings settings() {
		return settings;
	}
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		this.settings = new Settings(this);
		initializePreferenceValues();
		updatePreferenceSummaries();
	}

	private void initializePreferenceValues() {
		addPreferencesFromResource(R.layout.settings);

		initializeCountryValues();

		PreferenceManager.setDefaultValues(this, R.layout.settings, false);
		getPreferenceManager().setSharedPreferencesName(
				Settings.SHARED_PREFS_NAME);
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		setPopUpValues();
	}

	private void initializeCountryValues() {
		try {
			countryPreference()
					.setEntryValues((CharSequence[]) countryPhonePrefixes());

			countryPreference().setEntries((CharSequence[]) countryNames());
		} catch (NuntiumClientException e) {
			handle(e);
		} catch (UnauthorizedException e) {
			handle(e);
		} catch (WrongHostException e) {
			handle(e.withMessage(getResources().getString(R.string.fix_host)));
		}
	}

	private void handle(Exception e) {
		exception = e;
		countryPreference().setEnabled(false);
		showDialog(ERROR_DIALOG);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePreferenceSummaries();
		setPopUpValues();
	}

	private void updatePreferenceSummaries() {
		updateRefreshRateSummary();
		updateEndpointUrlSummary();
		updateNameSummary();
		// The password is not shown in summary
		updateCountryCodeSummary();
		updateTelephoneNumberSummary();
	}

	public void setPopUpValues() {
		setRefreshRatePopUpValue();
		setEndpointUrlPopUpValue();
		setUserNamePopUpValue();
		setPasswordPopUpValue();
		setCountryCodePopUpValue();
		setTelephoneNumberPopUpValue();
	}

	private void updateNameSummary() {
		userNamePreference().setSummary(storedUserName());
	}

	private void updateCountryCodeSummary() {
		updateListPreferenceSummary(storedCountryCode(), countryPreference());
	}

	private void updateTelephoneNumberSummary() {
		telephoneNumberPreference().setSummary(storedTelephoneNumber());
	}

	private void updateEndpointUrlSummary() {
		endpointUrlPreference().setSummary(storedEndpointUrl());
	}

	private void updateRefreshRateSummary() {
		updateListPreferenceSummary(storedRefreshRate(),
				refreshRatePreference());
	}

	private void setTelephoneNumberPopUpValue() {
		telephoneNumberPreference().setText(storedTelephoneNumber());
	}

	private void setEndpointUrlPopUpValue() {
		endpointUrlPreference().setText(storedEndpointUrl());
	}

	private void setUserNamePopUpValue() {
		userNamePreference().setText(storedUserName());
	}

	private void setPasswordPopUpValue() {
		passwordPreference().setText(storedPassword());
	}

	private void setRefreshRatePopUpValue() {
		refreshRatePreference().setValue(storedRefreshRate());
	}

	private void setCountryCodePopUpValue() {
		countryPreference().setValue(storedCountryCode());
	}

	private EditTextPreference telephoneNumberPreference() {
		return (EditTextPreference) findPreference(Settings.NUMBER);
	}

	private EditTextPreference endpointUrlPreference() {
		return (EditTextPreference) findPreference(Settings.ENDPOINT_URL);
	}

	private EditTextPreference userNamePreference() {
		return (EditTextPreference) findPreference(Settings.NAME);
	}

	private EditTextPreference passwordPreference() {
		return (EditTextPreference) findPreference(Settings.PASSWORD);
	}

	private ListPreference refreshRatePreference() {
		return (ListPreference) findPreference(Settings.REFRESH_RATE);
	}

	private ListPreference countryPreference() {
		return (ListPreference) findPreference(Settings.COUNTRY_CODE);
	}

	private String storedTelephoneNumber() {
		return String.valueOf(settings.storedTelephoneNumber());
	}

	private String storedEndpointUrl() {
		return String.valueOf(settings.storedEndpointUrl());
	}

	private String storedUserName() {
		return String.valueOf(settings.storedUserName());
	}

	private String storedPassword() {
		return String.valueOf(settings.storedPassword());
	}

	private String storedRefreshRate() {
		return String.valueOf(settings.storedRefreshRateInMinutes());
	}

	private String storedCountryCode() {
		return String.valueOf(settings.storedCountryCode());
	}

	private void updateListPreferenceSummary(String storedValue,
			ListPreference preference) {
		if (preference.getEntryValues() != null) {
			for (int i = 0; i < preference.getEntryValues().length; i++) {
				CharSequence entry = preference.getEntryValues()[i];
				if (entry.equals(storedValue)) {
					preference.setSummary(preference.getEntries()[i]);
					break;
				}
			}
		} else {
			preference
					.setSummary(internetConnectionErrorMessage());
		}
	}

	private String internetConnectionErrorMessage() {
		return getResources().getString(
				R.string.internet_connection_error,
				exception.getMessage());
	}

	public String[] countryNames() throws NuntiumClientException,
			UnauthorizedException, WrongHostException {
		return settings.nuntiumClient().countryNames();
	}

	public String[] countryPhonePrefixes() throws NuntiumClientException,
			UnauthorizedException, WrongHostException {
		return settings.nuntiumClient().countryPhonePrefixes();
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(internetConnectionErrorMessage())
				.setTitle(R.string.internet_connection_error_title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok, null);

		return builder.create();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.resetConfiguration(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}
}
