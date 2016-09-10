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

package com.lloydtorres.stately.issues;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-28.
 * This activity displays options for a particular issue.
 */
public class IssueDecisionActivity extends SlidrActivity {
    // Keys for Intent data
    public static final String ISSUE_DATA = "issueData";
    public static final String NATION_DATA = "nationData";
    public static final int DISMISSED = -1;
    private static final String LEGISLATION_PASSED = "LEGISLATION PASSED";
    private static final String STORY_SO_FAR = "The Story So Far";
    private static final String NOT_AVAILABLE = "Issue Not Available";

    private Issue issue;
    private Nation mNation;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isInProgress;
    private View view;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshview);
        view = findViewById(R.id.refreshview_main);
        isInProgress = false;

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            issue = getIntent().getParcelableExtra(ISSUE_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null)
        {
            issue = savedInstanceState.getParcelable(ISSUE_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshview_toolbar);
        setToolbar(toolbar);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryIssueInfo();
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.issue_activity_title), mNation.name, issue.id));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     */
    private void startQueryIssueInfo()
    {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryIssueInfo();
            }
        });
    }

    /**
     * Query information on the current issue from the actual NationStates site
     */
    private void queryIssueInfo()
    {
        String targetURL = String.format(Locale.US, IssueOption.QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processIssueInfo(view, d);
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

    /**
     * Process the received page into the Issue and its IssueOptions
     * @param v Activity view
     * @param d Document received from NationStates
     */
    private void processIssueInfo(View v, Document d)
    {
        // First check if the issue is still available
        if (d.text().contains(NOT_AVAILABLE))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(v, String.format(getString(R.string.issue_unavailable), mNation.name));
            return;
        }

        Element issueInfoContainer = d.select("div#dilemma").first();

        if (issueInfoContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(v, getString(R.string.login_error_parsing));
            return;
        }

        Elements issueInfoRaw = issueInfoContainer.children();

        String issueText = issueInfoRaw.select("p").first().text();
        // If this is an issue chain, grab the second paragraph instead
        if (d.select("div.dilemmachain").first() != null)
        {
            issueText = issueInfoRaw.select("p").get(1).text();
            if (d.text().contains(STORY_SO_FAR))
            {
                issueText = issueText + "<br><br>" + issueInfoRaw.select("p").get(2).text();
            }
        }
        issue.content = issueText;

        issue.options = new ArrayList<IssueOption>();

        Element optionHolderMain = issueInfoRaw.select("ol.diloptions").first();
        if (optionHolderMain != null)
        {
            Elements optionsHolder = optionHolderMain.select("li");

            int i = 0;
            for (Element option : optionsHolder)
            {
                IssueOption issueOption = new IssueOption();
                issueOption.index = i++;

                Element button = option.select("button").first();
                if (button != null)
                {
                    issueOption.header = button.attr("name");
                }
                else
                {
                    issueOption.header = IssueOption.SELECTED_HEADER;
                }

                Element optionContentHolder = option.select("p").first();
                if (optionContentHolder == null)
                {
                    // safety check
                    mSwipeRefreshLayout.setRefreshing(false);
                    SparkleHelper.makeSnackbar(v, getString(R.string.login_error_parsing));
                    return;
                }

                issueOption.content = optionContentHolder.text();
                issue.options.add(issueOption);
            }
        }

        IssueOption dismissOption = new IssueOption();
        dismissOption.index = -1;
        dismissOption.header = IssueOption.DISMISS_HEADER;
        dismissOption.content = "";
        issue.options.add(dismissOption);

        setRecyclerAdapter(issue);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(false);
    }

    private void setRecyclerAdapter(Issue issue)
    {
        mRecyclerAdapter = new IssueDecisionRecyclerAdapter(this, issue);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    /**
     * Helper to confirm the position selected by the user.
     * @param option The option selected.
     */
    public void setAdoptPosition(final IssueOption option)
    {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPostAdoptPosition(option);
                dialog.dismiss();
            }
        };

        if (SettingsActivity.getConfirmIssueDecisionSetting(this))
        {
            dialogBuilder
                    .setNegativeButton(getString(R.string.explore_negative), null);

            switch (option.index)
            {
                case DISMISSED:
                    dialogBuilder.setTitle(getString(R.string.issue_option_confirm_dismiss))
                            .setPositiveButton(getString(R.string.issue_option_dismiss), dialogClickListener);
                    break;
                default:
                    dialogBuilder.setTitle(String.format(getString(R.string.issue_option_confirm_adopt), option.index + 1))
                            .setPositiveButton(getString(R.string.issue_option_adopt), dialogClickListener);
                    break;
            }

            dialogBuilder.show();
        }
        else
        {
            startPostAdoptPosition(option);
        }
    }

    private void startPostAdoptPosition(final IssueOption option)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                postAdoptPosition(option);
            }
        });
    }

    /**
     * Send the position selected by the user back to the server.
     * @param option The option selected.
     */
    public void postAdoptPosition(final IssueOption option)
    {
        isInProgress = true;
        String targetURL = String.format(Locale.US, IssueOption.POST_QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isInProgress = false;
                        if (!IssueOption.DISMISS_HEADER.equals(option.header))
                        {
                            if (response.contains(LEGISLATION_PASSED))
                            {
                                Intent issueResultsActivity = new Intent(IssueDecisionActivity.this, IssueResultsActivity.class);
                                issueResultsActivity.putExtra(IssueResultsActivity.RESPONSE_DATA, response);
                                issueResultsActivity.putExtra(IssueResultsActivity.OPTION_DATA, option);
                                issueResultsActivity.putExtra(IssueResultsActivity.NATION_DATA, mNation);
                                startActivity(issueResultsActivity);
                                finish();
                            }
                            else
                            {
                                SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                            }
                        }
                        else
                        {
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put(option.header, "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
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
    public void onResume()
    {
        super.onResume();
        if (issue.options == null)
        {
            startQueryIssueInfo();
        }
        else
        {
            setRecyclerAdapter(issue);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issue != null)
        {
            savedInstanceState.putParcelable(ISSUE_DATA, issue);
        }
        if (mNation != null)
        {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (issue == null)
            {
                issue = savedInstanceState.getParcelable(ISSUE_DATA);
            }
            if (mNation == null)
            {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
