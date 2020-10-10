/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.feed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-02-08.
 * This shows a dialog allowing users to modify their activity feed subscriptions.
 */
public class SubscriptionsDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_subscriptions_dialog";
    public static final String CURRENT_NATION = "subs_curnation";
    public static final String SWITCH_NATIONS = "subs_switch";
    public static final String DOSSIER_NATIONS = "subs_dossier_n";
    public static final String CURRENT_REGION = "subs_curregion";
    public static final String DOSSIER_REGIONS = "subs_dossier_r";
    public static final String WORLD_ASSEMBLY = "subs_wa";

    private SharedPreferences storage; // shared preferences
    private ActivityFeedFragment callback;

    private CheckBox curNation;
    private CheckBox switchNations;
    private CheckBox dossierNations;
    private CheckBox curRegion;
    private CheckBox dossierRegions;
    private CheckBox assembly;

    public SubscriptionsDialog() {
    }

    public void setCallback(ActivityFeedFragment c) {
        callback = c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_subscriptions_dialog, null);

        curNation = dialogView.findViewById(R.id.subscriptions_curnation);
        curNation.setChecked(storage.getBoolean(CURRENT_NATION, true));
        switchNations = dialogView.findViewById(R.id.subscriptions_switch);
        switchNations.setChecked(storage.getBoolean(SWITCH_NATIONS, true));
        dossierNations = dialogView.findViewById(R.id.subscriptions_dossier_n);
        dossierNations.setChecked(storage.getBoolean(DOSSIER_NATIONS, true));
        curRegion = dialogView.findViewById(R.id.subscriptions_region);
        curRegion.setChecked(storage.getBoolean(CURRENT_REGION, true));
        dossierRegions = dialogView.findViewById(R.id.subscriptions_dossier_r);
        dossierRegions.setChecked(storage.getBoolean(DOSSIER_REGIONS, true));
        assembly = dialogView.findViewById(R.id.subscriptions_wa);
        assembly.setChecked(storage.getBoolean(WORLD_ASSEMBLY, true));

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = storage.edit();
                editor.putBoolean(CURRENT_NATION, curNation.isChecked());
                editor.putBoolean(SWITCH_NATIONS, switchNations.isChecked());
                editor.putBoolean(DOSSIER_NATIONS, dossierNations.isChecked());
                editor.putBoolean(CURRENT_REGION, curRegion.isChecked());
                editor.putBoolean(DOSSIER_REGIONS, dossierRegions.isChecked());
                editor.putBoolean(WORLD_ASSEMBLY, assembly.isChecked());
                editor.apply();
                if (callback != null) {
                    callback.startQueryHappenings();
                }
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),
                RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.activityfeed_subscriptions)
                .setView(dialogView)
                .setPositiveButton(R.string.update, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }
}
