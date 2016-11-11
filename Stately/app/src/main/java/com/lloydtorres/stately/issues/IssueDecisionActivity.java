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
import android.support.v7.app.AlertDialog;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueFullHolder;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.settings.SettingsActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-28.
 * This activity displays options for a particular issue.
 */
public class IssueDecisionActivity extends RefreshviewActivity {
    // Keys for Intent data
    public static final String ISSUE_DATA = "issueData";
    public static final String NATION_DATA = "nationData";

    private static final String LEGISLATION_PASSED = "LEGISLATION PASSED";
    private static final String NOT_AVAILABLE = "Issue Not Available";

    private Issue issue;
    private Nation mNation;

    private boolean isInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removePaddingTop();

        isInProgress = false;
        // Either get data from intent or restore state
        if (getIntent() != null) {
            issue = getIntent().getParcelableExtra(ISSUE_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null) {
            issue = savedInstanceState.getParcelable(ISSUE_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        getSupportActionBar().setTitle(String.format(Locale.US, getString(R.string.issue_activity_title), mNation.name, SparkleHelper.getPrettifiedNumber(issue.id)));
        mSwipeRefreshLayout.setEnabled(false);

        startConfirmIssueAvailable();
    }

    /**
     * Helper call to start check if issue is still available.
     */
    private void startConfirmIssueAvailable() {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                confirmIssueAvailable();
            }
        });
    }

    /**
     * Actually confirms that the issue is still available.
     */
    private void confirmIssueAvailable() {
        String targetURL = String.format(Locale.US, IssueFullHolder.CONFIRM_QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains(NOT_AVAILABLE)) {
                            SparkleHelper.makeSnackbar(mView, String.format(Locale.US, getString(R.string.issue_unavailable), mNation.name));
                        } else {
                            setRecyclerAdapter(issue);
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show the issue anyway on error
                SparkleHelper.logError(error.toString());
                setRecyclerAdapter(issue);
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            // Show the issue anyway if API limit reached
            mSwipeRefreshLayout.setRefreshing(false);
            setRecyclerAdapter(issue);
        }
    }

    /**
     * Either initializes the recycler adapter or resets the data.
     * @param issue
     */
    private void setRecyclerAdapter(Issue issue) {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssueDecisionRecyclerAdapter(this, issue);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssueDecisionRecyclerAdapter) mRecyclerAdapter).setIssue(issue);
        }
    }

    /**
     * Helper to confirm the position selected by the user.
     * @param option The option selected.
     */
    public void setAdoptPosition(final IssueOption option) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.multiple_request_error));
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPostAdoptPosition(option);
                dialog.dismiss();
            }
        };

        if (SettingsActivity.getConfirmIssueDecisionSetting(this)) {
            dialogBuilder
                    .setNegativeButton(getString(R.string.explore_negative), null);

            switch (option.id) {
                case IssueOption.DISMISS_ISSUE_ID:
                    dialogBuilder.setTitle(getString(R.string.issue_option_confirm_dismiss))
                            .setPositiveButton(getString(R.string.issue_option_dismiss), dialogClickListener);
                    break;
                default:
                    dialogBuilder.setTitle(String.format(Locale.US, getString(R.string.issue_option_confirm_adopt), option.index))
                            .setPositiveButton(getString(R.string.issue_option_adopt), dialogClickListener);
                    break;
            }

            if (!isFinishing()) {
                dialogBuilder.show();
            }
        }
        else {
            startPostAdoptPosition(option);
        }
    }

    private void startPostAdoptPosition(final IssueOption option) {
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
    public void postAdoptPosition(final IssueOption option) {
        isInProgress = true;
        String targetURL = String.format(Locale.US, IssueOption.POST_QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isInProgress = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (option.id != IssueOption.DISMISS_ISSUE_ID) {
                            if (response.contains(LEGISLATION_PASSED)) {
                                Intent issueResultsActivity = new Intent(IssueDecisionActivity.this, IssueResultsActivity.class);
                                issueResultsActivity.putExtra(IssueResultsActivity.RESPONSE_DATA, response);
                                issueResultsActivity.putExtra(IssueResultsActivity.OPTION_DATA, option);
                                issueResultsActivity.putExtra(IssueResultsActivity.NATION_DATA, mNation);
                                startActivity(issueResultsActivity);
                                finish();
                            }
                            else {
                                SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                            }
                        }
                        else {
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
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put(String.format(Locale.US, IssueOption.POST_HEADER_TEMPLATE, option.id), "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issue != null) {
            savedInstanceState.putParcelable(ISSUE_DATA, issue);
        }
        if (mNation != null) {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (issue == null) {
                issue = savedInstanceState.getParcelable(ISSUE_DATA);
            }
            if (mNation == null) {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
