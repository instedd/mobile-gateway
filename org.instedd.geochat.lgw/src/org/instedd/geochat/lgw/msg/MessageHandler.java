package org.instedd.geochat.lgw.msg;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MessageHandler extends DefaultHandler {
	
	private Message[] NO_MESSAGES = {};
	
	private final static int NONE = 0;
	private final static int TEXT = 1;
	
	private boolean inMessage;
	private int tagName;
	private Message[] messages;
	private int messagesCount;
	private Message message;
	private StringBuilder text;
	
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
				message.from = attributes.getValue("from");
				message.to = attributes.getValue("to");
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

}
