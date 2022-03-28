package com.message.sender.model;

import javax.jms.TextMessage;

public class ChanelAndMessage {
	private String chanel;
	private String message;
	
	public ChanelAndMessage() {}

	public ChanelAndMessage(String chanel, String message) {
		this.chanel = chanel;
		this.message = message;
	}

	public String getChanel() {
		return chanel;
	}

	public void setChanel(String chanel) {
		this.chanel = chanel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	};
	
	
}
