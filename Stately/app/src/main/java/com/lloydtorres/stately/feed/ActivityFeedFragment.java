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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.IToolbarActivity;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.HappeningFeed;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.EventRecyclerAdapter;
import com.lloydtorres.stately.helpers.NameListDialog;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lloyd on 2016-02-08.
 * This fragment shows a user's activity feed.
 */
public class ActivityFeedFragment extends Fragment {
    public static final String NATION_KEY = "nationName";
    public static final String REGION_KEY = "regionName";
    public static final String DOSSIER_QUERY = "https://www.nationstates.net/page=dossier/template-overall=none";

    private static final String DOSSIER_CONFIRM = "Your Dossier is a collection of intelligence on nations and regions of interest.";
    private static final String NO_NATIONS = "You have no nations in your Dossier.";
    private static final String NO_REGIONS = "You have no regions in your Dossier.";
    private static final String NATION_LINK_PREFIX = "nation=";
    private static final String REGION_LINK_PREFIX = "region=";

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private SharedPreferences storage; // shared preferences
    private List<Event> events;
    private String nationName;
    private String regionName;
    private ArrayList<String> dossierNations = new ArrayList<String>();
    private List<String> dossierRegions = new ArrayList<String>();

    private AlertDialog.Builder dialogBuilder;

    public void setNationName(String n)
    {
        nationName = n;
    }

    public void setRegionName(String r)
    {
        regionName = r;
    }

    @Override
    public void onAttach(Context context) {
        // Get activity for manipulation
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        storage = PreferenceManager.getDefaultSharedPreferences(getContext());
        events = new ArrayList<Event>();
        dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MaterialDialog);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_refreshview, container, false);

        // Restore state
        if (savedInstanceState != null)
        {
            if (nationName == null)
            {
                nationName = savedInstanceState.getString(NATION_KEY);
            }
            if (regionName == null)
            {
                regionName = savedInstanceState.getString(REGION_KEY);
            }
        }

        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);
        toolbar.setTitle(getString(R.string.menu_activityfeed));

        if (mActivity != null && mActivity instanceof IToolbarActivity)
        {
            ((IToolbarActivity) mActivity).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getActivity() == null || !isAdded())
                {
                    return;
                }
                startQueryHappenings(false);
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        startQueryHappenings(true);
        return mView;
    }

    /**
     * Convenience method to show swipe refresh and start query.
     * @param firstRun If this is the first time the function is being called
     */
    public void startQueryHappenings(final boolean firstRun)
    {
        events = new ArrayList<Event>();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null || !isAdded())
                {
                    return;
                }

                mSwipeRefreshLayout.setRefreshing(true);

                if (firstRun)
                {
                    // Query dossier first when running for first time
                    queryDossier();
                }
                else
                {
                    // Just query regular happenings otherwise
                    queryNationalHappenings();
                }
            }
        });
    }

    /**
     * Queries a nation's dossier.
     */
    private void queryDossier()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, DOSSIER_QUERY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded())
                        {
                            return;
                        }

                        // Confirm that we're looking at the dossier page
                        if (response.contains(DOSSIER_CONFIRM))
                        {
                            Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                            boolean nationsExist = !response.contains(NO_NATIONS);
                            boolean regionsExist = !response.contains(NO_REGIONS);

                            // If there's nations in the dossier
                            if (nationsExist)
                            {
                                Element nationContainer = d.select("div.widebox").first();

                                if (nationContainer == null)
                                {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                                    return;
                                }

                                // Get nations in dossier
                                Elements nations = nationContainer.select("a.nlink");
                                for (Element e : nations)
                                {
                                    String id = e.attr("href").replace(NATION_LINK_PREFIX, "");
                                    dossierNations.add(SparkleHelper.getNameFromId(id));
                                }
                            }

                            // If there's regions in the dossier
                            if (regionsExist)
                            {
                                Element regionContainer = d.select("div.widebox").first();
                                // If nations are also on the dossier, regions is actually the second box
                                if (nationsExist)
                                {
                                    regionContainer = d.select("div.widebox").get(1);
                                }

                                if (regionContainer == null)
                                {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                                    return;
                                }

                                // Get regions in dossier
                                Elements regions = regionContainer.select("a.rlink");
                                for (Element e : regions)
                                {
                                    String id = e.attr("href").replace(REGION_LINK_PREFIX, "");
                                    dossierRegions.add(SparkleHelper.getNameFromId(id));
                                }
                            }

                            queryNationalHappenings();
                        }
                        else
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded())
                {
                    return;
                }
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                if (getActivity() != null && isAdded())
                {
                    UserLogin u = SparkleHelper.getActiveUser(getContext());
                    params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                    params.put("Cookie", String.format("autologin=%s", u.autologin));
                }
                return params;
            }
        };

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
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
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_NATION, true))
        {
            UserLogin curNation = SparkleHelper.getActiveUser(getContext());
            nationQuery.add(curNation.nationId);
        }

        // Include switch nations?
        if (storage.getBoolean(SubscriptionsDialog.SWITCH_NATIONS, true))
        {
            // Query all user logins, sort then remove current nation
            List<UserLogin> switchNations = UserLogin.listAll(UserLogin.class);
            Collections.sort(switchNations);
            for (UserLogin u : switchNations)
            {
                nationQuery.add(u.nationId);
            }
        }

        // Include dossier nations?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_NATIONS, true))
        {
            nationQuery.addAll(dossierNations);
        }

        if (nationQuery.size() >= 0) {
            String target = String.format(Locale.US, HappeningFeed.QUERY_NATION, SparkleHelper.joinStringList(nationQuery,","));

            StringRequest stringRequest = new StringRequest(Request.Method.GET, target,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded())
                            {
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
                    if (getActivity() == null || !isAdded())
                    {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        // No connection, just show results now
                        finishHappeningQuery();
                    }
                    else if (error instanceof ServerError)
                    {
                        // if some data seems missing, continue anyway
                        queryRegionalHappenings();
                    }
                    else
                    {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        queryRegionalHappenings();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> params = new HashMap<String, String>();
                    if (getActivity() != null && isAdded())
                    {
                        UserLogin u = SparkleHelper.getActiveUser(getContext());
                        params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                    }
                    return params;
                }
            };

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
            {
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
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_REGION, true))
        {
            regionQuery.add(regionName);
        }

        // Include dossier regions?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_REGIONS, true))
        {
            regionQuery.addAll(dossierRegions);
        }

        if (regionQuery.size() >= 0) {
            String target = String.format(Locale.US, HappeningFeed.QUERY_REGION, SparkleHelper.joinStringList(regionQuery,","));

            StringRequest stringRequest = new StringRequest(Request.Method.GET, target,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded())
                            {
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
                    if (getActivity() == null || !isAdded())
                    {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                        // No connection, just show results now
                        finishHappeningQuery();
                    }
                    else if (error instanceof ServerError)
                    {
                        // if some data seems missing, continue anyway
                        queryAssemblyHappenings();
                    }
                    else
                    {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                        queryRegionalHappenings();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> params = new HashMap<String, String>();
                    if (getActivity() != null && isAdded())
                    {
                        UserLogin u = SparkleHelper.getActiveUser(getContext());
                        params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                    }
                    return params;
                }
            };

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
            {
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
        if (storage.getBoolean(SubscriptionsDialog.WORLD_ASSEMBLY, true))
        {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, HappeningFeed.QUERY_WA,
                    new Response.Listener<String>() {
                        HappeningFeed happeningResponse = null;
                        @Override
                        public void onResponse(String response) {
                            if (getActivity() == null || !isAdded())
                            {
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
                    if (getActivity() == null || !isAdded())
                    {
                        return;
                    }
                    SparkleHelper.logError(error.toString());
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                    }
                    else
                    {
                        SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                    }
                    finishHappeningQuery();
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> params = new HashMap<String, String>();
                    if (getActivity() != null && isAdded())
                    {
                        UserLogin u = SparkleHelper.getActiveUser(getContext());
                        params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                    }
                    return params;
                }
            };

            if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
            {
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
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (nationName != null)
        {
            savedInstanceState.putString(NATION_KEY, nationName);
        }
        if (regionName != null)
        {
            savedInstanceState.putString(REGION_KEY, regionName);
        }
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
    private void showNationDossier(FragmentManager fm)
    {
        if (dossierNations.size() > 0)
        {
            Collections.sort(dossierNations);
            NameListDialog nameListDialog = new NameListDialog();
            nameListDialog.setTitle(getString(R.string.activityfeed_dossier_n));
            nameListDialog.setNames(dossierNations);
            nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
        }
        else
        {
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
    private void showRegionDossier(FragmentManager fm)
    {
        if (dossierRegions.size() > 0)
        {
            Collections.sort(dossierRegions);
            NameListDialog nameListDialog = new NameListDialog();
            nameListDialog.setTitle(getString(R.string.activityfeed_dossier_r));
            nameListDialog.setNames((ArrayList<String>) dossierRegions);
            nameListDialog.setTarget(SparkleHelper.CLICKY_REGION_MODE);
            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
        }
        else
        {
            dialogBuilder.setTitle(R.string.activityfeed_dossier_r)
                    .setMessage(R.string.dossier_r_none)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        }
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
