package org.instedd.geochat.lgw;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

public class Notifier {
	
	private final Context context;
	
	public final static int SERVICE = 1;
	public final static int MESSAGES_COULD_NOT_BE_SENT = 2;

	public Notifier(Context context) {
		this.context = context;
	}
	
	public void startTranscieving() {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat_sync, R.string.transceiving);
	}
	
	public void stopTransceiving() {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat, R.string.signed_in_as_name, new GeoChatLgwSettings(context).getName());
	}
	
	public void offline() {
		setForegroundNotificationContent(R.drawable.ic_stat_geochat_offline, R.string.signed_in_as_name_offline, new GeoChatLgwSettings(context).getName());
	}
	
	public void someMessagesCouldNotBeSent() {
		NotificationManager man = getNotificationManager();
		Notification notification = new Notification(R.drawable.ic_stat_geochat_offline, null, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Resources r = context.getResources();
		String title = r.getString(R.string.some_messages_could_not_be_sent);
		String content = r.getString(R.string.some_messages_could_not_be_sent_description);
		
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, getViewMessagesIntent(), 0));
		man.notify(MESSAGES_COULD_NOT_BE_SENT, notification);
	}
	
	private void setForegroundNotificationContent(int icon, int resource) {
		setForegroundNotificationContent(icon, resource, null);
	}
	
	private void setForegroundNotificationContent(int icon, int resource, String argument) {
		NotificationManager man = getNotificationManager();
		
		Resources r = context.getResources();
		String title = r.getString(R.string.app_name);
		String content = argument == null ? r.getString(resource) : r.getString(resource, argument);
		
		Notification notification = new Notification(icon, null, System.currentTimeMillis());
		notification.setLatestEventInfo(context, title, content, PendingIntent.getActivity(context, 0, getViewMessagesIntent(), 0));
		man.notify(SERVICE, notification);
	}
	
	private Intent getViewMessagesIntent() {
		return getHomeIntent(context);
	}
	
	public static Intent getHomeIntent(Context context) {
		Intent intent = new Intent().setClass(context, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
	
	private NotificationManager getNotificationManager() {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

}
