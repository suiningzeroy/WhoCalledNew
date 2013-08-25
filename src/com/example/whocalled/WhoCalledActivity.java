package com.example.whocalled;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WhoCalledActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_called);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.who_called, menu);
        return true;
    }
    
}
