package org.instedd.geochat.lgw;

import android.content.Context;
import android.net.Uri;
import android.view.Menu;

public class Menues {
	
	public final static int SETTINGS = 1;
	public final static int STOP = 2;
	
	public static void executeAction(Context context, int menuItemId) {
		executeAction(context, menuItemId, null);
	}
	
	public static void executeAction(Context context, int menuItemId, Uri data) {
		switch(menuItemId) {
		case Menues.SETTINGS:
			Actions.settings(context);
			break;
		case Menues.STOP:
			Actions.stop(context);
			break;
		}
	}
	
	public static void settings(Menu menu) {
		menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.ic_menu_preferences);
	}
	
	public static void stop(Menu menu) {
		menu.add(0, STOP, 0, R.string.stop).setIcon(R.drawable.ic_menu_signout);
	}

}
