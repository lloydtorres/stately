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

package com.lloydtorres.stately.feed;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewFragment;
import com.lloydtorres.stately.dto.Dossier;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.HappeningFeed;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.helpers.happenings.EventRecyclerAdapter;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Lloyd on 2016-02-08.
 * This fragment shows a user's activity feed.
 */
public class ActivityFeedFragment extends RefreshviewFragment {

    private SharedPreferences storage; // shared preferences
    private List<Event> events = new ArrayList<Event>();
    private ArrayList<String> dossierNations = new ArrayList<String>();
    private ArrayList<String> dossierRegions = new ArrayList<String>();

    private AlertDialog.Builder dialogBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = PreferenceManager.getDefaultSharedPreferences(getContext());
        dialogBuilder = new AlertDialog.Builder(getContext(), SparkleHelper.getThemeMaterialDialog(getContext()));
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);

        toolbar.setTitle(getString(R.string.menu_activityfeed));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                startQueryHappenings(false);
            }
        });

        startQueryHappenings(true);
        return mView;
    }

    /**
     * Convenience method to show swipe refresh and start query.
     * @param firstRun If this is the first time the function is being called
     */
    public void startQueryHappenings(final boolean firstRun) {
        events = new ArrayList<Event>();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                mSwipeRefreshLayout.setRefreshing(true);

                if (firstRun) {
                    // Query dossier first when running for first time
                    queryDossier();
                }
                else {
                    // Just query regular happenings otherwise
                    queryNationalHappenings();
                }
            }
        });
    }

    /**
     * Queries a nation's dossier.
     */
    private void queryDossier() {
        String userId = SparkleHelper.getActiveUser(getContext()).nationId;
        String targetURL = String.format(Locale.US, Dossier.QUERY, SparkleHelper.getIdFromName(userId));

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        Dossier dossierResponse;
                        Persister serializer = new Persister();
                        try {
                            dossierResponse = serializer.read(Dossier.class, response);
                            if (dossierResponse.nations != null) {
                                for (String n : dossierResponse.nations) {
                                    dossierNations.add(SparkleHelper.getNameFromId(n));
                                }
                            }
                            if (dossierResponse.regions != null) {
                                for (String r : dossierResponse.regions) {
                                    dossierRegions.add(SparkleHelper.getNameFromId(r));
                                }
                            }
                        }
                        catch (Exception e) {
                            // Keep going even if there's an error
                            SparkleHelper.logError(e.toString());
                        }
                        queryNationalHappenings();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                // Keep going even if there's an error
                SparkleHelper.logError(error.toString());
                queryNationalHappenings();
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * First part of query sequence. Builds a list of nations then performs the query.
     */
    private void queryNationalHappenings() {
        // Build list of nations to query
        // Used for enforcing unique nations
        Set<String> nationQuery = new LinkedHashSet<String>();

        // Include current nation?
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_NATION, true)) {
            UserLogin curNation = SparkleHelper.getActiveUser(getContext());
            nationQuery.add(curNation.nationId);
        }

        // Include switch nations?
        if (storage.getBoolean(SubscriptionsDialog.SWITCH_NATIONS, true)) {
            // Query all user logins, sort then remove current nation
            List<UserLogin> switchNations = UserLogin.listAll(UserLogin.class);
            Collections.sort(switchNations);
            for (UserLogin u : switchNations) {
                nationQuery.add(u.nationId);
            }
        }

        // Include dossier nations?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_NATIONS, true)) {
            for (String n : dossierNations) {
                nationQuery.add(SparkleHelper.getIdFromName(n));
            }
        }

        if (nationQuery.size() > 0) {
            String target = String.format(Locale.US, HappeningFeed.QUERY_NATION, SparkleHelper.joinStringList(nationQuery,","));

            NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, target,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded()) {
                                return;
                            }

                            Persister serializer = new Persister();
                            try {
                                happeningResponse = serializer.read(HappeningFeed.class, response);
                                events.addAll(happeningResponse.happenings);
                            }
                            catch (Exception e) {
                                SparkleHelper.logError(e.toString());
                                SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            }
                            queryRegionalHappenings();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        // No connection, just show results now
                        finishHappeningQuery();
                    }
                    else if (error instanceof ServerError) {
                        // if some data seems missing, continue anyway
                        queryRegionalHappenings();
                    }
                    else {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        queryRegionalHappenings();
                    }
                }
            });

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
                // No connection, just show results now
                finishHappeningQuery();
                SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
            }
        }
        else {
            queryRegionalHappenings();
        }
    }

    /**
     * Second part of query sequence. Builds a list of regions then performs the query.
     */
    private void queryRegionalHappenings() {
        // Build list of regions to query
        // Used for enforcing unique regions
        Set<String> regionQuery = new LinkedHashSet<String>();

        // Include current region?
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_REGION, true)) {
            regionQuery.add(SparkleHelper.getIdFromName(SparkleHelper.getRegionSessionData(getContext())));
        }

        // Include dossier regions?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_REGIONS, true)) {
            for (String r : dossierRegions) {
                regionQuery.add(SparkleHelper.getIdFromName(r));
            }
        }

        if (regionQuery.size() > 0) {
            String target = String.format(Locale.US, HappeningFeed.QUERY_REGION, SparkleHelper.joinStringList(regionQuery,","));

            NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, target,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded()) {
                                return;
                            }

                            Persister serializer = new Persister();
                            try {
                                happeningResponse = serializer.read(HappeningFeed.class, response);
                                events.addAll(happeningResponse.happenings);
                            }
                            catch (Exception e) {
                                SparkleHelper.logError(e.toString());
                                SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            }
                            queryAssemblyHappenings();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        // No connection, just show results now
                        finishHappeningQuery();
                    }
                    else if (error instanceof ServerError) {
                        // if some data seems missing, continue anyway
                        queryAssemblyHappenings();
                    }
                    else {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        queryAssemblyHappenings();
                    }
                }
            });

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
                // No connection, just show results now
                finishHappeningQuery();
                SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
            }
        }
        else {
            queryAssemblyHappenings();
        }
    }

    /**
     * Third part of query sequence. Checks if WA happenings should be queried.
     */
    private void queryAssemblyHappenings() {
        if (storage.getBoolean(SubscriptionsDialog.WORLD_ASSEMBLY, true)) {
            NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, HappeningFeed.QUERY_WA,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded()) {
                                return;
                            }

                            Persister serializer = new Persister();
                            try {
                                happeningResponse = serializer.read(HappeningFeed.class, response);
                                events.addAll(happeningResponse.happenings);
                            }
                            catch (Exception e) {
                                SparkleHelper.logError(e.toString());
                                SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            }
                            finishHappeningQuery();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                    }
                    else {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                    }
                    finishHappeningQuery();
                }
            });

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
                finishHappeningQuery();
                SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
            }
        }
        else {
            finishHappeningQuery();
        }
    }

    /**
     * Sets up the list of happenings and finishes the queries.
     */
    private void finishHappeningQuery() {
        Collections.sort(events);
        mRecyclerAdapter = new EventRecyclerAdapter(getContext(), events);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_activityfeed, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getChildFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_dossier_n:
                showNationDossier(fm);
                return true;
            case R.id.nav_dossier_r:
                showRegionDossier(fm);
                return true;
            case R.id.nav_subscriptions:
                SubscriptionsDialog subscriptionsDialog = new SubscriptionsDialog();
                subscriptionsDialog.setCallback(this);
                subscriptionsDialog.show(fm, SubscriptionsDialog.DIALOG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup and show contents of the nation dossier
     * @param fm Fragment Manager
     */
    private void showNationDossier(FragmentManager fm) {
        if (dossierNations.size() > 0) {
            Collections.sort(dossierNations);
            NameListDialog nameListDialog = new NameListDialog();
            nameListDialog.setTitle(getString(R.string.activityfeed_dossier_n));
            nameListDialog.setNames(dossierNations);
            nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
        }
        else {
            dialogBuilder.setTitle(R.string.activityfeed_dossier_n)
                    .setMessage(R.string.dossier_n_none)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        }
    }

    /**
     * Setup and show contents of the region dossier
     * @param fm Fragment Manager
     */
    private void showRegionDossier(FragmentManager fm) {
        if (dossierRegions.size() > 0) {
            Collections.sort(dossierRegions);
            NameListDialog nameListDialog = new NameListDialog();
            nameListDialog.setTitle(getString(R.string.activityfeed_dossier_r));
            nameListDialog.setNames(dossierRegions);
            nameListDialog.setTarget(SparkleHelper.CLICKY_REGION_MODE);
            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
        }
        else {
            dialogBuilder.setTitle(R.string.activityfeed_dossier_r)
                    .setMessage(R.string.dossier_r_none)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        }
    }
}
