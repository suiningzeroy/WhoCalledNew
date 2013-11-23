package com.example.whocalled;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class WhoCalledIntentService extends IntentService {
	private static final String LOCK_TAG = "WhoCalledIntentService";
	private String LOGGING_TAG = "WhoCalledIntentService";;
	
	private static PowerManager.WakeLock wakeLock = null;
	
	
	public WhoCalledIntentService() {
		super("WhoCalledIntentService");
	}
	public static synchronized void acquireLock(Context ctx){
		if (wakeLock == null){
			PowerManager pMgr = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			wakeLock = pMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG);
			wakeLock.setReferenceCounted(false);
		}
		wakeLock.acquire();
	}
	
	public static synchronized void releaseLock(){
		if (wakeLock != null){
			wakeLock.release();
		}
	}

	@Override  
	public void onCreate() {  
		System.out.println("onCreate");  
		super.onCreate();  
	}

	@Override  
	public void onStart(Intent intent, int startId) {  
		System.out.println("onStart");  
		acquireLock(this);
		super.onStart(intent, startId);  
	}  
  
  
	@Override  
	public int onStartCommand(Intent intent, int flags, int startId) {  
		System.out.println("onStartCommand");  
		return super.onStartCommand(intent, flags, startId);  
	}  
  
  
	@Override  
	public void setIntentRedelivery(boolean enabled) {  
		super.setIntentRedelivery(enabled);  
		System.out.println("setIntentRedelivery");  
	}  
  
	@Override  
	protected void onHandleIntent(Intent intent) {  
		prepareStatistic();  
	}  

	@Override  
	public void onDestroy() {  
		System.out.println("onDestroy");  
		releaseLock();
		super.onDestroy();  
	}
	
	private void prepareStatistic(){
		Log.i(LOGGING_TAG, "Start Refresh Data");	
		WhoCalledUtil.storeCallLogsFromQureyToCallRecordTable(this,null,null);
		WhoCalledUtil.storeStatisticsFromRecordsToStatisticTable(this);
	}
}
