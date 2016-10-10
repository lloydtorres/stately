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

package com.lloydtorres.stately.explore;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-19.
 * A dialog that takes in a nation or region name, lets the user select the type, then launches
 * the appropriate explore activity.
 */
public class ExploreDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_explore_dialog";

    private AppCompatEditText exploreSearch;
    private RadioGroup exploreToggleState;
    private Activity activityCloseOnFinish;

    public ExploreDialog() { }

    public void setActivityCloseOnFinish(Activity closeOnFinish) {
        activityCloseOnFinish = closeOnFinish;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_explore_dialog, null);

        exploreSearch = (AppCompatEditText) dialogView.findViewById(R.id.explore_searchbar);
        exploreToggleState = (RadioGroup) dialogView.findViewById(R.id.explore_radio_group);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startExploreActivity();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.menu_explore)
                .setView(dialogView)
                .setPositiveButton(R.string.explore_positive, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        // Get focus on edit text and open keyboard
        exploreSearch.requestFocus();
        Dialog d = dialogBuilder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;
    }

    private void startExploreActivity()
    {
        int mode;

        switch (exploreToggleState.getCheckedRadioButtonId())
        {
            case R.id.explore_radio_nation:
                mode = ExploreActivity.EXPLORE_NATION;
                break;
            default:
                mode = ExploreActivity.EXPLORE_REGION;
                break;
        }

        String name = exploreSearch.getText().toString();
        SparkleHelper.startExploring(getContext(), name, mode);

        if (activityCloseOnFinish != null) {
            activityCloseOnFinish.finish();
        }
    }
}
