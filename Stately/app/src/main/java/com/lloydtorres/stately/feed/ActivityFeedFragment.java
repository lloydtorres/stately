package com.lloydtorres.stately.feed;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.HappeningFeed;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.EventRecyclerAdapter;
import com.lloydtorres.stately.helpers.NameListDialog;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private static final int SWITCH_LIMIT = 10;

    private static final String DOSSIER_CONFIRM = "Your Dossier is a collection of intelligence on nations and regions of interest.";
    private static final String NO_NATIONS = "You have no nations in your Dossier.";
    private static final String NO_REGIONS = "You have no regions in your Dossier.";
    private static final String NATION_LINK_PREFIX = "nation=";
    private static final String REGION_LINK_PREFIX = "region=";

    private Activity mActivity;
    private View mView;
    private View mainView;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private SharedPreferences storage; // shared preferences
    private List<Event> events;
    private String nationName;
    private String regionName;
    private List<UserLogin> dossierNations = new ArrayList<UserLogin>();
    private List<String> dossierRegions = new ArrayList<String>();

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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_activityfeed, container, false);
        mainView = mView.findViewById(R.id.refreshview_main);
        SparkleHelper.initAd(mView, R.id.ad_activityfeed_fragment);

        // Restore state
        if (savedInstanceState != null)
        {
            if (nationName == null)
            {
                nationName = savedInstanceState.getParcelable(NATION_KEY);
            }
            if (regionName == null)
            {
                regionName = savedInstanceState.getParcelable(REGION_KEY);
            }
        }

        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);
        toolbar.setTitle(getActivity().getString(R.string.menu_activityfeed));

        if (mActivity != null && mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
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

                events = new ArrayList<Event>();
                queryHappenings(buildHappeningsQuery());
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
                    queryHappenings(buildHappeningsQuery());
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
                                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_parsing));
                                    return;
                                }

                                // Get nations in dossier
                                Elements nations = nationContainer.select("a.nlink");
                                for (Element e : nations)
                                {
                                    String id = e.attr("href").replace(NATION_LINK_PREFIX, "");
                                    UserLogin n = new UserLogin();
                                    n.nationId = id;
                                    n.name = SparkleHelper.getNameFromId(id);
                                    dossierNations.add(n);
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
                                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_parsing));
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

                            queryHappenings(buildHappeningsQuery());
                        }
                        else
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_generic));
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
                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                if (getActivity() != null && isAdded())
                {
                    UserLogin u = SparkleHelper.getActiveUser(getContext());
                    params.put("Cookie", String.format("autologin=%s", u.autologin));
                }
                return params;
            }
        };

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mainView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Builds the list to be passed into queryHappenings();
     * If it's a UserLogin --> Nation
     * If it's a String --> Region
     * If it's an Integer --> World Assembly
     */
    private List<Object> buildHappeningsQuery()
    {
        List<Object> q = new ArrayList<Object>();
        UserLogin curNation = SparkleHelper.getActiveUser(getContext());

        // Include current nation?
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_NATION, true))
        {
            q.add(curNation);
        }

        // Used for enforcing unique nations
        Set<String> uniqueEnforcer = new HashSet<String>();

        // Include switch nations?
        if (storage.getBoolean(SubscriptionsDialog.SWITCH_NATIONS, true))
        {
            // Query all user logins, sort then remove current nation
            List<UserLogin> switchNations = UserLogin.listAll(UserLogin.class);
            Collections.sort(switchNations);
            for (UserLogin u : switchNations)
            {
                if (u.nationId.equals(curNation.nationId))
                {
                    switchNations.remove(u);
                    break;
                }
            }

            // Only get first 10
            if (switchNations.size() >= SWITCH_LIMIT)
            {
                switchNations = switchNations.subList(0, SWITCH_LIMIT);
            }
            q.addAll(switchNations);

            // Add to unique enforcer
            for (UserLogin s : switchNations)
            {
                uniqueEnforcer.add(s.nationId);
            }
        }

        // Include dossier nations?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_NATIONS, true))
        {
            // Only add entries not already being queried
            // and limit to 10
            int dossierNationCounter = 0;
            for (UserLogin n : dossierNations)
            {
                if (!uniqueEnforcer.contains(n.nationId))
                {
                    if (++dossierNationCounter > SWITCH_LIMIT)
                    {
                        break;
                    }
                    q.add(n);
                }
            }
        }

        // Flag to track if self region was added
        boolean regionAdded = false;

        // Include current region?
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_REGION, true))
        {
            q.add(regionName);
            regionAdded = true;
        }

        // Include dossier regions?
        if (storage.getBoolean(SubscriptionsDialog.DOSSIER_REGIONS, true))
        {
            List<String> fDossierRegions = new ArrayList<String>();
            if (regionAdded)
            {
                // If region already added, we need to get rid of the self-region entry
                for (String r : dossierRegions)
                {
                    if (!r.equals(regionName))
                    {
                        fDossierRegions.add(r);
                    }
                }
            }
            else
            {
                fDossierRegions = dossierRegions;
            }

            // Only get first 10
            if (fDossierRegions.size() >= SWITCH_LIMIT)
            {
                fDossierRegions = fDossierRegions.subList(0, SWITCH_LIMIT);
            }

            q.addAll(fDossierRegions);
        }

        // Include World Assembly?
        if (storage.getBoolean(SubscriptionsDialog.WORLD_ASSEMBLY, true))
        {
            q.add(0);
        }

        return q;
    }

    /**
     * Convenience method for handling happening queries.
     * @param query Remaining queries
     */
    private void queryHappenings(List<Object> query)
    {
        if (query.size() <= 0)
        {
            Collections.sort(events);
            mRecyclerAdapter = new EventRecyclerAdapter(getContext(), events);
            mRecyclerView.setAdapter(mRecyclerAdapter);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        else
        {
            Object q = query.remove(0);

            String url = "";
            String appendName = null;
            // If query is for a nation
            if (q instanceof UserLogin)
            {
                url = String.format(HappeningFeed.QUERY_NATION, ((UserLogin) q).nationId);
            }
            // If query is for a region
            else if (q instanceof String)
            {
                url = String.format(HappeningFeed.QUERY_REGION, SparkleHelper.getIdFromName((String) q));
                appendName = (String) q;
            }
            // If query is for the World Assembly
            else if (q instanceof Integer)
            {
                url = HappeningFeed.QUERY_WA;
            }
            queryHappeningsHeavy(url, query, appendName);
        }
    }

    /**
     * Heavy-lifting for querying happenings.
     * @param target Target URL
     * @param remainingQueries Queries remaining that need to be run
     * @param appendName If target name needs to be appended to happenings
     */
    private void queryHappeningsHeavy(final String target, final List<Object> remainingQueries, final String appendName)
    {
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
                            List<Event> queriedHappenings = happeningResponse.happenings;

                            if (appendName != null)
                            {
                                for (Event e : queriedHappenings)
                                {
                                    e.content = String.format(getString(R.string.activityfeed_append), appendName, e.content);
                                }
                            }

                            events.addAll(queriedHappenings);
                            queryHappenings(remainingQueries);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_parsing));
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
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_no_internet));
                    // force queryHappenings to load recyclerview
                    queryHappenings(new ArrayList<Object>());
                }
                else if (error instanceof ServerError)
                {
                    // if some data seems missing, continue anyway
                    queryHappenings(remainingQueries);
                }
                else
                {
                    SparkleHelper.makeSnackbar(mainView, getString(R.string.login_error_generic));
                    // force queryHappenings to load recyclerview
                    queryHappenings(new ArrayList<Object>());
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            // force queryHappenings to load recyclerview
            queryHappenings(new ArrayList<Object>());
            SparkleHelper.makeSnackbar(mainView, getString(R.string.rate_limit_error));
        }
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
        ArrayList<String> dossierNationNames = new ArrayList<String>();
        for (UserLogin u : dossierNations)
        {
            dossierNationNames.add(u.name);
        }
        Collections.sort(dossierNationNames);
        NameListDialog nameListDialog = new NameListDialog();
        nameListDialog.setTitle(getString(R.string.activityfeed_dossier_n));
        nameListDialog.setNames(dossierNationNames);
        nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
        nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
    }

    /**
     * Setup and show contents of the region dossier
     * @param fm Fragment Manager
     */
    private void showRegionDossier(FragmentManager fm)
    {
        Collections.sort(dossierRegions);
        NameListDialog nameListDialog = new NameListDialog();
        nameListDialog.setTitle(getString(R.string.activityfeed_dossier_r));
        nameListDialog.setNames((ArrayList<String>) dossierRegions);
        nameListDialog.setTarget(SparkleHelper.CLICKY_REGION_MODE);
        nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
