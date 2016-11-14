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
import com.lloydtorres.stately.dto.IssueFullHolder;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.zombie.NightmareHelper;

import org.simpleframework.xml.core.Persister;

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
    private static final Pattern CHAIN_ISSUE_REGEX = Pattern.compile("^\\[(.+?)\\] (.+?)$");

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
        String targetURL = String.format(Locale.US, IssueFullHolder.QUERY, SparkleHelper.getIdFromName(mNation.name));

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            IssueFullHolder issueResponse = serializer.read(IssueFullHolder.class, response);
                            processIssues(view, issueResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                        }
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
     * @param v Root view
     * @param holder Issue response from NS
     */
    private void processIssues(View v, IssueFullHolder holder) {
        issues = new ArrayList<Object>();

        // Add zombie card if Z-Day is active
        if (NightmareHelper.getIsZDayActive(getContext()) && holder.zombieData != null) {
            issues.add(holder.zombieData);
        }

        if (holder.issues != null) {
            for (Issue i : holder.issues) {
                // Get data on issue chains
                Matcher chainMatcher = CHAIN_ISSUE_REGEX.matcher(i.title);
                if (chainMatcher.find()) {
                    i.chain = chainMatcher.group(1);
                    i.title = chainMatcher.group(2);
                }

                // Add dismiss option
                if (i.options == null) {
                    i.options = new ArrayList<IssueOption>();
                }

                IssueOption dismiss = new IssueOption();
                dismiss.id = IssueOption.DISMISS_ISSUE_ID;
                i.options.add(dismiss);

                for (int ind=0; ind < i.options.size(); ind++) {
                    i.options.get(ind).index = ind + 1;
                }

                issues.add(i);
            }
        }

        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime < holder.nextIssueTime) {
            issues.add(holder.nextIssueTime);
        }

        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssuesRecyclerAdapter(getContext(), issues, mNation);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssuesRecyclerAdapter) mRecyclerAdapter).setIssueCards(issues);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
