package com.rm.keepdoingit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Settings extends ActionBarActivity {
	private SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>Settings</font>"));
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setDisplayShowHomeEnabled(true);

		
		LinearLayout questionnaireButton = (LinearLayout) findViewById(R.id.questionnaire_button);
		questionnaireButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(Settings.this, Questionnaire.class);
				startActivity(intent);
			}
		});
		

		LinearLayout uploadButton = (LinearLayout) findViewById(R.id.upload_button);
		uploadButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				if (((KeepDoingItApplication) getApplicationContext()).isConnected()) {
					((KeepDoingItApplication) getApplicationContext()).uploadLogFiles();
					
					Toast.makeText(getApplicationContext(), "Thank you for sending your answers and log files! :)", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Ops! Try again when your phone is connected to the Internet.", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}
	
}

