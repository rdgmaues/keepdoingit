/*
 * PUC-Rio, Informatics Department
 * Final Programming Project
 * 
 * Semester: 2013.1
 * Author: Rodrigo Maues
 * Project: Keep Doing It
 * Version: 1.0
 */

package com.rm.keepdoingit;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity and GUI component of the application. Displays the list of rule recommendations 
 * and shows in the bottom bar the interactions considered in the recommendations.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class RecommendationsList extends ActionBarActivity {
	//Constant with the minimum and default value of the time window
	public static final int FIFTEEN_SECONDS = 15000;

	//View that references the list of rule recommendations
	private ListView mList;
	//Adapter that connects the data about the recommendations to the view
	private RecommendationsListAdapter mAdapter;
	//view that references the filters bar
	private LinearLayout filtersBar;
	//
	//private AlertDialog mTimeWindowDialog;
	private SharedPreferences mPreferences;

	ActionBar actionBar;
	private OnNavigationListener mOnNavigationListener;
	SpinnerAdapter mSpinnerAdapter;

	/**
	 * Actions to perform when the Activity is created.
	 * Default method used to set up all the necessary variables.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Inflates the layout of the activity
		setContentView(R.layout.recommendations_list);

		//Get the saved application preferences.
		//These preferences persist even when the application is closed
		mPreferences = getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);

		//
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt("session", mPreferences.getInt("session", 0)+1);
		editor.commit();
		
		//
		mSpinnerAdapter = ArrayAdapter.
				createFromResource(this, R.array.time_window_array,
						android.R.layout.simple_spinner_item);

		mOnNavigationListener = new OnNavigationListener() {
			public boolean onNavigationItemSelected(int position, long itemId) {
				//Updates the preferences with the new time window index
				//and the correspondent time window in milliseconds
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putInt("timeWindowIndex", position);
				editor.putLong("timeWindowMillis", FIFTEEN_SECONDS*(position+1));
				editor.commit();

				//Refreshs the list of recommendations using the new time window
				((KeepDoingItApplication) getApplicationContext()).
				generateRecommendations(System.currentTimeMillis() 
						- FIFTEEN_SECONDS*(position+1), System.currentTimeMillis());
				mAdapter.notifyDataSetChanged();
				refreshFiltersBar();
				logGenerateAction();

				return true;
			}
		};

		//
		actionBar = getSupportActionBar();
		//actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_blue));
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
		actionBar.setSelectedNavigationItem(mPreferences.getInt("timeWindowIndex", 0));
		actionBar.setDisplayHomeAsUpEnabled(true);

		//To be sure that the logger service is already running, ask it to start again
		//whenever the app (Gui component) is launched
		//startService(new Intent(getApplicationContext(), KeepDoingItService.class));

		//Creates the list
		mList = (ListView) findViewById(R.id.results);
		mList.setVerticalScrollBarEnabled(false);
		//Improves scrolling performance
		mList.setCacheColorHint(Color.TRANSPARENT);
		//Sets the view that will be displayed when the list is empty
		mList.setEmptyView((LinearLayout) findViewById(R.id.empty_view));
		mList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Intent intent = new Intent(getApplicationContext(), RuleDetails.class);

				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle rule = mAdapter.getRule(position);
				intent.putExtra("rule", rule);
				
				//
				Bundle action = new Bundle();
				action.putInt("actionType", KeepDoingItApplication.ACTION_SELECT);
				action.putInt("rulePosition", position+1);
				action.putBundle("rule", rule);
				//action.putString("ruleDescription", ((TextView) view.findViewById(R.id.rule_text)).getText().toString());
				((KeepDoingItApplication) getApplicationContext()).writeLog(action);
				//

				startActivity(intent);
				//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});


		//Creates a new adapter connected to the data of the past and future rule recommendations
		mAdapter = new RecommendationsListAdapter(this, R.layout.recommendationslist_row, 
				((KeepDoingItApplication) getApplicationContext()).rules);
		//Associates the adapter to the list
		mList.setAdapter(mAdapter);

		filtersBar = (LinearLayout) findViewById(R.id.interactions_buttonbar);
		//Call the method to populate the bottom bar 
		refreshFiltersBar();
	}

	protected void logGenerateAction() {
		//String[] timeWindows = getResources().getStringArray(R.array.time_window_array);
		
		Bundle action = new Bundle();
		action.putInt("actionType", KeepDoingItApplication.ACTION_GENERATE);
		//action.putString("timeWindow", timeWindows[actionBar.getSelectedNavigationIndex()]);
		action.putString("timeWindow", String.valueOf((int)FIFTEEN_SECONDS/1000*(actionBar.getSelectedNavigationIndex()+1)));
		((KeepDoingItApplication) getApplicationContext()).writeLog(action);

		//
		if (((KeepDoingItApplication) getApplicationContext()).rules.size()>0) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putLong("lastTime", System.currentTimeMillis());
			editor.commit();
			
			//Toast.makeText(getApplicationContext(), String.valueOf(System.currentTimeMillis()), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method that populates and refreshes the bottom bar, which displays the
	 * interactions performed by the user.
	 */
	public void refreshFiltersBar() {
		//Clears the filters bar 
		filtersBar.removeAllViews();
		//Gets the interactions/filters related to the displayed rule recommendations
		ArrayList<Bundle> interactions = ((KeepDoingItApplication) getApplicationContext()).filters;

		//Populates the filters bar
		for (int i=0; i<interactions.size(); i++) {
			Bundle interaction = interactions.get(i);
			Drawable drawable = null;
			//String text = "";
			String value = interaction.getString("value");
			final int type = interaction.getInt("type");

			switch (type) {
			case KeepDoingItApplication.APPLICATION:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_app);
				break;
			case KeepDoingItApplication.BLUETOOTH:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_bluetooth);
				break;
			case KeepDoingItApplication.CHARGING:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_charging);
				if (Boolean.valueOf(value)) {
					value = "charging";
				} else {
					value = "discharging";
				}
				break;
			case KeepDoingItApplication.HEADSET:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_headset);
				if (Boolean.valueOf(value)) {
					value = "connected";
				} else {
					value = "disconnected";
				}
				break;
			case KeepDoingItApplication.OUTGOINGCALL:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_call);
				break;
			case KeepDoingItApplication.RINGER:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_ringer);
				break;
			case KeepDoingItApplication.SCREEN:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_screen);
				break;
			case KeepDoingItApplication.WIFI:
				drawable = this.getResources().getDrawable(R.drawable.btn_rule_wifi);
				break;
			default:
				break;
			}

			//Creates a new interaction button that is used to filter the recommendations
			Button interactionButton = (Button) this.getLayoutInflater().inflate(R.layout.bottombar_button, null);
			interactionButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			interactionButton.setText(value);
			interactionButton.setSelected(true);

			OnClickListener filterResults = new OnClickListener() {
				public void onClick(View v){
					if (v.isSelected())
					{
						//Toggle off
						v.setSelected(false);

					}
					else
					{
						//Toggle on
						v.setSelected(true);
					}

					int index = filtersBar.indexOfChild(v);
					((KeepDoingItApplication) getApplicationContext()).toggleFilter(index);
					//Calls the method to generate the recommendations with the last start and end times
					((KeepDoingItApplication) getApplicationContext()).generateRecommendations();
					//Notifies the adapter to refresh the list
					mAdapter.notifyDataSetChanged();

					Bundle action = new Bundle();
					action.putInt("actionType", KeepDoingItApplication.ACTION_REGENERATE);
					action.putInt("index", index);
					((KeepDoingItApplication) getApplicationContext()).writeLog(action);
				}
			};
			interactionButton.setOnClickListener(filterResults);

			//Adds the create interaction button to the filters bar
			filtersBar.addView(interactionButton);
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.recommendationslist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	/**
	 * Default Android method that is called to handle when a button in the action bar is pressed.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//Handles when the button to refresh the list is pressed
		case R.id.menu_refresh:
			//Calls the method to generate the recommendations with the current time
			((KeepDoingItApplication) getApplicationContext()).
			generateRecommendations(System.currentTimeMillis() 
					- mPreferences.getLong("timeWindowMillis", FIFTEEN_SECONDS), System.currentTimeMillis());
			//Notifies the adapter to refresh the list
			mAdapter.notifyDataSetChanged();
			//Calls the method to refresh the bottom bar
			this.refreshFiltersBar();
			logGenerateAction();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	protected void onPause() {
		super.onPause();
		//Bundle action = new Bundle();
		//action.putInt("type", KeepDoingItApplication.ACTION_GO_BACKGROUND);
		//((KeepDoingItApplication) getApplicationContext()).writeLog(action);
	}


	protected void onResume() {
		super.onResume();
		
		NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
	    nm.cancel(1);
		//Bundle action = new Bundle();
		//action.putInt("type", KeepDoingItApplication.ACTION_GO_FOREGROUND);
		//((KeepDoingItApplication) getApplicationContext()).writeLog(action);
	}
	
	
	protected void onStop() {
		super.onStop();
	}


	protected void onStart() {
		super.onStart();
	}
	

}

