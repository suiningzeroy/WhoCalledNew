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
	
	private String LOGGING_TAG = "WhoCalled Application";
	private Map<Long, Bitmap> imageCache;
	private SharedPreferences prefs;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.imageCache = new HashMap<Long, Bitmap>();
	}

	@Override
	public void onTerminate() {
		// not guaranteed to be called
		super.onTerminate();
	}
	
	public Map<Long, Bitmap> getImageCache() {
		return this.imageCache;
	}
	
	public SharedPreferences getPrefs() {
		return this.prefs;
	}
	

}
