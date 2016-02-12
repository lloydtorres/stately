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
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-02-08.
 * This fragment shows a user's activity feed.
 */
public class ActivityFeedFragment extends Fragment {
    public static final String NATION_KEY = "nationName";
    public static final String REGION_KEY = "regionName";
    private static final int SWITCH_LIMIT = 10;

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
                events = new ArrayList<Event>();
                queryHappenings(buildHappeningsQuery());
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        startQueryHappenings();
        return mView;
    }

    /**
     * Convenience method to show swipe refresh and start query.
     */
    public void startQueryHappenings()
    {
        events = new ArrayList<Event>();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryHappenings(buildHappeningsQuery());
            }
        });
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
        }

        // Include current region?
        if (storage.getBoolean(SubscriptionsDialog.CURRENT_REGION, true))
        {
            q.add(regionName);
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
            // If query is for a nation
            if (q instanceof UserLogin)
            {
                url = String.format(HappeningFeed.QUERY_NATION, ((UserLogin) q).nationId);
            }
            // If query is for a region
            else if (q instanceof String)
            {
                url = String.format(HappeningFeed.QUERY_REGION, SparkleHelper.getIdFromName((String) q));
            }
            // If query is for the World Assembly
            else if (q instanceof Integer)
            {
                url = HappeningFeed.QUERY_WA;
            }
            queryHappeningsHeavy(url, query);
        }
    }

    /**
     * Heavy-lifting for querying happenings.
     * @param target
     * @param remainingQueries
     */
    private void queryHappeningsHeavy(final String target, final List<Object> remainingQueries)
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
                            events.addAll(happeningResponse.happenings);
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
        switch (item.getItemId()) {
            case R.id.nav_subscriptions:
                FragmentManager fm = getChildFragmentManager();
                SubscriptionsDialog subscriptionsDialog = new SubscriptionsDialog();
                subscriptionsDialog.setCallback(this);
                subscriptionsDialog.show(fm, SubscriptionsDialog.DIALOG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
