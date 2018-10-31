package com.biniam.rss.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by biniam Haddish on 7/7/17.
 * This Class will check if whether or not the device is Connected to internet and if so
 * it will let us know what type of connection that it is having
 * <p>
 * : I have changed some stuff here. We should use  NetworkInfo.isConnected() instead of
 * NetworkInfo.isConnectedOrConnecting(). I also couldn't found a method explicitly indicating whether we are
 * in WiFi.
 */

public class ConnectivityState extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivityReceiverListener;
    public static NetworkInfo activeNetwork;

    public ConnectivityState() {
        super();
    }

    /**
     * This method indicates whether we have a functioning data connection regardless
     * of the kind of the network the device is on
     *
     * @return boolean whether active data connection exists
     */
    public static boolean hasDataConnection() {
        ConnectivityManager cm = (ConnectivityManager) PaperApp.getInstance()
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnected()
                && activeNetwork.isAvailable();
    }


    /**
     * This method returns true if we the device is on Wi-Fi and has a functioning data connection
     * @return whether we are on Wi-Fi and it's has internet
     */
    public static boolean isOnWiFi() {
        ConnectivityManager cm = (ConnectivityManager) PaperApp.getInstance()
                .getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected()
                && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Will tell if the connections is mobile data and it's active
     *
     * @return whether we have a functioning mobile data connection
     */
    public static boolean hasMobileDataConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) PaperApp.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connMgr.getActiveNetworkInfo();

        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting()
                && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    /**
     * Interface for informing other components about connectivity changes
     */
    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
