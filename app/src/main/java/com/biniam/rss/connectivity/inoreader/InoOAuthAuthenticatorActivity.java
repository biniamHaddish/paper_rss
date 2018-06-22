package com.biniam.rss.connectivity.inoreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.biniam.rss.R;
import com.biniam.rss.models.inoreader.InoReaderAuthItems;
import com.biniam.rss.persistence.preferences.InoReaderAccountPreferences;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.ui.base.HomeActivity;
import com.biniam.rss.utils.ConnectivityState;
import com.biniam.rss.utils.Constants;
import com.google.gson.Gson;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by biniam on 3/18/17.
 */

public class InoOAuthAuthenticatorActivity extends AppCompatActivity implements InoReaderAccountPreferences.PreferenceChangeListener, ConnectivityState.ConnectivityReceiverListener {

    public static final String TAG = InoOAuthAuthenticatorActivity.class.getSimpleName();
    private static final String RDIRECT_URL = "readably://oauth";
    private static String OPTIONAL_SCOPES = "read write";
    private final Gson gson = new Gson();
    public InoReaderAccountPreferences inoReaderAccountPreferences;
    // lib references
    private View mProgressView;
    private Context context;
    private Intent mIntent;
    private OkHttpClient client = new OkHttpClient();
    private InoReaderConstants inoReaderConstants;
    private InternalStatePrefs internalStatePrefs;
    private InoAccountAuthenticator inoAccountAuthenticator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_auth);

        context = this;
        inoReaderConstants = new InoReaderConstants();
        inoAccountAuthenticator = InoAccountAuthenticator.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
        mIntent = getIntent();
        mProgressView = findViewById(R.id.login_progress);
        this.inoReaderAccountPreferences = new InoReaderAccountPreferences(getApplicationContext());
        this.inoReaderAccountPreferences.setPreferenceChangeListener(this);

        getUserAuthData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inoReaderAccountPreferences.unregisterPreferenceChangeListeners();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        this.inoReaderAccountPreferences = new InoReaderAccountPreferences(getApplicationContext());
        this.inoReaderAccountPreferences.setPreferenceChangeListener(this);

        if (ConnectivityState.hasDataConnection()) {
            getUserAuthData();
        }
        super.onResume();
    }


    /**
     * @return InoReaderAuthItems
     * @throws IOException
     */
    @NonNull
    public InoReaderAuthItems getInoReaderTokens() {

        InoReaderAuthItems inoReaderAuthItems = null;
        Response response = null;
        final Uri uri = mIntent.getData();
        Log.d("UriData Code:\t", uri.getQueryParameter("code"));
        if (uri != null && (uri.toString().startsWith(RDIRECT_URL)))

            if (uri.getQueryParameter("state").matches(Constants.CSRF_STRING)) {

                final FormBody formBody = new FormBody.Builder()
                        .addEncoded("code", uri.getQueryParameter("code"))
                        .addEncoded("redirect_uri", RDIRECT_URL)
                        .addEncoded("client_id", Constants.InoReader_APP_ID)
                        .addEncoded("client_secret", Constants.CLIENT_SECRET)
                        .addEncoded("scope", OPTIONAL_SCOPES)
                        .addEncoded("grant_type", "authorization_code")
                        .build();

                Log.d("Tokens", "post data: " + formBody.toString());
                Request request = new Request.Builder()
                        .header("content-type", "application/x-www-form-urlencoded")
                        .url("https://www.inoreader.com/oauth2/token")
                        .post(formBody)
                        .build();
                try {
                    response = client.newCall(request).execute();
                    if (response != null && response.code() == 200) {
                        String tokens = response.body().string();
                        Log.d("Tokens", "post data: " + tokens.toString());
                        inoReaderAuthItems = gson.fromJson(tokens, InoReaderAuthItems.class);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("AccessToken", "Error=>\t" + e.getMessage());
                } finally {
                    if (response != null)
                        response.body().close();
                }

            } else {
                restartOAuthentication(true);
            }

        return inoReaderAuthItems;
    }

    /**
     *
     * @return
     */
    private Observable<InoReaderAuthItems> getObservable() {
        return Observable.create(e -> {
            if (!e.isDisposed()) {
                e.onNext(getInoReaderTokens());
                e.onComplete();
            }
        });
    }

    private Observer<InoReaderAuthItems> getObserver() {
        return new Observer<InoReaderAuthItems>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(InoReaderAuthItems authItems) {
                Log.d(TAG, "onNext: started");
                // getting up the data from InoReader Service
                inoReaderAccountPreferences.accessToken = authItems.getAccess_token();
                inoReaderAccountPreferences.refreshToken = authItems.getRefresh_token();
                inoReaderAccountPreferences.tokenType = authItems.getToken_type();
                inoReaderAccountPreferences.expireTime = authItems.getExpires_in();
                inoReaderAccountPreferences.isAuthenticated = true;

                // setting  up the data from InoReader Service to preference file
                inoReaderAccountPreferences.setString(InoReaderConstants.ACCESS_TOKEN, authItems.getAccess_token());
                inoReaderAccountPreferences.setString(InoReaderConstants.REFRESH_TOKEN, authItems.getRefresh_token());
                inoReaderAccountPreferences.setString(InoReaderConstants.TOKEN_TYPE, authItems.getToken_type());
                inoReaderAccountPreferences.setInt(InoReaderConstants.EXPIRES_IN, authItems.getExpires_in() + (System.currentTimeMillis() / 1000));
                inoReaderAccountPreferences.setBoolean(InoReaderConstants.IS_AUTHENTICATED, true);

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "onError: " + e.getMessage());
                restartOAuthentication(true);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, " onComplete");
                internalStatePrefs.setIntPref(InternalStatePrefs.ACCOUNT_SELECTED_PREF_KEY, InternalStatePrefs.INOREADER_ACCOUNT);
                Intent intent = new Intent(InoOAuthAuthenticatorActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
    }

    /**
     *
     */
    private void getUserAuthData() {
        getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(inoReaderAuthItems -> inoReaderAuthItems != null)
                .map(inoReaderAuthItems -> inoReaderAuthItems).subscribe(getObserver());
    }

    /**
     * @param isError
     */
    private void restartOAuthentication(Boolean isError) {
        if (isError) {
            Snackbar.make(
                    mProgressView,
                    "Connection error reconnecting.... please wait.",
                    Snackbar.LENGTH_LONG)
                    .show();
            Log.d(TAG, "restart OAuthentication: " + "Connection Error reconnecting!!!? ");
        }
    }
    @Override
    public void onPrefernceChanged(SharedPreferences sharedPreferences, String str) {
        // Track the preference Changes here ...
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) ;
    }
}
