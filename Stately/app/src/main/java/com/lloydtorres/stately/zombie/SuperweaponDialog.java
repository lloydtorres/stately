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

package com.lloydtorres.stately.zombie;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.dto.ZSuperweaponStatus;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-10-26.
 * Dialog shown when choosing between different superweapons to deploy during Z-Day.
 */
public class SuperweaponDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_superweapon_dialog";

    private RadioGroup actionState;
    private ZSuperweaponStatus status;
    private ZombieChartCard zombieCard;

    public void setSuperweaponStatus(ZSuperweaponStatus s) {
        status = s;
    }

    public void setZombieCard(ZombieChartCard card) {
        zombieCard = card;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_superweapon_dialog, null);

        actionState = (RadioGroup) dialogView.findViewById(R.id.superweapon_group);

        // Show available options
        if (status != null) {
            actionState.findViewById(R.id.superweapon_tzes).setVisibility(status.isTZES ? View.VISIBLE : View.GONE);
            actionState.findViewById(R.id.superweapon_cure).setVisibility(status.isCure ? View.VISIBLE : View.GONE);
            actionState.findViewById(R.id.superweapon_horde).setVisibility(status.isHorde ? View.VISIBLE : View.GONE);
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitAction();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.zombie_button_missile)
                .setView(dialogView)
                .setPositiveButton(R.string.superweapon_deploy, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }

    /**
     * Calls ZombieControlActivity's own startSubmitAction logic.
     */
    private void submitAction() {
        String choice = null;
        switch (actionState.getCheckedRadioButtonId()) {
            case R.id.superweapon_tzes:
                choice = ZSuperweaponStatus.ZSUPER_TZES;
                break;
            case R.id.superweapon_cure:
                choice = ZSuperweaponStatus.ZSUPER_CURE;
                break;
            case R.id.superweapon_horde:
                choice = ZSuperweaponStatus.ZSUPER_HORDE;
                break;
        }

        if (choice != null && getActivity() != null && getActivity() instanceof ExploreActivity) {
            ((ExploreActivity) getActivity()).deployZSuperweapon(choice, zombieCard);
        }
    }
}
