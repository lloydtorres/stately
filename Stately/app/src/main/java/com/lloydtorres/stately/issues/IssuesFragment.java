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

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
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
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-28.
 * A fragment to display current issues.
 */
public class IssuesFragment extends RefreshviewFragment {
    private static final Pattern NEXT_ISSUE_REGEX = Pattern.compile("\\$\\('#nextdilemmacountdown'\\)\\.countdown\\(\\{timestamp:new Date\\(([0-9]*?)\\)\\}\\);");
    private static final Pattern CHAIN_ISSUE_REGEX = Pattern.compile("^\\[(.+)\\] (.+)$");

    private List<Object> issues;
    private Nation mNation;

    public void setNationData(Nation n)
    {
        mNation = n;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(getString(R.string.menu_issues));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryIssues(mView);
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh on resume
        startQueryIssues();
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     */
    private void startQueryIssues() {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryIssues(mView);
            }
        });
    }

    /**
     * Scrape the issues from the actual NationStates site
     * @param view
     */
    private void queryIssues(final View view) {
        String targetURL = Issue.QUERY;

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processIssues(view, d);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Process the HTML contents of the issues into actual Issue objects
     * @param d
     */
    private void processIssues(View v, Document d) {
        issues = new ArrayList<Object>();

        Element issuesContainer = d.select("ul.dilemmalist").first();

        if (issuesContainer == null) {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(v, getString(R.string.login_error_parsing));
            return;
        }

        Elements issuesRaw = issuesContainer.children();

        for (Element i : issuesRaw) {
            Issue issueCore = new Issue();

            Elements issueContents = i.children();

            // Get issue ID and name
            Element issueMain = issueContents.select("a").first();

            if (issueMain == null) {
                continue;
            }

            String issueLink = issueMain.attr("href");
            issueCore.id = Integer.valueOf(issueLink.replace("page=show_dilemma/dilemma=", ""));
            Matcher chainMatcher = CHAIN_ISSUE_REGEX.matcher(issueMain.text());
            if (chainMatcher.find()) {
                issueCore.chain = chainMatcher.group(1);
                issueCore.title = chainMatcher.group(2);
            }
            else {
                issueCore.title = issueMain.text();
            }

            issues.add(issueCore);
        }

        Element nextIssueUpdate = d.select("p.dilemmanextupdate").first();
        if (nextIssueUpdate != null) {
            String nextUpdate = nextIssueUpdate.text();
            issues.add(nextUpdate);
        }

        if (issuesRaw.size() <= 0) {
            String nextUpdate = getString(R.string.no_issues);

            Matcher m = NEXT_ISSUE_REGEX.matcher(d.html());
            if (m.find()) {
                long nextUpdateTime = Long.valueOf(m.group(1)) / 1000L;
                nextUpdate = String.format(Locale.US, getString(R.string.next_issue), SparkleHelper.getReadableDateFromUTC(getContext(), nextUpdateTime));
            }

            issues.add(nextUpdate);
        }

        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssuesRecyclerAdapter(getContext(), issues, mNation);
        }
        else {
            ((IssuesRecyclerAdapter) mRecyclerAdapter).setIssueCards(issues);
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
