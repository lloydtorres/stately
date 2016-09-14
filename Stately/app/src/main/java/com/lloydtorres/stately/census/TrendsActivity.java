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

package com.lloydtorres.stately.census;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.SlidrActivity;
import com.lloydtorres.stately.dto.CensusHistory;
import com.lloydtorres.stately.dto.CensusNationRankData;
import com.lloydtorres.stately.dto.CensusNationRankList;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.simpleframework.xml.core.Persister;

import java.util.Collections;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-04-10.
 * This activity downloads and graphs the history of a given dataset.
 * Users can also switch between different datasets.
 */
public class TrendsActivity extends SlidrActivity {
    // Keys for intent/saved instance data
    public static final String TREND_DATA_TARGET = "trendTarget";
    public static final String TREND_DATA_ID = "trendId";
    public static final String TREND_DATA_START = "trendStart";
    public static final String TREND_DATA_MODE = "trendMode";
    public static final String TREND_DATA_DATASET = "trendDataset";

    public static final int TREND_NATION = 0;
    public static final int TREND_REGION = 1;
    public static final int TREND_WORLD = 2;

    public static final int CENSUS_CIVIL_RIGHTS = 0;
    public static final int CENSUS_ECONOMY = 1;
    public static final int CENSUS_POLITICAL_FREEDOM = 2;
    public static final int CENSUS_GOVERNMENT_SIZE = 27;
    public static final int CENSUS_TAXATION = 49;
    public static final int CENSUS_INFLUENCE = 65;
    public static final int CENSUS_AVERAGE_INCOME = 72;
    public static final int CENSUS_ECONOMIC_OUTPUT = 76;
    public static final int CENSUS_CRIME = 77;

    private String target;
    private int id;
    private int mode;
    private int start = 1;
    private CensusHistory dataset;

    private String[] WORLD_CENSUS_ITEMS;

    private View view;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);

        WORLD_CENSUS_ITEMS = getResources().getStringArray(R.array.census);

        if (getIntent() != null) {
            target = getIntent().getStringExtra(TREND_DATA_TARGET);
            id = getIntent().getIntExtra(TREND_DATA_ID, 0);
            mode = getIntent().getIntExtra(TREND_DATA_MODE, TREND_NATION);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.trends_toolbar);
        setToolbar(toolbar);

        view = findViewById(R.id.trends_main);

        mSwipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.trends_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.getThemeRefreshColours(this));
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction.equals(SwipyRefreshLayoutDirection.BOTTOM)) {
                    queryNextRankData();
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.trends_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (dataset == null) {
            startQueryDataset();
        }
        else {
            processDataset(dataset);
        }
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        setToolbarTitle(getString(R.string.trends_title_generic));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Convenience function to change toolbar title.
     * @param t New title
     */
    private void setToolbarTitle(String t)
    {
        getSupportActionBar().setTitle(t);
    }

    /**
     * Convenience class to show refresh animation on dataset query.
     */
    private void startQueryDataset()
    {
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setDirection(SwipyRefreshLayoutDirection.TOP);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryDataset();
            }
        });
    }

    /**
     * Public function for DatasetDialog; allows it to scan for a new dataset.
     * @param i Census ID
     */
    public void queryNewDataset(int i)
    {
        id = i;
        startQueryDataset();
    }

    /**
     * Queries NS for the history of a given dataset.
     * Gets data from 60 days ago up to the present.
     */
    private void queryDataset()
    {
        start = 1;

        String targetURL = "";
        String queryTarget = "";
        long curTime = System.currentTimeMillis() / 1000L;
        long sixtyDaysAgo = curTime - CensusHistory.SIXTY_DAYS_IN_SECONDS;

        switch (mode) {
            case TREND_NATION:
                queryTarget = String.format(Locale.US, CensusHistory.NATION_HISTORY, SparkleHelper.getIdFromName(target));
                targetURL = String.format(Locale.US, CensusHistory.QUERY_NATION, queryTarget, id, sixtyDaysAgo, curTime);
                break;
            case TREND_REGION:
                queryTarget = String.format(Locale.US, CensusHistory.REGION_HISTORY, SparkleHelper.getIdFromName(target));
                targetURL = String.format(Locale.US, CensusHistory.QUERY_RANKED, queryTarget, id, sixtyDaysAgo, curTime, start);
                break;
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    CensusHistory censusResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            censusResponse = serializer.read(CensusHistory.class, response);

                            // Set titles
                            String newTitle = String.format(getString(R.string.trends_title), censusResponse.name);
                            setToolbarTitle(newTitle);

                            if (censusResponse.scale.points != null) {
                                processDataset(censusResponse);
                            }
                            else {
                                SparkleHelper.makeSnackbar(view, getString(R.string.trends_empty));
                                stopRefreshing();
                            }
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            stopRefreshing();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                stopRefreshing();
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            stopRefreshing();
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Sets the given dataset as the global dataset and then displays the data to the user.
     * @param data
     */
    private void processDataset(CensusHistory data)
    {
        dataset = data;
        updateStartCounter(dataset.ranks);

        String[] censusType = SparkleHelper.getCensusScale(WORLD_CENSUS_ITEMS, id);

        mRecyclerAdapter = new TrendsRecyclerAdapter(this, mode, censusType[0], censusType[1], dataset);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        stopRefreshing();
    }

    /**
     * Updates the counter that tracks where to start querying for nation ranks.
     * @param rankList
     */
    private void updateStartCounter(CensusNationRankList rankList) {
        if (rankList != null && rankList.ranks.size() > 0) {
            start += rankList.ranks.size();
        }
    }

    /**
     * Query the next set of nation census rankings.
     */
    private void queryNextRankData() {
        String queryTarget = "";
        String targetURL = "";

        switch (mode) {
            case TREND_REGION:
                queryTarget = String.format(Locale.US, CensusHistory.REGION_HISTORY, SparkleHelper.getIdFromName(target));
                targetURL = String.format(Locale.US, CensusNationRankData.QUERY, queryTarget, id, start);
                break;
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    CensusNationRankData rankDataResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            rankDataResponse = serializer.read(CensusNationRankData.class, response);
                            if (rankDataResponse.ranks != null && rankDataResponse.ranks.ranks.size() > 0) {
                                dataset.ranks.ranks.addAll(rankDataResponse.ranks.ranks);
                                Collections.sort(dataset.ranks.ranks);

                                int oldItemCount = mRecyclerAdapter.getItemCount()-1;
                                ((TrendsRecyclerAdapter) mRecyclerAdapter).addNewCensusNationRanks(rankDataResponse.ranks);
                                ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(oldItemCount, 40);
                                updateStartCounter(rankDataResponse.ranks);
                            }
                            else {
                                SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
                            }
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                        }
                        stopRefreshing();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                stopRefreshing();
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            stopRefreshing();
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Call to setup the swipe refresher to stop refreshing.
     */
    private void stopRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);
        if (mode != TREND_NATION) {
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_trends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_dataset:
                FragmentManager fm = getSupportFragmentManager();
                DatasetDialog dialog = new DatasetDialog();
                dialog.setDatasets(WORLD_CENSUS_ITEMS, id);
                dialog.show(fm, DatasetDialog.DIALOG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TREND_DATA_ID, id);
        savedInstanceState.putInt(TREND_DATA_MODE, mode);
        savedInstanceState.putInt(TREND_DATA_START, start);
        if (target != null)
        {
            savedInstanceState.putString(TREND_DATA_TARGET, target);
        }
        if (dataset != null)
        {
            savedInstanceState.putParcelable(TREND_DATA_DATASET, dataset);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(TREND_DATA_ID);
            mode = savedInstanceState.getInt(TREND_DATA_MODE);
            start = savedInstanceState.getInt(TREND_DATA_START);
            if (target == null)
            {
                target = savedInstanceState.getString(TREND_DATA_TARGET);
            }
            if (dataset == null)
            {
                dataset = savedInstanceState.getParcelable(TREND_DATA_DATASET);
            }
        }
    }
}
