package org.instedd.geochat.lgw;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GeoChatSettings {
	
	public final static String SHARED_PREFS_NAME = "org.instedd.geochat.lgw.settings";
	
	public final static String NAME = "name";
	public final static String PASSWORD = "password";
	public final static String NUMBER = "number";
	
	private final Context context;
	
	public GeoChatSettings(Context context) {
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
	
	public void setCredentials(String name, String password, String number) {
		Editor editor = openWrite();
		editor.putString(NAME, name);
		editor.putString(PASSWORD, password);
		editor.putString(NUMBER, number);
		editor.commit();
	}
	
	private SharedPreferences openRead() {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	private Editor openWrite() {
		return openRead().edit();
	}

}
