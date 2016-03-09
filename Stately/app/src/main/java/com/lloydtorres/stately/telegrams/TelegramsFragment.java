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

package com.lloydtorres.stately.telegrams;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Lloyd on 2016-03-08.
 * This is the Telegrams section of the main Stately activity.
 */
public class TelegramsFragment extends Fragment {
    public static final String KEY_PAST_OFFSET = "keyPastOffset";
    public static final String KEY_TELEGRAMS = "keyTelegrams";

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipyRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<Telegram> telegrams;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;

    @Override
    public void onAttach(Context context) {
        // Get activity for manipulation
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.content_message_board, container, false);

        // Restore state
        if (savedInstanceState != null)
        {
            pastOffset = savedInstanceState.getInt(KEY_PAST_OFFSET, 0);
            telegrams = savedInstanceState.getParcelableArrayList(KEY_TELEGRAMS);
        }

        toolbar = (Toolbar) mView.findViewById(R.id.message_board_toolbar);
        toolbar.setTitle(getString(R.string.menu_telegrams));

        if (mActivity != null && mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipyRefreshLayout) mView.findViewById(R.id.message_board_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                // @TODO
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.message_board_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_PAST_OFFSET, pastOffset);
        if (telegrams != null)
        {
            savedInstanceState.putParcelableArrayList(KEY_TELEGRAMS, telegrams);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_telegrams, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getChildFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_folders:
                return true;
            case R.id.nav_compose:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
