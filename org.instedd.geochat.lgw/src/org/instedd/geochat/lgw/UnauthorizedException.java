package org.instedd.geochat.lgw;

import org.instedd.geochat.lgw.msg.QstClientException;

public class UnauthorizedException extends QstClientException {

	private static final long serialVersionUID = 1L;
	
	public UnauthorizedException() {
		super("");
	}

}
