package org.instedd.geochat.lgw;

import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class Connectivity {
	
	public static boolean hasConnectivity(Context context) {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conn.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	public static void reEnableWifi(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(false);
		wifi.setWifiEnabled(true);
	}
	
	public static boolean isConnectivityException(Exception e) {
		return e != null && (e.getCause() instanceof SocketException || e.getCause() instanceof UnknownHostException);
	}
	
	public static AlertDialog showNoConnectionDialog(final Activity activity) {
		String message = activity.getResources().getString(
				R.string.cannot_start_error, activity.getResources().getString(R.string.no_connection));
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(message).setTitle(R.string.cannot_start)
				.setCancelable(false)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.moveTaskToBack(true);
					}
				});
		return builder.create();
	}

}
