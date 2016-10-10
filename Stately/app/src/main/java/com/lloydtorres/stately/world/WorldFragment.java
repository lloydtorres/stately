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

package com.lloydtorres.stately.world;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewFragment;
import com.lloydtorres.stately.dto.BaseRegion;
import com.lloydtorres.stately.dto.World;
import com.lloydtorres.stately.explore.ExploreDialog;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.simpleframework.xml.core.Persister;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-11.
 * Fragment for displaying world data within StatelyActivity.
 */
public class WorldFragment extends RefreshviewFragment {
    public static final String WORLD_DATA = "worldData";
    public static final String WORLD_FEATURED_DATA = "worldFeaturedData";

    private World worldData;
    private BaseRegion featuredRegion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // Restore state
        if (savedInstanceState != null) {
            if (worldData == null) {
                worldData = savedInstanceState.getParcelable(WORLD_DATA);
            }
            if (featuredRegion == null) {
                featuredRegion = savedInstanceState.getParcelable(WORLD_FEATURED_DATA);
            }
        }

        toolbar.setTitle(getString(R.string.menu_world));
        mSwipeRefreshLayout.setEnabled(false);

        if (worldData != null && featuredRegion != null) {
            initRecyclerAdapter();
        }
        else {
            startWorldQuery();
        }

        return mView;
    }

    /**
     * Helper function that starts the onSwipeRefresh animation and calls on the real query function.
     */
    private void startWorldQuery() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryWorldData();
            }
        });
    }

    /**
     * Queries the NS API for world data, then calls on the query for featured region data if successful.
     */
    private void queryWorldData() {
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, World.QUERY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            worldData = serializer.read(World.class, response);
                            queryFeaturedRegionData(worldData.featuredRegion);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        SparkleHelper.logError(error.toString());
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        }
                        else {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        }
                    }
                });
        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Queries the NS API for featured region data. If this fails, just show the existing world data.
     * @param regionName
     */
    private void queryFeaturedRegionData(String regionName) {
        final String query = String.format(Locale.US, BaseRegion.BASE_QUERY, SparkleHelper.getIdFromName(regionName));
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, query,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            featuredRegion = serializer.read(BaseRegion.class, response);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                        }
                        // If this fails, just process the data we already have.
                        initRecyclerAdapter();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        SparkleHelper.logError(error.toString());
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        }
                        else {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        }
                        // If this fails, just process the data we already have.
                        initRecyclerAdapter();
                    }
                });
        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
            // If this fails, just process the data we already have.
            initRecyclerAdapter();
        }
    }

    /**
     * Initializes the recycler adapter with the available data.
     */
    private void initRecyclerAdapter() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new WorldRecyclerAdapter(getContext(), getFragmentManager(), worldData, featuredRegion);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((WorldRecyclerAdapter) mRecyclerAdapter).setContent(worldData, featuredRegion);
        }
        
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_explore_default, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_explore:
                ExploreDialog exploreDialog = new ExploreDialog();
                exploreDialog.show(getFragmentManager(), ExploreDialog.DIALOG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);

        if (worldData != null) {
            savedInstanceState.putParcelable(WORLD_DATA, worldData);
        }
        if (featuredRegion != null) {
            savedInstanceState.putParcelable(WORLD_FEATURED_DATA, featuredRegion);
        }
    }
}
