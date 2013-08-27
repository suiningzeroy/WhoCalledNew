package com.example.whocalled;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.whocalled.model.CallRecord;
import com.example.whocalled.model.Contact;
import com.example.whocalled.model.Statistic;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

public class WhoCalledApp extends Application {
	
	public static final long ONE_DAY = 24 * 60 * 60 * 1000;
	public static final long MORE_THAN_ONE_DAY = 3 * 24 * 60 * 60 * 1000;
	
	private String LOGGING_TAG = "WhoCalled Application";
	private WhoCalledOrmLiteHelper ormLiteHelper;
	private Map<Long, Bitmap> imageCache;
	private SharedPreferences prefs;
	
	
	public WhoCalledOrmLiteHelper getOrmLiteHelper() {
		if (ormLiteHelper == null) {
			ormLiteHelper = OpenHelperManager.getHelper(this, WhoCalledOrmLiteHelper.class);
			Log.d(LOGGING_TAG, "getOrmLiteHelper success!");
		}
		return ormLiteHelper;
		
	}
	
	public void releaseOrmLiteHelper() {
		if (ormLiteHelper != null) {
			OpenHelperManager.releaseHelper();
			ormLiteHelper = null;
			Log.d(LOGGING_TAG, "getOrmLiteHelper success!");
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.ormLiteHelper = getOrmLiteHelper();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.imageCache = new HashMap<Long, Bitmap>();
	}

	@Override
	public void onTerminate() {
		// not guaranteed to be called
		super.onTerminate();
		releaseOrmLiteHelper();
	}
	
	public void insertToStatistics(Statistic staistic){
		try {
			getOrmLiteHelper().getStatisticDao().create(staistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Map<Long, Bitmap> getImageCache() {
		return this.imageCache;
	}
	
	public SharedPreferences getPrefs() {
		return this.prefs;
	}
	
	public void clearCallRecordTable() {
		Log.d(LOGGING_TAG, "clearCallRecordTable  ");
		try {
			getOrmLiteHelper().getCallRecordDao().delete(getOrmLiteHelper().getCallRecordDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void clearContactTable() {
		Log.d(LOGGING_TAG, "clearContactTable  ");
		try {
			getOrmLiteHelper().getContactDao().delete(getOrmLiteHelper().getContactDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void clearStatisticTable() {
		Log.d(LOGGING_TAG, "clearStatisticTable  ");
		try {
			getOrmLiteHelper().getStatisticDao().delete(getOrmLiteHelper().getStatisticDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Contact getContactBaseOnPhoneNumber(String phoneNumber){
		List<Contact> names = null;
		Contact nullContact = new Contact();
		
		try {
			names = getOrmLiteHelper().getContactDao().queryBuilder().where().eq("phonenumber", phoneNumber).query();
		}catch (SQLException e) {
			Log.d(LOGGING_TAG, "writing CallRecord reading to database failed");
			e.printStackTrace();
		}		
		if(names.size()==0){
			return nullContact;
		}else{
			return names.get(0);
		}
	}
	
	public void wirteDataToStatistcTable(String sampledate,String[] input) {		
		
		Statistic statistic = new Statistic();		
		statistic = getStatistcBaseOnArray(sampledate,input);

		insertToStatistics(statistic);	
	}
	
	public Statistic getStatistcBaseOnArray(String sampledate,String[] input) {		
		Contact currentContact = getContactBaseOnPhoneNumber(input[0]);
		
		Statistic statistic = new Statistic();		
		statistic.setPhonenumber(input[0]);
		statistic.setCallcounts(Long.valueOf(input[1]));
		statistic.setCallduration(Long.valueOf(input[2]));
		statistic.setCallaverage(Long.valueOf(input[2])/Long.valueOf(input[1]));
		statistic.setStatisticdate(Long.valueOf(sampledate));
		statistic.setContacturi(currentContact.getContactId());
		statistic.setUsername(currentContact.getContactname());
		
		return statistic;

	}
	
	public String getStatisticDateOfCurrentStatistic(){
		
		String statisticdate = "";
		try{
			GenericRawResults<String[]> maxdateResults =
				getOrmLiteHelper().getCallRecordDao().queryRaw(
					"select max(statisticdate) as sampledate from Statistic ");
			for (String[] maxdateResult : maxdateResults) {
				statisticdate = maxdateResult[0];
		}
		}catch (SQLException e){
				e.printStackTrace();
		}
		
		Log.d(LOGGING_TAG, "statisticdate is :" + statisticdate);
		
		return statisticdate;
	}
	
	public String getSampleDateOfCurrentStatistic(){
		Date now = new Date();
		long statisticSampleDateInLong = now.getTime();
		
		return Long.toString(statisticSampleDateInLong);
	}
	
	public void storeStatisticFromRecordsToTable(){		
		
		String sampleDate = getSampleDateOfCurrentStatistic();
		
		clearStatisticTable();
		
		try{
			GenericRawResults<String[]> rawResults =
				getOrmLiteHelper().getCallRecordDao().queryRaw(
						"select phonenumber,count(_id) as counts, sum(callduration) as sumdu, sum(callduration)/count(_id) as ave from " +
						"CallRecorder group by phonenumber order by count(_id) desc");
			for (String[] resultArray : rawResults) {
				if(resultArray != null){
					wirteDataToStatistcTable(sampleDate,resultArray);
				}
			}
				
		}catch (SQLException e){
			e.printStackTrace();
		
		}		
		clearCallRecordTable();
		Log.d(LOGGING_TAG, "statistic table is prepared!");
	}

//------------------------------------------------------------------------------//
	public void insertRecordToCallrecords(CallRecord callRecord){
		try {
			getOrmLiteHelper().getCallRecordDao().create(callRecord);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void wirteDataToCallRecordTable(String number, Long callDate, Long duration) {
		CallRecord callRecord =  new CallRecord();
		callRecord.setPhonenumber(number);
		callRecord.setCalldate(callDate);
		callRecord.setCallduration(duration);
		
		insertRecordToCallrecords(callRecord);
	}
		
	public void StoreTheCallLogToTable(Cursor cursor) {
		
		Log.d(LOGGING_TAG, "StoreTheCallLogToTable!");
		cursor.moveToFirst();
		
		do {
			String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
			Long du = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			Long numberDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			wirteDataToCallRecordTable(phoneNumber,numberDate,du);
		}while(cursor.moveToNext()) ;
	}
	
	private Statistic queryStatisticBasedOnPhoneNumber(String phoneNumber){
		Statistic result = null;
		List<Statistic> resultList = null;
		try {
			resultList = getOrmLiteHelper().getStatisticDao().queryBuilder().where().eq("phonenumber", phoneNumber).query();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(resultList.size() ==0){
			return result;
		}else{
			return resultList.get(0);
		}
	}
	
	private int updateStatistic(Statistic statistic){
		int result = 0;
		try {
			result = getOrmLiteHelper().getStatisticDao().update(statistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void updateStatisticUseAddedCallLog(Cursor cursor) {
		
		Log.d(LOGGING_TAG, "updateStatisticUse!");
		cursor.moveToFirst();
		
		do {
			Statistic result = new Statistic();
			Statistic resultForUpdate = new Statistic();
			String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
			Long du = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			Long numberDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			resultForUpdate = queryStatisticBasedOnPhoneNumber(phoneNumber);
			if(resultForUpdate != null){
				resultForUpdate.setCallcounts(resultForUpdate.getCallcounts()+1);
				resultForUpdate.setCallduration(resultForUpdate.getCallduration()+ du);
				resultForUpdate.setCallaverage(resultForUpdate.getCallduration()/resultForUpdate.getCallcounts());
				resultForUpdate.setStatisticdate(numberDate);
				updateStatistic(resultForUpdate);
			}
			else{
				result.setPhonenumber(phoneNumber);
				result.setCallcounts(1);
				result.setCallduration(du);
				result.setCallaverage(du);
				result.setStatisticdate(numberDate);
				insertToStatistics(result);
			}			
		}while(cursor.moveToNext()) ;
	}
	
	public void StoreCallLogsFromQurey(Context context,String selection, String[] arg){
		
		Log.d(LOGGING_TAG, "StoreCallLogsFromQurey!");
		//Log.d(LOGGING_TAG, "sql is!" + "select * from calls where " + selection + " > " + arg[0]);
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);
		Log.d(LOGGING_TAG, "cursor " + String.valueOf(cursor.getCount()));
		StoreTheCallLogToTable(cursor);
		cursor.close();
	}
	
	public void StoreAddedCallLogsFromQurey(Context context,String selection, String[] arg){
		
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);
		Log.d(LOGGING_TAG, "added cursor !" + String.valueOf(cursor.getCount()));

		if (cursor != null & cursor.getCount() != 0){
			updateStatisticUseAddedCallLog(cursor);
		}else{
			Log.d(LOGGING_TAG, "cursor is null!");
		}		
		cursor.close();
	}
	//-----------------------------------------------------
	public void contactDeals(Context context) {
		Cursor cursor= context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
										null, null, null, null);		
		Log.d(LOGGING_TAG, String.valueOf(cursor.getCount()));
		clearContactTable();
		StoreTheContactInfoToTable(context,cursor);
		cursor.close();
	}
	
	private void StoreTheContactInfoToTable(Context context,Cursor cursor) {
		Integer count = 0;
		while (cursor.moveToNext()) {  
			count++;
			String id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));  
			String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));  
			String phoneNumber=null;  
			
			Cursor phones=context.getContentResolver()
					 .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
					 					ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id, null, null);
			while (phones.moveToNext()) {  
				phoneNumber=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
				Contact contact = new Contact();
				contact.setContactId(id);
				contact.setContactname(name);
				contact.setPhonenumber(phoneNumber);
				insertToContact(contact);
			}
			phones.close();
			//Log.d(LOGGING_TAG," id ="+id+" ,name= "+name+" ,phone: "+phoneNumber);  
		}
	}
	private void insertToContact(Contact contact){
		try {
			getOrmLiteHelper().getContactDao().create(contact);
		}catch (SQLException e) {
			Log.d(LOGGING_TAG, "writing CallRecord reading to database failed");
			e.printStackTrace();
		}
	}
	
	public boolean isTodaysDataPrepare(){
		Log.i(LOGGING_TAG, "isTodaysDataPrepare");
		
		long StatisticDate = Long.valueOf(getStatisticDateOfCurrentStatistic());
		Date now = new Date();
			if( now.getTime() - StatisticDate < ONE_DAY ){
				return true;
			}else{
				return false;				
			}
	}
	

}
