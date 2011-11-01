package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.NuntiumClientException;
import org.instedd.geochat.lgw.msg.NuntiumTicket;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class WaitingForChannelActivity extends Activity {

	private Exception exception;
	private Settings settings = new Settings(this);
	private final static int DIALOG_UNKNOWN_ERROR = 1;
	private final static int DIALOG_WELCOME = 2;
	private NuntiumTicket nuntiumTicket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.waiting_for_channel);
		Bundle bundle = getIntent().getExtras();
		nuntiumTicket = (NuntiumTicket) bundle.get("ticket");

		((TextView) findViewById(R.id.ticketCode))
				.setText(nuntiumTicket.code());

		new KeepAliveTicketTask().execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_UNKNOWN_ERROR:
			return (new AlertDialog.Builder(this))
					.setMessage(errorOnStartMessage())
					.setTitle(R.string.cannot_start).setCancelable(true)
					.setNeutralButton(android.R.string.ok, null).create();
		case DIALOG_WELCOME:
			AlertDialog welcomeDialog = (new AlertDialog.Builder(this))
					.setMessage(nuntiumTicket.data().get("message"))
					.setTitle(R.string.success).setCancelable(true)
					.setNeutralButton(android.R.string.ok, null).create();
			welcomeDialog.setOnDismissListener(dismissListener());
			return welcomeDialog;
		}
		return super.onCreateDialog(id);
	}

	private OnDismissListener dismissListener() {
		return new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				Actions.accesStartSessionActivity(WaitingForChannelActivity.this);
			}
		};
	}

	private String errorOnStartMessage() {
		return getResources().getString(R.string.cannot_start_error,
				exception.getMessage());
	}

	private class KeepAliveTicketTask extends
			AsyncTask<String, Integer, Integer> {

		protected void onPreExecute() {
		}

		protected Integer doInBackground(String... params) {
			try {
				while (nuntiumTicket.status().equals("pending")) {
					nuntiumTicket = settings.nuntiumClient().askForUpdateAbout(
							nuntiumTicket);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
				return 0;
			} catch (NuntiumClientException e) {
				exception = e;
				return 1;
			} catch (WrongHostException e) {
				exception = e.withMessage(getResources().getString(
						R.string.fix_host));
				return 1;
			}
		}

		protected void onPostExecute(Integer result) {
			switch (result) {
			case 1:
				showDialog(DIALOG_UNKNOWN_ERROR);
				break;
			case 0:
				settings.saveCredentialsFrom(nuntiumTicket);
				showDialog(DIALOG_WELCOME);
				break;
			}
		}
	}
}
