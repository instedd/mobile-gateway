package org.instedd.geochat.lgw;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class GeoChatLgwPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.settings);
        getPreferenceManager().setSharedPreferencesName(GeoChatLgwSettings.SHARED_PREFS_NAME);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        updateEndpointUrl();
    }
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (GeoChatLgwSettings.HTTP_BASE.equals(key)) {
			updateEndpointUrl();
		}
	}

	private void updateEndpointUrl() {
		String httpBase = new GeoChatLgwSettings(this).getHttpBase();
		Preference preference = findPreference(GeoChatLgwSettings.HTTP_BASE);
		preference.setSummary(httpBase);
	}

}
