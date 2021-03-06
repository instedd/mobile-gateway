package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogsActivity extends ListActivity {
	
	private Cursor cursor;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Logs.CONTENT_URI);
		}
		
		this.cursor = managedQuery(intent.getData(), Logs.PROJECTION, null, null, null);

        SimpleCursorAdapter adapter = new LogsCursorAdapter(this, R.layout.log_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);

		ListView lv = getListView();
		lv.setLongClickable(true);

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				cursor.moveToPosition(i);
				final String stackTrace = cursor.getString(cursor.getColumnIndex(Logs.STACK_TRACE));
				if (stackTrace != null && !stackTrace.equals("")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setTitle("Error");
					builder.setMessage(stackTrace);
					builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
						}
					});
					builder.show();
					return true;
				}
				return false;
			}
		});
    }
	
	private static class LogsCursorAdapter extends SimpleCursorAdapter {

		private final Cursor c;
		private final Context context;

		public LogsCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.c = c;
			this.context = context;
		}
		
		@Override
		public View getView(int pos, View inView, ViewGroup parent) {
			View v = inView;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.message_item, null);
			}
			this.c.moveToPosition(pos); 
			
			((TextView) v.findViewById(R.id.message)).setText(c.getString(c.getColumnIndex(Logs.TEXT)));
			
			long longDate = c.getLong(c.getColumnIndex(Logs.WHEN));
			if (longDate > 0) {
				CharSequence date = DateUtils.getRelativeDateTimeString(context, longDate, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);;
				((TextView) v.findViewById(R.id.date)).setText(date);
			} else {
				((TextView) v.findViewById(R.id.date)).setText("");
			}
			
			return v;
		}

	}
}


