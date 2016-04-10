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
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusDetailedRank;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-04-09.
 * This fragment displays a nation or region's census scores and rankings, which are sortable
 * by the user. Takes in a list of census data as well as mode.
 */
public class CensusSubFragment extends Fragment {
    public static final String CENSUS_DATA_KEY = "censusData";
    public static final String MODE_KEY = "censusMode";

    public static final int CENSUS_MODE_NATION = 0;
    public static final int CENSUS_MODE_REGION = 1;

    private ArrayList<CensusDetailedRank> censusData;
    private int censusMode;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    public void setCensusData(ArrayList<CensusDetailedRank> c)
    {
        censusData = c;
    }

    public void setMode(int mode)
    {
        censusMode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        // Restore save state
        if (savedInstanceState != null && censusData == null)
        {
            censusMode = savedInstanceState.getInt(MODE_KEY);
            censusData = savedInstanceState.getParcelableArrayList(CENSUS_DATA_KEY);
        }

        if (censusData != null)
        {
            initCensusRecycler(view);
        }

        return view;
    }

    private void initCensusRecycler(View view)
    {
        // Setup recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerAdapter = new CensusRecyclerAdapter(getContext(), censusData, censusMode);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putInt(MODE_KEY, censusMode);
        if (censusData != null)
        {
            outState.putParcelableArrayList(CENSUS_DATA_KEY, censusData);
        }
    }
}
