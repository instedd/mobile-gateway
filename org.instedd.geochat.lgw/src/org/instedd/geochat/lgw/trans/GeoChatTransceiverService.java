package org.instedd.geochat.lgw.trans;

import org.instedd.geochat.lgw.GeoChatLgwSettings;
import org.instedd.geochat.lgw.Notifier;
import org.instedd.geochat.lgw.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class GeoChatTransceiverService extends CompatibilityService {
	
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
	final Handler handler = new Handler();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.transceiver = new Transceiver(this, handler);
		
		this.displayForegroundNotification();
		
		// Listen for network changes
		this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		transceiver.start();
	}
	
	@Override
	public void onDestroy() {
		stopTransceiving();
		super.onDestroy();
	}
	
	public void stopTransceiving() {
		transceiver.stop();
		
		// Remove foreground notification
		stopForegroundCompat(Notifier.SERVICE);
	}
	
	private void displayForegroundNotification() {
		String title = getResources().getString(R.string.app_name);
		String content = getResources().getString(R.string.signed_in_as_name, new GeoChatLgwSettings(this).getName());
		Notification notification = new Notification(R.drawable.ic_stat_geochat, null, System.currentTimeMillis());
		notification.setLatestEventInfo(this, title, content, PendingIntent.getActivity(this, 0, Notifier.getLoginIntent(this), 0));
		startForegroundCompat(Notifier.SERVICE, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
