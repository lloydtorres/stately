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

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-03-11.
 * Shows the conversational history of a particular telegram.
 */
public class TelegramHistoryActivity extends SlidrActivity {
    // Keys for intent data and saved preferences
    public static final String ID_DATA = "telegramId";
    public static final String TELEGRAMS_DATA = "telegramsData";

    private static final int INVALID_TELEGRAM = -1;

    private int mainTelegramId;
    private ArrayList<Telegram> telegrams;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View view;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshview);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            mainTelegramId = getIntent().getIntExtra(ID_DATA, INVALID_TELEGRAM);
        }
        if (savedInstanceState != null)
        {
            mainTelegramId = savedInstanceState.getInt(ID_DATA, INVALID_TELEGRAM);
            telegrams = savedInstanceState.getParcelableArrayList(TELEGRAMS_DATA);
        }

        view = findViewById(R.id.refreshview_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshview_toolbar);
        setToolbar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.getThemeRefreshColours(this));

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        startQueryTelegramHistory();
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(getString(R.string.telegram_history_title));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void startQueryTelegramHistory() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryTelegramHistory();
            }
        });
    }

    private void queryTelegramHistory() {
        String targetURL = String.format(Locale.US, Telegram.TELEGRAM_CONVERSATION, mainTelegramId);
        NSStringRequest stringRequest = new NSStringRequest(this, Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processRawTelegrams(d);
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
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    private void processRawTelegrams(Document d) {
        Element telegramsContainer = d.select("div.widebox").first();
        if (telegramsContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
            return;
        }
        telegrams = MuffinsHelper.processRawTelegrams(telegramsContainer, SparkleHelper.getActiveUser(this).nationId);
        initTelegramsRecyclerAdapter();
    }

    private void initTelegramsRecyclerAdapter() {
        if (telegrams.size() > 0)
        {
            Collections.sort(telegrams);
            Collections.reverse(telegrams);
            mRecyclerAdapter = new TelegramsAdapter(this, telegrams);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            int scrollIndex = ((TelegramsAdapter) mRecyclerAdapter).getIndexOfId(mainTelegramId);
            if (scrollIndex != -1)
            {
                mLayoutManager.scrollToPosition(scrollIndex);
            }
        }
        else
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.telegrams_empty_convo));
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (telegrams != null) {
            initTelegramsRecyclerAdapter();
        } else {
            startQueryTelegramHistory();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(ID_DATA, mainTelegramId);
        if (telegrams != null)
        {
            savedInstanceState.putParcelableArrayList(TELEGRAMS_DATA, telegrams);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            mainTelegramId = savedInstanceState.getInt(ID_DATA, INVALID_TELEGRAM);
            if (telegrams == null)
            {
                telegrams = savedInstanceState.getParcelableArrayList(TELEGRAMS_DATA);
            }
        }
    }
}
