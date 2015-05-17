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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Implements a broadcast receiver responsible for automatically starting the logger service on boot.
 * 
 * @author Rodrigo Maues
 * @version 1.0
 */
public class StartKeepDoingItServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, KeepDoingItService.class);
		//Starts the logger service/component
		context.startService(service);
	}

}
