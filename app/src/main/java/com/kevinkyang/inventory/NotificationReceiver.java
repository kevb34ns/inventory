package com.kevinkyang.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Kevin on 2/2/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {
	public static final String NOTIFICATION_ID = "notification_id";
	public static final String NOTIFICATION = "notification";

	// TODO must re enable alarm when device boots
	// TODO must have ability to cancel alarms
	@Override
	public void onReceive(Context context, Intent intent) {
		ExpirationManager expirationMgr = new ExpirationManager(context);
		expirationMgr.sendNotifications();
	}
}
