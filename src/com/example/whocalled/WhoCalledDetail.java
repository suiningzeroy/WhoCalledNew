package com.example.whocalled;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class WhoCalledDetail extends Activity {
	
	private WhoCalledApp app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_detail);
		
		app =(WhoCalledApp) getApplication();
		
		Intent request = getIntent();
		if (request != null ){
			ImageView image = (ImageView) findViewById(R.id.callerImage);
			long id = request.getLongExtra("id", 0);
			Bitmap bitmap = app.getImageCache().get(id);
			if (bitmap != null){
				image.setImageBitmap(bitmap);
			}else{
				image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ddicon));
			}
			String name = request.getStringExtra("username");
			String number = request.getStringExtra("number");
			String nameOrNumber = name != null ? name+"("+number+")" : number;
			((TextView) findViewById(R.id.callerName)).setText(nameOrNumber);
			((TextView) findViewById(R.id.callerInfo)).setText( getCallerInfor(nameOrNumber, request));
		}
		
		findViewById(R.id.ok).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
	}
	
	private String getCallerInfor(String nameOrNumber, Intent request){
		String countsInfo = "There are " + String.valueOf(request.getLongExtra("callcounts", 0)) + 
				" call logs for " + nameOrNumber +  " ;\n"+"\n" +"Total " + 
				String.valueOf(request.getLongExtra("duration", 0)) + " seconds;\n"+"\n";
		String aveInfo = "About " + String.valueOf(request.getLongExtra("duration", 0)/request.getLongExtra("callcounts", 999))
				+ " seconds for each log .";
		return countsInfo + aveInfo;		
	}


}
