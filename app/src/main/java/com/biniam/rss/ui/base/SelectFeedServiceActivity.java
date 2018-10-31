package com.biniam.rss.ui.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.biniam.rss.R;
import com.biniam.rss.connectivity.inoreader.InoAccountAuthenticator;
import com.biniam.rss.connectivity.inoreader.InoApiFactory;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.utils.PaperApp;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SelectFeedServiceActivity extends AppCompatActivity {

    public static final String TAG = SelectFeedServiceActivity.class.getSimpleName();

    //private Button noServiceSetup;
    private Button feedbinSetup;
    private Button loginWithInoReader;
    private InoAccountAuthenticator inoAccountAuthenticator;
    private InoApiFactory inoApiFactory;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_feed_source);
        //Inoreader OAuth Authenticator instance
        inoAccountAuthenticator = InoAccountAuthenticator.getInstance(getApplicationContext());
        inoApiFactory = InoApiFactory.getInstance(getApplicationContext());


       // feedbinSetup = findViewById(R.id.loginWithFeedbin);
        //noServiceSetup = findViewById(R.id.setupLocalFeedSourceButton);
        loginWithInoReader = findViewById(R.id.loginWithInoReader);

        // Make the local account button text underlined
        SpannableString content = new SpannableString(getText(R.string.choose_no_service));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
         //noServiceSetup.setText(content);

        // Launch the feed-bin login activity
       // feedbinSetup.setOnClickListener(view -> startActivity(new Intent(SelectFeedServiceActivity.this, FeedbinLoginActivity.class)));
        /*noServiceSetup.setOnClickListener(view -> {
            InternalStatePrefs internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
            internalStatePrefs.setIntPref(InternalStatePrefs.ACCOUNT_SELECTED_PREF_KEY, InternalStatePrefs.LOCAL_ACCOUNT);
            startActivity(new Intent(SelectFeedServiceActivity.this, HomeActivity.class));
        });*/

        // Oauth 2.0 login to inoReader
        loginWithInoReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //inoApiFactory.getSubscriptionList();// getting the subscription list in here.
                inoAccountAuthenticator.openWebPage(inoAccountAuthenticator.loginUrl());
            }
        });

        new Observable<Void>() {
            @Override
            protected void subscribeActual(Observer<? super Void> observer) {
                PaperDatabase rssDatabase = PaperApp.getInstance().getDatabase();
                rssDatabase.dao().deleteAllFeedItems();
                rssDatabase.dao().deleteAllSubscriptions();
                rssDatabase.dao().deleteAllTags();

                // Delete all caches
                File[] caches = getCacheDir().listFiles();
                for (File cache : caches) {
                    if (cache.isDirectory()) {
                        File[] cacheDirFiles = cache.listFiles();
                        for (File cacheDirFIle : cacheDirFiles) {
                            Log.d(SettingCategoriesActivity.class.getSimpleName(), String.format("subscribeActual: deleting %s", cacheDirFIle.getName()));
                            cacheDirFIle.delete();
                        }
                    }
                }
                observer.onComplete();
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        //void
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("Cleaning-up", "deleting database and cache");
                    }
                });
    }
}
