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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author Rodrigo Maues
 * @version 1.0
 */
public class OptionsList extends ActionBarActivity {
	ActionBar actionBar;
	public FrameLayout doneButton;
	public FrameLayout cancelButton;
	Bundle option;
	public static int role;
	int position;
	int color;

	/**
	 * Actions to perform when the Activity is created.
	 * Default method used to set up all the necessary variables.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Inflates the layout of the activity
		setContentView(R.layout.options_list);

		//
		role = getIntent().getIntExtra("role", 0);
		option = getIntent().getBundleExtra("option");
		position = getIntent().getIntExtra("position",0);
		//

		// BEGIN_INCLUDE (inflate_set_custom_view)
		// Inflate a "Done/Cancel" custom action bar view.
		actionBar = getSupportActionBar();

		final LayoutInflater inflater = (LayoutInflater) actionBar.getThemedContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(
				R.layout.actionbar_custom_view_done_cancel, null);

		doneButton = (FrameLayout) customActionBarView.findViewById(R.id.actionbar_done);
		doneButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						// "Done"
						Intent data = new Intent();
						data.putExtra("option", option);
						data.putExtra("position", position);
						setResult(RESULT_OK, data);
						finish();
					}
				});
		doneButton.setVisibility(View.INVISIBLE);

		cancelButton = (FrameLayout) customActionBarView.findViewById(R.id.actionbar_cancel);
		cancelButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						// "Cancel"
						setResult(RESULT_CANCELED);
						finish();
					}
				});		

		// Show the custom action bar view and hide the normal Home icon and title.
		actionBar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView,
				new ActionBar.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
		// END_INCLUDE (inflate_set_custom_view)


		color = getResources().getColor(R.color.red);

		switch (role)
		{
		case 0:
			break;
		case 1:
			color = getResources().getColor(R.color.yellow);
			break;
		case 2:
			color = getResources().getColor(R.color.green);
			break;
		}

		actionBar.setBackgroundDrawable(new ColorDrawable(color));

		// get an instance of FragmentTransaction from your Activity
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		//add a fragment
		Fragment optionsFragment = new OptionsFragment();
		fragmentTransaction.add(R.id.container, optionsFragment);
		//fragmentTransaction.addToBackStack("options");
		fragmentTransaction.commit();
	}

	/////////

	public static class OptionsFragment extends ListFragment {
		//int mCurCheckPosition = 0;
		private OptionsListAdapter mAdapter;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			//int role = ((OptionsList) getActivity()).role;

			// Populate list according to the role (event/conditions/actions)
			ArrayList<Bundle> items = new ArrayList<Bundle>();
			Bundle item = new Bundle();

			switch (role)
			{
			case 0:
			case 1:
				item.putInt("type", KeepDoingItApplication.CHARGING);
				item.putString("title", "Battery");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				item.putInt("type", KeepDoingItApplication.HEADSET);
				item.putString("title", "Headset");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				item.putInt("type", KeepDoingItApplication.LOCATION);
				item.putString("title", "Location");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				item.putInt("type", KeepDoingItApplication.SCREEN);
				item.putString("title", "Screen and Keyguard");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				item.putInt("type", KeepDoingItApplication.TIME);
				item.putString("title", "Time");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				break;
			case 2:
				item.putInt("type", KeepDoingItApplication.APPLICATION);
				item.putString("title", "Application");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				item.putInt("type", KeepDoingItApplication.BLUETOOTH);
				item.putString("title", "Bluetooth");
				item.putString("value", "...");
				items.add((Bundle) item.clone());
				break;
			}

			// Common options
			item.putInt("type", KeepDoingItApplication.OUTGOINGCALL);
			item.putString("title", "Telephone");
			item.putString("value", "...");
			items.add((Bundle) item.clone());
			item.putInt("type", KeepDoingItApplication.RINGER);
			item.putString("title", "Ringer");
			item.putString("value", "...");
			items.add((Bundle) item.clone());
			item.putInt("type", KeepDoingItApplication.WIFI);
			item.putString("title", "WiFi");
			item.putString("value", "...");
			items.add((Bundle) item.clone());

			////

			mAdapter = new OptionsListAdapter(this.getActivity(), R.layout.optionslist_row, items, role);
			//Associates the adapter to the list
			setListAdapter(mAdapter);

			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			getListView().setVerticalScrollBarEnabled(false);
			//Improves scrolling performance
			getListView().setCacheColorHint(Color.TRANSPARENT);
			getListView().setBackgroundColor(Color.WHITE);

			int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					1, getActivity().getResources().getDisplayMetrics());

			getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.gray_transparent)));
			getListView().setDividerHeight(padding);


			/*
			if (savedInstanceState != null) {
				// Restore last state for checked position.
				//mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
			}*/

			// Make sure our UI is in the correct state.

			int type = ((OptionsList) getActivity()).option.getInt("type", 0);

			if (type > 0) {
				for (int i=0;i<mAdapter.getCount();i++){
					if(type == mAdapter.getType(i)) {
						showDetails(i);
						break;
					}
				}
			}
		}

		/*
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}*/

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			showDetails(position);
		}

		/**
		 * Helper function to show the details of a selected item
		 */
		void showDetails(int position) {
			//mCurCheckPosition = position;

			Bundle data = new Bundle();
			data.putInt("index", position);
			data.putInt("type", mAdapter.getType(position));

			// Create fragment
			Fragment optionSettingsFragment = new DetailsFragment();
			optionSettingsFragment.setArguments(data);

			// Create new transaction
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.add(R.id.container, optionSettingsFragment);
			transaction.addToBackStack("details");
			// Commit the transaction
			transaction.commit();

			//
			((OptionsList) getActivity()).doneButton.setVisibility(View.VISIBLE);
		}
	}



	public static class DetailsFragment extends Fragment 
	{
		private ImageView icon;
		private TextView text;
		private TextView title;
		private Button backButton;
		private RadioGroup radioGroup;
		private RadioButton radioButton1, radioButton2, radioButton3, radioButton4;
		private GoogleMap googleMap;
		ArrayList <String> names = new ArrayList<String>();
		ArrayList <String> phoneNumbers = new ArrayList<String>();
		ArrayList<String> packageNames = new ArrayList<String>();		
		MapView map;

		@Override
		public void onResume() {
			super.onResume();

			if (map != null)
			{
				map.onResume();
				try {
					setUpMapIfNeeded();
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onPause() {
			super.onPause();

			if (map != null)
			{
				map.onPause();
			}
		}

		@Override
		public void onDestroy() {
			super.onDestroy();

			if (map != null)
			{
				map.onDestroy();
			}
		}

		@Override
		public void onLowMemory() {
			super.onLowMemory();

			if (map != null)
			{
				map.onLowMemory();
			}
		}

		private void setUpMapIfNeeded() throws GooglePlayServicesNotAvailableException {
			// Do a null check to confirm that we have not already instantiated the map.
			if (googleMap == null) {
				googleMap = map.getMap();
				// Check if we were successful in obtaining the map.
				if (googleMap != null) {
					// The Map is verified. It is now safe to manipulate the map.
					googleMap.setMyLocationEnabled(true);
					googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
						public void onCameraChange(CameraPosition position) {
							//String locationCoordinates = String.format("%f,%f", 
							//		googleMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude, 
							//		googleMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude);

							MarkerOptions markerOptions = new MarkerOptions();
							markerOptions.position(position.target);
							markerOptions.title("Location");

							googleMap.clear();
							//googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
							googleMap.addMarker(markerOptions);

							String locationCoordinates = String.format("%f;%f", 
									position.target.longitude, 
									position.target.latitude);

							((OptionsList) getActivity()).option.putString("value", "Location");
							((OptionsList) getActivity()).option.putString("extra_value", locationCoordinates);

							String description = "";

							switch (role)
							{
							case 0:
								description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
								break;
							case 1:
								description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
								break;
							case 2:
								description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
								break;
							}

							text.setText(Html.fromHtml(description));
						}
					});
					/*
					googleMap.setOnMapClickListener(new OnMapClickListener()
					{
						public void onMapClick(LatLng latLng)
						{
							// Creating a marker
							MarkerOptions markerOptions = new MarkerOptions();
							markerOptions.position(latLng);
							markerOptions.title(latLng.latitude + " : " + latLng.longitude);

							googleMap.clear();
							//googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
							googleMap.addMarker(markerOptions);			
						}
					});
					 */

					String locationCoordinates = ((OptionsList) getActivity()).option.getString("extra_value");
					if (locationCoordinates!= null)
					{
						//String[] individualLocationCoordinates = locationCoordinates.split("\\;");
						//LatLng latLng = new LatLng(Float.valueOf(individualLocationCoordinates[1].replace("", ".")), Float.valueOf(individualLocationCoordinates[0].replace("", ".")));	

						int separatorIndex = locationCoordinates.indexOf(";");
						LatLng latLng = new LatLng(
								Float.valueOf(locationCoordinates.substring(separatorIndex+1).replace(",", ".")), 
								Float.valueOf(locationCoordinates.substring(0, separatorIndex).replace(",", ".")));

						MapsInitializer.initialize(this.getActivity());
						googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
					}
				}
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View settingsLayout;
			String value;
			int type = getArguments().getInt("type");

			((OptionsList) getActivity()).option.putInt("type", type);

			Button.OnClickListener back = new Button.OnClickListener()
			{
				public void onClick(View v) {
					((OptionsList) getActivity()).option.clear();
					((OptionsList) getActivity()).doneButton.setVisibility(View.INVISIBLE);
					getFragmentManager().popBackStackImmediate();
				}
			};

			switch (type)
			{
			case KeepDoingItApplication.APPLICATION:
			{
				settingsLayout = inflater.inflate(R.layout.option_app_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Telephone");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					break;
				case 1:
					backButton.setText("Switch condition");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				//

				PackageManager pm = getActivity().getPackageManager();
				final Intent filter = new Intent(Intent.ACTION_MAIN, null);
				filter.addCategory(Intent.CATEGORY_LAUNCHER);

				//Adds all the application packages
				for (ResolveInfo info : pm.queryIntentActivities(filter, 0)) {
					String packageName = info.activityInfo.packageName;

					//Avoid unnecessary packages
					if (!packageName.equalsIgnoreCase("com.android.launcher") 
							&& !packageName.equalsIgnoreCase(getActivity().getPackageName()) 
							&& !packageName.equalsIgnoreCase("com.android.settings"))
					{						
						packageNames.add(packageName);
						String appName = packageName;
						try {
							appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 
									PackageManager.GET_META_DATA)).toString();
						} catch (NameNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						names.add(appName);
					}
				}

				//List<PackageInfo> packs = getActivity().getPackageManager().getInstalledPackages(0);

				ListView appsList = (ListView) settingsLayout.findViewById(R.id.list);
				appsList.setVerticalScrollBarEnabled(false);
				appsList.setCacheColorHint(Color.TRANSPARENT); // Improves scrolling performance
				appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				appsList.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						((OptionsList) getActivity()).option.putString("value", names.get(position));
						((OptionsList) getActivity()).option.putString("extra_value", packageNames.get(position));

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
						android.R.layout.simple_list_item_single_choice, names);
				appsList.setAdapter(adapter);

				//

				int position = -1;
				String packageName = ((OptionsList) getActivity()).option.getString("extra_value");
				position = packageNames.indexOf(packageName);

				if (position==-1)
				{
					position = 0;
				}

				appsList.setSelection(position);
				appsList.setItemChecked(position, true);

				((OptionsList) getActivity()).option.putString("value", names.get(position));
				((OptionsList) getActivity()).option.putString("extra_value", packageNames.get(position));	

				//

				String description = "";

				switch (role)
				{
				case 0:
					description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
					break;
				case 1:
					description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
					break;
				case 2:
					description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
					break;
				}

				text.setText(Html.fromHtml(description));

				return settingsLayout;
			}
			case KeepDoingItApplication.BLUETOOTH:
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Bluetooth");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);
				radioButton3.setVisibility(View.GONE);
				radioButton4.setVisibility(View.GONE);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					break;
				case 1:
					backButton.setText("Switch condition");
					break;
				case 2:
					backButton.setText("Switch action");
					radioButton1.setText("On");
					radioButton2.setText("Off");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "on");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "off");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (value.equalsIgnoreCase("on")) {
						radioGroup.check(R.id.radio_1);
					} else {
						radioGroup.check(R.id.radio_2);
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			case KeepDoingItApplication.CHARGING:
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Battery");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);
				radioButton3.setVisibility(View.GONE);
				radioButton4.setVisibility(View.GONE);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					radioButton1.setText("Starts charging");
					radioButton2.setText("Stops charging");
					break;
				case 1:
					backButton.setText("Switch condition");
					radioButton1.setText("Charging");
					radioButton2.setText("Not charging");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "true");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "false");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (Boolean.valueOf(value)) {
						radioGroup.check(R.id.radio_1);
					} else {
						radioGroup.check(R.id.radio_2);
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			case KeepDoingItApplication.HEADSET:
			{
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Headset");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);
				radioButton3.setVisibility(View.GONE);
				radioButton4.setVisibility(View.GONE);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					radioButton1.setText("Connected");
					radioButton2.setText("Disconnected");
					break;
				case 1:
					backButton.setText("Switch condition");
					radioButton1.setText("Connected");
					radioButton2.setText("Not connected");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "true");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "false");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (Boolean.valueOf(value)) {
						radioGroup.check(R.id.radio_1);
					} else {
						radioGroup.check(R.id.radio_2);
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			}
			case KeepDoingItApplication.LOCATION:
			{
				settingsLayout = inflater.inflate(R.layout.option_location_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Location");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					break;
				case 1:
					backButton.setText("Switch condition");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				//SupportMapFragment supportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
				// Getting a reference to the map
				//googleMap = supportMapFragment.getMap();

				map = (MapView) settingsLayout.findViewById(R.id.map);
				map.onCreate(savedInstanceState);
				try {
					setUpMapIfNeeded();
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//

				return settingsLayout;
			}
			case KeepDoingItApplication.OUTGOINGCALL:
			{
				settingsLayout = inflater.inflate(R.layout.option_outgoingcall_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Telephone");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				boolean showList = true;

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					break;
				case 1:
					backButton.setText("Switch condition");
					showList = false;
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				//

				if (showList)
				{

					ArrayList <String> contacts = new ArrayList<String>();
					String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
					ContentResolver resolver= getActivity().getContentResolver();
					Cursor contactsCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, sortOrder);

					while (contactsCursor.moveToNext())
					{
						String name=contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						names.add(name);
						phoneNumbers.add(phoneNumber);
						contacts.add(name + "\n(" + phoneNumber + ")");
					}
					contactsCursor.close();// close cursor

					ListView contactsList = (ListView) settingsLayout.findViewById(R.id.list);
					contactsList.setVerticalScrollBarEnabled(false);
					contactsList.setCacheColorHint(Color.TRANSPARENT); // Improves scrolling performance
					contactsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
					contactsList.setOnItemClickListener(new OnItemClickListener()
					{
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							((OptionsList) getActivity()).option.putString("value", names.get(position));
							((OptionsList) getActivity()).option.putString("extra_value", phoneNumbers.get(position));

							String description = "";

							switch (role)
							{
							case 0:
								description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
								break;
							case 1:
								description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
								break;
							case 2:
								description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
								break;
							}

							text.setText(Html.fromHtml(description));
						}
					});

					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), 
							android.R.layout.simple_list_item_single_choice, contacts);
					contactsList.setAdapter(adapter);

					//

					if (!contacts.isEmpty())
					{
						int position = -1;
						String phoneNumber = ((OptionsList) getActivity()).option.getString("extra_value");

						if (phoneNumber!=null&&phoneNumber!="") {
							Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
							Cursor lookupCursor = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, sortOrder);

							if (lookupCursor!=null&&lookupCursor.moveToFirst()) {
								String name = lookupCursor.getString(lookupCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
								if (name!=null&&name.equalsIgnoreCase(((OptionsList) getActivity()).option.getString("value"))) {
									position = names.indexOf(name);
								}
							}

							lookupCursor.close();
						}

						if (position==-1)
						{
							position = 0;
						}

						contactsList.setSelection(position);
						contactsList.setItemChecked(position, true);

						((OptionsList) getActivity()).option.putString("value", names.get(position));
						((OptionsList) getActivity()).option.putString("extra_value", phoneNumbers.get(position));	
					}

					//

				}
				//

				String description = "";

				switch (role)
				{
				case 0:
					description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
					break;
				case 1:
					description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
					break;
				case 2:
					description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
					break;
				}

				text.setText(Html.fromHtml(description));

				return settingsLayout;
			}
			case KeepDoingItApplication.RINGER:
			{
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Ringer");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);
				radioButton4.setVisibility(View.GONE);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					radioButton1.setText("Silent");
					radioButton2.setText("Vibrate");
					radioButton3.setText("Normal");
					break;
				case 1:
					backButton.setText("Switch condition");
					radioButton1.setText("Silent");
					radioButton2.setText("Vibrate");
					radioButton3.setText("Normal");
					break;
				case 2:
					backButton.setText("Switch action");
					radioButton1.setText("Silent");
					radioButton2.setText("Vibrate");
					radioButton3.setText("Normal");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "silent");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "vibrate");
							break;
						case R.id.radio_3:
							((OptionsList) getActivity()).option.putString("value", "normal");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (value.equalsIgnoreCase("silent"))
					{
						radioGroup.check(R.id.radio_1);
					}
					else
					{
						if (value.equalsIgnoreCase("vibrate"))
						{
							radioGroup.check(R.id.radio_2);
						}
						else
						{
							radioGroup.check(R.id.radio_3);
						}
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			}
			case KeepDoingItApplication.SCREEN:
			{
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Screen and Keyguard");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					radioButton1.setText("Lock");
					radioButton2.setText("Unlock");
					radioButton3.setText("On");
					radioButton4.setText("Off");
					break;
				case 1:
					backButton.setText("Switch condition");
					radioButton1.setText("Locked");
					radioButton2.setText("Unlocked");
					radioButton3.setText("On");
					radioButton4.setText("Off");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "lock");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "unlock");
							break;
						case R.id.radio_3:
							((OptionsList) getActivity()).option.putString("value", "on");
							break;
						case R.id.radio_4:
							((OptionsList) getActivity()).option.putString("value", "off");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (value.equalsIgnoreCase("lock"))
					{
						radioGroup.check(R.id.radio_1);
					}
					else
					{
						if (value.equalsIgnoreCase("unlock"))
						{
							radioGroup.check(R.id.radio_2);
						}
						else
						{
							if (value.equalsIgnoreCase("on"))
							{
								radioGroup.check(R.id.radio_3);
							}
							else
							{
								radioGroup.check(R.id.radio_4);
							}
						}
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			}
			case KeepDoingItApplication.TIME:
			{
				settingsLayout = inflater.inflate(R.layout.option_time_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("Time");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);
				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					break;
				case 1:
					backButton.setText("Switch condition");
					break;
				case 2:
					backButton.setText("Switch action");
					break;
				}

				TimePicker timePicker = (TimePicker) settingsLayout.findViewById(R.id.time_picker);
				timePicker.setOnTimeChangedListener(new OnTimeChangedListener()
				{
					public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
					{
						SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.getDefault());

						Calendar c = Calendar.getInstance();
						c.set(Calendar.HOUR_OF_DAY, hourOfDay);
						c.set(Calendar.MINUTE, minute);
						c.set(Calendar.SECOND, 0);

						((OptionsList) getActivity()).option.putString("value", timeFormat.format(c.getTime()));
						((OptionsList) getActivity()).option.putString("extra_value", String.valueOf(c.getTimeInMillis()));

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				Calendar c = Calendar.getInstance();
				value = ((OptionsList) getActivity()).option.getString("extra_value");

				if (value!=null)
				{
					c.setTimeInMillis(Long.valueOf(value));
				}

				timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
				timePicker.setCurrentMinute(c.get(Calendar.MINUTE)+1);
				timePicker.setCurrentMinute(timePicker.getCurrentMinute()-1);

				//

				return settingsLayout;
			}
			case KeepDoingItApplication.WIFI:
			{
				settingsLayout = inflater.inflate(R.layout.option_charging_details, container, false);

				title = (TextView) settingsLayout.findViewById(R.id.title);
				title.setTextColor(((OptionsList) getActivity()).color);
				title.setText("WiFi");

				text = (TextView) settingsLayout.findViewById(R.id.text);

				icon = (ImageView) settingsLayout.findViewById(R.id.image);
				icon.setImageDrawable(getResources().getDrawable(((KeepDoingItApplication) 
						this.getActivity().getApplicationContext()).getDrawableID(type)));
				icon.setImageLevel(role);

				backButton = (Button) settingsLayout.findViewById(R.id.back_button);
				backButton.setTextColor(((OptionsList) getActivity()).color);
				backButton.setOnClickListener(back);

				radioButton1 = (RadioButton) settingsLayout.findViewById(R.id.radio_1);
				radioButton2 = (RadioButton) settingsLayout.findViewById(R.id.radio_2);
				radioButton3 = (RadioButton) settingsLayout.findViewById(R.id.radio_3);
				radioButton4 = (RadioButton) settingsLayout.findViewById(R.id.radio_4);
				radioButton3.setVisibility(View.GONE);
				radioButton4.setVisibility(View.GONE);

				switch (role)
				{
				case 0:
					backButton.setText("Switch event");
					radioButton1.setText("On");
					radioButton2.setText("Off");
					break;
				case 1:
					backButton.setText("Switch condition");
					radioButton1.setText("On");
					radioButton2.setText("Off");
					break;
				case 2:
					backButton.setText("Switch action");
					radioButton1.setText("On");
					radioButton2.setText("Off");
					break;
				}

				radioGroup = (RadioGroup) settingsLayout.findViewById(R.id.radio_group);
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
				{
					public void onCheckedChanged(RadioGroup group, int checkedId)
					{
						switch (checkedId)
						{
						case R.id.radio_1:
							((OptionsList) getActivity()).option.putString("value", "on");
							break;
						case R.id.radio_2:
							((OptionsList) getActivity()).option.putString("value", "off");
							break;
						}

						String description = "";

						switch (role)
						{
						case 0:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateEventDescription(((OptionsList) getActivity()).option);
							break;
						case 1:
							description = "if "+((KeepDoingItApplication) getActivity().getApplicationContext()).generateConditionDescription(((OptionsList) getActivity()).option);
							break;
						case 2:
							description = ((KeepDoingItApplication) getActivity().getApplicationContext()).generateActionDescription(((OptionsList) getActivity()).option);
							break;
						}

						text.setText(Html.fromHtml(description));
					}
				});

				//

				value = ((OptionsList) getActivity()).option.getString("value");

				if (value!=null)
				{
					if (value.equalsIgnoreCase("on")) {
						radioGroup.check(R.id.radio_1);
					} else {
						radioGroup.check(R.id.radio_2);
					}
				}
				else
				{
					radioGroup.check(R.id.radio_1);
				}

				//

				((OptionsList) getActivity()).option.putString("extra_value", "");

				return settingsLayout;
			}
			default:
				return null;
			}
		}
	}

	protected void onResume() {
		super.onResume();

		NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doneButton.setVisibility(View.INVISIBLE);
			option.clear();
			//return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}

