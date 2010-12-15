package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.trans.GeoChatTransceiverService;

import android.content.Context;
import android.content.Intent;

public class Actions {
	
	private final static String PREFIX = "org.instedd.geochat.";
	public final static String HOME = PREFIX + "Home";
	
	public static void home(Context context) {
		context.startActivity(new Intent().setClass(context, HomeActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public static void settings(Context context) {
		startActivity(context, GeoChatLgwPreferences.class);
	}
	
	public static void stop(Context context) {
		context.getApplicationContext().stopService(new Intent().setClass(context, GeoChatTransceiverService.class));
		startActivity(context, LoginActivity.class);
	}
	
	private static void startActivity(Context context, Class<?> clazz) {
		context.startActivity(new Intent().setClass(context, clazz));
	}
	
	private Actions() { }

}
