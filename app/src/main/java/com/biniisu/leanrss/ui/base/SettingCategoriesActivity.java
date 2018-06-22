package com.biniisu.leanrss.ui.base;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.utils.AccountBroker;
import com.biniisu.leanrss.utils.ReadablyApp;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by biniam on 7/22/17.
 */

public class SettingCategoriesActivity extends AppCompatActivity {

    public InternalStatePrefs internalStatePrefs;
    public AccountBroker accountBroker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_layout);

        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
        accountBroker = AccountBroker.getInstance(getApplicationContext());


        RelativeLayout readingPrefs = findViewById(R.id.reading);
        readingPrefs.setOnClickListener(view -> {
            Intent intent = new Intent(SettingCategoriesActivity.this, SettingsSubCategoryActivity.class);
            intent.putExtra(SettingsSubCategoryActivity.PREF_CAT_EXTRA, 0);
            startActivity(intent);
        });


        RelativeLayout syncPrefs = findViewById(R.id.sync);
        syncPrefs.setOnClickListener(view -> {
            Intent intent = new Intent(SettingCategoriesActivity.this, SettingsSubCategoryActivity.class);
            intent.putExtra(SettingsSubCategoryActivity.PREF_CAT_EXTRA, 1);
            startActivity(intent);
        });

        RelativeLayout aboutSection = findViewById(R.id.about);
        aboutSection.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AboutActivity.class)));

        RelativeLayout logout = findViewById(R.id.logOut);
        logout.setOnClickListener(view -> new LogOutFromServiceDialog().show(getFragmentManager(), null));

    }


    public static class LogOutFromServiceDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            InternalStatePrefs internalStatePrefs = ((SettingCategoriesActivity) getActivity()).internalStatePrefs;
            AccountBroker accountBroker = ((SettingCategoriesActivity) getActivity()).accountBroker;

            File cacheDir = getActivity().getCacheDir();


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(String.format(getString(R.string.logout_from_account_title), getString(accountBroker.getAccountNameRes())));
            builder.setMessage(getText(R.string.logout_description));
            builder.setPositiveButton(getString(R.string.logout), (dialogInterface, i) -> {

                accountBroker.cancelAccountJob();

                ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);

                if (activityManager != null) {
                    if (activityManager.clearApplicationUserData()) {
                        Toast.makeText(getActivity(), "app data cleared", Toast.LENGTH_SHORT).show();
                    }
                }


                //internalStatePrefs.setIntPref(InternalStatePrefs.ACCOUNT_SELECTED_PREF_KEY, InternalStatePrefs.NO_ACCOUNT_CHOSEN);


//                    Intent intent = new Intent(getActivity(), SplashActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);


                new Observable<Void>() {
                    @Override
                    protected void subscribeActual(Observer<? super Void> observer) {
                        ReadablyDatabase readablyDatabase = ReadablyApp.getInstance().getDatabase();
                        readablyDatabase.dao().deleteAllFeedItems();
                        readablyDatabase.dao().deleteAllSubscriptions();
                        readablyDatabase.dao().deleteAllTags();

                        // Delete all caches
                        File[] caches = cacheDir.listFiles();


//                            for (File cache: caches){
//                                if (cache.isDirectory()) {
//                                    File[] cacheDirFiles = cache.listFiles();
//                                    for (File cacheDirFIle : cacheDirFiles){
//                                        Log.d(SettingCategoriesActivity.class.getSimpleName(), String.format("subscribeActual: deleting %s", cacheDirFIle.getName()));
//                                        cacheDirFIle.delete();
//                                    }
//                                }
//                            }

                        observer.onComplete();
                    }
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e("Logout", "deleting database and cache");

                    }
                });
            });


            builder.setNegativeButton(getString(android.R.string.cancel), null);

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }
}
