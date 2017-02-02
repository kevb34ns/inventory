package com.kevinkyang.inventory;

import android.app.NotificationManager;
import android.content.Context;
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
	public void sendNotifications() {
		ArrayList<Item> expiring = ItemData.getInstance().getExpiringItems(RANGE);
		if (expiring.isEmpty()) {
			return;
		}

		String contentTitle = "You have " + expiring.size() + " items expiring soon.";
		String contentText = "";
		for (Item i : expiring) {
			contentText += i.getName() + " in" +
					i.getDaysUntilExpiration() + "days; ";
		}
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_action_add)
						.setContentTitle(contentTitle)
						.setContentText("Your milk is expiring in 3 days.");
		int notificationId = 001;
		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
		notifyMgr.notify(notificationId, builder.build());
	}

}
