package com.example.whocalled;

import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

public class WhoCalledService  extends Service {

	private static final String LOCK_TAG = "com.example.whocalled";
	public static final int ONE_DAY = 24 * 60 * 1000;
	
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
		//Log.i(LOGGING_TAG, "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.i(LOGGING_TAG, "onStartCommand");		
		
		return Service.START_STICKY;
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private boolean isTodaysDataPrepare(){
		
		Date today = new Date();
		return false;
	}
	
	

}
