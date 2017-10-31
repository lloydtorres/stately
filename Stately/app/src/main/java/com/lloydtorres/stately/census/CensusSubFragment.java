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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-04-09.
 * This fragment displays a nation or region's census scores and rankings, which are sortable
 * by the user. Takes in a list of census data as well as mode.
 */
public class CensusSubFragment extends RecyclerSubFragment {
    public static final String TARGET_KEY = "target";
    public static final String CENSUS_DATA_KEY = "censusData";
    public static final String MODE_KEY = "censusMode";

    private String target;
    private ArrayList<CensusDetailedRank> censusData;
    private int censusMode;

    public void setTarget(String t) { target = t; }

    public void setCensusData(ArrayList<CensusDetailedRank> c) {
        censusData = c;
    }

    public void setMode(int mode) { censusMode = mode; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore save state
        if (savedInstanceState != null) {
            censusMode = savedInstanceState.getInt(MODE_KEY);
            censusData = savedInstanceState.getParcelableArrayList(CENSUS_DATA_KEY);
            target = savedInstanceState.getString(TARGET_KEY);
        }

        if (censusData != null) {
            // Get rid of Z-Day data if needed
            if (!(NightmareHelper.getIsZDayActive(getContext()) && censusMode == CensusSortDialog.CENSUS_MODE_REGION)) {
                censusData = new ArrayList<CensusDetailedRank>(NightmareHelper.trimZDayCensusData(censusData));
            }

            if (mRecyclerAdapter == null) {
                mRecyclerAdapter = new CensusRecyclerAdapter(this, censusData, target, censusMode);
            } else {
                ((CensusRecyclerAdapter) mRecyclerAdapter).setCensusData(censusData);
            }
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putInt(MODE_KEY, censusMode);
        if (censusData != null) {
            outState.putParcelableArrayList(CENSUS_DATA_KEY, censusData);
        }
        if (target != null) {
            outState.putString(TARGET_KEY, target);
        }
    }
}
