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

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-09-12.
 * Nation-specific sub-fragments.
 */
public abstract class NationSubFragment extends RecyclerSubFragment {
    public static final String CARDS_DATA = "cards";

    protected Nation mNation;
    protected ArrayList<Parcelable> cards = new ArrayList<Parcelable>();

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore state
        if (savedInstanceState != null && cards == null) {
            cards = savedInstanceState.getParcelableArrayList(CARDS_DATA);
        }

        if ((cards == null || cards.size() <= 0) && mNation != null) {
            initData();
        }

        initRecyclerAdapter();

        return view;
    }

    public void forceRefreshData() {
        cards = new ArrayList<Parcelable>();
        initData();
        initRecyclerAdapter();
    }

    protected abstract void initData();

    protected void initRecyclerAdapter() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new NationCardsRecyclerAdapter(getContext(), cards, getFragmentManager());
        } else {
            ((NationCardsRecyclerAdapter) mRecyclerAdapter).setCards(cards);
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cards != null) {
            outState.putParcelableArrayList(CARDS_DATA, cards);
        }
    }
}
