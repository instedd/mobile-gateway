package org.instedd.geochat.lgw.trans;

import org.instedd.geochat.lgw.Notifier;
import org.instedd.geochat.lgw.R;
import org.instedd.geochat.lgw.Settings;
import org.instedd.geochat.lgw.data.GeoChatLgwProvider;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class GeoChatTransceiverService extends CompatibilityService implements OnSharedPreferenceChangeListener {
	
	public class LocalBinder extends Binder {
		public GeoChatTransceiverService getService() {
            return GeoChatTransceiverService.this;
        }
    }
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				transceiver.connectivityChanged();
				transceiver.resync();
			}
		}
	};
	
	final IBinder mBinder = new LocalBinder();
	
	Transceiver transceiver;
	WakeLock wakeLock;
	final Handler handler = new Handler();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.transceiver = new Transceiver(this, handler);
		
		this.displayForegroundNotification();
		
		// Listen for preference changes to resync when that happens
		this.getSharedPreferences(Settings.SHARED_PREFS_NAME, 0)
			.registerOnSharedPreferenceChangeListener(this);
		
		// Listen for network changes
		this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		acquireWakeLock();
		startTransceiving();
	}
	
	@Override
	public void onDestroy() {
		stopTransceiving();
		releaseWakeLock();
		
		// Unregister preferences listener
		this.getSharedPreferences(Settings.SHARED_PREFS_NAME, 0)
			.unregisterOnSharedPreferenceChangeListener(this);
		
		// Unregister the network changes receiver
		this.unregisterReceiver(receiver);
		
		super.onDestroy();
	}
	
	private void acquireWakeLock() {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, GeoChatLgwProvider.TAG);
		wakeLock.acquire();
	}
	
	private void releaseWakeLock() {
		if (wakeLock != null) {
			wakeLock.release();
		}
	}
	
	private void startTransceiving() {
		transceiver.start();
	}
	
	private void stopTransceiving() {
		transceiver.stop();
		
		// Remove foreground notification
		stopForegroundCompat(Notifier.SERVICE);
	}
	
	private void displayForegroundNotification() {
		String title = getResources().getString(R.string.app_name);
		String content = getResources().getString(R.string.signed_in_as_name, new Settings(this).storedUserName());
		Notification notification = new Notification(R.drawable.ic_stat_connected, null, System.currentTimeMillis());
		notification.setLatestEventInfo(this, title, content, PendingIntent.getActivity(this, 0, Notifier.getHomeIntent(this), 0));
		startForegroundCompat(Notifier.SERVICE, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void resync() {
		transceiver.resync();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (Settings.ENDPOINT_URL.equals(key)) {
			transceiver.recreateQstClient();
			transceiver.resync();
		} else if (Settings.REFRESH_RATE.equals(key)) {
			transceiver.resync();
		}
	}

}
