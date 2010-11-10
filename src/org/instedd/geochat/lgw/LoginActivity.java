package org.instedd.geochat.lgw;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.EditText;

public class LoginActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        TelephonyManager telman = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String num = telman.getLine1Number();
        if (num != null) {
        	((EditText) findViewById(R.id.number)).setText(num);
        }
    }
}