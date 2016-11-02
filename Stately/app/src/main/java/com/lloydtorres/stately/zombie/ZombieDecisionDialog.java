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
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-10-16.
 * Dialog used to choose between different options for Z-Day.
 */
public class ZombieDecisionDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_zombie_decision_dialog";

    private Zombie zombieData;
    private RadioGroup actionState;

    public ZombieDecisionDialog() { }

    public void setZombieData(Zombie z) { zombieData = z; }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_zombie_decision_dialog, null);

        actionState = (RadioGroup) dialogView.findViewById(R.id.zombie_decision_group);

        // Show available options
        if (zombieData != null) {
            if (zombieData.survivors > 0) {
                actionState.findViewById(R.id.zombie_action_military).setVisibility(View.VISIBLE);
                actionState.findViewById(R.id.zombie_action_cure).setVisibility(View.VISIBLE);
            }
            if (zombieData.zombies > 0) {
                actionState.findViewById(R.id.zombie_action_horde).setVisibility(View.VISIBLE);
            }
        }

        if (zombieData != null && zombieData.action != null) {
            switch(zombieData.action) {
                case Zombie.ZACTION_MILITARY:
                    actionState.check(R.id.zombie_action_military);
                    break;
                case Zombie.ZACTION_CURE:
                    actionState.check(R.id.zombie_action_cure);
                    break;
                case Zombie.ZACTION_ZOMBIE:
                    actionState.check(R.id.zombie_action_horde);
                    break;
            }
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitAction();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.zombie_control)
                .setView(dialogView)
                .setPositiveButton(R.string.zombie_action_do_button, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }

    /**
     * Calls ZombieControlActivity's own startSubmitAction logic.
     */
    private void submitAction() {
        String choice = null;
        switch (actionState.getCheckedRadioButtonId()) {
            case R.id.zombie_action_military:
                choice = Zombie.ZACTION_MILITARY;
                break;
            case R.id.zombie_action_cure:
                choice = Zombie.ZACTION_CURE;
                break;
            case R.id.zombie_action_horde:
                choice = Zombie.ZACTION_ZOMBIE;
                break;
        }

        if (zombieData != null && choice != null && !choice.equals(zombieData.action)) {
            ((ZombieControlActivity) getActivity()).startSubmitAction(choice);
        }
    }
}
