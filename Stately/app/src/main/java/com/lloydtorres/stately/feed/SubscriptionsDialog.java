package com.lloydtorres.stately.feed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-02-08.
 * This shows a dialog allowing users to modify their activity feed subscriptions.
 */
public class SubscriptionsDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_subscriptions_dialog";

    private CheckBox curNation;
    private CheckBox switchNations;
    private CheckBox curRegion;
    private CheckBox assembly;

    public SubscriptionsDialog() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_subscriptions_dialog, null);

        curNation = (CheckBox) dialogView.findViewById(R.id.subscriptions_curnation);
        switchNations = (CheckBox) dialogView.findViewById(R.id.subscriptions_switch);
        curRegion = (CheckBox) dialogView.findViewById(R.id.subscriptions_region);
        assembly = (CheckBox) dialogView.findViewById(R.id.subscriptions_wa);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // @TODO
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MaterialDialog);
        dialogBuilder.setTitle(R.string.activityfeed_subscriptions)
                .setView(dialogView)
                .setPositiveButton(R.string.update, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }
}
