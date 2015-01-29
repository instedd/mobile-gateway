package org.instedd.geochat.lgw.msg;

public class WrongHostException extends Exception {

	private static final long serialVersionUID = -703515882035607741L;

	public WrongHostException(Exception e) {
		super(e);
	}

	public WrongHostException(String message) {
		super(message);
	}

	public Exception withMessage(String string) {
		return new WrongHostException(string);
	}
	
}
