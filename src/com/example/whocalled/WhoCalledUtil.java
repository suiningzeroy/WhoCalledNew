package com.example.whocalled;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.whocalled.model.CallRecord;
import com.example.whocalled.model.Contact;
import com.example.whocalled.model.Statistic;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;

public class WhoCalledUtil {
	public static final long ONE_DAY = 24 * 60 * 60 * 1000;
	
	private static String LOGGING_TAG = "WhoCalledUtil";
	private static WhoCalledOrmLiteHelper ormLiteHelper;
	private static GenericRawResults<String[]> contacts = null;
	
	public static WhoCalledOrmLiteHelper getOrmLiteHelper(Context context) {
		if (ormLiteHelper == null) {
			ormLiteHelper = OpenHelperManager.getHelper(context, WhoCalledOrmLiteHelper.class);
			Log.i("getOrmLiteHelper", ormLiteHelper.getReadableDatabase().getPath());
			Log.d(LOGGING_TAG, "getOrmLiteHelper success!");
		}
		return ormLiteHelper;
		
	}
	
	public static void releaseOrmLiteHelper() {
		if (ormLiteHelper != null) {
			OpenHelperManager.releaseHelper();
			ormLiteHelper = null;
			Log.d(LOGGING_TAG, "releaseOrmLiteHelper success!");
		}
	}
	
	public static void clearCallRecordTable(Context context) {
		Log.d(LOGGING_TAG, "clearCallRecordTable  ");
		try {
			getOrmLiteHelper(context).getCallRecordDao().delete(getOrmLiteHelper(context).getCallRecordDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearContactTable(Context context) {
		Log.d(LOGGING_TAG, "clearContactTable  ");
		try {
			getOrmLiteHelper(context).getContactDao().delete(getOrmLiteHelper(context).getContactDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public static void clearStatisticTable(Context context) {
		Log.d(LOGGING_TAG, "clearStatisticTable  ");
		try {
			getOrmLiteHelper(context).getStatisticDao().delete(getOrmLiteHelper(context).getStatisticDao().queryForAll());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Contact getContactBaseOnPhoneNumber(Context context,String phoneNumber){
		List<Contact> names = null;
		Contact nullContact = new Contact();
		
		try {
			names = getOrmLiteHelper(context).getContactDao().queryBuilder().where().eq("phonenumber", phoneNumber).query();
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
	
	public static void insertToStatistics(Context context,Statistic staistic){
		try {
			getOrmLiteHelper(context).getStatisticDao().create(staistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static Statistic getStatistcBaseOnArray(Context context,String sampledate,String[] input) {	
		Contact currentContact = getContactBaseOnPhoneNumber(context,input[0]);
		
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
	private static void wirteDataToStatistcTable(Context context,String sampledate,String[] input) {		
		
		Statistic statistic = new Statistic();		
		statistic = getStatistcBaseOnArray(context,sampledate,input);
		insertToStatistics(context,statistic);	
	}
	
	private static String getStatisticDateOfCurrentStatistic(){
		Date now = new Date();
		long statisticDate = now.getTime();
		
		return Long.toString(statisticDate);
	}
	
	public static void storeStatisticsFromRecordsToStatisticTable(Context context){		
		Log.d(LOGGING_TAG, "storeStatisticsFromRecordsToStatisticTable  ");
		String statisticDate = getStatisticDateOfCurrentStatistic();
		clearStatisticTable(context);
		try{
			GenericRawResults<String[]> rawResults =
				getOrmLiteHelper(context).getCallRecordDao().queryRaw(
						"select phonenumber,count(_id) as counts, sum(callduration) as sumdu, sum(callduration)/count(_id) as ave from " +
						"CallRecorder group by phonenumber order by count(_id) desc");
			for (String[] resultArray : rawResults) {
				if(resultArray != null){					
					wirteDataToStatistcTable(context,statisticDate,resultArray);
				}
			}
				
		}catch (SQLException e){
			e.printStackTrace();
		}
		clearCallRecordTable(context);
	}

//------------------------------------------------------------------------------//
	public static void insertRecordToCallrecords(Context context,CallRecord callRecord){
		try {
			getOrmLiteHelper(context).getCallRecordDao().create(callRecord);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void wirteDataToCallRecordTable(Context context,String number, Long callDate, Long duration) {
		CallRecord callRecord =  new CallRecord();
		callRecord.setPhonenumber(number);
		callRecord.setCalldate(callDate);
		callRecord.setCallduration(duration);
		
		insertRecordToCallrecords(context,callRecord);
	}
	
	public static void storeTheCallLogToCallRecordTable(Context context,Cursor cursor) {
		String phoneNumber;
		
		cursor.moveToFirst();
		
		do {
			phoneNumber = modifyNumberInCallRecordIfUnrcognized(context,cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));;
			Long du = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			Long numberDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			wirteDataToCallRecordTable(context,phoneNumber,numberDate,du);
		}while(cursor.moveToNext()) ;
	}
	
	public static void storeCallLogsFromQureyToCallRecordTable(Context context,String selection, String[] arg){
		clearCallRecordTable(context);
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);
		storeTheCallLogToCallRecordTable(context,cursor);
		//updateCallRecordForUnrecognizedPhoneNumber(context);
		cursor.close();
	}
//-------------------------------------------------------------
	private static int updateStatistic(Context context,Statistic statistic){
		int result = 0;
		try {
			result = getOrmLiteHelper(context).getStatisticDao().update(statistic);
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return result;
	}
	
	private static Statistic queryStatisticBasedOnPhoneNumber(Context context,String phoneNumber){
		Statistic result = null;
		List<Statistic> resultList = null;
		try {
			resultList = getOrmLiteHelper(context).getStatisticDao().queryBuilder().where().eq("phonenumber", phoneNumber).query();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(resultList.size() ==0){
			return result;
		}else{
			return resultList.get(0);
		}
	}
	
	public static void updateStatisticUseAddedCallLog(Context context,Cursor cursor) {
		cursor.moveToFirst();
		String phoneNumber;
		Log.d(LOGGING_TAG, "updateStatisticUseAddedCallLog!");
		do {
			Log.d(LOGGING_TAG, "CACHED_NAME!: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
			Statistic result = new Statistic();
			Statistic resultForUpdate = new Statistic();
			phoneNumber =modifyNumberInCallRecordIfUnrcognized(context,cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
			Long du = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
			Long numberDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
			resultForUpdate = queryStatisticBasedOnPhoneNumber(context,phoneNumber);
			if(resultForUpdate != null){
				resultForUpdate.setCallcounts(resultForUpdate.getCallcounts()+1);
				resultForUpdate.setCallduration(resultForUpdate.getCallduration()+ du);
				resultForUpdate.setCallaverage(resultForUpdate.getCallduration()/resultForUpdate.getCallcounts());
				resultForUpdate.setStatisticdate(numberDate);
				updateStatistic(context,resultForUpdate);
			}
			else{
				result.setPhonenumber(phoneNumber);
				result.setCallcounts(1);
				result.setCallduration(du);
				result.setCallaverage(du);
				result.setStatisticdate(numberDate);
				insertToStatistics(context,result);
			}			
		}while(cursor.moveToNext()) ;
	}
	
	public static void updateStatisticTableBaseOnAddedCallLogs(Context context,String selection, String[] arg){		
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, selection, arg, CallLog.Calls.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() != 0){
			updateStatisticUseAddedCallLog(context,cursor);
		}else{
			Log.d(LOGGING_TAG, "cursor is null!");
		}		
		cursor.close();
	}
//-----------------------------------------------------//////
	public static void insertToContact(Context context,Contact contact){
		try {
			getOrmLiteHelper(context).getContactDao().create(contact);
		}catch (SQLException e) {
			Log.d(LOGGING_TAG, "writing CallRecord reading to database failed");
			e.printStackTrace();
		}
	}
	
	private static void storeTheContactsInfoToContactTable(Context context,Cursor cursor) {
		
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
				insertToContact(context,contact);
			}
			contactCursor.close();
		}
	}

	public static void storeUpToDateContactsInfoToContactTable(Context context) {
		Cursor cursor= context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, 
										null, null, null, null);		
		Log.d(LOGGING_TAG, String.valueOf(cursor.getCount()));
		clearContactTable(context);
		storeTheContactsInfoToContactTable(context,cursor);
		cursor.close();
	}
	
//---------------------------------------------------------------

	public static String getStatisticDateOfStatisticTable(Context context){		
		String statisticDate = "";
		try{
			GenericRawResults<String[]> maxdateResults =
				getOrmLiteHelper(context).getCallRecordDao().queryRaw(
					"select max(statisticdate) as sampledate from Statistic ");
			for (String[] maxdateResult : maxdateResults) {
				statisticDate = maxdateResult[0];
		}
		}catch (SQLException e){
				e.printStackTrace();
		}
		
		return statisticDate;
	}
	
	public static boolean isTodaysDataPrepare(Context context){
		Log.i(LOGGING_TAG, "isTodaysDataPrepare");
		
		long StatisticDate = Long.valueOf(getStatisticDateOfStatisticTable(context));
		Date now = new Date();
			if( now.getTime() - StatisticDate < ONE_DAY ){
				return true;
			}else{
				return false;
			}
	}	
	
//----------------------------------------
	
	public static GenericRawResults<String[]> getContactsPhoneNumber(Context context)
	{
		
		try {
			contacts = getOrmLiteHelper(context).getContactDao().queryRaw("select phonenumber from Contact");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return contacts;
	}
	
	public static void updateCallRecordForUnrecognizedPhoneNumber(Context context){
		contacts = null;
		List<CallRecord> records = new ArrayList<CallRecord>();
		getContactsPhoneNumber(context);
		if ( contacts != null ){
			for (String[] contact : contacts) {
				Log.i(LOGGING_TAG,"length < 8 number : " + contact[0]);
				try {
					records = getOrmLiteHelper(context).getCallRecordDao().queryBuilder().where().like("phonenumber", "0%"+ contact[0]).query();
					Log.i(LOGGING_TAG,"get callrecord whose number like length < 8 number" + Integer.toString(records.size()));
				} catch (SQLException e) {
					e.printStackTrace();
				}				
				if ( records.size() != 0){
					for (CallRecord record : records) {
						record.setPhonenumber(contact[0]);
						try {
							 getOrmLiteHelper(context).getCallRecordDao().update(record);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
		}
			
	}
	
	public static boolean compareTheLastSeveralFiguresBetweenTwoInputNumber(String contactNumber, String callRecordNumber,int figrueNumber) {
		return contactNumber.substring(contactNumber.length()-figrueNumber, contactNumber.length()).equals(
				callRecordNumber.substring(callRecordNumber.length()-figrueNumber, callRecordNumber.length()));
	}
	
	public static boolean compareFiguresBetweenTwoInputNumber(String contactNumber, String callRecordNumber) {
		Integer contactNumberLength = contactNumber.length();
		Integer callRecordNumberLength = callRecordNumber.length();
				
		if(contactNumberLength >= 10 ){
			
			return compareTheLastSeveralFiguresBetweenTwoInputNumber(contactNumber,callRecordNumber
						,callRecordNumberLength >= 10 ? 10 : callRecordNumberLength);
		}		
		if(contactNumberLength < 10 ){
			return compareTheLastSeveralFiguresBetweenTwoInputNumber(contactNumber,callRecordNumber
						,callRecordNumberLength >= 10 ? contactNumberLength : (contactNumberLength.compareTo(callRecordNumberLength) > 0 ? callRecordNumberLength : contactNumberLength));
		}	
		return false;
	}
	
	public static String modifyNumberInCallRecordIfUnrcognized(Context context,String phoneNumber){
		String result = new String(phoneNumber);
		
		GenericRawResults<String[]> allContactsNumber = getContactsPhoneNumber(context);
		
		if ( allContactsNumber != null ){
			for (String[] contact : allContactsNumber) {
				if (compareFiguresBetweenTwoInputNumber(contact[0],phoneNumber)){
					result = contact[0];
					break;
				}
			}
		}
		return result;
	}

}
