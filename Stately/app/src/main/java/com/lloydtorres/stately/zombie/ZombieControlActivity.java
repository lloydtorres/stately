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

package com.lloydtorres.stately.zombie;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.dto.ZombieControlData;
import com.lloydtorres.stately.dto.ZombieRegion;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.wa.VoteDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-10-16.
 * Lets users access zombie control and play along on Z-Day.
 */
public class ZombieControlActivity extends RefreshviewActivity {
    public static final String ZOMBIE_USER_DATA = "zombieUserData";
    public static final String ZOMBIE_REGION_DATA = "zombieRegionData";

    private ZombieControlData userData;
    private ZombieRegion regionData;
    private boolean isInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isInProgress = false;

        // Restore state
        if (savedInstanceState != null) {
            userData = savedInstanceState.getParcelable(ZOMBIE_USER_DATA);
            regionData = savedInstanceState.getParcelable(ZOMBIE_REGION_DATA);
        }

        getSupportActionBar().setTitle(getString(R.string.zombie_control));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryUserZombieData();
            }

        });
        if (userData != null && regionData != null) {
            initRecycler();
        } else {
            startQuery();
        }
    }

    /**
     * Helper for showing the refresh layout while querying.
     */
    private void startQuery() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryUserZombieData();
            }
        });
    }

    /**
     * Queries the user's zombie data.
     */
    private void queryUserZombieData() {
        String targetURL = String.format(Locale.US, ZombieControlData.QUERY, PinkaHelper.getActiveUser(this).nationId);
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            userData = serializer.read(ZombieControlData.class, response);
                            queryRegionZombieData();
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
                } else {
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
     * Queries the user's region's zombie data.
     */
    private void queryRegionZombieData() {
        String targetURL = String.format(Locale.US, ZombieRegion.QUERY, SparkleHelper.getIdFromName(PinkaHelper.getRegionSessionData(this)));
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            regionData = serializer.read(ZombieRegion.class, response);
                            initRecycler();
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
                } else {
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
     * Initializes the recyclerview.
     */
    private void initRecycler() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new ZombieControlRecyclerAdapter(this, getSupportFragmentManager(), userData, regionData);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((ZombieControlRecyclerAdapter) mRecyclerAdapter).setContent(userData, regionData);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Either shows the zombie decision dialog or a message saying that no actions are available.
     */
    public void showDecisionDialog() {
        ZombieDecisionDialog zombieDialog = new ZombieDecisionDialog();
        zombieDialog.setZombieData(userData.zombieData);
        zombieDialog.show(getSupportFragmentManager(), VoteDialog.DIALOG_TAG);
    }

    /**
     * Starts the process for submitting the user's Z-Day action.
     * @param action
     */
    public void startSubmitAction(final String action) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.multiple_request_error));
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                isInProgress = true;
                getLocalId(action);
            }
        });
    }

    /**
     * Gets the local ID to use for submitting the action.
     * @param action
     */
    private void getLocalId(final String action) {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, ZombieControlData.ZOMBIE_CONTROL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=localid]").first();

                        if (input == null) {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                            isInProgress = false;
                            return;
                        }

                        String localid = input.attr("value");
                        postZombieDecision(localid, action);
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
            isInProgress = false;
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually submits the decision to NS, then requeries the Z-Day endpoints.
     * @param localid Local ID for verification
     * @param action User action
     */
    private void postZombieDecision(final String localid, final String action) {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, ZombieControlData.ZOMBIE_CONTROL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (action) {
                            case Zombie.ZACTION_MILITARY:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.zombie_action_military_done));
                                break;
                            case Zombie.ZACTION_CURE:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.zombie_action_cure_done));
                                break;
                            case Zombie.ZACTION_ZOMBIE:
                                SparkleHelper.makeSnackbar(mView, getString(R.string.zombie_action_join_done));
                                break;
                        }
                        queryUserZombieData();
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
        params.put(String.format(Locale.US, Zombie.ZACTION_PARAM_BASE, action), "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (userData != null) {
            savedInstanceState.putParcelable(ZOMBIE_USER_DATA, userData);
        }
        if (regionData != null) {
            savedInstanceState.putParcelable(ZOMBIE_REGION_DATA, regionData);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (userData == null) {
                userData = savedInstanceState.getParcelable(ZOMBIE_USER_DATA);
            }
            if (regionData == null) {
                regionData = savedInstanceState.getParcelable(ZOMBIE_REGION_DATA);
            }
        }
    }
}
