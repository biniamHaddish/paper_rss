package com.biniam.rss.ui.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

import com.biniam.rss.R;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.utils.ReadablyApp;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class MarkAsReadConfirmationDialog extends DialogFragment {
    public static final String TAG = MarkAsReadConfirmationDialog.class.getSimpleName();
    public static final String IS_ALL_SUB_EXTRA = "IS_ALL_SUB_EXTRA";
    public static final String TAG_EXTRA = "TAG_EXTRA";
    public static final String SUB_ID_EXTRA = "SUB_ID_EXTRA";
    public static final String SUB_NAME_EXTRA = "SUB_NAME_EXTRA";
    private InternalStatePrefs internalStatePrefs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        boolean isAllSubscriptions = getArguments().getBoolean(IS_ALL_SUB_EXTRA);
        String tag = getArguments().getString(TAG_EXTRA);
        String subscriptionName = getArguments().getString(SUB_NAME_EXTRA);
        String subscriptionId = getArguments().getString(SUB_ID_EXTRA);
        internalStatePrefs = InternalStatePrefs.getInstance(ReadablyApp.getInstance());//singleton

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getTheme());
        dialogBuilder.setTitle(getString(R.string.mark_all_as_read));

        if (isAllSubscriptions) {
            dialogBuilder.setMessage(getString(R.string.mark_as_read_all_confirmation_msg));
        } else if (tag != null) {
            dialogBuilder.setMessage(String.format(getString(R.string.mark_as_read_confirmation_msg), tag));
        } else if (subscriptionId != null) {
            dialogBuilder.setMessage(String.format(getString(R.string.mark_as_read_confirmation_msg), subscriptionName));
        }

        dialogBuilder.setNegativeButton(getString(R.string.no), null);
        dialogBuilder.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

            ReadablyDatabase readablyDatabase = ReadablyApp.getInstance().getDatabase();
            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    if (isAllSubscriptions) {
                        readablyDatabase.dao().markEverythingAsRead(System.currentTimeMillis());

                    } else if (tag != null) {
                        for (String subscriptionId : readablyDatabase.dao().getSubscriptionIdsForTag(tag)) {
                            readablyDatabase.dao().markSubscriptionRead(subscriptionId, System.currentTimeMillis());
                        }
                    } else if (subscriptionId != null) {
                        readablyDatabase.dao().markSubscriptionRead(subscriptionId, System.currentTimeMillis());
                    }
                    dismiss();
                }
            }.subscribeOn(Schedulers.io()).subscribeWith(new DisposableCompletableObserver() {
                @Override
                public void onComplete() {
                    Log.d(TAG, "onComplete: ");
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Log.d(TAG, "onError: " + e.getMessage());
                }

            });
        });
        return dialogBuilder.create();

    }
}