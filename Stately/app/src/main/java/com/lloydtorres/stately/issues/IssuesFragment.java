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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.BroadcastableActivity;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-28.
 * A fragment to display current issues.
 */
public class IssuesFragment extends RefreshviewFragment {
    // on save keys
    private static final String KEY_ISSUES_DATA = "issuesData";
    private static final String KEY_NATION_DATA = "nationData";
    private static final String KEY_NEXT_ISSUE_TIME_DATA = "nextIssueTimeData";

    private static final Pattern CHAIN_ISSUE_REGEX = Pattern.compile("^\\[(.+?)\\] (.+?)$");
    private static final Pattern EASTER_EGG_ISSUE_REGEX = Pattern.compile("^Easter Egg: (.+?)$");

    private ArrayList<Parcelable> issues;
    private Nation mNation;
    private long nextIssueTime = IssueFullHolder.UNKNOWN_NEXT_ISSUE_TIME;
    private final BroadcastReceiver issueDecisionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || !isAdded() || issues == null) {
                return;
            }

            // Mark the decided issue to be removed
            int issueToRemove = intent.getIntExtra(IssueDecisionActivity.ISSUE_ID_DATA, -1);
            removeIssue(issueToRemove);
            refreshRecycler();
        }
    };

    public void setNationData(Nation n) {
        mNation = n;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(getString(R.string.menu_issues));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryIssues(mView);
            }
        });

        // Register receiver
        IntentFilter issueDecisionFilter = new IntentFilter();
        issueDecisionFilter.addAction(IssueDecisionActivity.ISSUE_BROADCAST);
        ((BroadcastableActivity) getActivity()).registerBroadcastReceiver(issueDecisionReceiver,
                issueDecisionFilter);

        // Restore state
        if (savedInstanceState != null) {
            if (issues == null) {
                issues = savedInstanceState.getParcelableArrayList(KEY_ISSUES_DATA);
            }
            if (mNation == null) {
                mNation = savedInstanceState.getParcelable(KEY_NATION_DATA);
            }
            nextIssueTime = savedInstanceState.getLong(KEY_NEXT_ISSUE_TIME_DATA,
                    IssueFullHolder.UNKNOWN_NEXT_ISSUE_TIME);
        }

        return mView;
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
        String targetURL = String.format(Locale.US, IssueFullHolder.QUERY,
                SparkleHelper.getIdFromName(mNation.name));

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET,
                targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            IssueFullHolder issueResponse = serializer.read(IssueFullHolder.class,
                                    response);
                            processIssues(issueResponse);
                        } catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.login_error_parsing));
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
                } else {
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
     * @param holder Issue response from NS
     */
    private void processIssues(IssueFullHolder holder) {
        issues = new ArrayList<Parcelable>();

        // Add zombie card if Z-Day is active
        if (NightmareHelper.getIsZDayActive(getContext()) && holder.zombieData != null) {
            issues.add(holder.zombieData);
        }

        if (holder.issues != null) {
            for (Issue i : holder.issues) {
                // Get data on issue chains
                Matcher chainMatcher = CHAIN_ISSUE_REGEX.matcher(i.title);
                Matcher easterEggMatcher = EASTER_EGG_ISSUE_REGEX.matcher(i.title);
                if (chainMatcher.find()) {
                    i.chain = chainMatcher.group(1);
                    i.title = chainMatcher.group(2);
                } else if (easterEggMatcher.find()) {
                    i.chain = getString(R.string.issue_easter_egg);
                    i.title = easterEggMatcher.group(1);
                }

                // Add dismiss option
                if (i.options == null) {
                    i.options = new ArrayList<IssueOption>();
                }

                IssueOption dismiss = new IssueOption();
                dismiss.id = IssueOption.DISMISS_ISSUE_ID;
                i.options.add(dismiss);

                for (int ind = 0; ind < i.options.size(); ind++) {
                    i.options.get(ind).index = ind + 1;
                }

                issues.add(i);
            }
        }

        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime < holder.nextIssueTime) {
            nextIssueTime = holder.nextIssueTime;
        }

        refreshRecycler();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Given an issue ID, removes the first issue with that issue ID from the list.
     * @param id
     */
    public void removeIssue(int id) {
        for (int i = 0; i < issues.size(); i++) {
            Parcelable card = issues.get(i);
            if (card instanceof Issue) {
                Issue issueCard = (Issue) card;
                if (issueCard.id == id) {
                    issues.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * If the recycler doesn't exist, create a new one. Otherwise just update the contents of the
     * existing one.
     */
    public void refreshRecycler() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssuesRecyclerAdapter(getContext(), issues, nextIssueTime,
                    mNation);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssuesRecyclerAdapter) mRecyclerAdapter).setIssueCards(issues, nextIssueTime);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only requery issues on resume if no data available or
        // the next issue time is unknown or the time to next issue has passed
        // and the issues list isn't already at max
        int maxIssues = !NightmareHelper.getIsZDayActive(getContext()) ?
                IssueFullHolder.MAX_ISSUE_COUNT_REGULAR : IssueFullHolder.MAX_ISSUE_COUNT_ZOMBIE;
        if (issues == null ||
                ((nextIssueTime == IssueFullHolder.UNKNOWN_NEXT_ISSUE_TIME || nextIssueTime < System.currentTimeMillis() / 1000L)
                        && issues.size() < maxIssues)) {
            startQueryIssues();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issues != null) {
            savedInstanceState.putParcelableArrayList(KEY_ISSUES_DATA, issues);
        }
        if (mNation != null) {
            savedInstanceState.putParcelable(KEY_NATION_DATA, mNation);
        }
        savedInstanceState.putLong(KEY_NEXT_ISSUE_TIME_DATA, nextIssueTime);
    }
}
