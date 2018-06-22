package com.biniisu.leanrss.ui.base;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;

/**
 * Created by  on 3/9/17.
 */

public class UnsubscribeDialog extends DialogFragment {

    public static final String SUBSCRIPTION = "SUBSCRIPTION_KEY";
    private UnsubscribeListener unsubscribeListener;

    public void setUnsubscribeListener(UnsubscribeListener unsubscribeListener) {
        this.unsubscribeListener = unsubscribeListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (!getArguments().containsKey(SUBSCRIPTION)) {
            throw new IllegalArgumentException(String.format("You can't call this dialog without %s extra", SUBSCRIPTION));
        }

        final SubscriptionEntity subscriptionEntity = (SubscriptionEntity) getArguments().getSerializable(SUBSCRIPTION);

        if (subscriptionEntity == null) {
            throw new IllegalArgumentException("A null subscriptionEntity argument");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.unsubscribe);
        builder.setMessage(String.format(getString(R.string.unsubscribe_dialog_message), subscriptionEntity.title));
        builder.setPositiveButton(getString(R.string.unsubscribe), (dialogInterface, i) -> {
            if (unsubscribeListener != null) {
                unsubscribeListener.onUnsubscribed();
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public interface UnsubscribeListener {
        void onUnsubscribed();
    }
}
