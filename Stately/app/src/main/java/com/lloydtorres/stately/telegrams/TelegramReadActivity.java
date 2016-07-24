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
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.MuffinsHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lloyd on 2016-03-11.
 * Shows the contents of a particular telegram and any telegrams part of its conversation.
 */
public class TelegramReadActivity extends AppCompatActivity {
    // Keys for intent data and saved preferences
    public static final String ID_DATA = "idData";
    public static final String TITLE_DATA = "titleData";
    public static final String TELEGRAM_DATA = "telegramData";

    private int id;
    private String title;
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
            id = getIntent().getIntExtra(ID_DATA, 0);
            title = getIntent().getStringExtra(TITLE_DATA);
        }
        if (savedInstanceState != null)
        {
            id = savedInstanceState.getInt(ID_DATA);
            title = savedInstanceState.getString(TITLE_DATA);
            telegrams = savedInstanceState.getParcelableArrayList(TELEGRAM_DATA);
        }

        view = findViewById(R.id.refreshview_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshview_toolbar);
        setToolbar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        startQueryTelegramConvo();
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.telegram_title), title));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Hack to show SwipeRefreshLayout load; starts querying data about a telegram convo.
     */
    private void startQueryTelegramConvo()
    {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryTelegramConvo();
            }
        });
    }

    /**
     * Queries a convo for a particular telegram ID.
     */
    private void queryTelegramConvo()
    {
        String targetURL = String.format(Telegram.GET_CONVERSATION, id);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processRawTelegramConvo(d);
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
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
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
     * Processes the raw conversation data from NS.
     * @param d Document containing raw data
     */
    private void processRawTelegramConvo(Document d)
    {
        Element telegramsContainer = d.select("div.widebox").first();

        if (telegramsContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
            return;
        }

        ArrayList<Telegram> scannedTelegrams = MuffinsHelper.processRawTelegrams(telegramsContainer, SparkleHelper.getActiveUser(this).nationId, false);
        if (scannedTelegrams.size() > 0)
        {
            Collections.sort(scannedTelegrams);
            Collections.reverse(scannedTelegrams);
            telegrams = scannedTelegrams;
            mRecyclerAdapter = new TelegramsAdapter(this, telegrams);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            int scrollIndex = ((TelegramsAdapter) mRecyclerAdapter).getIndexOfId(id);
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
        savedInstanceState.putInt(ID_DATA, id);
        if (title != null)
        {
            savedInstanceState.putString(TITLE_DATA, title);
        }
        if (telegrams != null)
        {
            savedInstanceState.putParcelableArrayList(TELEGRAM_DATA, telegrams);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        id = savedInstanceState.getInt(ID_DATA);
        if (savedInstanceState != null)
        {
            if (title == null)
            {
                title = savedInstanceState.getString(TITLE_DATA);
            }
            if (telegrams == null)
            {
                telegrams = savedInstanceState.getParcelableArrayList(TELEGRAM_DATA);
            }
        }
    }
}
