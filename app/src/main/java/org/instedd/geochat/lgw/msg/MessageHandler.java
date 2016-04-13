package org.instedd.geochat.lgw.msg;

import android.util.Log;

import org.instedd.geochat.lgw.ISettings;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class MessageHandler extends DefaultHandler {
	
	private Message[] NO_MESSAGES = {};
	
	private final static int NONE = 0;
	private final static int TEXT = 1;

	private final static SimpleDateFormat DATE_FORMAT;
	static {
		DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private boolean inMessage;
	private int tagName;
	private Message[] messages;
	private int messagesCount;
	private Message message;
	private StringBuilder text;
	private final ISettings settings;

	public MessageHandler(ISettings settings) {
		this.settings = settings;
	}
	
	public Message[] getMessages() {
		if (messages == null)
			return NO_MESSAGES;
		
		Message[] finalMessages = new Message[messagesCount];
		System.arraycopy(messages, 0, finalMessages, 0, messagesCount);
		return finalMessages;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (!inMessage) {
			if ("message".equals(localName)) {
				inMessage = true;
				if (messages == null) {
					messages = new Message[10];
				}
				message = new Message();
				message.guid = attributes.getValue("id");
				message.from = removeProtocol(attributes.getValue("from"));
				message.to = addPlusIfMust(removeProtocol(attributes.getValue("to")));
				message.when = parseXmlDateTime(attributes.getValue("when"));
				inMessage = true;
				tagName = NONE;
			}
			return;
		}
		
		if ("text".equals(localName)) {
			tagName = TEXT;
		} else {
			tagName = NONE;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (inMessage && tagName == TEXT) {
			if (text == null) {
				text = new StringBuilder();
			}
			text.append(ch, start, length);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (inMessage && "message".equals(localName)) {
			if (text != null) {
				message.text = text.toString();
				text = null;
			}
			messages[messagesCount] = message;
			messagesCount++;
			inMessage = false;
		}
		tagName = NONE;
	}

	private long parseXmlDateTime(String when) {
		if (when == null || when.length() == 0) {
			return 0L;
		}

		try {
			return DATE_FORMAT.parse(when).getTime();
		} catch (ParseException e) {
			Log.w("Error parsing date " + when, e);
			return 0L;
		}
	}

	private String removeProtocol(String address) {
		if (address != null && address.startsWith("sms://")) {
			return address.substring(6);
		}
		return address;
	}
	
	private String addPlusIfMust(String address) {
		if (settings.storedAddPlusToOutgoing() && !address.startsWith("+")) {
			return "+" + address;
		}
		return address;
	}
	
	

}
