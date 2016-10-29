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

package com.lloydtorres.stately.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-09-11.
 * Skeleton for fragments that use the Refreshview layout.
 * IMPORTANT NOTE: Can't use Butter Knife here due to weird lifecycle issues.
 */
public abstract class RefreshviewFragment extends Fragment {

    protected View mView;
    protected Toolbar toolbar;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView.Adapter mRecyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_refreshview, container, false);

        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);
        toolbar.setTitle(getString(R.string.menu_issues));

        if (getActivity() != null && getActivity() instanceof IToolbarActivity)
        {
            ((IToolbarActivity) getActivity()).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(RaraHelper.getThemeRefreshColours(getContext()));

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = RaraHelper.getStaggeredLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return mView;
    }
}
