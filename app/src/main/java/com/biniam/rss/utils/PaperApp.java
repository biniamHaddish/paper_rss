package com.biniam.rss.utils;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.biniam.rss.persistence.db.PaperDatabase;


/**
 * Created by biniam on 7/7/17.
 *
 * Modified by biniam_Haddish Teweldeberhan on 22/7/17, to include the loading of html resources
 * to memory on startup to help launch feed articles faster
 *
 */

public class PaperApp extends Application {

    public static final String HTML_EXT = "html";
    public static final String UTF8_ENCODING = "UTF-8";
    public static final String APP_DB_NAME = "channel.db";
    public static final String TAG = PaperApp.class.getSimpleName();

    private static PaperApp mInstance;
    private PaperDatabase paperDatabase;

    public static synchronized PaperApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
/*
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

        mInstance = this;
        paperDatabase = Room.databaseBuilder(getApplicationContext(), PaperDatabase.class, APP_DB_NAME).build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * Any class in Lean Rss App has to implement this class inorder to get the Internet connectivity status
     * @param listener
     */
    public void setConnectivityListener(ConnectivityState.ConnectivityReceiverListener listener) {
        ConnectivityState.connectivityReceiverListener = listener;
    }

    /*This method returns a Room database for the rest of the app to use*/
    public PaperDatabase getDatabase() {
        return paperDatabase;
    }
}