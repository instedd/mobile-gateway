package org.instedd.geochat.lgw.msg;

import java.io.Serializable;
import java.util.HashMap;

public class NuntiumTicket implements Serializable{
	
	private static final long serialVersionUID = 304716855944779401L;
	
	private String code;
	private String secretKey;
	private String status;
	private HashMap<String, String> data;
	
	public NuntiumTicket(String code, String secretKey , String status , HashMap<String, String> hashMap){
		this.code = code;
		this.secretKey = secretKey;
		this.status = status;
		this.data = hashMap;
		
	}

	public String code() {
		return code;
	}

	public String secretKey() {
		return secretKey;
	}
	
	public String status() {
		return status;
	}

	public HashMap<String, String> data() {
		return data;
	}

}
