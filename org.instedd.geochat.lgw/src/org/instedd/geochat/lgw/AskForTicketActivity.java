package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.NuntiumClientException;
import org.instedd.geochat.lgw.msg.NuntiumTicket;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class AskForTicketActivity extends Activity {

	private final static int DIALOG_GETTING_TICKET = 1;
	private final static int DIALOG_UNKNOWN_ERROR = 2;

	private Handler handler = new Handler();
	private ProgressDialog progressDialog;

	private Settings settings = new Settings(this);
	private Exception exception;

	private NuntiumTicket ticket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ask_for_ticket);

		initializeTelephoneNumber();

		initializeCountriesWidget();

		findViewById(R.id.start_button).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						new AskTicketTask().execute();

					}
				});
	}

	private void initializeTelephoneNumber() {

		String existingNumber = settings.storedTelephoneNumber();

		if (existingNumber == null) {
			TelephonyManager telman = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			existingNumber = telman.getLine1Number();
			settings.saveTelephoneNumber(existingNumber);
		}

		if (existingNumber != null) {
			telephoneNumberWidget().setText(existingNumber);
		}

	}

	private EditText telephoneNumberWidget() {
		return (EditText) findViewById(R.id.number);
	}

	private void initializeCountriesWidget() {
		try {
			setUpPossibleCountries();
			setStoredCountry();
			setUpOnChangeCountryCallback();
		} catch (NuntiumClientException e) {
			handle(e);
		} catch (WrongHostException e) {
			handle(e.withMessage(getResources().getString(R.string.fix_host)));
		}
	}

	private void handle(Exception e) {
		exception = e;
		countrySpinner().setEnabled(false);
		showDialog(DIALOG_UNKNOWN_ERROR);
	}

	private void setUpOnChangeCountryCallback() {
		countrySpinner()
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						try {
							settings.saveCountryCodeAtIndex(position);
						} catch (NuntiumClientException e) {
							handle(e);
						} catch (WrongHostException e) {
							handle(e.withMessage(getResources().getString(R.string.fix_host)));
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
	}

	private void setStoredCountry() throws NuntiumClientException, WrongHostException {
		countrySpinner().setSelection(settings
				.storedCountryCodeIndex());
	}

	private Spinner countrySpinner() {
		return (Spinner) findViewById(R.id.country_code);
	}

	private void setUpPossibleCountries() throws NuntiumClientException, WrongHostException {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, settings.nuntiumClient()
						.countryNames());

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner().setAdapter(adapter);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		Actions.stop(this);
		initializeTelephoneNumber();
		try {
			setStoredCountry();
		} catch (NuntiumClientException e) {
			handle(e);
		} catch (WrongHostException e) {
			handle(e);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_GETTING_TICKET) {
			String message = getResources().getString(R.string.starting);

			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(message);
			progressDialog.setMessage(message);
			progressDialog.setCancelable(false);
			return progressDialog;
		} else {
			String message = getResources().getString(
					R.string.cannot_start_error, exception.getMessage());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(message).setTitle(R.string.cannot_start)
					.setCancelable(true)
					.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.settings(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}

	private class AskTicketTask extends AsyncTask<String, Integer, Integer> {

		protected void onPreExecute() {
			showDialog(DIALOG_GETTING_TICKET);
			settings.saveTelephoneNumber(telephoneNumberWidget().getText().toString());
		}

		protected Integer doInBackground(String... params) {
			try {
				ticket = settings.nuntiumClient()
						.requestTicketFor(
								settings.storedTelephoneNumber());
				return 0;
			} catch (NuntiumClientException e) {
				exception = e;
				return 1;
			}
		}

		protected void onPostExecute(Integer result) {
			switch (result) {
			case 1:
				showDialog(DIALOG_UNKNOWN_ERROR);
				dismissDialog(DIALOG_GETTING_TICKET);
				break;
			case 0:
				dismissDialog(DIALOG_GETTING_TICKET);
				Actions.accessWaitingForTicketActivity(AskForTicketActivity.this, ticket);
				break;
			}
		}
	}

}