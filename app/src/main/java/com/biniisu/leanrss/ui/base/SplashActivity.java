package com.biniisu.leanrss.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InternalStatePrefs internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());

        if (internalStatePrefs.isBetaExpired) {
            startActivity(new Intent(this, BetaExpiredActivity.class));
        } else if (internalStatePrefs.currentAccount == InternalStatePrefs.NO_ACCOUNT_CHOSEN) {
            startActivity(new Intent(this, SelectFeedServiceActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }

        finish();
    }

}
