package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.data.GeoChatLgwData;
import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Messages;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class IncomingMessagesActivity extends ListActivity implements OnItemLongClickListener {
	
	Cursor cursor;
	int position;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(IncomingMessages.CONTENT_URI);
		}
		
		this.cursor = managedQuery(intent.getData(), IncomingMessages.PROJECTION, null, null, null);

        SimpleCursorAdapter adapter = new MessageCursorAdapter(this, R.layout.message_item, cursor,
                new String[] { }, new int[] { });
        
        setListAdapter(adapter);
        
        getListView().setOnItemLongClickListener(this);
    }
    
    public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
		this.position = position;
		showDialog(0);
		return true;
	}
    
    @Override
	protected Dialog onCreateDialog(int id) {
		final CharSequence[] items = { 
				getResources().getString(R.string.delete),
		};
		
		cursor.moveToPosition(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.MT));
		builder.setItems(items, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int id = cursor.getInt(cursor.getColumnIndex(Messages._ID));
				switch(which) {
				case 0:
					new GeoChatLgwData(IncomingMessagesActivity.this).deleteIncomingMessage(id);
					break;
				}
			}
		});
		return builder.create();
	}
	
	private static class MessageCursorAdapter extends SimpleCursorAdapter {

		private final Cursor c;
		private final Context context;

		public MessageCursorAdapter(Context context, int layout, Cursor c,
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
			
			((TextView) v.findViewById(R.id.message)).setText(c.getString(c.getColumnIndex(Messages.TEXT)));
			((TextView) v.findViewById(R.id.number)).setText(c.getString(c.getColumnIndex(Messages.FROM)));
			
			long longDate = c.getLong(c.getColumnIndex(Messages.WHEN));
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

