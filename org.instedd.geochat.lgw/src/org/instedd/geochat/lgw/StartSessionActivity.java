package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.QstClientException;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class StartSessionActivity extends Activity {

	private final static int DIALOG_LOGGING_IN = 1;
	private final static int DIALOG_WRONG_CREDENTIALS = 2;
	private final static int DIALOG_UNKNOWN_ERROR = 3;

	private Handler handler = new Handler();
	private ProgressDialog progressDialog;

	private Exception exception;
	private Settings settings = new Settings(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start_session);
		
		if (settings.areIncomplete()) {
			Actions.startAutomaticConfiguration(this);
		}

		setOnClickCallback();
	}

	private void setOnClickCallback() {
		findViewById(R.id.start_button).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						new LoginTask().execute();
					}
				});
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Actions.stop(this);
		
		if (settings.areIncomplete()) {
			Actions.startAutomaticConfiguration(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.start(menu);
		Menues.settings(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_LOGGING_IN) {
			String message = getResources().getString(R.string.starting);

			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(message);
			progressDialog.setMessage(message);
			progressDialog.setCancelable(false);
			return progressDialog;
		} else {
			if (Connectivity.isConnectivityException(exception)) {
				return Connectivity.showNoConnectionDialog(this);
			} else {
				String message;
				if (id == DIALOG_WRONG_CREDENTIALS) {
					message = getResources()
							.getString(R.string.invalid_credentials);
				} else {
					message = getResources().getString(R.string.cannot_start_error,
							exception.getMessage());
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(message).setTitle(R.string.cannot_start)
						.setCancelable(true)
						.setNeutralButton(android.R.string.ok, null);
				return builder.create();
			}
		}
	}
	
	private class LoginTask extends AsyncTask<String, Integer, Integer> {

		protected void onPreExecute() {
			showDialog(DIALOG_LOGGING_IN);
		}

		protected Integer doInBackground(String... params) {
			try {
				settings.qstClient().getLastSentMessageId();
				return 0;
			} catch (QstClientException e) {
				exception = e;
				return 1;
			} catch (WrongHostException e) {
				exception = e.withMessage(getResources().getString(R.string.fix_host));
				return 1;
			}
		}

		protected void onPostExecute(Integer result) {
			switch (result) {
			case 1:
				dismissDialog(DIALOG_LOGGING_IN);
				showDialog(DIALOG_UNKNOWN_ERROR);
				break;
			case 0:
				dismissDialog(DIALOG_LOGGING_IN);
				Actions.accessHomeActivity(StartSessionActivity.this);
				break;
			}
		}
	}

}
