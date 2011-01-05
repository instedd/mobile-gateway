package org.instedd.geochat.lgw;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.Menu;

public class Menues {
	
	public final static int SETTINGS = 1;
	public final static int STOP = 2;
	public final static int REFRESH = 3;
	public final static int SEND_ACTIVITY_LOG = 4;
	
	public static void executeAction(Context context, Handler handler, int menuItemId) {
		executeAction(context, handler, menuItemId, null);
	}
	
	public static void executeAction(Context context, Handler handler, int menuItemId, Uri data) {
		switch(menuItemId) {
		case Menues.REFRESH:
			Actions.refresh(context, handler);
			break;
		case Menues.SETTINGS:
			Actions.settings(context);
			break;
		case Menues.SEND_ACTIVITY_LOG:
			Actions.sendActivityLog(context, handler);
			break;
		case Menues.STOP:
			Actions.stop(context);
			break;
		}
	}
	
	public static void refresh(Menu menu) {
		menu.add(0, REFRESH, 0, R.string.refresh).setIcon(R.drawable.ic_menu_refresh);
	}
	
	public static void settings(Menu menu) {
		menu.add(0, SETTINGS, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
	}
	
	public static void sendActivityLog(Menu menu) {
		menu.add(0, SEND_ACTIVITY_LOG, 0, R.string.send_activity_log).setIcon(android.R.drawable.ic_menu_send);
	}
	
	public static void stop(Menu menu) {
		menu.add(0, STOP, 0, R.string.stop).setIcon(R.drawable.ic_menu_signout);
	}

}
