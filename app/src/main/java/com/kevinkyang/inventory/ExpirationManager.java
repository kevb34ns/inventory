package com.kevinkyang.inventory;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Kevin on 1/30/2017.
 */

public class ExpirationManager {
	public static final int RANGE = 3;

	private Context context;

	public ExpirationManager(Context context) {
		this.context = context;
	}

	// TODO style notification, look at Notification.InboxStyle
	private Notification getNotification() {
		ArrayList<Item> expiring = ItemData.getInstance().getExpiringItems(RANGE);
		if (expiring.isEmpty()) {
			return null;
		}

		String contentTitle = "You have " + expiring.size() + " items expiring soon.";
		String contentText = "";
		for (Item i : expiring) {
			contentText += i.getName() + " in " +
					i.getDaysUntilExpiration() + " days; "; // TODO day(s)
		}
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_action_add)
						.setContentTitle(contentTitle)
						.setContentText(contentText);
		return builder.build();
	}

	public void sendNotifications() {
		Notification notification = getNotification();
		int notificationId = 001;
		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		notifyMgr.notify(notificationId, notification);
	}

	public void scheduleNotifications() {
		Intent intent = new Intent(context, NotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 18); // TODO make user customizable

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent); //TODO could this be inexact?
	}

}
