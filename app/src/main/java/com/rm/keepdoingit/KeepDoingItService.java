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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/*import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;*/

/**
 * Implements the service that acts as logger component.
 * Manages the broadcast receivers used in the application to monitor
 * the interaction of the user with the device and the associated context.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class KeepDoingItService extends Service { /*implements GooglePlayServicesClient.ConnectionCallbacks,
															GooglePlayServicesClient.OnConnectionFailedListener,
															LocationListener{*/
	//Constant with the maximum value to wait before notifying the user to answer the questionnaire
	public static final int TWO_MINUTES = 120000;
	//public static final int TWO_MINUTES = 15000;
	public static final int NOTIFICATION_ID = 1;

	//name of the last app that was launched
	private String lastApplication = "";
	//array with all the applications currently installed in the smartphone 
	private ArrayList<String> applications = new ArrayList<String>(); //nome das apps aceitas
	//handler for the thread that will check which application is open every second
	private Handler handler = new Handler();
	//manager for the user location data
	private LocationManager locationManager;
	//coordinates of the last know user location
	private String locationCoordinates = "";
	//address of the last know user location
	private String locationAddress = "";
    //LocationClient mLocationClient;
 // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Define an object that holds accuracy and frequency parameters
    //LocationRequest mLocationRequest;

	private SharedPreferences mPreferences;

	/**
	 * Actions to perform when the service is started.
	 * Default method used to set up all the necessary variables.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Get the saved application preferences.
		//These preferences persist even when the application is closed
		mPreferences = getSharedPreferences(KeepDoingItApplication.PREFERENCES_FILE, MODE_PRIVATE);

		//Registers to receive updates about the Bluetooth state
		this.registerReceiver(this.bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		//Registers to receive updates about the battery state (whether it is charging or not)
		this.registerReceiver(this.batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
		this.registerReceiver(this.batteryReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
		//Registers to receive updates about whether the headset is connected or not
		this.registerReceiver(this.audioReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		//Registers to receive updates about the ringer mode
		this.registerReceiver(this.audioReceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
		//Registers to receive updates about the screen status (whether it is locked or not)
		this.registerReceiver(this.screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		this.registerReceiver(this.screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		this.registerReceiver(this.screenReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
		//Registers to receive updates about the Wi-Fi state
		this.registerReceiver(this.networkReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		//Registers to receive updates about the Wi-Fi state
		this.registerReceiver(this.telephonyReceiver, new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));

		//Registers to receive updates about the current location
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);

		 // Check that Google Play services is available
		
        /*int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
        	// Create the LocationRequest object
            mLocationRequest = LocationRequest.create();
            // Use high accuracy
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            // Set the update interval to 5 seconds
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            // Set the fastest update interval to 1 second
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            
            mLocationClient = new LocationClient(this, this, this);
            mLocationClient.connect();
            //mLocationClient.getLastLocation();
        }*/
		
		//Monitors the launched applications
		PackageManager pm = getApplicationContext().getPackageManager();
		final Intent filter = new Intent(Intent.ACTION_MAIN, null);
		filter.addCategory(Intent.CATEGORY_LAUNCHER);
		//Adds all the application packages
		for (ResolveInfo info : pm.queryIntentActivities(filter, 0)) {
			applications.add(info.activityInfo.packageName);
		}
		//Remove unnecessary packages and add the missing ones
		//applications.remove(this.getPackageName());
		//applications.remove("com.android.settings");
		applications.add("com.android.launcher");
		//Starts the thread that will check which application is open every second
		handler.post(runnable);

		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	/**
	 * Actions to perform when the service is finished.
	 * Stop the monitoring and unregisters all the previously registered receivers.
	 */
	@Override
	public void onDestroy() {
		this.unregisterReceiver(this.bluetoothReceiver);
		this.unregisterReceiver(this.audioReceiver);
		this.unregisterReceiver(this.batteryReceiver);
		this.unregisterReceiver(this.networkReceiver);
		this.unregisterReceiver(this.screenReceiver);
		this.unregisterReceiver(this.telephonyReceiver);
		locationManager.removeUpdates(locationListener);
		handler.removeCallbacks(runnable);
	}

	/**
	 * Creates a thread that runs each second checking which app is in the foreground
	 * and if a new app was launched
	 */
	private Runnable runnable = new Runnable() {
		public void run() {
			ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
			PackageManager pm = getApplicationContext().getPackageManager();

			//Gets the info from the currently running task
			String  packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();


			//// Checks if the user should be notifyied to answer the questionnaire
			if (mPreferences.getLong("lastTime", 0) != 0)
			{
				if (!packageName.equalsIgnoreCase(getPackageName()))
				{
					if ((System.currentTimeMillis() - mPreferences.getLong("lastTime", 0)) > TWO_MINUTES)
					{
						sendNotification();

						//SharedPreferences.Editor editor = mPreferences.edit();
						//editor.putLong("lastTime", 0);
						//editor.commit();
					}
				}
				else
				{
				    SharedPreferences.Editor editor = mPreferences.edit();
					editor.putLong("lastTime", System.currentTimeMillis());
					editor.commit();
				}
			}
			////

			if (applications.contains(packageName)) {
				String name = packageName;
				try {
					name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 
							PackageManager.GET_META_DATA)).toString();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				if ((!name.equalsIgnoreCase(""))&&(!name.equalsIgnoreCase(lastApplication))) {
					lastApplication = name;

					//if (!name.equalsIgnoreCase("Launcher")) {
					if (!packageName.equalsIgnoreCase("com.android.launcher") 
							&& !packageName.equalsIgnoreCase(getPackageName()) 
							&& !packageName.equalsIgnoreCase("com.android.settings")) {
						save(KeepDoingItApplication.APPLICATION, name, packageName);
					}
				}
			}

			//Here comes the "trick" to call this process every second */
			handler.postDelayed(this, 1000);
		}
	};

	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * regarding the Bluetooth state.
	 */
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!this.isInitialStickyBroadcast()) {
				String value = "";

				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
				case BluetoothAdapter.STATE_ON:
					value = "on";
					break;
				case BluetoothAdapter.STATE_OFF:
					value = "off";
					break;
				default:
					break;
				}

				if (!value.equalsIgnoreCase("")) {
					save(KeepDoingItApplication.BLUETOOTH, value);
				}
			}
		}
	};

	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * regarding the battery state (whether it is charging or not).
	 */
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(Intent.ACTION_POWER_CONNECTED)) {
				int type = KeepDoingItApplication.CHARGING;
				String value = "true";
				save(type, value);
			} else { 
				if (action.equalsIgnoreCase(Intent.ACTION_POWER_DISCONNECTED)) {
					int type = KeepDoingItApplication.CHARGING;
					String value = "false";
					save(type, value);
				}
			}
		}
	};

	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * regarding the ringer mode and the headset connection.
	 */
	private BroadcastReceiver audioReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!this.isInitialStickyBroadcast()) {

				String action = intent.getAction();
				//Checks if the received message is related to the ringer mode
				//or related to the the headset connection instead
				if (action.equalsIgnoreCase(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
					AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					String ringerMode = "normal";

					switch (am.getRingerMode()) {
					case AudioManager.RINGER_MODE_SILENT:
						ringerMode = "silent";
						break;
					case AudioManager.RINGER_MODE_VIBRATE:
						ringerMode = "vibrate";
						break;
					default:
						break;
					}

					int type = KeepDoingItApplication.RINGER;
					String value = ringerMode; 
					save(type, value);

				} else {
					AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					String isHeadsetConnected = String.valueOf(am.isWiredHeadsetOn());

					int type = KeepDoingItApplication.HEADSET;
					String value = isHeadsetConnected; 
					save(type, value);
				}
			}
		}
	};

	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * regarding the Wi-Fi connection.
	 */
	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!this.isInitialStickyBroadcast()) {
				String value = "";
				//Records the interaction only if the message was indeed about the Wi-Fi
				//and not about the 3G instead
				switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
				case WifiManager.WIFI_STATE_ENABLED:
					value = "on";
					break;
				case WifiManager.WIFI_STATE_DISABLED:
					value = "off";
					break;
				default:
					break;
				}

				if (!value.equalsIgnoreCase("")) {
					int type = KeepDoingItApplication.WIFI;
					save(type, value);
				} 
			}
		}
	};

	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * when the status of the screen changes.
	 */
	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {			
			KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
			String action = intent.getAction();
			String value = "";

			if (action.equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
				if (!keyguardManager.inKeyguardRestrictedInputMode()) {
					value = "off";
				} else {
					value = "lock";
				}
			}

			if (action.equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
				if (!keyguardManager.inKeyguardRestrictedInputMode()) {
					value = "on";
				}
			}

			if (action.equalsIgnoreCase(Intent.ACTION_USER_PRESENT)) {
				value = "unlock";
			}

			if (!value.equalsIgnoreCase("")) {
				int type = KeepDoingItApplication.SCREEN;
				save(type, value);
			}
		}
	};


	/**
	 * Defines a listener for the messages broadcasted by the system
	 * about the current location of the user/device.
	 */
	private android.location.LocationListener locationListener = new android.location.LocationListener() {

		public void onLocationChanged(Location location) {
			locationCoordinates = String.format("%f;%f", location.getLongitude(), location.getLatitude());

			//Toast.makeText(getApplicationContext(), locationCoordinates, Toast.LENGTH_SHORT).show();
			
			// Bypass reverse-geocoding if the Geocoder service is not available on the
			// device. The isPresent() convenient method is only available on Gingerbread or above.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
				// Since the geocoding API is synchronous and may take a while.  You don't want to lock
				// up the UI thread.  Invoking reverse geocoding in an AsyncTask.
				(new ReverseGeocodingTask(getApplicationContext())).execute(new Location[] {location});
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};


	/**
	 * AsyncTask encapsulating the reverse-geocoding API.
	 * Since the geocoder API is blocked, we do not want to invoke it from the UI thread.
	 */
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
		private Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			Location loc = params[0];
			List<Address> addresses = null;

			try {
				//Call the synchronous getFromLocation() method by passing in
				//the latitude and longitude values.
				addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				//Formats the first line of address (if available) and locality.
				locationAddress = String.format("%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "Location",
								address.getLocality());
			} else {
				locationAddress = "Location";
			}
			return null;
		}
	}


	/**
	 * Defines a receiver for the messages broadcasted by the system
	 * when a phone call is made.
	 */
	private BroadcastReceiver telephonyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int type = KeepDoingItApplication.OUTGOINGCALL;
			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			String contactName = getContactName(phoneNumber);
			//Records the name of the contact and the phone number called
			save(type, contactName, phoneNumber);
		}
	};


	/**
	 * Method that retrieves the contact name associated with a given phone number
	 * @param phoneNumber the contact phone number 
	 * @return the contact name or the phone number in case the name could not be found
	 */
	public String getContactName(String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		ContentResolver resolver=getContentResolver();
		Cursor cur = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
		if (cur!=null&&cur.moveToFirst()) {
			String value=cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			if (value!=null) { 
				cur.close();
				return value;
			}
		}
		cur.close();
		return phoneNumber;
	}


	/**
	 * Calls the method that records the interactions and context data without
	 * the need of informing an extra value for the interaction.
	 */
	public void save(int type, String value) {
		save(type, value, "");
	}

	
	/**
	 * Method that records the interactions and context data detected.
	 */
	protected void save(int type, String value, String extraValue) {
		//Defines an object to contain the new interaction values to insert
		ContentValues mNewInteractionValues = new ContentValues();
		/*
		 * Sets the values of each column and inserts the data. The arguments to the "put"
		 * method are "column name" and "value"
		 */
		mNewInteractionValues.put(KeepDoingItProvider.Interactions.TYPE, type);
		mNewInteractionValues.put(KeepDoingItProvider.Interactions.VALUE, value);
		mNewInteractionValues.put(KeepDoingItProvider.Interactions.EXTRA_VALUE, extraValue);
		mNewInteractionValues.put(KeepDoingItProvider.Interactions.TIMESTAMP, System.currentTimeMillis());

		//Defines a new Uri object that receives the result of the insertion
		Uri mNewInteractionUri = getContentResolver().
				insert(KeepDoingItProvider.Interactions.CONTENT_URI, mNewInteractionValues);

		if (!locationAddress.equalsIgnoreCase("")) {
			//The id of the recently recorded interaction
			long interactionId = ContentUris.parseId(mNewInteractionUri);

			//Defines an object to contain the new context values to insert
			ContentValues mNewContextValues = new ContentValues();

			mNewContextValues.put(KeepDoingItProvider.Contexts.INTERACTION_ID, interactionId);
			mNewContextValues.put(KeepDoingItProvider.Contexts.TYPE, KeepDoingItApplication.LOCATION);
			mNewContextValues.put(KeepDoingItProvider.Contexts.VALUE, locationAddress);
			mNewContextValues.put(KeepDoingItProvider.Contexts.EXTRA_VALUE, locationCoordinates);

			//Inserts the location context associated with the recently recorded interaction
			getContentResolver().insert(KeepDoingItProvider.Contexts.CONTENT_URI, mNewContextValues);
		}

	}


	/**
	 * Method that records the interactions and context data detected.
	 */
	protected void sendNotification()
	{
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		//.setSmallIcon(R.drawable.notification_icon)
		.setAutoCancel(true)
		.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
		.setOnlyAlertOnce(true)
		.setSmallIcon(R.drawable.ic_stat_notify)
		.setContentTitle("App usage questionnaire")
		.setContentText("Touch to answer a few questions.");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, Questionnaire.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		//stackBuilder.addParentStack(Questionnaire.class);
		stackBuilder.addParentStack(MyRulesList.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		//mNotificationManager.notify(mId, mBuilder.build());
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}


	/*public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}


	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}


	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}


	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		locationCoordinates = String.format("%f;%f", location.getLongitude(), location.getLatitude());

		//Toast.makeText(getApplicationContext(), locationCoordinates, Toast.LENGTH_SHORT).show();
		
		// Bypass reverse-geocoding if the Geocoder service is not available on the
		// device. The isPresent() convenient method is only available on Gingerbread or above.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
			// Since the geocoding API is synchronous and may take a while.  You don't want to lock
			// up the UI thread.  Invoking reverse geocoding in an AsyncTask.
			(new ReverseGeocodingTask(getApplicationContext())).execute(new Location[] {location});
		}
	}*/

}
