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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-19.
 * A dialog that takes in a nation or region name, lets the user select the type, then launches
 * the appropriate explore activity.
 */
public class ExploreDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_explore_dialog";

    public static final int NO_SEARCH_TYPE_OVERRIDE = -1;

    private AppCompatEditText exploreSearch;
    private RadioGroup exploreToggleState;

    private Activity activityCloseOnFinish;
    private String searchContentOverride;
    private int searchTypeOverride = NO_SEARCH_TYPE_OVERRIDE;

    public ExploreDialog() {
    }

    public void setActivityCloseOnFinish(Activity closeOnFinish) {
        activityCloseOnFinish = closeOnFinish;
    }

    public void setSearchOverride(String content, int type) {
        searchContentOverride = content;
        searchTypeOverride = type;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_explore_dialog, null);

        exploreSearch = dialogView.findViewById(R.id.explore_searchbar);
        exploreToggleState = dialogView.findViewById(R.id.explore_radio_group);

        if (searchContentOverride != null) {
            exploreSearch.setText(searchContentOverride);
        }
        if (searchTypeOverride != NO_SEARCH_TYPE_OVERRIDE) {
            switch (searchTypeOverride) {
                case ExploreActivity.EXPLORE_NATION:
                    exploreToggleState.check(R.id.explore_radio_nation);
                    break;
                case ExploreActivity.EXPLORE_REGION:
                    exploreToggleState.check(R.id.explore_radio_region);
                    break;
            }
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startExploreActivity();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),
                RaraHelper.getThemeMaterialDialog(getContext()));
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

    private void startExploreActivity() {
        int mode;

        if (exploreToggleState.getCheckedRadioButtonId() == R.id.explore_radio_nation) {
            mode = ExploreActivity.EXPLORE_NATION;
        } else {
            mode = ExploreActivity.EXPLORE_REGION;
        }

        String name = exploreSearch.getText().toString();
        SparkleHelper.startExploring(getContext(), name, mode);

        if (activityCloseOnFinish != null) {
            activityCloseOnFinish.finish();
        }
    }
}
