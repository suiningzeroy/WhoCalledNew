package com.example.whocalled.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "CallRecorder")

public class CallRecord {
	@DatabaseField(generatedId = true)
	private int _id;
	@DatabaseField(canBeNull = false)
	private String phonenumber;
	@DatabaseField(canBeNull = false)
	private long calldate;
	@DatabaseField(canBeNull = false)
	private long callduration;
	
	public CallRecord() {}
	public String getPhonenumber() {
		return phonenumber;
	}
	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}
	public long getCalldate() {
		return calldate;
	}
	public void setCalldate(long calldate) {
		this.calldate = calldate;
	}
	public long getCallduration() {
		return callduration;
	}
	public void setCallduration(long callduration) {
		this.callduration = callduration;
	}
}
