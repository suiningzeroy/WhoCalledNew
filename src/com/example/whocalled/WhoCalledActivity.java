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
	public static final String DASH = "-";
	public static final String PHONE_NUMBER = "Number";
	public static final String NAME = "Name";
	public static final String DURATION = "Duration";
	public static final String COUNTS = "Counts";
	public static final String AVERAGE_DURATION = "Average-duration";
	public static final String IMAGE = "UserImage";	
	public final String IS_INITIAL_COMPLETE_FLAG = "IsInitialComplete";
	public final String INITIAL_COMPLETE_MESSAGE = "completed";
	private final String  IS_INITIAL_FLAG = "IsIntial";
	
	
	private String orderByColumn;
	private WhoCalledOrmLiteHelper ormLiteHelper;
	private WhoCalledApp app;
	public static final String myACTION="android.whocalled.Start";
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
		ListView myListView = (ListView)this.findViewById(R.id.WhoCalledList);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMax(2);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(getString(R.string.prepare));
		
		Button startService = (Button) findViewById(R.id.startService);		
		
		startService.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {				
				Log.d(LOGGING_TAG, "button click");
		 		myStatisticAdapter.notifyDataSetChanged();
				//Intent intent = new Intent(myACTION);
				//sendBroadcast(intent);
			}
			
		});

		app.contactDeals(this);
		statistics = new ArrayList<Statistic>();
		myStatisticAdapter = new StatisticAdapter(statistics);		
		myListView.setAdapter(myStatisticAdapter);
		
		SharedPreferences prefs = app.getPrefs();
		Log.d(LOGGING_TAG, "IS_INITIAL_FLAG = " + Boolean.toString(!prefs.getBoolean(IS_INITIAL_FLAG, false)));
		if(!prefs.getBoolean(IS_INITIAL_FLAG, false)){
			new PrepareTask().execute();
		}else{
			resetListItems(getListDisplayData());
		}	
	}
	
	public boolean handleMessage(Message msg) {
		String text = msg.getData().getString(IS_INITIAL_COMPLETE_FLAG);
		if(text == INITIAL_COMPLETE_MESSAGE){
			resetListItems(newstatistics);
		}
		return true;
	}
	
	private void resetListItems(List<Statistic> newstatistics) {
		statistics.clear();
		statistics.addAll(newstatistics);
		myStatisticAdapter.notifyDataSetChanged();
	}
	
	private class PrepareTask extends AsyncTask<Void, Integer, Integer> {
		
		private void sendMessage(String what) {
			Bundle bundle = new Bundle();
			bundle.putString(IS_INITIAL_COMPLETE_FLAG, what);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);		
		}
		
		private void setBooleanValueToSharedPreferences(String Key, Boolean bool) {
			SharedPreferences prefs = app.getPrefs();
	 		Editor editor = prefs.edit();
	 		editor.putBoolean(Key, true);
	 		editor.commit();
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
			app.StoreCallLogsFromQurey(WhoCalledActivity.this);
	 		app.getStatisticFromRecords();
	 		newstatistics = getListDisplayData();
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
	
	@Override
	public void onPause() {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.who_called, menu);
		return true;
	}
	
private List<Statistic>getStatistics(){
		
		List<Statistic> result = null;
		try {
			result = app.getOrmLiteHelper().getStatisticDao().queryBuilder().orderBy("callduration",false).query();
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
	
	private Bitmap getDisplayImageBaseOnContactId(String contactId){
		Bitmap image = null;
		
		if(contactId == null){
			//Log.d(LOGGING_TAG, "contactId is null");
			image = BitmapFactory.decodeResource(getResources(), R.drawable.ddicon);
			return image;
		}else{
			ContentResolver cr = getContentResolver(); 
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, 
					Long.parseLong(contactId)); 
			//Log.d(LOGGING_TAG, uri.toString());
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri); 
			image = BitmapFactory.decodeStream(input); 
		
			return image;
		}
	}
	private List<Statistic> getListDisplayData(){
		List<Statistic> statistics = getStatistics();
		
		return statistics;
	}
	
	private class StatisticAdapter extends ArrayAdapter<Statistic> {
		
		private ThreadPoolExecutor executor =
				(ThreadPoolExecutor) Executors.newFixedThreadPool(5);
		
		
		
		public StatisticAdapter(List<Statistic> statistics) {
			super(WhoCalledActivity.this, R.layout.list_item, statistics);
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
				} else {
					if(getDisplayImageBaseOnContactId(statistic.getContacturi()) != null){
						image.setImageBitmap(getDisplayImageBaseOnContactId(statistic.getContacturi()));
						imageStoreInCache(image,getDisplayImageBaseOnContactId(statistic.getContacturi()));
					}
				//image.setTag(statistic.get_id());
				// separate thread/via task, for retrieving each image
				// (note that this is brittle as is, should stop all threads in onPause)	
				//new RetrieveImageTask(image).executeOnExecutor(executor,statistic.getContacturi());
				}
			}
	
			return convertView;
		}
		 
	}
	
	private class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView imageView;

		public RetrieveImageTask(ImageView imageView) {
			this.imageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... args) {
			ContentResolver cr = getContentResolver(); 
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, 
					Long.parseLong(args[0])); 
			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri); 
			Bitmap bitmap = BitmapFactory.decodeStream(input); 
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				app.getImageCache().put((Long) imageView.getTag(), bitmap);
				imageView.setTag(null);
			}
		}
	}
	
	private void imageStoreInCache(ImageView imageView, Bitmap bitmap) {
		if (bitmap != null) {

			app.getImageCache().put((Long) imageView.getTag(), bitmap);
		}
	}
	

}
