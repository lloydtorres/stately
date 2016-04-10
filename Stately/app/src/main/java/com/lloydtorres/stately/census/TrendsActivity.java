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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusHistory;
import com.lloydtorres.stately.dto.CensusHistoryPoint;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-04-10.
 * This activity downloads and graphs the history of a given dataset.
 * Users can also switch between different datasets.
 */
public class TrendsActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    // Keys for intent/saved instance data
    public static final String TREND_TARGET = "trendTarget";
    public static final String TREND_ID = "trendId";
    public static final String TREND_MODE = "trendMode";
    public static final String TREND_DATASET = "trendDataset";

    public static final int TREND_NATION = 0;
    public static final int TREND_REGION = 1;

    private String[] WORLD_CENSUS_ITEMS;

    private String target;
    private int id;
    private int mode;
    private CensusHistory dataset;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View view;
    private TextView title;
    private TextView unit;
    private TextView date;
    private TextView value;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);

        WORLD_CENSUS_ITEMS = getResources().getStringArray(R.array.census);

        if (getIntent() != null) {
            target = getIntent().getStringExtra(TREND_TARGET);
            id = getIntent().getIntExtra(TREND_ID, 0);
            mode = getIntent().getIntExtra(TREND_MODE, TREND_NATION);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.trends_toolbar);
        setToolbar(toolbar);

        view = findViewById(R.id.trends_main);
        title = (TextView) findViewById(R.id.trends_title);
        unit = (TextView) findViewById(R.id.trends_unit);
        date = (TextView) findViewById(R.id.trends_date);
        value = (TextView) findViewById(R.id.trends_value);
        chart = (LineChart) findViewById(R.id.trends_chart);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.trends_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setEnabled(false);

        if (dataset == null)
        {
            startQueryDataset();
        }
        else
        {
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
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryDataset();
            }
        });
    }

    /**
     * Queries NS for the history of a given dataset.
     * Gets data from 60 days ago up to the present.
     */
    private void queryDataset()
    {
        String queryMode = mode == TREND_NATION ? CensusHistory.NATION_HISTORY : CensusHistory.REGION_HISTORY;
        long curTime = System.currentTimeMillis() / 1000L;
        long sixtyDaysAgo = curTime - CensusHistory.SIXTY_DAYS_IN_SECONDS;

        String targetURL = String.format(Locale.US, CensusHistory.QUERY, queryMode, SparkleHelper.getIdFromName(target), id, sixtyDaysAgo, curTime);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    CensusHistory censusResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            censusResponse = serializer.read(CensusHistory.class, response);
                            processDataset(censusResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getApplicationContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Sets the given dataset as the global dataset and then displays the data to the user.
     * @param data
     */
    private void processDataset(CensusHistory data)
    {
        // Set passed in dataset as new global dataset
        dataset = data;

        // Set titles
        String newTitle = String.format(getString(R.string.trends_title), dataset.name);
        setToolbarTitle(newTitle);

        int censusId = id;
        if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
        {
            censusId = WORLD_CENSUS_ITEMS.length - 1;
        }
        String[] censusType = WORLD_CENSUS_ITEMS[censusId].split("##");
        title.setText(censusType[0]);
        unit.setText(censusType[1]);

        // Set selected indicator text to latest data
        resetDataSelected();

        // Set up chart
        final float lineWidth = 2.5f;
        List<CensusHistoryPoint> datapoints = dataset.scale.points;
        List<Entry> historyEntries = new ArrayList<Entry>();
        for (int i=0; i < datapoints.size(); i++)
        {
            historyEntries.add(new Entry(datapoints.get(i).score, i));
        }

        // Formatting
        LineDataSet lineHistoryData = new LineDataSet(historyEntries, "");
        lineHistoryData.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineHistoryData.setColors(SparkleHelper.waColourFor, this);
        lineHistoryData.setDrawValues(false);
        lineHistoryData.setDrawVerticalHighlightIndicator(true);
        lineHistoryData.setDrawHorizontalHighlightIndicator(false);
        lineHistoryData.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        lineHistoryData.setHighlightLineWidth(lineWidth);
        lineHistoryData.setDrawCircles(false);
        lineHistoryData.setLineWidth(lineWidth);

        // Match data with x-axis labels
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(lineHistoryData);
        List<String> xLabels = new ArrayList<String>();
        for (int i=0; i < datapoints.size(); i++)
        {
            xLabels.add(String.format(SparkleHelper.getDateNoYearFromUTC(datapoints.get(i).timestamp), i));
        }
        LineData dataFinal = new LineData(xLabels, dataSets);

        // formatting
        chart = SparkleHelper.getFormattedLineChart(chart, this, 6, false);
        chart.setData(dataFinal);
        chart.invalidate();

        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This resets the displayed data label to the most current one.
     */
    private void resetDataSelected()
    {
        List<CensusHistoryPoint> datapoints = dataset.scale.points;
        CensusHistoryPoint latest = datapoints.get(datapoints.size() - 1);
        setDataSelected(latest);
    }

    /**
     * Sets the displayed data label based on the passed-in data point.
     * @param point
     */
    private void setDataSelected(CensusHistoryPoint point)
    {
        date.setText(SparkleHelper.getDateNoYearFromUTC(point.timestamp));
        value.setText(SparkleHelper.getPrettifiedNumber(point.score));
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        CensusHistoryPoint selectedPoint = dataset.scale.points.get(e.getXIndex());
        setDataSelected(selectedPoint);
    }

    @Override
    public void onNothingSelected() {
        resetDataSelected();
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
                // @TODO dialog to switch to different dataset + callback
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TREND_ID, id);
        savedInstanceState.putInt(TREND_MODE, mode);
        if (target != null)
        {
            savedInstanceState.putString(TREND_TARGET, target);
        }
        if (dataset != null)
        {
            savedInstanceState.putParcelable(TREND_DATASET, dataset);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(TREND_ID);
            mode = savedInstanceState.getInt(TREND_MODE);
            if (target == null)
            {
                target = savedInstanceState.getString(TREND_TARGET);
            }
            if (dataset == null)
            {
                dataset = savedInstanceState.getParcelable(TREND_DATASET);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        if (chart != null)
        {
            chart = null;
        }
        super.onDestroy();
    }
}
