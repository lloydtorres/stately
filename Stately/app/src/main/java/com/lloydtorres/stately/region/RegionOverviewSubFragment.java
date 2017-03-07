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

import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.RegionFactbookCardData;
import com.lloydtorres.stately.dto.RegionQuickFactsCardData;
import com.lloydtorres.stately.dto.RegionTagsCardData;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-01-22.
 * A sub-fragment of the Region fragment displaying an overview about a region.
 * Takes in a Region object.
 */
public class RegionOverviewSubFragment extends RecyclerSubFragment {
    public static final String REGION_NAME_DATA = "regionName";
    public static final String CARDS_DATA = "cards";

    private Region mRegion;
    private String regionName;
    private ArrayList<Parcelable> cards = new ArrayList<Parcelable>();

    public void setRegion(Region r)
    {
        mRegion = r;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore save state
        if (savedInstanceState != null) {
            if (cards == null) {
                cards = savedInstanceState.getParcelableArrayList(CARDS_DATA);
            }
            if (regionName == null) {
                regionName = savedInstanceState.getString(REGION_NAME_DATA);
            }
        }

        if ((cards == null || cards.size() <= 0) && mRegion != null) {
            regionName = mRegion.name;
            initData();
        }

        initRecyclerAdapter();

        return view;
    }

    private void initData() {
        if (NightmareHelper.getIsZDayActive(getContext()) && mRegion.zombieData != null) {
            cards.add(mRegion.zombieData);
        }

        if (mRegion.waBadges != null) {
            cards.addAll(mRegion.waBadges);
        }

        RegionQuickFactsCardData quickFacts = new RegionQuickFactsCardData();
        quickFacts.waDelegate = mRegion.delegate;
        quickFacts.delegateVotes = mRegion.delegateVotes;
        quickFacts.founder = mRegion.founder;
        quickFacts.founded = mRegion.founded;
        quickFacts.power = mRegion.power;
        cards.add(quickFacts);

        if (mRegion.factbook != null && mRegion.factbook.length() > 0) {
            RegionFactbookCardData factbook = new RegionFactbookCardData();
            factbook.factbook = mRegion.factbook;
            cards.add(factbook);
        }
        
        RegionTagsCardData tags = new RegionTagsCardData();
        tags.tags = mRegion.tags;
        cards.add(tags);
    }

    private void initRecyclerAdapter() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new RegionOverviewRecyclerAdapter(getContext(), regionName, getFragmentManager(), cards);
        } else {
            ((RegionOverviewRecyclerAdapter) mRecyclerAdapter).setCards(cards);
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (cards != null) {
            outState.putParcelableArrayList(CARDS_DATA, cards);
        }
        if (regionName != null) {
            outState.putString(REGION_NAME_DATA, regionName);
        }
    }
}
