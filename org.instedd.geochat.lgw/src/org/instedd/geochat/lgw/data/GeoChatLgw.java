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
        public static final String GUID = "_guid";
        
        /**
         * The id of this message
         * <P>Type: TEXT</P>
         */
        public static final String FROM = "_from";
        
        /**
         * The id of this message
         * <P>Type: TEXT</P>
         */
        public static final String TO = "_to";
        
        /**
         * The text of this message
         * <P>Type: TEXT</P>
         */
        public static final String TEXT = "_text";
        
        /**
         * The timestamp for when the message was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String WHEN = "_when";
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " DESC";
    }
    
    public final static class IncomingMessages implements BaseColumns {
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
    }
    
    public final static class OutgoingMessages implements BaseColumns {
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
    }

}
