package com.biniisu.leanrss.connectivity.inoreader.browserUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.biniisu.leanrss.R;

/**
 * Created by biniam on 10/26/17.
 */

public class InoBrowserUtil {

    public static final String page_web_buildin = "page_web_buildin";
    public static final String TAG = InoBrowserUtil.class.getSimpleName();

    public static void openAppointedBrowser(Context context, String url, String... specifiedAppInfo) {
        try {
            if (isBlank(url)) {
                Log.d(TAG, "openAppointedBrowser: " + "No Url provided");
            }
            if (specifiedAppInfo == null || specifiedAppInfo.length <= 0 || specifiedAppInfo[0] == null) {
                openSystemDefaultBrowser(context, url);
                return;
            }
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            String[] cc = specifiedAppInfo[0].split("/");
            intent.setClassName(cc[0], cc[1]);
            context.startActivity(intent);
        } catch (Throwable e) {
            Log.d(TAG, "openAppointedBrowser: " + e.getMessage());
        }
    }

    public static void openSystemDefaultBrowser(Context activity, String url) {
        try {
            if (isBlank(url)) {
                Log.d(TAG, "openSystemDefaultBrowser: " + "no provided Url");
            }
            buildCustomTabsIntent(activity).launchUrl(activity, Uri.parse(url));
        } catch (Throwable e) {
            Log.d(TAG, "openSystemDefaultBrowser: " + e.getMessage());
        }
    }

    public static void openCustomTab(Context context, Uri uri) {
        String packageName = CustomTabsHelper.getPackageNameToUse(context);
        if (packageName == null) {
            openBuiltInBrowser(context, uri.toString());
            return;
        }
        CustomTabsIntent customTabsIntent = buildCustomTabsIntent(context);
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(context, uri);
    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    private static CustomTabsIntent buildCustomTabsIntent(Context context) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(R.color.colorPrimaryDark);
        builder.setSecondaryToolbarColor(R.color.colorPrimary);
        return builder.build();
    }

    private static void openBuiltInBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setClass(context, BrowserActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
        add(context, page_web_buildin);
    }

    public static boolean isBlank(String s) {
        if (s == null || s.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static void add(Context context, String eventId) {
        //   MobclickAgent.onEvent(context, eventId);
    }
}
