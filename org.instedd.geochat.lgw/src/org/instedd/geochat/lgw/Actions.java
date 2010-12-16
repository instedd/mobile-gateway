package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.trans.GeoChatTransceiverService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class Actions {
	
	private final static String PREFIX = "org.instedd.geochat.";
	public final static String HOME = PREFIX + "Home";
	
	private static GeoChatTransceiverService geochatService;
	private static ServiceConnection geochatServiceConnection;
	
	public static void home(Context context) {
		context.startActivity(new Intent().setClass(context, HomeActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public static void settings(Context context) {
		startActivity(context, GeoChatLgwPreferences.class);
	}
	
	public static void stop(Context context) {
		if (geochatService != null) {
			context.getApplicationContext().unbindService(geochatServiceConnection);
			geochatService = null;
			geochatServiceConnection = null;
		}
		
		context.getApplicationContext().stopService(new Intent().setClass(context, GeoChatTransceiverService.class));
		startActivity(context, LoginActivity.class);
	}
	
	public static synchronized void refresh(final Context context, final Uri data, final Handler handler) {
		if (geochatService == null) {
			geochatServiceConnection = new ServiceConnection() {
				public void onServiceDisconnected(ComponentName className) {
					geochatService = null;
				}
				public void onServiceConnected(ComponentName className, IBinder service) {
					geochatService = ((GeoChatTransceiverService.LocalBinder)service).getService();
					refreshInternal(context, data, handler);
				}
			};
			context.getApplicationContext().bindService(new Intent(context, GeoChatTransceiverService.class), geochatServiceConnection, Context.BIND_AUTO_CREATE);
		} else {
			refreshInternal(context, data, handler);	
		}
	}
	
	private static void refreshInternal(final Context context, final Uri data, final Handler handler) {
		geochatService.resync();
		
		final Toast toast = Toast.makeText(context, context.getResources().getString(R.string.refreshing), Toast.LENGTH_LONG);
		handler.post(new Runnable() {
			public void run() {
				toast.show();
			}
		});
	}
	
	private static void startActivity(Context context, Class<?> clazz) {
		context.startActivity(new Intent().setClass(context, clazz));
	}
	
	private Actions() { }	

}
