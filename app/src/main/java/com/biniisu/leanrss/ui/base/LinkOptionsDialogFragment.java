package com.biniisu.leanrss.ui.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.biniisu.leanrss.R;

/**
 * Created by biniam_Haddish on 8/2/17.
 * <p>
 * This class represents a dialog that appears when a link is long clicks and it presents options to
 * open the link, share the link or copy the link to clipboard
 */

public class LinkOptionsDialogFragment extends DialogFragment {
    public static final String LINK_URL = "LINK_URL";
    private LinkOptionsDialogCallback linkOptionsDialogCallback;

    public void setLinkOptionsDialogCallback(LinkOptionsDialogCallback linkOptionsDialogCallback) {
        this.linkOptionsDialogCallback = linkOptionsDialogCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String url = getArguments().getString(LINK_URL);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), getTheme());


        dialogBuilder.setItems(R.array.link_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        if (linkOptionsDialogCallback != null) {
                            linkOptionsDialogCallback.openLinkClicked(url);
                        }
                        break;
                    case 1:
                        if (linkOptionsDialogCallback != null) {
                            linkOptionsDialogCallback.shareLinkClicked(url);
                        }
                        break;
                    case 2:
                        linkOptionsDialogCallback.copyLinkClicked(url);
                        break;
                }
            }
        });

        dialogBuilder.setTitle(url);
        AlertDialog dialog = dialogBuilder.create();
        return dialog;
    }

    public interface LinkOptionsDialogCallback {
        void openLinkClicked(String url);

        void shareLinkClicked(String url);

        void copyLinkClicked(String url);
    }
}

