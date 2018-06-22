package com.biniisu.leanrss.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.widget.Button;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.connectivity.feedWangler.retrofit.FeedWranglerAPI;
import com.biniisu.leanrss.connectivity.feedWangler.retrofit.FeedWranglerClient;
import com.biniisu.leanrss.models.feedWragler.FeedWranglerAccess;
import com.biniisu.leanrss.utils.Constants;
import com.biniisu.leanrss.utils.PreferencesUtil;
import com.biniisu.leanrss.utils.ReadablyApp;
import com.biniisu.leanrss.utils.Utils;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ComputationScheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Biniam_Haddish  on 12/23/17.
 */

public class FeedWranglerLogin extends AppCompatActivity {

    public static final String TAG = FeedWranglerLogin.class.getSimpleName();
    private Button feedWranglerLoginbtn;
    private PreferencesUtil preferencesUtil;
    private AppCompatEditText feedWrnglerBinEmailAppCompatEditText;
    private AppCompatEditText feedwranglerBinPasswordAppCompatEditText;
    private Context context;

    /**
     * @param email
     * @param password
     * @param client_key
     */
    private static void login(String email, String password, String client_key) {
        FeedWranglerClient.getRetrofitLogin()
                .create(FeedWranglerAPI.class)
                .login(email, password, client_key)
                .subscribeOn(Schedulers.io())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<FeedWranglerAccess>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(FeedWranglerAccess feedWranglerAccess) {
                        if (feedWranglerAccess.getAccess_token() != null) {
//                            SecureAccountManager secureAccountManager = new SecureAccountManager.Builder(ReadablyApp.getInstance()).build();
//                            secureAccountManager.obfuscateAcessToken(feedWranglerAccess.getAccess_token())
//                                    .subscribe(new Consumer<String>() {
//                                        @Override
//                                        public void accept(String s) throws Exception {
//
//                                        }
//                                    });
//                            Utils.showToast(ReadablyApp.getInstance(), feedWranglerAccess.getAccess_token());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: Login" + e.getMessage());
                        Utils.showToast(ReadablyApp.getInstance(), "Login Failed please try later." + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: login ");
                    }
                });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_wrangler_login);
        // Init views
        context = this;
        this.preferencesUtil = new PreferencesUtil(context);
        feedWranglerLoginbtn = findViewById(R.id.loginToFeedWranglerButton);
        feedWrnglerBinEmailAppCompatEditText = findViewById(R.id.feedWranglerEmailEditText);
        feedwranglerBinPasswordAppCompatEditText = findViewById(R.id.feedwranglerPasswordEditText);
        //login button click event
        feedWranglerLoginbtn.setOnClickListener(view -> {
            Completable completable = Completable
                    .fromRunnable(() -> {
                        String email = String.valueOf(feedWrnglerBinEmailAppCompatEditText.getText().toString().trim());
                        String pass = String.valueOf(feedwranglerBinPasswordAppCompatEditText.getText());
                        String client_key = Constants.FEEDWRANGLER_CLIENT_KEY;
                        if (!email.isEmpty() && !pass.isEmpty() && !client_key.isEmpty()) {
                            try {
                                FeedWranglerLogin.this.processLogin(email, pass, client_key);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            completable
                    //run on Background thread
                    .subscribeOn(Schedulers.io())
                    .observeOn(new ComputationScheduler())
                    .subscribe(getCompletableObserver());
        });
    }

    /**
     * --------------Observer ---------
     *
     * @return
     */
    private CompletableObserver getCompletableObserver() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                if (d.isDisposed()) return;
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "onError: " + e.getMessage());
            }
        };
    }

    /**
     *
     */
    private void processLogin(String email, String password, String clientKey) throws IOException {
        login(email, password, clientKey);// login here.
    }

}
