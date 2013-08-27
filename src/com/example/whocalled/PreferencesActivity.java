package com.example.whocalled;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class PreferencesActivity extends Activity {
	CheckBox autoUpdate;
	Spinner updateFreqSpinner;
	Spinner magnitudeSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		updateFreqSpinner = (Spinner)findViewById(R.id.spinner_update_freq);
		magnitudeSpinner = (Spinner)findViewById(R.id.spinner_quake_mag);
		autoUpdate = (CheckBox)findViewById(R.id.checkbox_auto_update);
		populateSpinners();
	}
	
	private void populateSpinners() {
		ArrayAdapter<CharSequence> fAdapter;
		fAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options,
		android.R.layout.simple_spinner_item);
		int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
		fAdapter.setDropDownViewResource(spinner_dd_item);
		updateFreqSpinner.setAdapter(fAdapter);
		// Populate the minimum magnitude spinner
		ArrayAdapter<CharSequence> mAdapter;
		mAdapter = ArrayAdapter.createFromResource(this,R.array.magnitude_options,
		android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(spinner_dd_item);
		magnitudeSpinner.setAdapter(mAdapter);
	}
}


