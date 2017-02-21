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

package com.lloydtorres.stately.wa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.BaseAssembly;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-17.
 * This activity shows an active resolution from either WA chamber.
 * Takes in the chamber (council) ID to get the right chamber, as well as a
 * Resolution object (optional). If Resolution is null, it can get it on its own.
 * Also has refreshing!
 */
public class ResolutionActivity extends RefreshviewActivity {
    // Uri to invoke ResolutionActivity
    public static final String RESOLUTION_PROTOCOL = "com.lloydtorres.stately.resolution";
    public static final String RESOLUTION_TARGET = RESOLUTION_PROTOCOL + "://";
    public static final String RESOLUTION_BROADCAST = RESOLUTION_PROTOCOL + ".RESOLUTION_VOTE";

    // Keys for Intent data
    public static final String TARGET_COUNCIL_ID = "councilId";
    public static final String TARGET_RESOLUTION = "resolution";
    public static final String TARGET_VOTE_STATUS = "voteStatus";
    public static final String TARGET_OVERRIDE_RES_ID = "overrideResId";
    public static final String TARGET_IS_ACTIVE = "isActive";
    public static final String TARGET_OLD_VOTE_STATUS = "oldVoteStatus";

    private static final int NO_RESOLUTION = -1;

    private Resolution mResolution;
    private WaVoteStatus voteStatus;
    private int councilId;
    private int overrideResId = NO_RESOLUTION;
    private boolean isActive = true;

    private boolean isInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removePaddingTop();

        isInProgress = false;

        // Either get data from intent or restore state
        if (getIntent() != null) {
            councilId = getIntent().getIntExtra(TARGET_COUNCIL_ID, 1);
            mResolution = getIntent().getParcelableExtra(TARGET_RESOLUTION);
            voteStatus = getIntent().getParcelableExtra(TARGET_VOTE_STATUS);
            overrideResId = getIntent().getIntExtra(TARGET_OVERRIDE_RES_ID, NO_RESOLUTION);

            if (getIntent().getData() != null) {
                // Handle invocations via URL
                councilId = Integer.valueOf(getIntent().getData().getHost());
                overrideResId = Integer.valueOf(getIntent().getData().getLastPathSegment()) + 1;
            }
        }
        if (savedInstanceState != null) {
            councilId = savedInstanceState.getInt(TARGET_COUNCIL_ID);
            mResolution = savedInstanceState.getParcelable(TARGET_RESOLUTION);
            voteStatus = savedInstanceState.getParcelable(TARGET_VOTE_STATUS);
        }

        isActive =  overrideResId == NO_RESOLUTION;

        setToolbar();

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryResolution(councilId);
            }
        });

        // if no resolution passed in, go get it from server.
        if (mResolution == null) {
            startQueryResolution();
        }
        // Otherwise just show it normally
        else {
            setRecyclerAdapter();
        }
    }

    private void setToolbar() {
        switch (councilId) {
            case Assembly.GENERAL_ASSEMBLY:
                getSupportActionBar().setTitle(getString(R.string.wa_general_assembly));
                break;
            case Assembly.SECURITY_COUNCIL:
                getSupportActionBar().setTitle(getString(R.string.wa_security_council));
                break;
        }
    }

    private void setRecyclerAdapter() {
        String voteStats = null;
        if (voteStatus != null) {
            switch(councilId) {
                case Assembly.GENERAL_ASSEMBLY:
                    voteStats = voteStatus.gaVote;
                    break;
                case Assembly.SECURITY_COUNCIL:
                    voteStats = voteStatus.scVote;
                    break;
            }
        }

        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new ResolutionRecyclerAdapter(this, mResolution, voteStats, councilId);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((ResolutionRecyclerAdapter) mRecyclerAdapter).setUpdatedResolutionData(mResolution, voteStats);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void startQueryResolution() {
        // hack to get swiperefreshlayout to show
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryResolution(councilId);
            }
        });
    }

    /**
     * Queries the resolution from the specified chamber, then calls to check the nation status.
     * @param chamberId Current WA chamber being checked
     */
    private void queryResolution(int chamberId) {
        String targetURL = String.format(Locale.US, Resolution.QUERY, chamberId);
        if (overrideResId != NO_RESOLUTION) {
            targetURL = String.format(Locale.US, Resolution.QUERY_INACTIVE, chamberId, overrideResId);
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            BaseAssembly waResponse = BaseAssembly.parseAssemblyXML(ResolutionActivity.this, serializer, response);
                            mResolution = waResponse.resolution;

                            // Resolution doesn't exist, stop now
                            if (mResolution == null || mResolution.name == null) {
                                SparkleHelper.makeSnackbar(mView, getString(R.string.wa_error));
                                mSwipeRefreshLayout.setRefreshing(false);
                                return;
                            }

                            if (isActive) {
                                queryVoteStatus();
                            }  else {
                                setRecyclerAdapter();
                            }
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Called from queryResolution(). Checks the current nation's WA voting rights.
     */
    private void queryVoteStatus() {
        UserLogin u = PinkaHelper.getActiveUser(this);
        String targetURL = String.format(Locale.US, WaVoteStatus.QUERY, u.nationId);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            WaVoteStatus oldVoteStatus = voteStatus;
                            voteStatus = serializer.read(WaVoteStatus.class, response);
                            PinkaHelper.setWaSessionData(ResolutionActivity.this, voteStatus.waState);

                            // Send broadcast containing data about the user's WA votes
                            Intent resolutionVoteBroadcast = new Intent();
                            resolutionVoteBroadcast.setAction(RESOLUTION_BROADCAST);
                            resolutionVoteBroadcast.putExtra(TARGET_VOTE_STATUS, voteStatus);
                            resolutionVoteBroadcast.putExtra(TARGET_OLD_VOTE_STATUS, oldVoteStatus);
                            sendBroadcast(resolutionVoteBroadcast);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                        }

                        setRecyclerAdapter();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Continue even on error
                SparkleHelper.logError(error.toString());
                setRecyclerAdapter();
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            // Continue even on error
            setRecyclerAdapter();
        }
    }

    /**
     * Convenience class to show voting dialog.
     * @param vote Current choice in voting
     */
    public void showVoteDialog(int vote) {
        if (isFinishing()) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        VoteDialog voteDialog = new VoteDialog();
        voteDialog.setChoice(vote);
        voteDialog.show(fm, VoteDialog.DIALOG_TAG);
    }

    /**
     * Starts the vote submission process.
     * @param choice Voting choice.
     */
    public void submitVote(int choice) {
        String url;
        switch(councilId) {
            case Assembly.GENERAL_ASSEMBLY:
                url = Assembly.TARGET_GA;
                break;
            default:
                url = Assembly.TARGET_SC;
                break;
        }
        getLocalId(url, choice);
    }

    /**
     * Gets the required localid to post vote.
     * @param url Target URL to scrape.
     * @param p Vote
     */
    private void getLocalId(final String url, final int p) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=localid]").first();

                        if (input == null) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            return;
                        }

                        String localid = input.attr("value");
                        postVote(url, localid, p);
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

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually post the user's vote
     * @param url Target URL.
     * @param localid Required localid
     * @param p Vote
     */
    private void postVote(final String url, final String localid, final int p) {
        final String votePost;
        switch (p) {
            case VoteDialog.VOTE_FOR:
                votePost = getString(R.string.wa_post_for);
                break;
            case VoteDialog.VOTE_AGAINST:
                votePost = getString(R.string.wa_post_against);
                break;
            default:
                votePost = getString(R.string.wa_post_undecided);
                break;
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch(p) {
                            case VoteDialog.VOTE_FOR:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.wa_resolution_vote_for));
                                break;
                            case VoteDialog.VOTE_AGAINST:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.wa_resolution_vote_against));
                                break;
                            default:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.wa_resolution_vote_undecided));
                                break;
                        }
                        isInProgress = false;
                        startQueryResolution();
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
        params.put("localid", localid);
        params.put("vote", votePost);
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TARGET_COUNCIL_ID, councilId);
        if (mResolution != null) {
            savedInstanceState.putParcelable(TARGET_RESOLUTION, mResolution);
        }
        if (voteStatus != null) {
            savedInstanceState.putParcelable(TARGET_VOTE_STATUS, voteStatus);
        }
        savedInstanceState.putInt(TARGET_OVERRIDE_RES_ID, overrideResId);
        savedInstanceState.putBoolean(TARGET_IS_ACTIVE, isActive);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            councilId = savedInstanceState.getInt(TARGET_COUNCIL_ID);
            if (mResolution == null) {
                mResolution = savedInstanceState.getParcelable(TARGET_RESOLUTION);
            }
            if (voteStatus == null) {
                voteStatus = savedInstanceState.getParcelable(TARGET_VOTE_STATUS);
            }
            overrideResId = savedInstanceState.getInt(TARGET_OVERRIDE_RES_ID);
            isActive = savedInstanceState.getBoolean(TARGET_IS_ACTIVE);
        }
    }
}
