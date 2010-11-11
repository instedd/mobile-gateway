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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
public final static String EXTRA_WRONG_CREDENTIALS = "WrongCredentials";
	
	private final static int DIALOG_LOGGING_IN = 1;
	private final static int DIALOG_WRONG_CREDENTIALS = 2;
	private final static int DIALOG_UNKNOWN_ERROR = 3;
	
	private final Handler handler = new Handler();
	private ProgressDialog progressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
	    
	    final GeoChatSettings settings = new GeoChatSettings(this);
	    String existingName = settings.getName();
	    String existingPassword = settings.getPassword();
	    String existingNumber = settings.getNumber();
	    
	    final EditText uiName = (EditText) findViewById(R.id.name);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final EditText uiNumber = (EditText) findViewById(R.id.number);
	    final Button uiStart = (Button) findViewById(R.id.start_button); 
	    
	    uiName.setText(existingName);
	    uiPassword.setText(existingPassword);
	    
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
						
						String name = uiName.getText().toString();
						String password = uiPassword.getText().toString();
						String number = uiNumber.getText().toString();
						
						try {
							QstClient client = new QstClient(name, password);
							client.sendAddress(number);
							
							settings.setCredentials(name, password, number);
							
							Actions.home(LoginActivity.this);
						} catch (QstClientException e) {
							handler.post(new Runnable() {
								public void run() {
									uiStart.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									showDialog(DIALOG_WRONG_CREDENTIALS);
								}									
							});
						} catch (Exception e) {
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
			int messageResourceId = id == DIALOG_WRONG_CREDENTIALS ? R.string.invalid_credentials : R.string.cannot_start_maybe_no_connection; 

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(messageResourceId)
				.setTitle(R.string.cannot_start)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}
	}
}