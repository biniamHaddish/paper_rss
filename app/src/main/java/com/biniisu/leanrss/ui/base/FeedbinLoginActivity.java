package com.biniisu.leanrss.ui.base;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.connectivity.feedbin.feedbinUtils.FeedbinUrls;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.utils.ConnectivityState;
import com.biniisu.leanrss.utils.Constants;
import com.biniisu.leanrss.utils.SecureAccountUtil.RxSecureStorage;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FeedbinLoginActivity extends AppCompatActivity {

    private static final String TAG = FeedbinLoginActivity.class.getSimpleName();

    private RelativeLayout loginContainer;
    private ProgressBar loginProgress;
    private Button signInToFeedBinButton;
    private Button visitFeedbinSite;
    private AppCompatEditText feedBinEmailAppCompatEditText;
    private AppCompatEditText feedBinPasswordAppCompatEditText;
    private InternalStatePrefs internalStatePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbin_login);

        loginContainer = findViewById(R.id.loginContainer);
        loginProgress = findViewById(R.id.loginProgress);
        signInToFeedBinButton = findViewById(R.id.loginToFeedBinButton);
        visitFeedbinSite = findViewById(R.id.visiFeedbinSite);
        feedBinEmailAppCompatEditText = findViewById(R.id.feedBinEmailEditText);
        feedBinPasswordAppCompatEditText = findViewById(R.id.feedBinPasswordEditText);
        internalStatePrefs = InternalStatePrefs.getInstance(this);

        SpannableString content = new SpannableString(getText(R.string.feedbin_dot_com));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        visitFeedbinSite.setText(content);

        visitFeedbinSite.setOnClickListener(view -> {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse(getString(R.string.feedbin_url)));
            startActivity(viewIntent);
        });

        //Login click event
        signInToFeedBinButton.setOnClickListener(view -> {
            String email = feedBinEmailAppCompatEditText.getText().toString();
            String password = feedBinPasswordAppCompatEditText.getText().toString();

            if (email.isEmpty()) {
                feedBinEmailAppCompatEditText.requestFocus();
            } else if (password.isEmpty()) {
                feedBinPasswordAppCompatEditText.requestFocus();
            } else if (!email.isEmpty() && !password.isEmpty()) {
                if (ConnectivityState.hasDataConnection()) {
                    showProgress();
                    new EncryptLoginCredentials().execute(email.trim(), password.trim());
                    return;
                } else {
                    showSnackBarMessage(R.string.no_connection);
                }
            }
        });
    }

    /**
     * @param username
     * @param password
     */
    public void processLoginData(String username, String password) {
        if (username != null && password != null) {
            RxSecureStorage rxSecureStorage = RxSecureStorage.create(this, getPackageName());

            rxSecureStorage.putString(Constants.ENCRYPTED_KEY, username)
                    .observeOn(Schedulers.single())
                    .subscribe();

            rxSecureStorage.putString(Constants.ENCRYPTED_KEY_PASS, password)
                    .observeOn(Schedulers.single())
                    .subscribe();


            internalStatePrefs.setIntPref(InternalStatePrefs.ACCOUNT_SELECTED_PREF_KEY, InternalStatePrefs.FEED_BIN_ACCOUNT);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void showProgress() {
        loginContainer.setVisibility(View.GONE);
        loginProgress.setVisibility(View.VISIBLE);
        visitFeedbinSite.setVisibility(View.GONE);
        findViewById(R.id.snackBarAnchor).setVisibility(View.GONE);
    }

    private void hideProgress() {
        loginContainer.setVisibility(View.VISIBLE);
        loginProgress.setVisibility(View.GONE);
        visitFeedbinSite.setVisibility(View.VISIBLE);
        findViewById(R.id.snackBarAnchor).setVisibility(View.VISIBLE);
    }

    private void showSnackBarMessage(int messageRes) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackBarAnchor), messageRes, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * Private Class for login that extends AsyncTask
     */
    private class EncryptLoginCredentials extends AsyncTask<String, String, String> {

        EncryptLoginCredentials() {

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];


            OkHttpClient okHttpClient = new OkHttpClient();

            Request authenticationRequest = new Request.Builder()
                    .url(FeedbinUrls.getAuthentication_URL())
                    .addHeader("Authorization", Credentials.basic(username, password))
                    .addHeader("Content-Type", "application/json")
                    .build();

            Log.d(TAG, String.format("doInBackground: url is %s", authenticationRequest.url().toString()));

            try {
                okhttp3.Response response = okHttpClient.newCall(authenticationRequest).execute();
                if (response.isSuccessful() && response.code() == HttpURLConnection.HTTP_OK) {
                    processLoginData(username, password);
                }

                publishProgress(String.valueOf(response.code()));
            } catch (IOException e) {
                Log.e(TAG, String.format("onFailure: error loggin-in to feedbin reason: %s", e.getMessage()));
                publishProgress("");
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d(TAG, String.format("onProgressUpdate: code is %s", values[0]));
            if (values[0].equals(String.valueOf(HttpURLConnection.HTTP_UNAUTHORIZED))) {
                hideProgress();
                showSnackBarMessage(R.string.pass_uname_err);
            } else {
                hideProgress();
                showSnackBarMessage(R.string.conn_err);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
