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
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.Embassy;
import com.lloydtorres.stately.dto.EmbassyHolder;
import com.lloydtorres.stately.dto.Officer;
import com.lloydtorres.stately.dto.OfficerHolder;
import com.lloydtorres.stately.dto.RMBButtonHolder;
import com.lloydtorres.stately.dto.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * This is a subfragment of the Region fragment showing information about the Region's community.
 * Accepts a Region object.
 */
public class RegionCommunitySubFragment extends RecyclerSubFragment {
    public static final String CARDS_DATA = "cards";
    public static final String NAME_DATA = "name";

    private Region mRegion;
    private String rmbUnreadCountText;
    private ArrayList<Parcelable> cards = new ArrayList<Parcelable>();
    private String regionName;

    public void setRegion(Region r)
    {
        mRegion = r;
    }
    public void setRMBUnreadCountText(String countText) { rmbUnreadCountText = countText; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore save state
        if (savedInstanceState != null) {
            if (cards == null) {
                cards = savedInstanceState.getParcelableArrayList(CARDS_DATA);
            }
            if (regionName == null) {
                regionName = savedInstanceState.getString(NAME_DATA);
            }
        }

        if ((cards == null || cards.size() <= 0) && mRegion != null) {
            initData();
        }

        initRecyclerAdapter();

        return view;
    }

    private void initData() {
        // This adds a button to the RMB
        RMBButtonHolder button = new RMBButtonHolder(mRegion.name, rmbUnreadCountText);
        cards.add(button);

        if (mRegion.poll != null)
        {
            cards.add(mRegion.poll);
        }

        if (mRegion.gaVote != null && (mRegion.gaVote.voteFor + mRegion.gaVote.voteAgainst) > 0)
        {
            mRegion.gaVote.chamber = Assembly.GENERAL_ASSEMBLY;
            cards.add(mRegion.gaVote);
        }

        if (mRegion.scVote != null && (mRegion.scVote.voteFor + mRegion.scVote.voteAgainst) > 0)
        {
            mRegion.scVote.chamber = Assembly.SECURITY_COUNCIL;
            cards.add(mRegion.scVote);
        }

        List<Officer> officers = new ArrayList<Officer>(mRegion.officers);
        if (!"0".equals(mRegion.delegate))
        {
            officers.add(new Officer(mRegion.delegate, getString(R.string.card_region_wa_delegate), Officer.DELEGATE_ORDER));
        }
        if (!"0".equals(mRegion.founder))
        {
            officers.add(new Officer(mRegion.founder, getString(R.string.card_region_founder), Officer.FOUNDER_ORDER));
        }
        Collections.sort(officers);
        cards.add(new OfficerHolder(officers));

        ArrayList<String> embassyList = new ArrayList<String>();
        if (mRegion.embassies != null && mRegion.embassies.size() > 0)
        {
            // Only add active embassies
            for (Embassy e : mRegion.embassies)
            {
                if (e.type == null)
                {
                    embassyList.add(e.name);
                }
            }
        }
        Collections.sort(embassyList);
        if (embassyList.size() > 0) {
            cards.add(new EmbassyHolder(embassyList));
        }
    }

    private void initRecyclerAdapter() {
        mRecyclerAdapter = new CommunityRecyclerAdapter(getContext(), getFragmentManager(), cards, regionName);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (cards != null)
        {
            outState.putParcelableArrayList(CARDS_DATA, cards);
        }
        if (regionName != null)
        {
            outState.putString(NAME_DATA, regionName);
        }
    }
}
