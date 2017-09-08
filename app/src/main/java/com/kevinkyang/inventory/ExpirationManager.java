package com.kevinkyang.inventory;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ExpirationManager {
	public static final String DEFAULT_NOTIFICATION_TIME = "12:00 PM";

	private Context mContext;

	public ExpirationManager(Context context) {
		mContext = context;
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

	private Notification getNotification() {
		ItemManager itemManager = ItemManager.getInstance();
		if (!itemManager.isInitialized()) {
			itemManager.init(mContext);
		}
		ArrayList<Item> expiring = itemManager.getInstance().getExpiringItems(getExpirationInterval(mContext));
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

		Intent clickIntent = new Intent(mContext, MainActivity.class);
		clickIntent.putExtra("inventory", "Expiring");
		PendingIntent clickPendingIntent =
				PendingIntent.getActivity(
						mContext,
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
			mContext
				.getResources()
				.getColor(R.color.colorPrimary, null);

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(mContext)
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
		NotificationManager notifyMgr = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
		notifyMgr.notify(notificationId, notification);
	}

	public void scheduleNotifications() {
		Intent intent = new Intent(mContext, NotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String timeString = prefs.getString(
				SettingsFragment.PREFKEY_NOTIFICATION_TIME,
				DEFAULT_NOTIFICATION_TIME);

		Calendar cal = TimeManager.timeStringToCal(timeString);

		AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, pendingIntent); //TODO could this be inexact?

		// enable NotificationReceiver
		ComponentName receiver = new ComponentName(mContext, NotificationReceiver.class);
		PackageManager pm = mContext.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	public void cancelNotifications() {
		Intent intent = new Intent(mContext, NotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (pendingIntent == null) {
			return;
		}

		AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(pendingIntent);
		pendingIntent.cancel();

		// disable NotificationReceiver
		ComponentName receiver = new ComponentName(mContext, NotificationReceiver.class);
		PackageManager pm = mContext.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	public static int getExpirationInterval(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return TimeManager.parseDays(prefs.getString(
				SettingsFragment.PREFKEY_EXPIRATION_INTERVAL,
				"3 days"));
	}
}
