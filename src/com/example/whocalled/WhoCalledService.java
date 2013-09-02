package com.example.whocalled;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.whocalled.model.Statistic;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.CallLog;
import android.util.Log;


public class WhoCalledService  extends Service {

	private static final String LOCK_TAG = "whocalled service";
	
	private String LOGGING_TAG = "whocalled service";;
	
	private static PowerManager.WakeLock wakeLock = null;
	
	public static synchronized void acquireLock(Context ctx){
		if (wakeLock == null){
			PowerManager pMgr = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			wakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
			wakeLock.setReferenceCounted(true);
		}
		wakeLock.acquire();
	}
	
	public static synchronized void releaseLock(){
		if (wakeLock != null){
			wakeLock.release();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();		
		Log.i(LOGGING_TAG, "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOGGING_TAG, "onStartCommand");		
		if(!WhoCalledUtil.isTodaysDataPrepare(this)){
			Log.i(LOGGING_TAG, "prepareStatistic");
			prepareStatistic();
		}
		return Service.START_STICKY;
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOGGING_TAG, "onDestroy");
		releaseLock();
	}
	
	private void prepareStatistic(){
		acquireLock(this);
		Log.i(LOGGING_TAG, "Start Refresh Data");	
		WhoCalledUtil.storeCallLogsFromQureyToCallRecordTable(this,null,null);
		WhoCalledUtil.storeStatisticsFromRecordsToStatisticTable(this);
		WhoCalledUtil.releaseOrmLiteHelper();
		releaseLock();
	}

}
