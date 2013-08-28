package com.example.whocalled;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.example.whocalled.model.Statistic;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class WhoCalledActivity extends Activity implements Handler.Callback {

	private static final String LOGGING_TAG = "WhoCalled Activity";
	public final String MESSAGE_FLAG = "MESSAGE_FLAG";
	public final int INITIAL_COMPLETE_MESSAGE = 111;
	private final String  IS_INITIAL_FLAG = "Intial_Flag";
	private final String ORDER_BY_DURATON = "callduration";
	private final String ORDER_BY_CALLCOUNTS = "callcounts";
	private final String ORDER_BY = "Order_By";
	
	private String orderByColumn;
	private SharedPreferences prefs;
	private WhoCalledOrmLiteHelper ormLiteHelper;
	private WhoCalledApp app;
	private ProgressDialog progressDialog;
	private StatisticAdapter myStatisticAdapter;
	private List<Statistic> statistics;
	private  List<Statistic> newstatistics;
	private Handler handler = new Handler(this);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_who_called);
		
		app =(WhoCalledApp) getApplication();
		prefs = app.getPrefs();
		ListView statisticsListView = (ListView)this.findViewById(R.id.WhoCalledList);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMax(2);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(getString(R.string.prepare));


		app.storeUpToDateContactsInfoToContactTable(this);
		statistics = new ArrayList<Statistic>();
		myStatisticAdapter = new StatisticAdapter(statistics);		
		statisticsListView.setAdapter(myStatisticAdapter);
		
		orderByColumn = prefs.getString(ORDER_BY, ORDER_BY_CALLCOUNTS);		;
		if(!prefs.getBoolean(IS_INITIAL_FLAG, false)){
			new PrepareTask().execute();
		}else{		
			resetListItems(getStatisticsForAdapter());
		}	
	}
	
	@Override
	public void onPause() {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		super.onPause();
	}

	 @Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.who_called, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
			 
		switch (item.getItemId()){
			case R.id.duration:
				orderByColumn = ORDER_BY_DURATON;
				setStringValueToSharedPreferences(ORDER_BY,ORDER_BY_DURATON);
				newstatistics = getStatisticsForAdapter();
				resetListItems(newstatistics);
				break;
			case R.id.counts:
				orderByColumn = ORDER_BY_CALLCOUNTS;
				setStringValueToSharedPreferences(ORDER_BY,ORDER_BY_CALLCOUNTS);
				newstatistics = getStatisticsForAdapter();
				resetListItems(newstatistics);
				break;
		}	
		return true;
	}
	
	public boolean handleMessage(Message msg) {
		
		switch (msg.getData().getInt(MESSAGE_FLAG)){
			case INITIAL_COMPLETE_MESSAGE:
				Log.i("REFRESH","INITIAL_COMPLETE_MESSAGE ");
				orderByColumn = prefs.getString(ORDER_BY, ORDER_BY_CALLCOUNTS);
				resetListItems(newstatistics);
				break;
			default:
				break;
		}
		return true;
	}
	
	private void resetListItems(List<Statistic> newstatistics) {
		statistics.clear();
		statistics.addAll(newstatistics);
		myStatisticAdapter.notifyDataSetChanged();
	}
		
	private void setBooleanValueToSharedPreferences(String Key, Boolean bool) {

		Editor editor = prefs.edit();
		editor.putBoolean(Key, bool);
		editor.commit();
	}
	
	private void setStringValueToSharedPreferences(String Key, String str) {

		Editor editor = prefs.edit();
		editor.putString(Key, str);
		editor.commit();
	}
	
	private class PrepareTask extends AsyncTask<Void, Integer, Integer> {
		
		private void sendMessage(int what) {
			Bundle bundle = new Bundle();
			bundle.putInt(MESSAGE_FLAG, what);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);		
		}
		
		@Override
		protected void onPreExecute() {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}
		
		@Override
		protected Integer doInBackground(Void... args) {
			publishProgress(1);
			app.storeCallLogsFromQureyToCallRecordTable(WhoCalledActivity.this,null,null);
			app.storeStatisticsFromRecordsToStatisticTable();
			newstatistics = getStatisticsForAdapter();
			app.releaseOrmLiteHelper();
			setBooleanValueToSharedPreferences(IS_INITIAL_FLAG,true);
			sendMessage(INITIAL_COMPLETE_MESSAGE);
			publishProgress(2);
			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			 int currentProgress = progress[0];
			 if ((currentProgress == 1) && !progressDialog.isShowing()) {
				progressDialog.show();
			 } else if ((currentProgress == 2) && progressDialog.isShowing()) {
				progressDialog.dismiss();
			 }
			 progressDialog.setProgress(progress[0]);
		}
	 }
	

private List<Statistic> getStatisticsFromTable(){		
		List<Statistic> result = null;
		try {
			result = app.getOrmLiteHelper().getStatisticDao().queryBuilder().orderBy(this.orderByColumn,false).query();
		}catch (SQLException e) {
			Log.d(LOGGING_TAG, "writing CallRecord reading to database failed");
			e.printStackTrace();
		}
		
		if (ormLiteHelper != null) {
			OpenHelperManager.releaseHelper();
			ormLiteHelper = null;
		}
		
		return result;
	}
	
	private Bitmap getContactImageBaseOnContactId(String contactId){
		Bitmap image = null;
		
		if(contactId == null){
			image = BitmapFactory.decodeResource(getResources(), R.drawable.ddicon);
			return image;
		}else{
			ContentResolver cr = getContentResolver(); 
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, 
					Long.parseLong(contactId)); 
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri); 
			image = BitmapFactory.decodeStream(input); 		
			return image;
		}
	}
	
	private List<Statistic> getStatisticsForAdapter(){
		String selection = CallLog.Calls.DATE + " > ?";
		String[] arg = {String.valueOf(app.getStatisticDateOfStatisticTable())};		
		app.updateStatisticTableBaseOnAddedCallLogs(this,selection,arg);
		List<Statistic> statistics = getStatisticsFromTable();
		
		return statistics;
	}
	
	private class StatisticAdapter extends ArrayAdapter<Statistic> {
		
		public StatisticAdapter(List<Statistic> statistics) {
			super(WhoCalledActivity.this, R.layout.list_item, statistics);
		}
		
		private void imageStoreInCache(ImageView imageView, Bitmap bitmap) {
			if (bitmap != null) {
				app.getImageCache().put((Long) imageView.getTag(), bitmap);
			}
		}
	
		 @Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_item, parent, false);
			}
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView phonenumber = (TextView) convertView.findViewById(R.id.number);
			TextView du = (TextView) convertView.findViewById(R.id.duration);
			TextView counts = (TextView) convertView.findViewById(R.id.counts);
			TextView ave = (TextView) convertView.findViewById(R.id.average_duration);
			ImageView image = (ImageView) convertView.findViewById(R.id.userImage);
			image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ddicon));	
			Statistic statistic = getItem(position);
	
			if (statistic != null) {
				name.setText(statistic.getUsername());
				phonenumber.setText(statistic.getPhonenumber());
				du.setText(String.valueOf(statistic.getCallduration()));
				counts.setText(String.valueOf(statistic.getCallcounts()));
				ave.setText(String.valueOf(statistic.getCallaverage()));
				Bitmap bitmap = app.getImageCache().get(statistic.get_id());
				if (bitmap != null) {
					image.setImageBitmap(bitmap);
				}else {
					if(getContactImageBaseOnContactId(statistic.getContacturi()) != null){
						image.setTag(statistic.get_id());
						image.setImageBitmap(getContactImageBaseOnContactId(statistic.getContacturi()));
						imageStoreInCache(image,getContactImageBaseOnContactId(statistic.getContacturi()));
					}
				}
			}	
			return convertView;
		}		 
	}

}
