package org.instedd.geochat.lgw.msg;

public class QstClientException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public QstClientException(Exception e) {
		super(e);
	}
	
	public QstClientException(String message) {
		super(message);
	}

}
