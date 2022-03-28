package com.message.sender.model;

import java.util.Date;

public class CommitMessage {

	private String type;
	private String hash;
	private String author;
	private String message;
	private Date date;
	
	public CommitMessage() {}
	
	public CommitMessage(String type, String hash, String author, String message, Date date) {
		this.type = type;
		this.hash = hash;
		this.author = author;
		this.message = message;
		this.date = date;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	
	
}
