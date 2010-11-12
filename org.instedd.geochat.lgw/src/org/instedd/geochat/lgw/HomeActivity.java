package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.trans.GeoChatTransceiverService;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class HomeActivity extends TabActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.home);
	    
	    getApplicationContext().startService(new Intent().setClass(this, GeoChatTransceiverService.class));
	    
	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;
	
	    intent = new Intent().setClass(this, IncomingMessagesActivity.class);
	    spec = tabHost.newTabSpec("incoming").setIndicator(res.getString(R.string.incoming),
	                      res.getDrawable(R.drawable.ic_tab_messages))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, OutgoingMessagesActivity.class);
	    spec = tabHost.newTabSpec("incoming").setIndicator(res.getString(R.string.outgoing),
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
		Menues.settings(menu);
		Menues.stop(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, item.getItemId());
		return true;
	}

}
