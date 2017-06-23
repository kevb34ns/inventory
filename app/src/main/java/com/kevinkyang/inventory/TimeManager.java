package com.kevinkyang.inventory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeManager {
	public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";

	public static String addDaysToDate(String date, int daysToAdd) {
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdFormat.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		cal.add(Calendar.DATE, daysToAdd);
		return sdFormat.format(cal.getTime());
	}

	/**
	 * Finds the difference in days between two
	 * dates.
	 * @param startDate the starting date as a
	 *                  String.
	 * @param endDate the ending date as a String.
	 * @return the difference in days, which is
	 * negative if endDate < startDate, zero if
	 * endDate == startDate, and positive if
	 * endDate > startDate.
	 */
	public static int getDateDifferenceInDays(String startDate, String endDate) {
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		int difference = 0;
		try {
			Date sDate = sdFormat.parse(startDate);
			Date eDate = sdFormat.parse(endDate);
			difference = ((int) (eDate.getTime() / (24 * 60 * 60 * 1000))) -
					((int) (sDate.getTime() / (24 * 60 * 60 * 1000)));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return difference;
	}

	public static String getDateTimeUTC() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return sdFormat.format(cal.getTime());
	}

	public static String getDateTimeLocal() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return sdFormat.format(cal.getTime());
	}

	public static String getLocalDateTimeFromUTC(String dateTimeUTC) {
		// TODO method does not work correctly
		SimpleDateFormat sdFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
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

	/**
	 * Converts a number of days to, approximately,
	 * the largest unit of time possible (weeks,
	 * months, or years).
	 * Rounds down, so 364 days is rounded to 11
	 * months, and 600 days is rounded to 1 year.
	 * @param days a non-negative number of days.
	 * @return a String in this format:
	 * "integer unitOfTime"
	 */
	public static String convertDays(int days) {
		int num = (days >= 0) ? days : -days;
		return convertTime(num, 0);
	}

	/**
	 * Recursive helper method for convertDays.
	 * @param num a non-negative integer.
	 * @param unit an integer representing a unit:
	 *             days = 0, weeks = 1, months = 2,
	 *             years = 3.
	 * @return a String in this format:
	 * "integer unitOfTime"
	 */
	private static String convertTime(int num, int unit) {
		// TODO issue here where anything > 360 days and < 365 will say 12 months when it should say 11. Probaby other cases where this happens as well
		if (unit == 0 && num < 7) {
			String result = "" + num + " DAY";
			return (num != 1) ? result + "S" : result;
		} else if (unit == 1 && num < 30) {
			num /= 7;
			String result = "" + num + " WEEK";
			return (num != 1) ? result + "S" : result;
		} else if (unit == 2 && num < 365) {
			num /= 30;
			String result = "" + num + " MONTH";
			return (num != 1) ? result + "S" : result;
		} else if (unit == 3) {
			num /= 365;
			String result = "" + num + " YEAR";
			return (num != 1) ? result + "S" : result;
		} else {
			return convertTime(num, ++unit);
		}
	}

	/**
	 * TODO better method name, same issue where you arbitrarily decide that 1 month = 30 days, and it's not the best solution
	 * @param suggestion
	 * @return
	 */
	public static String getDateFromSuggestion(String suggestion) {
		int amount = Integer.parseInt(
				suggestion.substring(0, suggestion.length() - 1));
		String unit = suggestion.substring(suggestion.length() - 1);
		int multiplier = 0;
		if (unit.equals("d")) {
			multiplier = 1;
		} else if (unit.equals("w")) {
			multiplier = 7;
		} else if (unit.equals("m")) {
			multiplier = 30;
		} else if (unit.equals("y")) {
			multiplier = 365;
		}

		return addDaysToDate(getDateTimeLocal(), amount * multiplier);
	}
}
