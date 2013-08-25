package com.example.whocalled;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Statistic")
public class Statistic {
	@DatabaseField(generatedId = true)
	private int _id;
	
	@DatabaseField(canBeNull = false)
	private String phonenumber;
	
	@DatabaseField(canBeNull = true)
	private String username;
	
	@DatabaseField(canBeNull = true)
	private String contacturi;
	
	@DatabaseField(canBeNull = true)
	private long callcounts;
	
	@DatabaseField(canBeNull = true)
	private long callduration;
	
	@DatabaseField(canBeNull = true)
	private long callaverage;
	
	@DatabaseField(canBeNull = true)
	private long statisticdate;
	
	public Statistic() {}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getCallcounts() {
		return callcounts;
	}

	public void setCallcounts(long callcounts) {
		this.callcounts = callcounts;
	}

	public long getCallaverage() {
		return callaverage;
	}

	public void setCallaverage(long callaverage) {
		this.callaverage = callaverage;
	}

	public long getCallduration() {
		return callduration;
	}

	public void setCallduration(long callduration) {
		this.callduration = callduration;
	}

	public String getContacturi() {
		return contacturi;
	}

	public void setContacturi(String contacturi) {
		this.contacturi = contacturi;
	}

	public long getStatisticdate() {
		return statisticdate;
	}

	public void setStatisticdate(long statisticdate) {
		this.statisticdate = statisticdate;
	}
	
	
}
