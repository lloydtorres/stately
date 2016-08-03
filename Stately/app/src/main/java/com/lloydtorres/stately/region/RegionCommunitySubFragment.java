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

package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Region;

/**
 * Created by Lloyd on 2016-01-24.
 * This is a subfragment of the Region fragment showing information about the Region's community.
 * Accepts a Region object.
 */
public class RegionCommunitySubFragment extends Fragment {
    public static final String REGION_KEY = "mRegion";
    public static final String REGION_RMB_UNREAD_KEY = "rmbUnreadCountText";

    private Region mRegion;
    private String rmbUnreadCountText;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    public void setRegion(Region r)
    {
        mRegion = r;
    }
    public void setRMBUnreadCountText(String countText) { rmbUnreadCountText = countText; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        // Restore save state
        if (savedInstanceState != null && mRegion == null)
        {
            mRegion = savedInstanceState.getParcelable(REGION_KEY);
            rmbUnreadCountText = savedInstanceState.getString(REGION_RMB_UNREAD_KEY);
        }

        if (mRegion != null)
        {
            initCommunityRecycler(view);
        }

        return view;
    }

    private void initCommunityRecycler(View view)
    {
        // Setup recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerAdapter = new CommunityRecyclerAdapter(getContext(), getFragmentManager(), mRegion, rmbUnreadCountText);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (mRegion != null)
        {
            outState.putParcelable(REGION_KEY, mRegion);
        }
        if (rmbUnreadCountText != null)
        {
            outState.putString(REGION_RMB_UNREAD_KEY, rmbUnreadCountText);
        }
    }
}
