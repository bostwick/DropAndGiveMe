package com.bostwickgarnes.dropandgiveme;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	
	/// Format for all date operations
	public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

	/**
	 * Formats a date into a standardized textual representation
	 * @param date	The date to format
	 * @return		A string in DATE_FORMAT representation
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		return sdf.format(date);
	}
	
	/**
	 * Given a string in DATE_FORMAT, returns the corrensponding date
	 * @param date	The string to parse
	 * @return		The Date object represented by that string, or the 
	 * 				current date object if str was invalid
	 */
	public static Date parseDate(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		Date date = null;
		
		try {
			date = sdf.parse(str);
		} catch(ParseException e) {
			Log.w(TAG, "Unable to parse date string \"" + str + "\"");
			Log.w(TAG, "Exception: " + e.getLocalizedMessage());
		}
		
		return (date == null) ? new Date() : date;
	}

	/**
	 * Returns a human readable version of a Date
	 * @param date	The date to format
	 * @return
	 */
	public static String timeAgoInWords(Date from) {
		Date now = new Date();
		long difference = now.getTime() - from.getTime();
		long distanceInMin = difference / 60000;
		
		if ( 0 <= distanceInMin && distanceInMin <= 1 ) {
			return "Less than 1 minute";
		} else if ( 1 <= distanceInMin && distanceInMin <= 45 ) {
			return distanceInMin + " minutes";
		} else if ( 45 <= distanceInMin && distanceInMin <= 89 ) {
			return "About 1 hour";
		} else if ( 90 <= distanceInMin && distanceInMin <= 1439 ) {
			return "About " + (distanceInMin / 60) + " hours";
		} else if ( 1440 <= distanceInMin && distanceInMin <= 2529 ) {
			return "1 day";
		} else if ( 2530 <= distanceInMin && distanceInMin <= 43199 ) {
			return (distanceInMin / 1440) + "days";
		} else if ( 43200 <= distanceInMin && distanceInMin <= 86399 ) {
			return "About 1 month";
		} else if ( 86400 <= distanceInMin && distanceInMin <= 525599 ) {
			return "About " + (distanceInMin / 43200) + " months";
		} else {
			long distanceInYears = distanceInMin / 525600;
			return "About " + distanceInYears + " years";
		}
	}
}
