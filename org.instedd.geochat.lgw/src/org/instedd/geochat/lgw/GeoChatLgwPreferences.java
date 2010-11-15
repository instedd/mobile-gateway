package org.instedd.geochat.lgw;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class GeoChatLgwPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	
	private GeoChatLgwSettings settings;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.settings);
        
        PreferenceManager.setDefaultValues(this, R.layout.settings, false);
        
        getPreferenceManager().setSharedPreferencesName(GeoChatLgwSettings.SHARED_PREFS_NAME);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        this.settings = new GeoChatLgwSettings(this);
        updateEndpointUrl();
        updateRefreshRate();
    }
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (GeoChatLgwSettings.HTTP_BASE.equals(key)) {
			updateEndpointUrl();
		} else if (GeoChatLgwSettings.REFRESH_RATE.equals(key)) {
			updateRefreshRate();
		}
	}

	private void updateEndpointUrl() {
		Preference preference = findPreference(GeoChatLgwSettings.HTTP_BASE);
		preference.setSummary(settings.getHttpBase());
	}
	
	private void updateRefreshRate() {
		String refreshRate = String.valueOf(settings.getRefreshRateInMinutes());
		ListPreference preference = (ListPreference) findPreference(GeoChatLgwSettings.REFRESH_RATE);
		for (int i = 0; i < preference.getEntryValues().length; i++) {
			CharSequence entry = preference.getEntryValues()[i];
			if (entry.equals(refreshRate)) {
				preference.setSummary(preference.getEntries()[i]);
				break;
			}
		}
	}

}
