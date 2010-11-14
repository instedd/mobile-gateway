package org.instedd.geochat.lgw.data;

import java.util.HashMap;
import java.util.Map;

import org.instedd.geochat.lgw.data.GeoChatLgw.IncomingMessages;
import org.instedd.geochat.lgw.data.GeoChatLgw.Logs;
import org.instedd.geochat.lgw.data.GeoChatLgw.Messages;
import org.instedd.geochat.lgw.data.GeoChatLgw.OutgoingMessages;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class GeoChatLgwProvider extends ContentProvider {
	
	private static final String TAG = "GeoChatLgwProvider";
	
	private static final String DATABASE_NAME = "geochat_lgw.db";
    private static final int DATABASE_VERSION = 1;
    
    private static final String INCOMING_TABLE_NAME = "incoming";
    private static final String OUTGOING_TABLE_NAME = "outgoing";
    private static final String LOGS_TABLE_NAME = "logs";
    
    private static HashMap<String, String> sIncomingProjectionMap;
    private static HashMap<String, String> sOutgoingProjectionMap;
    private static HashMap<String, String> sLogsProjectionMap;
    
    public final static int INCOMING = 1;
    public final static int OUTGOING = 2;
    public final static int INCOMING_BEFORE_UP_TO_GUID = 3;
    public final static int OUTGOING_ID = 4;
    public final static int OUTGOING_GUID = 5;
    public final static int OUTGOING_NOT_SENDING = 6;
    public final static int LOGS = 7;
    public final static int LOGS_OLD = 8;
    
    public static final UriMatcher URI_MATCHER;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + INCOMING_TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + Messages.GUID + " TEXT,"
                    + Messages.FROM + " TEXT,"
                    + Messages.TO + " TEXT,"
                    + Messages.TEXT + " TEXT,"
                    + Messages.WHEN + " INTEGER"
                    + ");");
            db.execSQL("CREATE TABLE " + OUTGOING_TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + Messages.GUID + " TEXT,"
                    + Messages.FROM + " TEXT,"
                    + Messages.TO + " TEXT,"
                    + Messages.TEXT + " TEXT,"
                    + Messages.WHEN + " INTEGER,"
                    + OutgoingMessages.SENDING + " INTEGER"
                    + ");");
            db.execSQL("CREATE TABLE " + LOGS_TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + Logs.TEXT + " TEXT,"
                    + Logs.WHEN + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + INCOMING_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OUTGOING_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + LOGS_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;
    
    @Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
        case INCOMING:
        	count = db.delete(INCOMING_TABLE_NAME, where, whereArgs);
            break;
        case OUTGOING:
        	count = db.delete(OUTGOING_TABLE_NAME, where, whereArgs);
            break;
        case INCOMING_BEFORE_UP_TO_GUID: {
            String msgId = uri.getPathSegments().get(1);
            msgId = msgId.replace("'", "''");
            count = db.delete(INCOMING_TABLE_NAME, BaseColumns._ID + " <= (SELECT " + BaseColumns._ID + " FROM " + INCOMING_TABLE_NAME + " WHERE " + Messages.GUID + " = '" + msgId +  "')"
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        }
        case OUTGOING_ID: {
            String msgId = uri.getPathSegments().get(1);
            count = db.delete(OUTGOING_TABLE_NAME, BaseColumns._ID + "=" + msgId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        }
        case OUTGOING_GUID: {
            String msgId = uri.getPathSegments().get(2);
            msgId = msgId.replace("'", "''");
            count = db.delete(OUTGOING_TABLE_NAME, Messages.GUID + "='" + msgId + "'"
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        }
        case LOGS_OLD: {
            count = db.delete(LOGS_TABLE_NAME, BaseColumns._ID + " <= (SELECT MAX(" + BaseColumns._ID + ") - 50 FROM " + LOGS_TABLE_NAME + ")"
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        if (count > 0) {
        	getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
	}
    
    @Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
        case INCOMING:
        case INCOMING_BEFORE_UP_TO_GUID:
            return IncomingMessages.CONTENT_TYPE;
        case OUTGOING:
        case OUTGOING_ID:
        case OUTGOING_GUID:
            return IncomingMessages.CONTENT_TYPE;
        case LOGS:
        case LOGS_OLD:
            return Logs.CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}
    
    @Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId;
		
		switch(URI_MATCHER.match(uri)) {
		case INCOMING:
			rowId = db.insert(INCOMING_TABLE_NAME, Messages.GUID, values);
			if (rowId > 0) {
				Uri entryUri = ContentUris.withAppendedId(IncomingMessages.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(entryUri, null);
	            return entryUri;
			}
			break;
		case OUTGOING:
			rowId = db.insert(OUTGOING_TABLE_NAME, Messages.GUID, values);
			if (rowId > 0) {
				Uri entryUri = ContentUris.withAppendedId(OutgoingMessages.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(entryUri, null);
	            return entryUri;
			}
			break;
		case LOGS:
			rowId = db.insert(LOGS_TABLE_NAME, Messages.WHEN, values);
			if (rowId > 0) {
				Uri entryUri = ContentUris.withAppendedId(Logs.CONTENT_URI, rowId);
	            getContext().getContentResolver().notifyChange(entryUri, null);
	            return entryUri;
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}
    
    @Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}
    
    @Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = sortOrder;

        switch (URI_MATCHER.match(uri)) {
        case INCOMING:
        	qb.setTables(INCOMING_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case OUTGOING:
        	qb.setTables(OUTGOING_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case OUTGOING_ID:
        	qb.setTables(OUTGOING_TABLE_NAME);
        	qb.appendWhere(Messages._ID + " = " + uri.getPathSegments().get(1));
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case OUTGOING_NOT_SENDING:
        	qb.setTables(OUTGOING_TABLE_NAME);
        	qb.appendWhere(OutgoingMessages.SENDING + " = 0");
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Messages.DEFAULT_SORT_ORDER;
            }
        	break;
        case LOGS:
        	qb.setTables(LOGS_TABLE_NAME);
        	if (TextUtils.isEmpty(sortOrder)) {
                orderBy = Logs.DEFAULT_SORT_ORDER;
            }
        	break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}
    
    @Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
        case OUTGOING_GUID:
        	String msgId = uri.getPathSegments().get(2);
            msgId = msgId.replace("'", "''");
        	count = db.update(OUTGOING_TABLE_NAME, values, Messages.GUID + "= '" + msgId + "'"
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
        	break;
        case OUTGOING_NOT_SENDING:
            count = db.update(OUTGOING_TABLE_NAME, values, OutgoingMessages.SENDING + "= 0"
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "incoming", INCOMING);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "incoming/*", INCOMING_BEFORE_UP_TO_GUID);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "outgoing", OUTGOING);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "outgoing/#", OUTGOING_ID);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "outgoing/guid/*", OUTGOING_GUID);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "outgoing/not_sending", OUTGOING_NOT_SENDING);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "logs", LOGS);
        URI_MATCHER.addURI(GeoChatLgw.AUTHORITY, "logs/old", LOGS_OLD);

        for (int i = 0; i < 2; i++) {
        	Map<String, String> map;
        	if (i == 0) {
        		sIncomingProjectionMap = new HashMap<String, String>();
        		map = sIncomingProjectionMap;
        	} else {
        		sOutgoingProjectionMap = new HashMap<String, String>();
        		map = sOutgoingProjectionMap;
        	}
        	map.put(Messages._ID, Messages._ID);
        	map.put(Messages.GUID, Messages.GUID);
        	map.put(Messages.FROM, Messages.FROM);
        	map.put(Messages.TO, Messages.TO);
        	map.put(Messages.TEXT, Messages.TEXT);
        	map.put(Messages.WHEN, Messages.WHEN);
		}
        
        sLogsProjectionMap = new HashMap<String, String>();
        sLogsProjectionMap.put(Logs._ID, Logs._ID);
        sLogsProjectionMap.put(Logs.TEXT, Logs.TEXT);
        sLogsProjectionMap.put(Logs.WHEN, Logs.WHEN);
	}
}
