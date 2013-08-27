package com.example.whocalled.model;


import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Contact")
public class Contact {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField(canBeNull = false)
	private String phonenumber;
	@DatabaseField(canBeNull = true)
	private String contactname;
	@DatabaseField(canBeNull = true)
	private String contactId;
	public String getPhonenumber() {
		return phonenumber;
	}
	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}
	public String getContactname() {
		return contactname;
	}
	public void setContactname(String contactname) {
		this.contactname = contactname;
	}
	public String getContactId() {
		return contactId;
	}
	public void setContactId(String contactId) {
		this.contactId = contactId;
	}
}
