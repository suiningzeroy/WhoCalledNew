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
	private GenericRawResults<String[]> contacts = null;
	
	
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
	
	public Map<Long, Bitmap> getImageCache() {
		return this.imageCache;
	}
	
	public SharedPreferences getPrefs() {
		return this.prefs;
	}
	
	private void clearCallRecordTable() {
		Log.d(LOGGING_TAG, "clearCallRecordTable  ");
	/*	try {
			getOrmLiteHelper().getCallRecordDao().delete(getOrmLiteHelper().getCallRecordDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	private void clearContactTable() {
		Log.d(LOGGING_TAG, "clearContactTable  ");
		try {
			getOrmLiteHelper().getContactDao().delete(getOrmLiteHelper().getContactDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void clearStatisticTable() {
		Log.d(LOGGING_TAG, "clearStatisticTable  ");
		try {
			getOrmLiteHelper().getStatisticDao().delete(getOrmLiteHelper().getStatisticDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Contact getContactBaseOnPhoneNumber(String phoneNumber){
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
	
	private void insertToStatistics(Statistic staistic){
		try {
			getOrmLiteHelper().getStatisticDao().create(staistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Statistic getStatistcBaseOnArray(String sampledate,String[] input) {		
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
	private void wirteDataToStatistcTable(String sampledate,String[] input) {		
		
		Statistic statistic = new Statistic();		
		statistic = getStatistcBaseOnArray(sampledate,input);
		insertToStatistics(statistic);	
	}
	
	private String getStatisticDateOfCurrentStatistic(){
		Date now = new Date();
		long statisticDate = now.getTime();
		
		return Long.toString(statisticDate);
	}
	
	public void storeStatisticsFromRecordsToStatisticTable(){		
		
		String statisticDate = getStatisticDateOfCurrentStatistic();
		
		clearStatisticTable();
		
		try{
			GenericRawResults<String[]> rawResults =
				getOrmLiteHelper().getCallRecordDao().queryRaw(
						"select phonenumber,count(_id) as counts, sum(callduration) as sumdu, sum(callduration)/count(_id) as ave from " +
						"CallRecorder group by phonenumber order by count(_id) desc");
			for (String[] resultArray : rawResults) {
				if(resultArray != null){
					wirteDataToStatistcTable(statisticDate,resultArray);
				}
			}
				
		}catch (SQLException e){
			e.printStackTrace();
		}
		clearCallRecordTable();
	}

//------------------------------------------------------------------------------//
	private void insertRecordToCallrecords(CallRecord callRecord){
		try {
			getOrmLiteHelper().getCallRecordDao().create(callRecord);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void wirteDataToCallRecordTable(String number, Long callDate, Long duration) {
		CallRecord callRecord =  new CallRecord();
		callRecord.setPhonenumber(number);
		callRecord.setCalldate(callDate);
		callRecord.setCallduration(duration);
		
		insertRecordToCallrecords(callRecord);
	}
	
	private void storeTheCallLogToCallRecordTable(Cursor cursor) {
		cursor.moveToFirst();
		
		do {
			String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
			Long du = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			Long numberDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			wirteDataToCallRecordTable(phoneNumber,numberDate,du);
		}while(cursor.moveToNext()) ;
	}
	
	public void storeCallLogsFromQureyToCallRecordTable(Context context,String selection, String[] arg){
		
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);
		storeTheCallLogToCallRecordTable(cursor);
		updateCallRecordForUnrecognizedPhoneNumber();
		cursor.close();
	}
//-------------------------------------------------------------
	private int updateStatistic(Statistic statistic){
		int result = 0;
		try {
			result = getOrmLiteHelper().getStatisticDao().update(statistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return result;
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
	
	public void updateStatisticUseAddedCallLog(Cursor cursor) {
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
	
	public void updateStatisticTableBaseOnAddedCallLogs(Context context,String selection, String[] arg){		
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);

		if (cursor != null & cursor.getCount() != 0){
			updateStatisticUseAddedCallLog(cursor);
		}else{
			Log.d(LOGGING_TAG, "cursor is null!");
		}		
		cursor.close();
	}
//-----------------------------------------------------//////
	private void insertToContact(Contact contact){
		try {
			getOrmLiteHelper().getContactDao().create(contact);
		}catch (SQLException e) {
			Log.d(LOGGING_TAG, "writing CallRecord reading to database failed");
			e.printStackTrace();
		}
	}
	
	private void storeTheContactsInfoToContactTable(Context context,Cursor cursor) {
		
		while (cursor.moveToNext()) {  
			String id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));  
			String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));  
			String phoneNumber=null;  
			
			Cursor contactCursor=context.getContentResolver()
					 .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
					 					ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id, null, null);
			while (contactCursor.moveToNext()) {  
				phoneNumber=contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
				Contact contact = new Contact();
				contact.setContactId(id);
				contact.setContactname(name);
				contact.setPhonenumber(phoneNumber);
				insertToContact(contact);
			}
			contactCursor.close();
		}
	}

	public void storeUpToDateContactsInfoToContactTable(Context context) {
		Cursor cursor= context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
										null, null, null, null);		
		Log.d(LOGGING_TAG, String.valueOf(cursor.getCount()));
		clearContactTable();
		storeTheContactsInfoToContactTable(context,cursor);
		cursor.close();
	}
	
//---------------------------------------------------------------

	public String getStatisticDateOfStatisticTable(){		
		String statisticDate = "";
		try{
			GenericRawResults<String[]> maxdateResults =
				getOrmLiteHelper().getCallRecordDao().queryRaw(
					"select max(statisticdate) as sampledate from Statistic ");
			for (String[] maxdateResult : maxdateResults) {
				statisticDate = maxdateResult[0];
		}
		}catch (SQLException e){
				e.printStackTrace();
		}
		
		return statisticDate;
	}
	
	public boolean isTodaysDataPrepare(){
		Log.i(LOGGING_TAG, "isTodaysDataPrepare");
		
		long StatisticDate = Long.valueOf(getStatisticDateOfStatisticTable());
		Date now = new Date();
			if( now.getTime() - StatisticDate < ONE_DAY ){
				return true;
			}else{
				return false;				
			}
	}	
	
//----------------------------------------
	
	public void getPhoneNumberIfLengthLowerThanEight()
	{
		try {
			contacts = this.getOrmLiteHelper().getContactDao().queryRaw("select distinct phonenumber from Contact " +
					"where length(phonenumber)<= 8;");
			Log.i(LOGGING_TAG,"get length < 8 number" );
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void updateCallRecordForUnrecognizedPhoneNumber(){
		contacts = null;
		List<CallRecord> records = new ArrayList<CallRecord>();
		getPhoneNumberIfLengthLowerThanEight();
		if ( contacts != null ){
			for (String[] contact : contacts) {
				Log.i(LOGGING_TAG,"length < 8 number : " + contact[0]);
				try {
					records =this.getOrmLiteHelper().getCallRecordDao().queryBuilder().where().like("phonenumber", "%"+ contact[0]).query();
					Log.i(LOGGING_TAG,"get callrecord whose number like length < 8 number" + Integer.toString(records.size()));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				if ( records.size() != 0){
					for (CallRecord record : records) {
						Log.i(LOGGING_TAG,"update this records, original number is :" 
								+ record.getPhonenumber() + " new number is :" + contact[0]);
						record.setPhonenumber(contact[0]);
						try {
							this.getOrmLiteHelper().getCallRecordDao().update(record);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}else{
					Log.i(LOGGING_TAG," there is no reords' number like length < 8 number" );
				}
			
			}
		}else{
			Log.i(LOGGING_TAG,"contacts has no length < 8 number" );
		}
			
	}
	
	public String modifyNumberInCallRecordIfUnrcognized(String phoneNumber){
		List<CallRecord> records = new ArrayList<CallRecord>();
		getPhoneNumberIfLengthLowerThanEight();

		if ( contacts != null ){
			for (String[] contact : contacts) {
				Log.i(LOGGING_TAG,"length < 8 number : " + contact[0]);
				//if(contact[0] == )
			}
		}else{
			Log.i(LOGGING_TAG,"contacts has no length < 8 number" );
		}
		return "";
	}

}
