package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.Country;
import org.instedd.geochat.lgw.msg.NuntiumClientException;
import org.instedd.geochat.lgw.msg.NuntiumTicket;
import org.instedd.geochat.lgw.msg.WrongHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AskForTicketActivity extends Activity {

	private final static int DIALOG_GETTING_TICKET = 1;
	private final static int DIALOG_UNKNOWN_ERROR = 2;
	
	private TextView countryCodeTextView;

	private Handler handler = new Handler();
	private ProgressDialog progressDialog;

	private Settings settings = new Settings(this);
	private Exception exception;

	private NuntiumTicket ticket;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ask_for_ticket);
		
		handleStartButtonEnablement();
		initializeTelephoneNumber();
		initializeCountriesWidget();
		
		findViewById(R.id.start_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				settings.saveTelephoneNumber(telephoneNumberWidget().getText().toString());
				if (settings.areIncomplete()) {
					new AskTicketTask().execute();
				} else {
					Actions.accesStartSessionActivity(AskForTicketActivity.this);
				}
			}
		});
		
		countryCodeTextView = (TextView)findViewById(R.id.country_code_text_view);
	}

	private void handleStartButtonEnablement() {
		final View start = findViewById(R.id.start_button);
		final TextView text = (TextView) findViewById(R.id.number);
		text.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				start.setEnabled(TextUtils.getTrimmedLength(text.getText()) != 0);
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
							
							Country country = settings.nuntiumClient().countries()[position];
							countryCodeTextView.setText("+" + country.getPhonePrefix());
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
		int index = settings.storedCountryCodeIndex();
		if (index >= 0) {
			countrySpinner().setSelection(index);
		}
	}
	
	private int getDrawableId(Country country) {
		int id;
		
		// Try iso2
		String iso2 = country.getIso2().toLowerCase();
		id = getResources().getIdentifier(iso2 , "drawable", getPackageName());
		
		// Try iso3
		if (id == 0) {
			String iso3 = country.getIso3().toLowerCase();
			id = getResources().getIdentifier(iso3 , "drawable", getPackageName());
		}
		
		return id;
	}

	private Spinner countrySpinner() {
		return (Spinner) findViewById(R.id.country_code);
	}

	private void setUpPossibleCountries() throws NuntiumClientException, WrongHostException {
		CountryAdapter adapter = new CountryAdapter(settings.nuntiumClient().countries());
		countrySpinner().setAdapter(adapter);
		
		String iso2 = getUserCountryIsoName();
		if (iso2 != null) {
			int index = findCountryIndex(iso2);
			if (index >= 0) {
				countrySpinner().setSelection(index);
			}
		}
	}
	
	private String getUserCountryIsoName() {
		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getNetworkCountryIso();
	}
	
	private int findCountryIndex(String iso2) throws NuntiumClientException, WrongHostException {
		Country[] countries = settings.nuntiumClient().countries();
		for(int i = 0; i < countries.length; i++) {
			Country country = countries[i];
			if (iso2.equalsIgnoreCase(country.getIso2()) || iso2.equalsIgnoreCase(country.getIso3())) {
				return i;
			}
		}
		return -1;
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
		} else if (Connectivity.isConnectivityException(exception)) {
			return Connectivity.showNoConnectionDialog(this);
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
	
	private class CountryAdapter extends BaseAdapter {
		
		private Country[] countries;
		
		public CountryAdapter(Country[] countries) {
			this.countries = countries;
		}

		@Override
		public int getCount() {
			return countries.length;
		}

		@Override
		public Object getItem(int arg0) {
			return countries[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.country_item, null);
            }
			
			fill(position, convertView);
			
			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.country_dropdown_item, null);
			}
			
			fill(position, convertView);
			
			return convertView;
		}
		
		private void fill(int position, View view) {
			Country country = countries[position];
			
			TextView countryName = (TextView)view.findViewById(R.id.country_name_text_view);
			
			countryName.setText(country.getName());
			
			int flagId = getDrawableId(country);
			if (flagId != 0) {
				Drawable flag = getResources().getDrawable(getDrawableId(country));
				countryName.setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null);
			}
		}
	}

}