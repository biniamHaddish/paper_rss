package com.biniam.rss.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by biniam on 28/8/17.
 * This Class is responsible for the Date conversion from ISO8601 to UTC format and vise versa
 */
public final class DateUtils {

    /*Empty Constructor*/
    private DateUtils() {
    }

    /**
     *  Will convert the UTC Date format to ISO 8601 Date format
     * @param date
     * @return
     */
    public static String toISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        df.setTimeZone(tz);
        Log.d("DateUtils", String.format("toISO8601UTC: %s", df.format(date)));
        return df.format(date);
    }

    /**
     *  Will convert the Iso8601 to UTC Date Format
     * @param dateStr
     * @return
     */
    public static long fromISO8601UTC(String dateStr) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        df.setTimeZone(tz);
        try {
            return df.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *  Will convert the long type Date to Date Format
     * @param longdatetime
     * @return
     */
    public static Date convertToDateTime(long longdatetime) {
        if (longdatetime != 0) {
            Date date = new Date(longdatetime);
            return date;
        }
        return null;
    }


    public static String inoReaderToISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public static Date inoReaderFromISO8601UTC2(String dateStr) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}