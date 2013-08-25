package com.example.whocalled;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WhoCalledStartUpReceiver extends BroadcastReceiver {

	private static final int INITIAL_DELAY_IN_MINUTES = 2;
	public static final int SAMPLING_INTERVAL_IN_MILLIS = 2 * 1000;
	public static final String LOGGING_TAG = "WhoCalledStartUpReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager alarmMgr = 
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, 
				i, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, INITIAL_DELAY_IN_MINUTES);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, 
				now.getTimeInMillis(), SAMPLING_INTERVAL_IN_MILLIS, sender);
		Log.d(LOGGING_TAG,"setAlarm");
	}

}
