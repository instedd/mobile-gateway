package org.instedd.geochat.lgw;

import java.io.IOException;

import org.instedd.geochat.lgw.data.GeoChatLgwData;
import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;
import org.instedd.geochat.lgw.msg.NuntiumTicket;
import org.instedd.geochat.lgw.trans.GeoChatTransceiverService;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

public class Actions {
	
	private final static String PREFIX = "org.instedd.geochat.";
	public final static String HOME = PREFIX + "Home";
	
	private static GeoChatTransceiverService geochatService;
	private static ServiceConnection geochatServiceConnection;
	
	public static void accessHomeActivity(Context context) {
		context.startActivity(new Intent().setClass(context, HomeActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	private static void startActivity(Context context, Class<?> clazz) {
		context.startActivity(new Intent().setClass(context, clazz));
	}
	
	public static void accesStartSessionActivity(Context context) {
		startActivity(context, StartSessionActivity.class);
	}
	
	public static void startAutomaticConfiguration(Context context) {
		startActivity(context, AskForTicketActivity.class);
	}
	
	public static void accessWaitingForTicketActivity(Context context, NuntiumTicket ticket) {
		context.startActivity(new Intent().setClass(context, WaitingForChannelActivity.class)
				.putExtra("ticket", ticket));
	}
	
	public static void settings(Context context) {
		startActivity(context, LGWPreferenceActivity.class);
	}
	
	public static void resetSettings(Context context) {
		((LGWPreferenceActivity) context).settings().resetAdvancedConfiguration();
	}
	
	public static void sendActivityLog(final Context context, final Handler handler) {
		final Toast toastSending = Toast.makeText(context, context.getResources().getString(R.string.sending_activity_log), Toast.LENGTH_LONG);
		final Toast toastOk = Toast.makeText(context, context.getResources().getString(R.string.activity_log_sent), Toast.LENGTH_LONG);
		final Toast toastError = Toast.makeText(context, context.getResources().getString(R.string.could_not_send_activity_log), Toast.LENGTH_LONG);
		
		confirm(context, R.string.send_activity_log, R.string.send_activity_log_description, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	new Thread() {
        			public void run() {
        				handler.post(new Runnable() {
        					public void run() {
        						toastSending.show();
        					}
        				});
        				
        				StringBuilder message = new StringBuilder();
        				Cursor cursor = context.getContentResolver().query(Logs.CONTENT_URI, Logs.PROJECTION, null, null, null);
        				try {
        					while(cursor.moveToNext()) {
        						CharSequence when = DateFormat.format("yyyy-MM-dd hh:mm:ss", cursor.getInt(cursor.getColumnIndex(Logs.WHEN)));
        						CharSequence text = cursor.getString(cursor.getColumnIndex(Logs.TEXT));

        						message.append(when).append(": ");
        						message.append(text).append("\n");
        						String stackTrace = cursor.getString(cursor.getColumnIndex(Logs.STACK_TRACE));
        						if (stackTrace != null && TextUtils.getTrimmedLength(stackTrace) > 0) {
        							message.append(stackTrace).append("\n");
        						}
        					}
        				} finally {
        					cursor.close();
        				}
        				
        				ExternalLog log = new ExternalLog(context);
        				try {
        					log.send(message.toString());
        					
        					handler.post(new Runnable() {
        						public void run() {
        							toastOk.show();
        						}
        					});
        				} catch (IOException e) {
        					handler.post(new Runnable() {
        						public void run() {
        							toastError.show();
        						}
        					});
        				}
        			};
        		}.start();
            }
        });
	}
	
	public static void retryAllOutgoingMessages(Context context, Handler handler) {
		new GeoChatLgwData(context).resetAllOutgoingMessageTries();
		refresh(context, handler, R.string.retrying_all);
	}
	
	public static void deleteAllOutgoingMessages(final Context context) {
		confirm(context, R.string.delete_all, R.string.are_you_sure, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new GeoChatLgwData(context).deleteAllOutgoingMessages();
			}
		});
	}
	
	public static void deleteAllIncomingMessages(final Context context) {
		confirm(context, R.string.delete_all, R.string.are_you_sure, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new GeoChatLgwData(context).deleteAllIncomingMessages();
			}
		});
	}
	
	public static void stop(Context context) {
		if (geochatService != null) {
			context.getApplicationContext().unbindService(geochatServiceConnection);
			geochatService = null;
			geochatServiceConnection = null;
		}
		context.getApplicationContext().stopService(new Intent().setClass(context, GeoChatTransceiverService.class));
	}
	
	public static synchronized void refresh(final Context context, final Handler handler) {
		refresh(context, handler, R.string.refreshing);
	}
	
	public static synchronized void refresh(final Context context, final Handler handler, final int message) {
		if (geochatService == null) {
			geochatServiceConnection = new ServiceConnection() {
				public void onServiceDisconnected(ComponentName className) {
					geochatService = null;
				}
				public void onServiceConnected(ComponentName className, IBinder service) {
					geochatService = ((GeoChatTransceiverService.LocalBinder)service).getService();
					refreshInternal(context, handler, message);
				}
			};
			context.getApplicationContext().bindService(new Intent(context, GeoChatTransceiverService.class), geochatServiceConnection, Context.BIND_AUTO_CREATE);
		} else {
			refreshInternal(context, handler, message);
		}
	}
	
	private static void refreshInternal(final Context context, final Handler handler, final int message) {
		geochatService.resync();
		
		final Toast toast = Toast.makeText(context, context.getResources().getString(message), Toast.LENGTH_LONG);
		handler.post(new Runnable() {
			public void run() {
				toast.show();
			}
		});
	}
	
	private static void confirm(Context context, int title, int message, DialogInterface.OnClickListener action) {
		new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, action)
        .setNegativeButton(android.R.string.no, null)
        .show();
	}

}
