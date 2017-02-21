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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.simpleframework.xml.core.Persister;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-16.
 * A fragment part of the StatelyActivity used to show the World Assembly.
 * Gets WA data on its own, can also refresh!
 */
public class AssemblyMainFragment extends RefreshviewFragment {
    public static final String VOTE_STATUS_KEY = "voteStatus";

    private Assembly genAssembly;
    private Assembly secCouncil;
    private WaVoteStatus voteStatus;

    private void setGeneralAssembly(Assembly g)
    {
        genAssembly = g;
    }

    private void setSecurityCouncil(Assembly s)
    {
        secCouncil = s;
    }

    public void setVoteStatus(WaVoteStatus w)
    {
        voteStatus = w;
    }

    // Receiver for WA vote broadcasts
    private BroadcastReceiver resolutionVoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || !isAdded()) {
                return;
            }

            voteStatus = intent.getParcelableExtra(ResolutionActivity.TARGET_VOTE_STATUS);
            refreshRecycler();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(getString(R.string.menu_wa));

        // Restore state
        if (savedInstanceState != null && voteStatus == null) {
            voteStatus = savedInstanceState.getParcelable(VOTE_STATUS_KEY);
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                        @Override
                                                        public void onRefresh() {
                                                            queryWorldAssembly(mView);
                                                        }
                                                 });

        // Register resolution vote receiver
        IntentFilter resolutionVoteFilter = new IntentFilter();
        resolutionVoteFilter.addAction(ResolutionActivity.RESOLUTION_BROADCAST);
        getActivity().registerReceiver(resolutionVoteReceiver, resolutionVoteFilter);

        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        queryWorldAssembly(mView);
        return mView;
    }

    // Wrapper function for the heavy-lifting query function
    // Starts PART 1 of the query process
    private void queryWorldAssembly(View view) {
        queryWorldAssemblyHeavy(view, Assembly.GENERAL_ASSEMBLY);
    }

    /**
     * The heavy-lifting query function to get various WA data.
     * In order to avoid async issues (e.g. race conditions), the data calls will be done
     * in an synchronous-like manner.
     *
     * The process is as follows:
     *
     * 1. Query General Assembly
     * 2. Query Security Council
     * 3. Refresh the WA fragment
     * @param view
     * @param chamberId
     */
    private void queryWorldAssemblyHeavy(final View view, final int chamberId) {
        String targetURL = String.format(Locale.US, Assembly.QUERY, chamberId);

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Assembly waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            waResponse = Assembly.parseAssemblyXML(getContext(), serializer, response);

                            if (chamberId == Assembly.GENERAL_ASSEMBLY) {
                                // Once a response is obtained for the General Assembly,
                                // start querying for the Security Council
                                setGeneralAssembly(waResponse);
                                queryWorldAssemblyHeavy(view, Assembly.SECURITY_COUNCIL);
                            }
                            else if (chamberId == Assembly.SECURITY_COUNCIL) {
                                // Once a response is obtained for the Security Council,
                                // setup the actual view
                                setSecurityCouncil(waResponse);
                                refreshRecycler();
                            }
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

    private void refreshRecycler() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new AssemblyRecyclerAdapter(getContext(), genAssembly, secCouncil, voteStatus);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        else {
            ((AssemblyRecyclerAdapter) mRecyclerAdapter).setData(genAssembly, secCouncil, voteStatus);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (voteStatus != null) {
            savedInstanceState.putParcelable(VOTE_STATUS_KEY, voteStatus);
        }
    }
}
