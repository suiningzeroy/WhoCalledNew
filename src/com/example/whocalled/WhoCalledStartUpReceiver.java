package com.example.whocalled;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WhoCalledStartUpReceiver extends BroadcastReceiver {
	private static final int INITIAL_DELAY_IN_MINUTES = 2;
	private static final int SAMPLING_INTERVAL_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final int ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final int ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;
	private static final String LOGGING_TAG = "WhoCalled StartUpReceiver";
	
	private int getMillsTimeToOneOclockInNextMorning(){
		Date beginOfDay = new Date();
		final SimpleDateFormat dateStringFormat =new SimpleDateFormat("yyyyMMdd",Locale.CHINA);
		Date now = new Date();		
		String dateString = dateStringFormat.format(now);
		
		try {
			beginOfDay = dateStringFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		long intervel =  now.getTime() - beginOfDay.getTime();
		
		return (int) ((ONE_DAY_IN_MILLIS + ONE_HOUR_IN_MILLIS - intervel)/(60*1000));
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOGGING_TAG,"recive intent");
		AlarmManager alarmMgr = 
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, WhoCalledAlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, 
				i, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, getMillsTimeToOneOclockInNextMorning());
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, 
				now.getTimeInMillis(), SAMPLING_INTERVAL_IN_MILLIS, sender);
		Log.d(LOGGING_TAG,"setAlarm");
	}

}
