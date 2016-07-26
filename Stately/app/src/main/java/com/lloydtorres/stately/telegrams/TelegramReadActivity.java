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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.TelegramFolder;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-03-11.
 * Shows the contents of a particular telegram and any telegrams part of its conversation.
 */
public class TelegramReadActivity extends AppCompatActivity {
    // Keys for intent data and saved preferences
    public static final String TITLE_DATA = "titleData";
    public static final String TELEGRAM_DATA_2 = "telegramData2";
    public static final String FOLDER_DATA = "folderData";
    public static final String SEL_FOLDER_DATA = "selectedFolderData";
    public static final String CHK_DATA = "chkData";

    public static final int TELEGRAM_READ_RESULTS = 12345;
    public static final String TELEGRAM_READ_RESULTS_ID = "telegramReadResultsId";
    public static final int TELEGRAM_READ_RESULTS_NULL = -1;

    private String title;
    private Telegram telegram;
    private ArrayList<TelegramFolder> folders;
    private int selectedFolder;
    private String chkValue;

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
            telegram = getIntent().getParcelableExtra(TELEGRAM_DATA_2);
            folders = getIntent().getParcelableArrayListExtra(FOLDER_DATA);
            selectedFolder = getIntent().getIntExtra(SEL_FOLDER_DATA, 0);
            title = getIntent().getStringExtra(TITLE_DATA);
            chkValue = getIntent().getStringExtra(CHK_DATA);
        }
        if (savedInstanceState != null)
        {
            title = savedInstanceState.getString(TITLE_DATA);
            telegram = savedInstanceState.getParcelable(TELEGRAM_DATA_2);
            folders = savedInstanceState.getParcelableArrayList(FOLDER_DATA);
            selectedFolder = savedInstanceState.getInt(SEL_FOLDER_DATA, 0);
            chkValue = savedInstanceState.getString(CHK_DATA);
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
        mRecyclerAdapter = new TelegramsAdapter(this, telegram);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        markAsRead();
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.telegram_title), title));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void markAsRead() {
        if (chkValue == null) {
            return;
        }

        String targetURL = String.format(Locale.US, Telegram.MARK_READ, telegram.id, chkValue);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(TelegramReadActivity.this);
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        DashHelper.getInstance(this).addRequest(stringRequest);
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
        if (title != null)
        {
            savedInstanceState.putString(TITLE_DATA, title);
        }
        if (telegram != null)
        {
            savedInstanceState.putParcelable(TELEGRAM_DATA_2, telegram);
        }
        if (folders != null)
        {
            savedInstanceState.putParcelableArrayList(FOLDER_DATA, folders);
            savedInstanceState.putInt(SEL_FOLDER_DATA, selectedFolder);
        }
        if (chkValue != null)
        {
            savedInstanceState.putString(CHK_DATA, chkValue);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (title == null)
            {
                title = savedInstanceState.getString(TITLE_DATA);
            }
            if (telegram == null)
            {
                telegram = savedInstanceState.getParcelable(TELEGRAM_DATA_2);
            }
            if (folders == null)
            {
                folders = savedInstanceState.getParcelableArrayList(FOLDER_DATA);
                selectedFolder = savedInstanceState.getInt(SEL_FOLDER_DATA, 0);
            }
            if (chkValue == null)
            {
                chkValue = savedInstanceState.getString(CHK_DATA);
            }
        }
    }
}
