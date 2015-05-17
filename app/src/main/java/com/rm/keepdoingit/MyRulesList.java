package com.rm.keepdoingit;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MyRulesList extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private SharedPreferences mPreferences;
	MyRulesListAdapter mAdapter;
	ActionBar actionBar;
	private LinearLayout mWarningLayout;
	private ImageButton mWarningButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myrules_list);

		mPreferences = getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);

		//precisa? sim, caso ele force parar o app
		startService(new Intent(getApplicationContext(), KeepDoingItService.class));

		actionBar = getSupportActionBar();
		//actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_solid_blue));
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		actionBar.setTitle(Html.fromHtml("<font color='#ffffff'>My rules</font>"));
		//actionBar.setDisplayHomeAsUpEnabled(true);

		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
		int padding_in_dp = 8;
		final float scale = getResources().getDisplayMetrics().density;
		int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

		TextView listHeader = new TextView(this);
		listHeader.setLayoutParams(layoutParams);
		listHeader.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
		listHeader.setText("99 automation rules activated and 1 deactivated");
		listHeader.setTextColor(Color.parseColor("#999999"));
		listHeader.setGravity(Gravity.CENTER);
		listHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

		//
		ListView lv = (ListView) this.findViewById(R.id.list);
		//lv.addHeaderView(listHeader);
		lv.setVerticalScrollBarEnabled(false);
		lv.setCacheColorHint(Color.TRANSPARENT); // Improves scrolling performance
		lv.setEmptyView((LinearLayout) findViewById(R.id.empty_view));
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Intent intent = new Intent(getApplicationContext(), RuleDetails.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle rule = mAdapter.getRule(position);
				intent.putExtra("rule", rule);
				intent.putExtra("new", false);
				intent.putExtra("id", mAdapter.getRuleId(position));
				startActivity(intent);
				//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		mAdapter = new MyRulesListAdapter(this, R.layout.myruleslist_row, null, new String[]{ }, new int[]{ }, 0);
		lv.setAdapter(mAdapter);

		getSupportLoaderManager().initLoader(0, null, this);
		
		//
		
		mWarningLayout = (LinearLayout) findViewById(R.id.warning);
		mWarningLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				Intent intent = new Intent(MyRulesList.this, Questionnaire.class);
				startActivity(intent);
				
				mWarningLayout.setVisibility(View.GONE);
			}
		});
		
		mWarningButton = (ImageButton) findViewById(R.id.warning_cancel);
		mWarningButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v) {
				mWarningLayout.setVisibility(View.GONE);
				
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putLong("lastTime", 0);
				editor.commit();
			}
		});
	}


	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String mSortOrder = KeepDoingItProvider.Rules._ID + " DESC";
		CursorLoader loader = new CursorLoader(this.getApplicationContext(), KeepDoingItProvider.Rules.CONTENT_URI, null, null, null, mSortOrder);

		return loader;
	}


	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		mAdapter.swapCursor(data);	
	}


	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.myrules_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.menu_new:
			//((KeepDoingItApplication) getApplicationContext()).generateRecommendations(System.currentTimeMillis()-preferences.getLong("time", 60000), System.currentTimeMillis());
			//
			intent = new Intent(MyRulesList.this, RecommendationsList.class);
			startActivity(intent);
			//this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			return true;
		case R.id.menu_settings:
			//open preferences/settings
			intent = new Intent(MyRulesList.this, Settings.class);
			startActivity(intent);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}		
	}


	protected void onResume() {
		super.onResume();

		NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(1);

		if (mPreferences.getLong("lastTime", 0) != 0)
		{
			mWarningLayout.setVisibility(View.VISIBLE);
		}
	}

}