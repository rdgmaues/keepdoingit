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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Base class that implements global functions and declare global variables to be available
 * throughout the entire application, from any component (activity, service, etc.).
 * Implements the function for generating the rule recommendations.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class KeepDoingItApplication extends Application {

	//private int atualizationTries = 0;
	//private int atualizationPeriods[] = {1, 3, 6, 15, 30, 60, 120, 240, 480, 960};

	//private SyncTask mTask;

	//private static int SECOND_IN_MILLIS = 1000;

	public static final int ACTION_GENERATE = 0x1;
	public static final int ACTION_REGENERATE = 0x2;
	public static final int ACTION_SELECT = 0x3;
	public static final int ACTION_EDIT = 0x4;
	public static final int ACTION_SAVE = 0x5;
	public static final int ACTION_GO_BACKGROUND = 0x7;
	public static final int ACTION_GO_FOREGROUND = 0x6;

	//public static final String NEW_RELIC_TOKEN = "AAc7ca0d46a700c78a66e4785b6ac270b3e3253f73";

	private AmazonS3Client s3Client = null;
	protected S3UploadLogTask s3Task;
	private static final String S3_ACCESS_KEY = "AKIAI3P2N5UXUZUO6A2A";
	private static final String S3_SECRET_KEY = "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";

	private static final String keepDoingItBucket = "keepdoingit-logfiles"; 
	private static final String keepDoingItLogsDir = "keepdoingit-logs";

	/*final private Handler mHandler = new Handler();
	Runnable mBeginSync = new Runnable() {

		public void run() {
			try {
				beginSync();
			} catch (Exception e) {
				Log.e(TAG, "exception", e);
			}
		}
	};

	public SyncTask getSyncTask() {
		return mTask;
	}*/

	static String emailID;
	static String deviceID;

	//Constants that define the type of interaction/context
	public static final int APPLICATION = 1;
	public static final int BLUETOOTH = 2;
	public static final int CHARGING = 3;
	public static final int HEADSET = 4;
	public static final int LOCATION = 5;
	public static final int OUTGOINGCALL = 6;
	public static final int RINGER = 7;
	public static final int SCREEN = 8;
	public static final int TIME = 9;
	public static final int WIFI = 10;
	//Constant that define the name of the shared preferences file
	public static final String PREFERENCES_FILE = "configs";
	//Instance of the application
	private static KeepDoingItApplication instance;

	private SharedPreferences mPreferences;

	//Projection used to query the interactions table when generating the recommendations
	static final String[] INTERACTION_PROJECTION = new String[] {
		KeepDoingItProvider.Interactions._ID,
		KeepDoingItProvider.Interactions.TYPE,
		KeepDoingItProvider.Interactions.VALUE,
		KeepDoingItProvider.Interactions.EXTRA_VALUE,
		KeepDoingItProvider.Interactions.TIMESTAMP
	};
	//Sorting used in the interactions table query
	static final String SORT_ORDER = KeepDoingItProvider.Interactions.TIMESTAMP + " DESC";
	//Projection used to query the contexts table when generating the recommendations
	static final String[] CONTEXT_PROJECTION = new String[] {
		KeepDoingItProvider.Contexts.TYPE,
		KeepDoingItProvider.Contexts.VALUE,
		KeepDoingItProvider.Contexts.EXTRA_VALUE
	};

	//The list of generated rule recommendations
	public ArrayList<Bundle> rules = new ArrayList<Bundle>();
	//The list of the interactions related to the rule recommendations
	public ArrayList<Bundle> interactions = new ArrayList<Bundle>();
	//The list of filters to consider when generating the rule recommendations (interactions related to the rule recommendations without duplicates)
	public ArrayList<Bundle> filters = new ArrayList<Bundle>();
	//The list of the 
	//public ArrayList<Bundle> interactionsWithoutDuplicates = new ArrayList<Bundle>();
	//Last start and end times used to generate the recommendations 
	private long startTime;
	private long endTime;

	/**
	 * Gets an instance or reference of the running application.
	 */
	public static KeepDoingItApplication getInstance() {
		return instance;
	}

	/**
	 * Actions to perform when the application is created.
	 * Default method used to set up all the necessary variables.
	 */
	@Override
	public final void onCreate() {
		super.onCreate();
		instance = this;
		startTime = 0;
		endTime = 0;

		emailID = "";
		deviceID = "";		

		Pattern emailPattern=Patterns.EMAIL_ADDRESS;
		Account []accounts=AccountManager.get(this).getAccounts();
		for(Account account:accounts)
		{
			if(emailPattern.matcher(account.name).matches())
			{
				//emailID=emailID+account.name+"\n\t\t\t\t";
				emailID=account.name;
			}
		}

		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		deviceID = mngr.getDeviceId();

		Region region = Region.getRegion(Regions.US_EAST_1);

		AWSCredentials credentials = new BasicAWSCredentials(S3_ACCESS_KEY, S3_SECRET_KEY);
		s3Client = new AmazonS3Client(credentials);
		s3Client.setRegion(region);

		mPreferences = getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);

		//if (isConnected()) {// && !username.equals("")) {
		//uploadLogFiles();
		//}
	}


	public boolean isConnected() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * Generates the automation rule recommendations using the time window
	 * defined by the start and end times informed
	 * @param startTime only the interactions that happened after this time will be retrieved
	 * @param endTime only the interactions that happened before this time will be retrieved
	 */
	public void generateRecommendations(long startTime, long endTime)
	{
		//Clears any filters
		filters.clear();

		//Sets the start and end times
		this.startTime = startTime;
		this.endTime = endTime;

		generateRecommendations();

		//Calls the method to generate a list composed of interactions involved in the generated recommendations 
		generateInteractionsList();
	}


	/**
	 * Generates the automation rule recommendations using the time window
	 * defined by the last start and end times informed
	 */		
	public void generateRecommendations()
	{
		//Clears the last recommendations
		rules.clear();		

		//Queries the database for the interactions within the time window that could be an action
		//These are called the main actions
		String mainSetSelectionClause = "(" + KeepDoingItProvider.Interactions.TYPE + " IN (?,?,?,?,?)) AND "
				+ "(" + KeepDoingItProvider.Interactions.TIMESTAMP + " >= ?) AND "
				+ "(" + KeepDoingItProvider.Interactions.TIMESTAMP + " <= ?)";

		String[] mainSetSelectionArgs = {
				String.valueOf(APPLICATION),
				String.valueOf(RINGER),
				String.valueOf(BLUETOOTH),
				String.valueOf(WIFI),
				String.valueOf(OUTGOINGCALL),
				String.valueOf(startTime),
				String.valueOf(endTime)
		};

		Cursor mainSetCursor = getApplicationContext().getContentResolver().
				query(KeepDoingItProvider.Interactions.CONTENT_URI, INTERACTION_PROJECTION,
						mainSetSelectionClause, mainSetSelectionArgs, SORT_ORDER);

		/*
		 * Moves to the next row in the cursor. Before the first movement in the cursor, the
		 * "row pointer" is -1, and if you try to retrieve data at that position you will get an
		 * exception.
		 */
		while (mainSetCursor.moveToNext()) //main action
		{
			// Gets the value from each column
			long id = mainSetCursor.getLong(mainSetCursor.getColumnIndex(KeepDoingItProvider.Interactions._ID));
			int type = mainSetCursor.getInt(mainSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.TYPE));
			long timestamp = mainSetCursor.
					getLong(mainSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.TIMESTAMP));
			String value = mainSetCursor.
					getString(mainSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.VALUE));
			String extraValue = mainSetCursor.
					getString(mainSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.EXTRA_VALUE));

			Bundle mainAction = new Bundle();
			mainAction.putLong("time", timestamp);
			mainAction.putInt("type", type);
			mainAction.putString("value", value);
			mainAction.putString("extra_value", extraValue);

			//// Filter main interactions
			boolean skip = false;
			for (int i=0; i<filters.size(); i++)
			{
				if ((filters.get(i).getInt("type") == mainAction.getInt("type"))
						&& (filters.get(i).getString("value").contentEquals(mainAction.getString("value")))
						&& (!filters.get(i).getBoolean("enabled",true)))
				{
					skip = true;
					break;
				}
			}

			////

			//checks if this main interaction should count or not according to the filters
			if (!skip)
			{
				//Queries the database for interactions that happened immediately before the main action/interaction
				ArrayList<Bundle> associatedSet = new ArrayList<Bundle>();

				String associatedSetSelectionClause = "(" + KeepDoingItProvider.Interactions.TYPE + " <> ?) "
						+ "AND (" + KeepDoingItProvider.Interactions.TIMESTAMP + " >= ?) AND "
						+ "(" + KeepDoingItProvider.Interactions.TIMESTAMP + " <= ?)";

				String[] associatedSetSelectionArgs = {
						String.valueOf(type),
						String.valueOf(startTime),
						String.valueOf(timestamp)
				};

				Cursor associatedSetCursor = getApplicationContext().getContentResolver().
						query(KeepDoingItProvider.Interactions.CONTENT_URI, INTERACTION_PROJECTION,
								associatedSetSelectionClause, associatedSetSelectionArgs, SORT_ORDER);

				//Starts to try to associate retrieved interactions to the main action/interaction
				while (associatedSetCursor.moveToNext())
				{
					long timestamp2 = associatedSetCursor.
							getLong(associatedSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.TIMESTAMP));
					int type2 = associatedSetCursor.
							getInt(associatedSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.TYPE));
					String value2 = associatedSetCursor.
							getString(associatedSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.VALUE));
					String extraValue2 = associatedSetCursor.
							getString(associatedSetCursor.getColumnIndex(KeepDoingItProvider.Interactions.EXTRA_VALUE));

					Bundle associatedEvent = new Bundle();
					associatedEvent.putLong("time", timestamp2);
					associatedEvent.putInt("type", type2);
					associatedEvent.putString("value", value2);
					associatedEvent.putString("extra_value", extraValue2);

					//// Filter associated interactions
					skip = false;
					for (int i=0; i<filters.size(); i++)
					{
						if ((filters.get(i).getInt("type") == associatedEvent.getInt("type"))
								&& (filters.get(i).getString("value").contentEquals(associatedEvent.getString("value")))
								&& (!filters.get(i).getBoolean("enabled",true)))
						{
							skip = true;
							break;
						}
					}
					////

					//checks if this associated interaction should count or not according to the filters
					if (!skip)
					{
						boolean associate = true;
						for (int i=0; i<associatedSet.size(); i++)
						{
							//Only one interaction of each type can be associated to an action
							if (associatedSet.get(i).getInt("type") == associatedEvent.getInt("type"))
							{
								associate = false;
								break;
							}
						}

						//Only the three closest interactions to the main interaction/action will be associated to it
						if (associatedSet.size()<3)
						{
							if (associate)
							{
								associatedSet.add(0, associatedEvent);
							}
						}
						else
						{
							break;
						}
					} //skip associated interaction because of a filter
				}

				//Starts to generate different rule combinates according to heuristics
				generateRuleCombinations(id, mainAction, associatedSet);

			} //skip a main interaction because of a filter

		}

	}


	@SuppressWarnings("unchecked")
	private void generateRuleCombinations(long id, Bundle mainAction, ArrayList<Bundle> associatedSet) {
		ArrayList<Bundle> moreActions = new ArrayList<Bundle>();
		ArrayList<Bundle> conditions = new ArrayList<Bundle>();
		ArrayList<Bundle> actions = new ArrayList<Bundle>();
		actions.add(mainAction);
		Bundle event;
		Bundle rule;

		//Checks if more than one interaction could be used in a action role simultaneously
		for (int i=associatedSet.size()-1; i>-1; i--) {
			if ((associatedSet.get(i).getInt("type")==BLUETOOTH)
					|| (associatedSet.get(i).getInt("type")==APPLICATION)) {
				Bundle action = associatedSet.remove(i);
				if (i == associatedSet.size()) {
					moreActions.add(0, action);
				}
			}
		}

		//Checks if there is at least one interaction associated with the action that could be the event
		//If negative then try to generate rules with location or time events
		if (!associatedSet.isEmpty()) {
			event = (Bundle) associatedSet.get(associatedSet.size()-1).clone();

			rule = new Bundle();
			rule.putBundle("event", (Bundle) event.clone());
			rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
			rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
			rules.add((Bundle) rule.clone());

			//
			if (associatedSet.size() > 1) {
				conditions.add((Bundle) associatedSet.get(associatedSet.size()-2).clone());
				if (associatedSet.size() > 2) {
					conditions.add(0, (Bundle) associatedSet.get(associatedSet.size()-3).clone());
				}

				rule = new Bundle();
				rule.putBundle("event", (Bundle) event.clone());
				rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
				rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
				rules.add((Bundle) rule.clone());

				if (!moreActions.isEmpty()) {
					Bundle action = actions.remove(0);
					actions.addAll(moreActions);
					actions.add(action);

					rule = new Bundle();
					rule.putBundle("event", (Bundle) event.clone());
					rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
					rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
					rules.add((Bundle) rule.clone());
				}

				if ((event.getInt("type") == RINGER)||(event.getInt("type") == WIFI)
						|| (event.getInt("type") == OUTGOINGCALL)) {
					actions.add(0, event);
					event = conditions.remove(conditions.size()-1);

					rule = new Bundle();
					rule.putBundle("event", (Bundle) event.clone());
					rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
					rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
					rules.add((Bundle) rule.clone());
				}
			} else {
				if (!moreActions.isEmpty()) {
					Bundle action = actions.remove(0);
					actions.addAll(moreActions);
					actions.add(action);

					rule = new Bundle();
					rule.putBundle("event", (Bundle) event.clone());
					rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
					rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
					rules.add((Bundle) rule.clone());
				}
			}


		} else {
			//Time and location rules are generated only if no other interaction was eligible to be an event

			//Generates rules with a location-type event
			String contextSelectionClause = "(" + KeepDoingItProvider.Contexts.INTERACTION_ID + " == ?)";
			String[] contextSelectionArgs = {String.valueOf(id)};

			Cursor contextCursor = getApplicationContext().getContentResolver().
					query(KeepDoingItProvider.Contexts.CONTENT_URI, CONTEXT_PROJECTION, 
							contextSelectionClause, contextSelectionArgs, null);

			while (contextCursor.moveToNext()) {
				int contextType = contextCursor.
						getInt(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.TYPE));
				String contextValue = contextCursor.
						getString(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.VALUE));
				String contextExtraValue = contextCursor.
						getString(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.EXTRA_VALUE));

				Bundle locationEvent = new Bundle();
				locationEvent.putInt("type", contextType);
				locationEvent.putString("value", contextValue);
				locationEvent.putString("extra_value", contextExtraValue);


				actions.clear();
				actions.add(mainAction);

				rule = new Bundle();
				rule.putBundle("event", (Bundle) locationEvent.clone());
				rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
				rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
				rules.add((Bundle) rule.clone());

				if (!moreActions.isEmpty()) {
					Bundle action = actions.remove(0);
					actions.addAll(moreActions);
					actions.add(action);

					rule = new Bundle();
					rule.putBundle("event", (Bundle) locationEvent.clone());
					rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
					rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
					rules.add((Bundle) rule.clone());
				}
			}

			//Generates rules with a temporal event				
			actions.clear();
			actions.add(mainAction);
			event = (Bundle) actions.get(0).clone();
			event.putInt("type", TIME);
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(event.getLong("time"));
			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.getDefault());
			event.putString("value", timeFormat.format(c.getTime()));
			event.putString("extra_value", String.valueOf(event.getLong("time"))); 

			rule = new Bundle();
			rule.putBundle("event", (Bundle) event.clone());
			rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
			rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
			rules.add((Bundle) rule.clone());

			if (!moreActions.isEmpty()) {
				Bundle action = actions.remove(0);
				actions.addAll(moreActions);
				actions.add(action);

				rule = new Bundle();
				rule.putBundle("event", (Bundle) event.clone());
				rule.putParcelableArrayList("conditions", (ArrayList<Bundle>) conditions.clone());
				rule.putParcelableArrayList("actions", (ArrayList<Bundle>) actions.clone());
				rules.add((Bundle) rule.clone());
			}
		}
	}


	/**
	 * Gets the interactions involved in the last rule recommendations and generates a list with them. 
	 */	
	public void generateInteractionsList() {
		interactions.clear();
		filters.clear();

		//Queries the database with the same time window used in the last rule recommendations
		String userActionsSelectionClause = "(" + KeepDoingItProvider.Interactions.TIMESTAMP + " >= ?) AND ("
				+ KeepDoingItProvider.Interactions.TIMESTAMP + " <= ?)";

		String[] userActionsSelectionArgs = {
				String.valueOf(startTime),
				String.valueOf(endTime)
		};

		Cursor interactionsCursor = getApplicationContext().getContentResolver().
				query(KeepDoingItProvider.Interactions.CONTENT_URI, INTERACTION_PROJECTION,
						userActionsSelectionClause, userActionsSelectionArgs, SORT_ORDER);

		while (interactionsCursor.moveToNext()) {
			long id = interactionsCursor.getLong(interactionsCursor.getColumnIndex(KeepDoingItProvider.Interactions._ID));
			int type = interactionsCursor.
					getInt(interactionsCursor.getColumnIndex(KeepDoingItProvider.Interactions.TYPE));
			String value = interactionsCursor.
					getString(interactionsCursor.getColumnIndex(KeepDoingItProvider.Interactions.VALUE));
			String extraValue = interactionsCursor.
					getString(interactionsCursor.getColumnIndex(KeepDoingItProvider.Interactions.EXTRA_VALUE));
			long timestamp = interactionsCursor.
					getLong(interactionsCursor.getColumnIndex(KeepDoingItProvider.Interactions.TIMESTAMP));

			Bundle interaction = new Bundle();
			interaction.putInt("type", type);
			interaction.putString("value", value);
			interaction.putString("extra_value", extraValue);
			interaction.putLong("time", timestamp);

			//gets the context information of the interaction (so far only location)
			String contextSelectionClause = "(" + KeepDoingItProvider.Contexts.INTERACTION_ID + " == ?)";
			String[] contextSelectionArgs = {String.valueOf(id)};

			Cursor contextCursor = getApplicationContext().getContentResolver().
					query(KeepDoingItProvider.Contexts.CONTENT_URI, CONTEXT_PROJECTION, 
							contextSelectionClause, contextSelectionArgs, null);

			while (contextCursor.moveToNext()) {
				int contextType = contextCursor.
						getInt(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.TYPE));
				String contextValue = contextCursor.
						getString(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.VALUE));
				//String contextExtraValue = contextCursor.
				//		getString(contextCursor.getColumnIndex(KeepDoingItProvider.Contexts.EXTRA_VALUE));

				if (contextType == LOCATION) {
					interaction.putString("location", contextValue);
				}
			}

			//Associates a new interaction to the head of the list
			interactions.add(0, interaction);

			//

			//Associates new interaction as a filter only if they are different from the previously added ones
			boolean associate = true;
			for (int i=0; i<filters.size(); i++) {
				if ((filters.get(i).getInt("type") == interaction.getInt("type"))
						&& (filters.get(i).getString("value").contentEquals(interaction.getString("value")))) {
					associate = false;
					break;
				}
			}

			if (associate) {
				//by default all the filters are enabled
				interaction.putBoolean("enabled", true);
				filters.add(0, interaction);
			}
		}
	}


	public void toggleFilter(int index)
	{
		boolean enabled = filters.get(index).getBoolean("enabled",true);
		filters.get(index).putBoolean("enabled", !enabled);
	}


	public String generateRuleDescription(Bundle rule)
	{		
		//Gets the event, conditions and actions of the automation rule					
		Bundle event = rule.getBundle("event");
		ArrayList<Bundle> conditions = rule.getParcelableArrayList("conditions");
		ArrayList<Bundle> actions = rule.getParcelableArrayList("actions");

		//Variable that will contain the built description of the automation rule
		String description = "";

		//Gets the description information related to the event
		description += generateEventDescription(event);

		//Gets the description information related to each condition
		for (int j=0; j<conditions.size(); j++) {
			//Coordinates the description
			if (j==0) {
				description += ", if";
			} else {
				if (j==conditions.size()-1) {
					description += " and";
				} else {
					description += ",";
				}
			}

			description += " " + generateConditionDescription(conditions.get(j));

			if (j==conditions.size()-1) {
				description += ",";
			}
		}

		//Gets the description information related to each action
		for (int j=0; j<actions.size(); j++) {
			if (j>0) {
				//Coordinates the description
				if (j==actions.size()-1) {
					description += " and";
				} else {
					description += ",";
				}
			}

			description += " " + generateActionDescription(actions.get(j));
		}

		return description;
	}


	/**
	 * Fills and gets the description template corresponding to the informed event.
	 * @param event the event in a rule
	 * @return the event description
	 */	
	public String generateEventDescription(Bundle event) {
		String output = "";
		String value = event.getString("value");

		switch (event.getInt("type")) {
		case CHARGING:
			if (!value.equalsIgnoreCase("..."))
			{
				if (Boolean.valueOf(value)) {
					value = "starts charging";
				} else {
					value = "stops charging";
				}
			}
			output += String.format(getResources().getString(R.string.charging_event_message), value);
			break;
		case HEADSET:
			if (!value.equalsIgnoreCase("..."))
			{
				if (Boolean.valueOf(value)) {
					value = "connected";
				} else {
					value = "disconnected";
				}
			}
			output += String.format(getResources().getString(R.string.headset_event_message), value);
			break;
		case LOCATION:
			output += String.format(getResources().getString(R.string.location_event_message), value);
			break;
		case OUTGOINGCALL:
			output += String.format(getResources().getString(R.string.outgoingcall_event_message), value);
			break;
		case RINGER:
			output += String.format(getResources().getString(R.string.ringer_event_message), value);
			break;
		case SCREEN:
			if ((value.equalsIgnoreCase("unlock"))||(value.equalsIgnoreCase("lock"))) {
				output += String.format(getResources().getString(R.string.screen_event_message_1), value);
			} else {
				output += String.format(getResources().getString(R.string.screen_event_message_2), value);
			}
			break;
		case TIME:
			output += String.format(getResources().getString(R.string.time_event_message), value);
			break;
		case WIFI:
			output += String.format(getResources().getString(R.string.wifi_event_message), value);
			break;
		default:
			break;
		}

		return output;
	}

	/**
	 * Fills and gets the description template corresponding to the informed condition.
	 * @param condition one of the conditions in a rule
	 * @return the condition description
	 */	
	public String generateConditionDescription(Bundle condition) {
		String output = "";
		String value = condition.getString("value");

		switch (condition.getInt("type")) {
		case CHARGING:
			if (!value.equalsIgnoreCase("..."))
			{
				if (Boolean.valueOf(value)) {
					value = "charging";
				} else {
					value = "not charging";
				}
			}
			output += String.format(getResources().getString(R.string.charging_condition_message), value);
			break;
		case HEADSET:
			if (!value.equalsIgnoreCase("..."))
			{
				if (Boolean.valueOf(value)) {
					value = "connected";
				} else {
					value = "not connected";
				}
			}
			output += String.format(getResources().getString(R.string.headset_condition_message), value);
			break;
		case LOCATION:
			output += String.format(getResources().getString(R.string.location_condition_message), value);
			break;
		case OUTGOINGCALL:
			output += String.format(getResources().getString(R.string.outgoingcall_condition_message), value);
			break;
		case RINGER:
			output += String.format(getResources().getString(R.string.ringer_condition_message), value);
			break;
		case SCREEN:
			if ((value.equalsIgnoreCase("unlock"))||(value.equalsIgnoreCase("lock"))) {
				value = value+"ed";
				output += String.format(getResources().getString(R.string.screen_condition_message_1), value);
			} else {
				output += String.format(getResources().getString(R.string.screen_condition_message_2), value);
			}
			break;
		case TIME:
			output += String.format(getResources().getString(R.string.time_condition_message), value);
			break;
		case WIFI:
			output += String.format(getResources().getString(R.string.wifi_condition_message), value);
			break;
		default:
			break;
		}

		return output;
	}

	/**
	 * Fills and gets the description template corresponding to the informed action.
	 * @param action one of the actions in a rule
	 * @return the action description
	 */	
	public String generateActionDescription(Bundle action) {
		String output = "";
		String value = action.getString("value");

		switch (action.getInt("type")) {
		case APPLICATION:
			output += String.format(getResources().getString(R.string.app_action_message), value);
			break;
		case BLUETOOTH:
			output += String.format(getResources().getString(R.string.bluetooth_action_message), value);
			break;
		case OUTGOINGCALL:
			output += String.format(getResources().getString(R.string.outgoingcall_action_message), value);
			break;
		case RINGER:
			output += String.format(getResources().getString(R.string.ringer_action_message), value);
			break;
		case WIFI:
			output += String.format(getResources().getString(R.string.wifi_action_message), value);
			break;
		default:
			break;
		}

		return output;
	}

	/**
	 * Gets the icon/drawable corresponding to the informed interaction/context.
	 * @param type the type of the interaction/context
	 * @return the icon id
	 */	
	public int getDrawableID(int type) {
		int drawable = -1;

		switch (type) {
		case APPLICATION:
			drawable = R.drawable.ic_rule_app;
			break;
		case BLUETOOTH:
			drawable = R.drawable.ic_rule_bluetooth;
			break;
		case CHARGING:
			drawable = R.drawable.ic_rule_charging;
			break;
		case HEADSET:
			drawable = R.drawable.ic_rule_headset;
			break;
		case LOCATION:
			drawable = R.drawable.ic_rule_location;
			break;
		case OUTGOINGCALL:
			drawable = R.drawable.ic_rule_call;
			break;
		case RINGER:
			drawable = R.drawable.ic_rule_ringer;
			break;
		case SCREEN:
			drawable = R.drawable.ic_rule_screen;
			break;
		case TIME:
			drawable = R.drawable.ic_rule_time;
			break;
		case WIFI:
			drawable = R.drawable.ic_rule_wifi;
			break;
		default:
			break;
		}

		return drawable;
	}




	//////



	public boolean isMyServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/*
	private static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
			{
				Toast.makeText(instance.getApplicationContext(),String.valueOf(child.delete()), Toast.LENGTH_LONG).show();
				DeleteRecursive(child);
			}

		fileOrDirectory.delete();
	}
	 */

	public void writeAnswers(Bundle questionnaire) {

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}

		File root = android.os.Environment.getExternalStorageDirectory();
		File dir;

		if (!emailID.equals(""))
			dir = new File(root.getAbsolutePath() + File.separator + keepDoingItLogsDir + File.separator + emailID);
		else
			dir = new File(root.getAbsolutePath() + File.separator + keepDoingItLogsDir);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		//

		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

		Calendar currentDateTime = Calendar.getInstance();
		int day = currentDateTime.get(Calendar.DAY_OF_MONTH);
		int month = currentDateTime.get(Calendar.MONTH)+1;
		int year = currentDateTime.get(Calendar.YEAR);

		File file;
		file = new File(dir, "Answers_" + day + month + year);// + "_" + timeFormat.format(currentDateTime.getTime()).replace(":", ""));

		try {
			FileOutputStream outputStream = new FileOutputStream(file, true);
			OutputStreamWriter writer = new OutputStreamWriter(outputStream);

			String session = String.valueOf(mPreferences.getInt("session", 0));

			String prefix = emailID + "\t" + deviceID + "\t" + session + 
					"\t" + String.valueOf(currentDateTime.getTimeInMillis()) +
					"\t" + day + "/" + month + "/" + year + 
					"\t" + timeFormat.format(currentDateTime.getTime());

			String log = prefix;

			//String log = day + "/" + month + "/" + year + "\t" + timeFormat.format(currentDateTime.getTime());

			String[] answers = questionnaire.getStringArray("answers");

			for (int i=0; i<answers.length; i++)
			{
				log += "\t"+answers[i];
			}

			//Toast.makeText(instance.getApplicationContext(), log + "\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

			writer.write(log + "\n");
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public Bundle renameTypeAndValue(Bundle interaction)
	{
		int type = interaction.getInt("type");
		String value = interaction.getString("value");
		String typeName = "";

		switch (type) {
		case APPLICATION:
			typeName = "APPLICATION";
			break;
		case BLUETOOTH:
			typeName = "BLUETOOTH";
			break;
		case CHARGING:
			typeName = "CHARGING";
			if (Boolean.valueOf(value)) {
				value = "charging";
			} else {
				value = "discharging";
			}
			break;
		case HEADSET:
			typeName = "HEADSET";
			if (Boolean.valueOf(value)) {
				value = "connected";
			} else {
				value = "disconnected";
			}
			break;
		case LOCATION:
			typeName = "LOCATION";
			break;
		case OUTGOINGCALL:
			typeName = "OUTGOINGCALL";
			break;
		case RINGER:
			typeName = "RINGER";
			break;
		case SCREEN:
			typeName = "SCREEN";
			break;
		case TIME:
			typeName = "TIME";
			break;
		case WIFI:
			typeName = "WIFI";
			break;
		default:
			break;
		}

		interaction.putString("typeName", typeName);
		interaction.putString("value", value);

		return interaction;
	}

	public void writeLog(Bundle action) {

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}

		int actionType = action.getInt("actionType");

		File root = android.os.Environment.getExternalStorageDirectory();

		File dir;
		if (!emailID.equals(""))
			dir = new File(root.getAbsolutePath() + File.separator + keepDoingItLogsDir + File.separator + emailID);
		else
			dir = new File(root.getAbsolutePath() + File.separator + keepDoingItLogsDir);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

		Calendar currentDateTime = Calendar.getInstance();
		int day = currentDateTime.get(Calendar.DAY_OF_MONTH);
		int month = currentDateTime.get(Calendar.MONTH)+1;
		int year = currentDateTime.get(Calendar.YEAR);

		File file;
		file = new File(dir, "Log_" + day + month + year);// + "_" + timeFormat.format(currentDateTime.getTime()).replace(":", ""));

		try {
			FileOutputStream outputStream = new FileOutputStream(file, true);
			OutputStreamWriter writer = new OutputStreamWriter(outputStream);

			String session = String.valueOf(mPreferences.getInt("session", 0));

			String prefix = emailID + "\t" + deviceID + "\t" + session + 
					"\t" + String.valueOf(currentDateTime.getTimeInMillis()) +
					"\t" + day + "/" + month + "/" + year + 
					"\t" + timeFormat.format(currentDateTime.getTime()) + "\t";

			String log = prefix;
			String actionName = "";

			switch(actionType) {
			case ACTION_GENERATE:
			case ACTION_REGENERATE:

				if (actionType == ACTION_GENERATE) {
					actionName = "GENERATE";
					log += actionName + "\t" + action.getString("timeWindow") +
							"\t\t\t\t\t\t\t\t" + String.valueOf(rules.size()) + 
							"\t" + String.valueOf(interactions.size());
				}
				else
				{
					actionName = "REGENERATE";
					log += actionName + "\t";

					Bundle filter = (Bundle) filters.get(action.getInt("index")).clone();

					if (filter.getBoolean("enabled"))
					{
						log += "enable";
					}
					else
					{
						log += "disable";
					}

					Bundle interaction = renameTypeAndValue(filter);

					int numberOfInteractions = interactions.size();

					for (int j=0; j<filters.size(); j++)
					{
						if (!filters.get(j).getBoolean("enabled",false))
						{
							numberOfInteractions--;
							break;
						}
					}

					log += "\t\t\t\t\t" + interaction.getString("typeName") + 
							"\t" + interaction.getString("value") + 
							"\t\t" + String.valueOf(rules.size()) + 
							"\t" + String.valueOf(numberOfInteractions);
				}

				////

				if (interactions.size()>0) {

					for (int i=0; i<interactions.size(); i++){

						//// Filter interactions
						boolean skip = false;
						for (int j=0; j<filters.size(); j++)
						{
							if ((filters.get(j).getInt("type") == interactions.get(i).getInt("type"))
									&& (filters.get(j).getString("value").contentEquals(interactions.get(i).getString("value")))
									&& (!filters.get(j).getBoolean("enabled",true)))
							{
								skip = true;
								break;
							}
						}

						//checks if this interaction should be shown or not according to the filters
						if (!skip)
						{	
							Bundle interaction =  renameTypeAndValue((Bundle) interactions.get(i).clone());

							Calendar c = Calendar.getInstance();
							c.setTimeInMillis(interactions.get(i).getLong("time"));

							log += "\n" + prefix + actionName + "_INTERACTION\t\t\t" +
									timeFormat.format(c.getTime()) + 
									"\t" + interactions.get(i).getString("location") + 
									"\t\t" + interaction.getString("typeName") + 
									"\t" + interaction.getString("value") + 
									"\t" +  interactions.get(i).getString("extra_value") +
									"\t\t\t" + String.valueOf(i+1); 
						}

					}

					for (int i=0; i<rules.size(); i++){
						Bundle rule = (Bundle) rules.get(i).clone();
						Bundle event = (Bundle) rule.getBundle("event").clone();
						ArrayList<Bundle> conditions = (ArrayList<Bundle>) rule.getParcelableArrayList("conditions").clone();
						ArrayList<Bundle> actions = (ArrayList<Bundle>) rule.getParcelableArrayList("actions").clone();
						int order = 1;

						log += "\n" + prefix + actionName + "_RULE\t\t" + String.valueOf(i+1);
						Bundle interaction = renameTypeAndValue(event);

						log += "\t\t\t0\t" + interaction.getString("typeName") + 
								"\t" + interaction.getString("value") + 
								"\t" +  interaction.getString("extra_value") +
								"\t\t\t" + String.valueOf(order);
						order++;

						for (int j=0; j<conditions.size(); j++) {
							log += "\n" + prefix + actionName + "_RULE\t\t" + String.valueOf(i+1);
							interaction = renameTypeAndValue(conditions.get(j));

							log += "\t\t\t1\t" + interaction.getString("typeName") +  
									"\t" + interaction.getString("value") + 
									"\t" +  interaction.getString("extra_value") +
									"\t\t\t" + String.valueOf(order);
							order++;
						}

						for (int j=0; j<actions.size(); j++) {
							log += "\n" + prefix + actionName + "_RULE\t\t" + String.valueOf(i+1);
							interaction = renameTypeAndValue(actions.get(j));

							log += "\t\t\t2\t" + interaction.getString("typeName") + 
									"\t" + interaction.getString("value") + 
									"\t" +  interaction.getString("extra_value") +
									"\t\t\t" + String.valueOf(order);
							order++;
						}

					}

				}

				break;

			case ACTION_SELECT:
			case ACTION_EDIT:
			case ACTION_SAVE:

				//log += " SELECT_RULE "  " \"" +  action.getString("ruleDescription")+ "\"";
				if (actionType == ACTION_SELECT)
				{
					actionName = "SELECT";
					log += actionName + "\t\t" + String.valueOf(action.getInt("rulePosition")) + "\t\t\t\t\t\t\t\t\t\t";
				}

				//log += " EDIT_RULE " + "\"" + Html.fromHtml(generateRuleDescription(action.getBundle("rule")))+ "\"";
				if (actionType == ACTION_EDIT)
				{
					Bundle interaction = renameTypeAndValue(action);
					actionName = "EDIT";
					log += actionName + "\t" + action.getString("operation") +
							"\t\t\t\t" + String.valueOf(action.getInt("role")) +
							"\t" + interaction.getString("typeName") +
							"\t" + interaction.getString("value") +
							"\t" + interaction.getString("extra_value") +
							"\t\t\t" + String.valueOf(action.getInt("order")) + "\t";
				}

				//log += " SAVE_RULE " + "\"" + Html.fromHtml(generateRuleDescription(action.getBundle("rule")))+ "\"";
				if (actionType == ACTION_SAVE)
				{
					actionName = "SAVE";
					log += actionName + "\t\t\t\t\t\t\t\t\t\t\t\t";	
				}

				////

				Bundle interaction;
				Bundle rule = action.getBundle("rule");
				Bundle event = rule.getBundle("event");
				ArrayList<Bundle> conditions = rule.getParcelableArrayList("conditions");
				ArrayList<Bundle> actions = rule.getParcelableArrayList("actions");
				int order = 1;



				//SELECT_RULE
				//EDIT_RULE
				//SAVE_RULE

				if (!event.isEmpty())
				{
					log += String.valueOf(1+conditions.size()+actions.size());
					//
					log += "\n" + prefix + actionName + "_RULE\t\t";				
					interaction = renameTypeAndValue(event);

					log += "\t\t\t0\t" + interaction.getString("typeName") + 
							"\t" + interaction.getString("value") + 
							"\t" +  interaction.getString("extra_value") +
							"\t\t\t" + String.valueOf(order);
				}
				else
				{
					log += String.valueOf(conditions.size()+actions.size());
				}

				order++;

				for (int j=0; j<conditions.size(); j++) {
					log += "\n" + prefix + actionName + "_RULE\t\t";
					interaction = renameTypeAndValue(conditions.get(j));

					log += "\t\t\t1\t" + interaction.getString("typeName") +  
							"\t" + interaction.getString("value") + 
							"\t" +  interaction.getString("extra_value") +
							"\t\t\t" + String.valueOf(order);
					order++;
				}

				for (int j=0; j<actions.size(); j++) {
					log += "\n" + prefix + actionName + "_RULE\t\t";
					interaction = renameTypeAndValue(actions.get(j));

					log += "\t\t\t2\t" + interaction.getString("typeName") + 
							"\t" + interaction.getString("value") + 
							"\t" +  interaction.getString("extra_value") +
							"\t\t\t" + String.valueOf(order);
					order++;
				}

				break;
			}

			////			
			writer.write(log + "\n");
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private class S3UploadLogTask extends AsyncTask<String, Void, S3TaskResult> {

		//String fileName = "";
		String filePath = "";

		protected void onPreExecute() { }

		protected S3TaskResult doInBackground(String... uris) {
			File root = android.os.Environment.getExternalStorageDirectory();
			filePath = new String(root.getAbsolutePath() + File.separator + keepDoingItLogsDir + File.separator + emailID + File.separator + uris[0]);
			//fileName = filePath;

			S3TaskResult result = new S3TaskResult();

			String objectName = emailID + File.separator + deviceID + File.separator + uris[0];

			try {
				PutObjectRequest por = new PutObjectRequest(keepDoingItBucket, objectName, new java.io.File(filePath));

				s3Client.putObject(por);
			} catch (Exception exception) {
				exception.printStackTrace();
				result.setErrorMessage(exception.getMessage());
			}

			return result;
		}

		protected void onPostExecute(S3TaskResult result) {
			if (result.getErrorMessage() == null)
			{
				//File file = new File(filePath);
				//file.delete();
				//Toast.makeText(instance.getApplicationContext(), fileName + " / " + String.valueOf(file.delete()), Toast.LENGTH_LONG).show();
			}

		}
	}


	private class S3TaskResult {
		String errorMessage = null;
		Uri uri = null;

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public Uri getUri() {
			return uri;
		}

		public void setUri(Uri uri) {
			this.uri = uri;
		}
	}


	//public void uploadLog(String logName) {
	//new S3UploadLogTask().execute(new String(logName));
	//}


	public void uploadLogFiles() {

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}

		//Calendar currentDateTime = Calendar.getInstance();
		//int currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
		//int currentMonth = currentDateTime.get(Calendar.MONTH);
		//int currentYear = currentDateTime.get(Calendar.YEAR);

		//SharedPreferences prefs = getInstance().getApplicationContext().getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);
		//String username = prefs.getString("email", "");

		//List<String> fileList = new ArrayList<String>();
		File root = android.os.Environment.getExternalStorageDirectory();
		File logsDir = new File(root.getAbsolutePath() + File.separator + keepDoingItLogsDir + File.separator + emailID);


		File[] files = logsDir.listFiles();

		//fileList.clear();
		for (File file : files) {

			//String[] splittedName = file.getName().split("_");

			//int logYear = Integer.parseInt(splittedName[splittedName.length-1]);
			//int logMonth = Integer.parseInt(splittedName[splittedName.length-2]);
			//int logDay = Integer.parseInt(splittedName[splittedName.length-3]);

			// Manda os logs antigos que ainda estao salvos no celular
			//if (logDay != currentDay || logMonth != currentMonth || logYear != currentYear) {
			new S3UploadLogTask().execute(file.getName());
			//}
		}


	}

}

