package com.example.whocalled;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.example.whocalled.model.CallRecord;
import com.example.whocalled.model.Contact;
import com.example.whocalled.model.Statistic;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class WhoCalledOrmLiteHelper  extends OrmLiteSqliteOpenHelper {
	
	private Dao<Statistic, Integer> statisticDao = null;
	private Dao<CallRecord, Integer> callRecordDao = null;
	private Dao<Contact, Integer> contactDao = null;
	private RuntimeExceptionDao<Statistic, Integer> statisticRuntimeDao = null;
	public static final String DB_NAME = "WhoCalledDB";
	public static final int DB_VERSION = 1;
	
	
	public WhoCalledOrmLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			Log.i(Statistic.class.getName(), "onCreating Bone db");
			TableUtils.createTable(connectionSource, Statistic.class);
			TableUtils.createTable(connectionSource, CallRecord.class);
			TableUtils.createTable(connectionSource, Contact.class);
		} catch (SQLException e) {
			Log.e(WhoCalledOrmLiteHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		try {
			Log.i(WhoCalledOrmLiteHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Statistic.class, true);
			TableUtils.dropTable(connectionSource, CallRecord.class, true);
			TableUtils.dropTable(connectionSource, Contact.class, true);
				// after we drop the old databases, we create the new ones
			onCreate(arg0, connectionSource);
		} catch (SQLException e) {
			Log.e(WhoCalledOrmLiteHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		
	}
	
	public void dropAllTable(){
		try {
			Log.i(WhoCalledOrmLiteHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Statistic.class, true);
			TableUtils.dropTable(connectionSource, CallRecord.class, true);
			TableUtils.dropTable(connectionSource, Contact.class, true);
		} catch (SQLException e) {
			Log.e(WhoCalledOrmLiteHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
	public void dropCallTable(){
		try {
			Log.i(WhoCalledOrmLiteHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, CallRecord.class, true);
		} catch (SQLException e) {
			Log.e(WhoCalledOrmLiteHelper.class.getName(), "Can't drop CallTable", e);
			throw new RuntimeException(e);
		}
	}
	
	public Dao<Statistic, Integer> getStatisticDao() throws SQLException {
		if (statisticDao == null) {
			statisticDao = getDao(Statistic.class);
		}
		return statisticDao;
	}
	
	public Dao<CallRecord, Integer> getCallRecordDao() throws SQLException {
		if (callRecordDao == null) {
			callRecordDao = getDao(CallRecord.class);
		}
		return callRecordDao;
	}
	
	public Dao<Contact, Integer> getContactDao() throws SQLException {
		if (contactDao == null) {
			contactDao = getDao(Contact.class);
		}
		return contactDao;
	}
	
	public RuntimeExceptionDao<Statistic, Integer> getSimpleDataDao() {
		if (statisticRuntimeDao == null) {
			statisticRuntimeDao = getRuntimeExceptionDao(Statistic.class);
		}
		return statisticRuntimeDao;
	}
	

	/**
	* Close the database connections and clear any cached DAOs.
	*/
	@Override
	public void close() {
		super.close();
		statisticRuntimeDao = null;
	}
}
