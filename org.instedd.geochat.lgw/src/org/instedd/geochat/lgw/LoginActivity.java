package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.QstClient;
import org.instedd.geochat.lgw.msg.QstClientException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
public final static String EXTRA_WRONG_CREDENTIALS = "WrongCredentials";
	
	private final static int DIALOG_LOGGING_IN = 1;
	private final static int DIALOG_WRONG_CREDENTIALS = 2;
	private final static int DIALOG_UNKNOWN_ERROR = 3;
	
	final Handler handler = new Handler();
	ProgressDialog progressDialog;
	
	Exception exception;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
	    
	    final GeoChatLgwSettings settings = new GeoChatLgwSettings(this);
	    String existingEndpointUrl = settings.getEndpointUrl();
	    String existingName = settings.getName();
	    String existingPassword = settings.getPassword();
	    String existingNumber = settings.getNumber();
	    String existingCountryCode = settings.getCountryCode();
	    
	    final EditText uiEndpointUrl = (EditText) findViewById(R.id.endpoint_url);
	    final EditText uiName = (EditText) findViewById(R.id.name);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final EditText uiCountryCode = (EditText) findViewById(R.id.country_code);
	    final EditText uiNumber = (EditText) findViewById(R.id.number);
	    final Button uiStart = (Button) findViewById(R.id.start_button); 
	    
	    uiEndpointUrl.setText(existingEndpointUrl);
	    uiName.setText(existingName);
	    uiPassword.setText(existingPassword);
	    uiCountryCode.setText(existingCountryCode);
	    
	    if (existingNumber == null) {
	        TelephonyManager telman = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        existingNumber = telman.getLine1Number();
	    }
	    
	    if (existingNumber != null) {
	    	uiNumber.setText(existingNumber);
	    }
	    
	    uiStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread() {
					public void run() {
						handler.post(new Runnable() {
							public void run() {
								showDialog(DIALOG_LOGGING_IN);
								uiStart.setEnabled(false);
							}
						});
						
						String endpointUrl = uiEndpointUrl.getText().toString();
						String name = uiName.getText().toString();
						String password = uiPassword.getText().toString();
						
						String countryCodeValue = uiCountryCode.getText().toString().trim();
						
						String countryCode = countryCodeValue.equals("") ? null : countryCodeValue;
						String number = uiNumber.getText().toString();
						
						settings.setCredentials(endpointUrl, name, password, number, countryCode);
						
						try {
							QstClient client = settings.newQstClient();
							
							// Do this as a login
							client.getLastSentMessageId();
							
							handler.post(new Runnable() {
								public void run() {
									uiStart.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									
									Actions.home(LoginActivity.this);
								}
							});
						} catch (UnauthorizedException e) {
							handler.post(new Runnable() {
								public void run() {
									uiStart.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									showDialog(DIALOG_WRONG_CREDENTIALS);
								}									
							});
						} catch (QstClientException e) {
							exception = e;
							handler.post(new Runnable() {
								public void run() {
									uiStart.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									showDialog(DIALOG_UNKNOWN_ERROR);
								}									
							});
						} catch (Exception e) {
							exception = e;
							handler.post(new Runnable() {
								public void run() {
									uiStart.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									showDialog(DIALOG_UNKNOWN_ERROR);
								}
							});
						}
					};
				}.start();
			}			
		});
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
			String message;
			if (id == DIALOG_WRONG_CREDENTIALS) {
				message = getResources().getString(R.string.invalid_credentials);
			} else {
				message = getResources().getString(R.string.cannot_start_error, exception.getMessage());
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(message)
				.setTitle(R.string.cannot_start)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.settings(menu);
		Menues.sendActivityLog(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}
}