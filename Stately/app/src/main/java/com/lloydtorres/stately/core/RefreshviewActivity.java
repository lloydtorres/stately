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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-09-16.
 * Skeleton for activities that use the Refreshview layout.
 */
public abstract class RefreshviewActivity extends SlidrActivity {
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected View mView;

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshview);

        mView = findViewById(R.id.refreshview_main);

        Toolbar toolbar = findViewById(R.id.refreshview_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSwipeRefreshLayout = findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(RaraHelper.getThemeRefreshColours(this));

        mRecyclerView = findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = RaraHelper.getStaggeredLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    protected void setLinearLayoutManager() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    protected void removePaddingTop() {
        int bottomPadding = mRecyclerView.getPaddingBottom();
        mRecyclerView.setPadding(0, 0, 0, bottomPadding);
    }

    protected void removePaddingBottom() {
        int topPadding = mRecyclerView.getPaddingTop();
        mRecyclerView.setPadding(0, topPadding, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
