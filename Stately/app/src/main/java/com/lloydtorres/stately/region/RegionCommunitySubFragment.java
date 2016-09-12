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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Region;

/**
 * Created by Lloyd on 2016-01-24.
 * This is a subfragment of the Region fragment showing information about the Region's community.
 * Accepts a Region object.
 */
public class RegionCommunitySubFragment extends RecyclerSubFragment {
    public static final String REGION_KEY = "mRegion";
    public static final String REGION_RMB_UNREAD_KEY = "rmbUnreadCountText";

    private Region mRegion;
    private String rmbUnreadCountText;

    public void setRegion(Region r)
    {
        mRegion = r;
    }
    public void setRMBUnreadCountText(String countText) { rmbUnreadCountText = countText; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore save state
        if (savedInstanceState != null && mRegion == null)
        {
            mRegion = savedInstanceState.getParcelable(REGION_KEY);
            rmbUnreadCountText = savedInstanceState.getString(REGION_RMB_UNREAD_KEY);
        }

        if (mRegion != null)
        {
            mRecyclerAdapter = new CommunityRecyclerAdapter(getContext(), getFragmentManager(), mRegion, rmbUnreadCountText);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }

        return view;
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
