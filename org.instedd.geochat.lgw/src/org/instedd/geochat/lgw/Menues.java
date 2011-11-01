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
	public final static int RETRY_ALL_OUTGOING_MESSAGES = 5;
	public final static int DELETE_ALL_OUTGOING_MESSAGES = 6;
	public final static int DELETE_ALL_INCOMING_MESSAGES = 7;
	public final static int START = 8;
	public final static int RESET_CONFIGURATION = 9;
	
	public static void executeAction(Context context, Handler handler, int menuItemId) {
		executeAction(context, handler, menuItemId, null);
	}
	
	public static void executeAction(Context context, Handler handler, int menuItemId, Uri data) {
		switch(menuItemId) {
		case REFRESH:
			Actions.refresh(context, handler);
			break;
		case SETTINGS:
			Actions.settings(context);
			break;
		case SEND_ACTIVITY_LOG:
			Actions.sendActivityLog(context, handler);
			break;
		case START:
			Actions.accessHomeActivity(context);
			break;
		case STOP:
			Actions.stop(context);
			Actions.accesStartSessionActivity(context);
			break;
		case RETRY_ALL_OUTGOING_MESSAGES:
			Actions.retryAllOutgoingMessages(context, handler);
			break;
		case DELETE_ALL_OUTGOING_MESSAGES:
			Actions.deleteAllOutgoingMessages(context);
			break;
		case DELETE_ALL_INCOMING_MESSAGES:
			Actions.deleteAllIncomingMessages(context);
			break;
		case RESET_CONFIGURATION:
			Actions.resetSettings(context);
			Actions.startAutomaticConfiguration(context);
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
	
	public static void start(Menu menu) {
		menu.add(0, START, 0, R.string.start).setIcon(R.drawable.ic_menu_start);
	}
	
	public static void stop(Menu menu) {
		menu.add(0, STOP, 0, R.string.stop).setIcon(R.drawable.ic_menu_stop);
	}
	
	public static void retryAllOutgoingMessages(Menu menu) {
		menu.add(0, RETRY_ALL_OUTGOING_MESSAGES, 0, R.string.retry_all).setIcon(android.R.drawable.ic_menu_share);
	}

	public static void deleteAllOutgoingMessages(Menu menu) {
		menu.add(0, DELETE_ALL_OUTGOING_MESSAGES, 0, R.string.delete_all).setIcon(android.R.drawable.ic_menu_delete);
	}
	
	public static void deleteAllIncomingMessages(Menu menu) {
		menu.add(0, DELETE_ALL_INCOMING_MESSAGES, 0, R.string.delete_all).setIcon(android.R.drawable.ic_menu_delete);
	}

	public static void resetConfiguration(Menu menu) {
		menu.add(0, RESET_CONFIGURATION, 0, R.string.reset_configuration).setIcon(R.drawable.ic_menu_refresh);
	}
	
}
