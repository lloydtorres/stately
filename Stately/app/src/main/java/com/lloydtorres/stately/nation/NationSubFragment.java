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

package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-09-12.
 * Nation-specific sub-fragments.
 */
public abstract class NationSubFragment extends RecyclerSubFragment {
    public static final String NATION_NAME_DATA = "nationName";
    public static final String IS_SAME_REGION_DATA = "isSameRegion";
    public static final String CARDS_DATA = "cards";

    protected Nation mNation;
    protected String nationName;
    protected boolean isSameRegion;
    protected ArrayList<Parcelable> cards = new ArrayList<Parcelable>();

    public void setNation(Nation n) {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore state
        if (savedInstanceState != null) {
            if (cards == null) {
                cards = savedInstanceState.getParcelableArrayList(CARDS_DATA);
            }
            if (nationName == null) {
                nationName = savedInstanceState.getString(NATION_NAME_DATA);
            }
            isSameRegion = savedInstanceState.getBoolean(IS_SAME_REGION_DATA);
        }

        if ((cards == null || cards.size() <= 0) && mNation != null) {
            nationName = mNation.name;
            String curRegionName = PinkaHelper.getRegionSessionData(getContext());
            isSameRegion =
                    (SparkleHelper.getIdFromName(mNation.region).equals(SparkleHelper.getIdFromName(curRegionName)));
            initData();
        }

        initRecyclerAdapter();

        return view;
    }

    public void forceRefreshData() {
        cards = new ArrayList<Parcelable>();
        initData();
        initRecyclerAdapter(true);
    }

    protected void initData() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
    }

    protected void initRecyclerAdapter() {
        initRecyclerAdapter(false);
    }

    private void initRecyclerAdapter(boolean isOnlySetAdapterOnNull) {
        if (mRecyclerAdapter == null) {
            if (getParentFragment() != null && getParentFragment().getActivity() instanceof ExploreActivity) {
                mRecyclerAdapter = new NationCardsRecyclerAdapter(cards, getFragmentManager(),
                        nationName, isSameRegion,
                        (ExploreActivity) getParentFragment().getActivity());
            } else {
                mRecyclerAdapter = new NationCardsRecyclerAdapter(getContext(), cards,
                        getFragmentManager(), nationName, isSameRegion);
            }
            if (isOnlySetAdapterOnNull) {
                mRecyclerView.setAdapter(mRecyclerAdapter);
            }
        } else {
            ((NationCardsRecyclerAdapter) mRecyclerAdapter).setCards(cards);
        }

        if (!isOnlySetAdapterOnNull) {
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cards != null) {
            outState.putParcelableArrayList(CARDS_DATA, cards);
        }
        if (nationName != null) {
            outState.putString(NATION_NAME_DATA, nationName);
        }
        outState.putBoolean(IS_SAME_REGION_DATA, isSameRegion);
    }
}
