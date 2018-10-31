package com.biniam.rss.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.biniam.rss.R;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by Biniam on 3/1/17.
 *
 * General utility functions
 *
 */

public class Utils {
    public  static final String TAG = Utils.class.getSimpleName();
    private static final String GOOGLE_CHROME_PACKAGE_ID = "com.android.chrome";
    /**
     * This code below will run before any of the constructors and will be helpful
     * if we accidentally forgot to initialize the constructor for this class
     */
    public Utils() {

    }
    /**
     * Get SHA-1 digest for the given string, largely used to make unique ids
     *
     * @param string the string to calculate SHA-1 for
     * @return SHA-1 string or null if error occurs
     */
    public static String getSHA1Digest(String string) {
        final String HEX = "0123456789abcdef";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(string.getBytes("UTF-8"), 0, string.length());
            byte[] sha1Hash = messageDigest.digest();
            if (sha1Hash == null) return null;
            int length = sha1Hash.length;
            StringBuffer result = new StringBuffer(2 * length);
            for (int i = 0; i < sha1Hash.length; i++) {
                result.append(HEX.charAt((sha1Hash[i] >> 4) & 0x0f)).append(HEX.charAt(sha1Hash[i] & 0x0f));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * Compares @param time1 and @param time2 and returns true if they are in the same day
     *
     * @param time1 long (time in ms)
     * @param time2 long (time in ms )
     * @return boolean representing whether those two time stamps exist in the same day
     */
    public static boolean areInTheSameDay(long time1, long time2) {
        Date date1 = new Date(time1);
        Date date2 = new Date(time2);
        // If the both dates has the same date, month and year we assume that
        return date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth() && date1.getYear() == date2.getYear();
    }

    /**
     * This function returns the timestamp of 0:00 of of that day
     *
     * @param time timestamp to get baseline timestamp for
     * @return returns the the midnight time stamp of that day
     */
    public static long getMidNightTimeStamp(long time) {
        Date date = new Date(time);
        return new Date(date.getYear(), date.getMonth(), date.getDate()).getTime();
    }


    public static boolean isInThisYear(long time) {
        Date compare = new Date(time);
        Date now = new Date(System.currentTimeMillis());
        return compare.getYear() == now.getYear();
    }

    /**
     * @param context
     * @return
     */
    public static DisplayMetrics getDeviceMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    /**
     * Thread safe Toast
     *
     * @param ctx
     * @param message
     */
    public static void showToast(final Context ctx, final CharSequence message) {
        if (ctx == null) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * An Alert Dialog for Error Message
     *
     * @param context
     * @param title
     * @param message
     */
    public static void AlertDlgMessage(Context context, String title, String message) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> AlertDlgMessage(context, title, message));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, Color.WHITE);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_error_24dp)// this Icon should be an alert icon
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.close), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    /**
     * @param rssDatabase
     */
    public static void getSubscriptionFavIcon(PaperDatabase rssDatabase) {
        for (SubscriptionEntity subscriptionEntity : rssDatabase.dao().getAllSubscriptions()) {
            if (subscriptionEntity.iconUrl == null) {
                String favIconUrl = FavIconFetcher.getFavIconUrl(subscriptionEntity.siteLink);

                if (favIconUrl != null && !favIconUrl.isEmpty()) {
                    Log.w(TAG, String.format("onNext: favicon url is %s", favIconUrl));
                    subscriptionEntity.iconUrl = favIconUrl;
                }

                rssDatabase.dao().updateSubscription(subscriptionEntity);
            }
        }
    }
}



