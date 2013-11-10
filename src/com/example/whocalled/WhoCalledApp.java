package com.example.whocalled;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

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
