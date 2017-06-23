package com.kevinkyang.inventory;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ExpirationManager {
	public static final int RANGE = 3;

	private Context context;

	public ExpirationManager(Context context) {
		this.context = context;
	}

	// returns a String with bolded title + content
	private SpannableString getFormattedLine(String title, String content) {
		if (title == null || content == null) {
			return null;
		}

		StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
		SpannableString string = new SpannableString(
				String.format("%s %s", title, content));
		string.setSpan(
				boldStyle,
				0, title.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return string;
	}

	// TODO style notification, look at Notification.InboxStyle
	private Notification getNotification() {
		if (!DBManager.getInstance().isInitialized()) {
			DBManager.getInstance().init(context);
		}
		ArrayList<Item> expiring = ItemData.getInstance().getExpiringItems(RANGE);
		if (expiring.isEmpty()) {
			return null;
		}

		String contentTitle = "You have " + expiring.size() +
				((expiring.size() == 1) ? " item" : " items") +
				" expiring soon.";
		String contentText = "";
		for (int i = 0; i < expiring.size(); i++) {
			// if size=1 or size=2, no comma
			// if size=2, put 'and' between items 1 and 2
			// if size > 2, put 'and x more'
			contentText += expiring.get(i).getName();
			if (expiring.size() > 2) {
				contentText += ", ";
			} else if (i == 0 && expiring.size() == 2) {
				contentText += " and ";
			}

			if (i == 2) {
				int remaining = expiring.size() - 2;
				contentText += "and " + remaining + " more";
				break;
			}
		}

		Intent clickIntent = new Intent(context, MainActivity.class);
		clickIntent.putExtra("inventory", "Expiring");
		PendingIntent clickPendingIntent =
				PendingIntent.getActivity(
						context,
						0,
						clickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		NotificationCompat.InboxStyle style =
				new NotificationCompat.InboxStyle();
		style.setBigContentTitle("Expiring soon:");
		for (int i = 0; i < expiring.size(); i++) {
			if (i == 5) {
				// limit 5 items
				int remaining = expiring.size() - 4;
				style.addLine("+" + remaining + " more...");
				break;
			}

			Item item = expiring.get(i);
			String title = item.getName();
			String content;
			int days = item.getDaysUntilExpiration();
			if (days < 0) {
				content = -days + " days ago";
			} else if (days == 0) {
				content = " today";
			} else if (days == 1) {
				content = " in 1 day";
			} else {
				content = " in " + days + " days";
			}
			style.addLine(getFormattedLine(title, content));
		}

		int accentColor =
			context
				.getResources()
				.getColor(R.color.colorPrimary, null);

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_action_inbox)
						.setContentTitle(contentTitle)
						.setContentText(contentText)
						.setContentIntent(clickPendingIntent)
						.setStyle(style)
						.setColor(accentColor)
						.setAutoCancel(true);
		return builder.build();
	}

	public void sendNotifications() {
		Notification notification = getNotification();
		if (notification == null) {
			return;
		}

		int notificationId = 1;
		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		notifyMgr.notify(notificationId, notification);
	}

	public void scheduleNotifications() {
		Intent intent = new Intent(context, NotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 12); // TODO make user customizable
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, pendingIntent); //TODO could this be inexact?
	}

	public void cancelNotifications() {
		Intent intent = new Intent(context, NotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(pendingIntent);

		// disable NotificationReceiver
		ComponentName receiver = new ComponentName(context, NotificationReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}
}
