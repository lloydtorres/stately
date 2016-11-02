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

package com.lloydtorres.stately.census;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-04-09.
 * A dialog that lets a user switch between different sorting modes.
 */
public class CensusSortDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_census_sort_dialog";
    public static final int CENSUS_MODE_NATION = 0;
    public static final int CENSUS_MODE_REGION = 1;

    private int mode;
    private RadioGroup sortOrderState;
    private RadioGroup directionState;
    private int sortOrderChoice;
    private boolean isAscending;
    private CensusRecyclerAdapter adapter;

    public CensusSortDialog() { }

    public void setMode(int m)
    {
        mode = m;
    }

    public void setSortOrder(int so)
    {
        sortOrderChoice = so;
    }

    public void setIsAscending(boolean ia)
    {
        isAscending = ia;
    }

    public void setAdapter(CensusRecyclerAdapter a)
    {
        adapter = a;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_census_sort_dialog, null);

        sortOrderState = (RadioGroup) dialogView.findViewById(R.id.census_sort_category_radio_group);

        if (mode == CENSUS_MODE_REGION)
        {
            sortOrderState.findViewById(R.id.census_sort_region_rank).setVisibility(View.GONE);
            sortOrderState.findViewById(R.id.census_sort_region_percent).setVisibility(View.GONE);
        }

        switch (sortOrderChoice)
        {
            case CensusRecyclerAdapter.SORT_MODE_SCORE:
                sortOrderState.check(R.id.census_sort_score);
                break;
            case CensusRecyclerAdapter.SORT_MODE_WORLD_RANK:
                sortOrderState.check(R.id.census_sort_world_rank);
                break;
            case CensusRecyclerAdapter.SORT_MODE_WORLD_PERCENT:
                sortOrderState.check(R.id.census_sort_world_percent);
                break;
            case CensusRecyclerAdapter.SORT_MODE_REGION_RANK:
                sortOrderState.check(R.id.census_sort_region_rank);
                break;
            case CensusRecyclerAdapter.SORT_MODE_REGION_PERCENT:
                sortOrderState.check(R.id.census_sort_region_percent);
                break;
        }

        directionState = (RadioGroup) dialogView.findViewById(R.id.census_sort_order_radio_group);
        directionState.check(isAscending ? R.id.census_sort_ascending : R.id.census_sort_descending);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sort();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.census_sort_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.census_sort_confirm, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }

    private void sort()
    {
        int newChoice = R.id.census_sort_score;
        switch (sortOrderState.getCheckedRadioButtonId())
        {
            case R.id.census_sort_score:
                newChoice = CensusRecyclerAdapter.SORT_MODE_SCORE;
                break;
            case R.id.census_sort_world_rank:
                newChoice = CensusRecyclerAdapter.SORT_MODE_WORLD_RANK;
                break;
            case R.id.census_sort_world_percent:
                newChoice = CensusRecyclerAdapter.SORT_MODE_WORLD_PERCENT;
                break;
            case R.id.census_sort_region_rank:
                newChoice = CensusRecyclerAdapter.SORT_MODE_REGION_RANK;
                break;
            case R.id.census_sort_region_percent:
                newChoice = CensusRecyclerAdapter.SORT_MODE_REGION_PERCENT;
                break;
        }

        boolean direction = directionState.getCheckedRadioButtonId() == R.id.census_sort_ascending;

        if (sortOrderChoice != newChoice || isAscending != direction)
        {
            adapter.sort(newChoice, direction);
        }
    }
}
