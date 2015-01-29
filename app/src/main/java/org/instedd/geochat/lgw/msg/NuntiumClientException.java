package org.instedd.geochat.lgw.msg;

public class NuntiumClientException extends Exception {

	private static final long serialVersionUID = -5980824463654599306L;

	public NuntiumClientException(Exception e) {
		super(e);
	}

	public NuntiumClientException(String message) {
		super(message);
	}

}
