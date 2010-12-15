package org.instedd.geochat.lgw.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class GeoChatLgw {
	
	public static final String AUTHORITY = "org.instedd.geochat.lgw.provider.GeoChatLGW";
	
	// This class cannot be instantiated
    private GeoChatLgw() {}
    
    public static interface Messages extends BaseColumns {
    	/**
         * The id of this message
         * <P>Type: TEXT</P>
         */
        String GUID = "_guid";
        
        /**
         * The id of this message
         * <P>Type: TEXT</P>
         */
        String FROM = "_from";
        
        /**
         * The id of this message
         * <P>Type: TEXT</P>
         */
        String TO = "_to";
        
        /**
         * The text of this message
         * <P>Type: TEXT</P>
         */
        String TEXT = "_text";
        
        /**
         * The timestamp for when the message was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        String WHEN = "_when";
        
        /**
         * The default sort order for this table
         */
        String DEFAULT_SORT_ORDER = BaseColumns._ID;
    }
    
    public final static class IncomingMessages implements Messages, BaseColumns {
    	// This class cannot be instantiated
        private IncomingMessages() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/incoming");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of incoming messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.lgw.incoming";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.lgw.incoming";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " DESC";
        
        public final static String[] PROJECTION = {
    		_ID,
    		GUID,
    		FROM,
    		TO,
    		TEXT,
    		WHEN,
    	};
    }
    
    public final static class OutgoingMessages implements Messages, BaseColumns {
    	// This class cannot be instantiated
        private OutgoingMessages() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/outgoing");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of incoming messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.lgw.outgoing";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.lgw.outgoing";
        
        /**
         * Is this message being sent?
         * <P>Type: INTEGER</P>
         */
        public static final String SENDING = "_sending";
        
        public final static String[] PROJECTION = {
    		_ID,
    		GUID,
    		FROM,
    		TO,
    		TEXT,
    		WHEN,
    		SENDING,
    	};
    }
    
    public final static class Logs implements BaseColumns {
    	// This class cannot be instantiated
        private Logs() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/logs");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of log messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geochat.lgw.logs";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single log message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geochat.lgw.log";
        
        /**
         * The text of this log
         * <P>Type: TEXT</P>
         */
        public final static String TEXT = "_text";
        
        /**
         * The timestamp for when the log was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public final static String WHEN = "_when";
        
        /**
         * The default sort order for this table
         */
        public final static String DEFAULT_SORT_ORDER = BaseColumns._ID + " DESC";
        
        public final static String[] PROJECTION = {
    		_ID,
    		TEXT,
    		WHEN,
    	};
    }

}
