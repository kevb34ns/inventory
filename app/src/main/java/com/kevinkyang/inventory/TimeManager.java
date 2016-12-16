package com.kevinkyang.inventory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Kevin on 12/15/2016.
 */

public class TimeManager {
	public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String getDateTimeUTC() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		return sdFormat.format(cal.getTime());
	}

	public static String getDateTimeLocal() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		return sdFormat.format(cal.getTime());
	}

	public static String getLocalDateTimeFromUTC(String dateTimeUTC) {
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		sdFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		Date date = null;
		try {
			date = sdFormat.parse(dateTimeUTC);
		} catch (ParseException e) {
			return "error";
		}

		sdFormat.setTimeZone(TimeZone.getDefault());
		return sdFormat.format(date);
	}
}
