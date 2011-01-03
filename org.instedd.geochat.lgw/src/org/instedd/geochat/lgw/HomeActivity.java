package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.trans.GeoChatTransceiverService;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.home);
	    
	    getApplicationContext().startService(new Intent().setClass(this, GeoChatTransceiverService.class));
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;
	    
	    intent = new Intent().setClass(this, LogsActivity.class);
	    spec = tabHost.newTabSpec("logs").setIndicator(res.getString(R.string.activity),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	
	    intent = new Intent().setClass(this, OutgoingMessagesActivity.class);
	    spec = tabHost.newTabSpec("outgoing").setIndicator(res.getString(R.string.MTs),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, IncomingMessagesActivity.class);
	    spec = tabHost.newTabSpec("incoming").setIndicator(res.getString(R.string.MOs),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		getApplicationContext().startService(new Intent().setClass(this, GeoChatTransceiverService.class));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.refresh(menu);
		Menues.settings(menu);
		Menues.sendActivityLog(menu);
		Menues.stop(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		return true;
	}

}
