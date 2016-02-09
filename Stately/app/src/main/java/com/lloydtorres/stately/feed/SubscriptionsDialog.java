package com.lloydtorres.stately.feed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    public static final String CURRENT_NATION = "subs_curnation";
    public static final String SWITCH_NATIONS = "subs_switch";
    public static final String CURRENT_REGION = "subs_curregion";
    public static final String WORLD_ASSEMBLY = "subs_wa";

    private SharedPreferences storage; // shared preferences
    private ActivityFeedFragment callback;

    private CheckBox curNation;
    private CheckBox switchNations;
    private CheckBox curRegion;
    private CheckBox assembly;

    public SubscriptionsDialog() { }

    public void setCallback(ActivityFeedFragment c)
    {
        callback = c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        storage = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_subscriptions_dialog, null);

        curNation = (CheckBox) dialogView.findViewById(R.id.subscriptions_curnation);
        curNation.setChecked(storage.getBoolean(CURRENT_NATION, true));
        switchNations = (CheckBox) dialogView.findViewById(R.id.subscriptions_switch);
        switchNations.setChecked(storage.getBoolean(SWITCH_NATIONS, true));
        curRegion = (CheckBox) dialogView.findViewById(R.id.subscriptions_region);
        curRegion.setChecked(storage.getBoolean(CURRENT_REGION, true));
        assembly = (CheckBox) dialogView.findViewById(R.id.subscriptions_wa);
        assembly.setChecked(storage.getBoolean(WORLD_ASSEMBLY, true));

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = storage.edit();
                editor.putBoolean(CURRENT_NATION, curNation.isChecked());
                editor.putBoolean(SWITCH_NATIONS, switchNations.isChecked());
                editor.putBoolean(CURRENT_REGION, curRegion.isChecked());
                editor.putBoolean(WORLD_ASSEMBLY, assembly.isChecked());
                editor.commit();
                if (callback != null)
                {
                    callback.startQueryHappenings();
                }
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
