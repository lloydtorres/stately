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

package com.lloydtorres.stately.wa;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-02-02.
 * This dialog displays voting options for a WA resolution.
 */
public class VoteDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_vote_dialog";
    public static final int VOTE_FOR = 0;
    public static final int VOTE_AGAINST = 1;
    public static final int VOTE_UNDECIDED = 2;

    private RadioGroup voteToggleState;
    private int choice;

    public VoteDialog() { }

    public void setChoice(int c)
    {
        choice = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_vote_dialog, null);

        voteToggleState = (RadioGroup) dialogView.findViewById(R.id.vote_radio_group);

        switch(choice)
        {
            case VOTE_FOR:
                voteToggleState.check(R.id.vote_radio_for);
                break;
            case VOTE_AGAINST:
                voteToggleState.check(R.id.vote_radio_against);
                break;
            default:
                voteToggleState.check(R.id.vote_radio_undecided);
                break;
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitVote();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MaterialDialog);
        dialogBuilder.setTitle(R.string.wa_vote_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.wa_vote_dialog_submit, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }

    /**
     * Calls ResolutionActivity's own submitVote logic.
     */
    private void submitVote()
    {
        int mode;
        switch (voteToggleState.getCheckedRadioButtonId())
        {
            case R.id.vote_radio_for:
                mode = VOTE_FOR;
                break;
            case R.id.vote_radio_against:
                mode = VOTE_AGAINST;
                break;
            default:
                mode = VOTE_UNDECIDED;
                break;
        }

        if (mode != choice)
        {
            ((ResolutionActivity) getActivity()).submitVote(mode);
        }
    }
}
