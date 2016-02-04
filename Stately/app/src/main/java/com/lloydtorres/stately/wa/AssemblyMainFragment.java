package com.lloydtorres.stately.wa;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-16.
 * A fragment part of the StatelyActivity used to show the World Assembly.
 * Gets WA data on its own, can also refresh!
 */
public class AssemblyMainFragment extends Fragment {
    public static final String VOTE_STATUS_KEY = "voteStatus";

    private Activity mActivity;
    private View mView;
    private View mainView;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private Assembly genAssembly;
    private Assembly secCouncil;
    private WaVoteStatus voteStatus;

    public void setVoteStatus(WaVoteStatus w)
    {
        voteStatus = w;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_wa_main, container, false);
        mainView = mView.findViewById(R.id.refreshview_main);
        SparkleHelper.initAd(mView, R.id.ad_wa_fragment);

        // Restore state
        if (savedInstanceState != null && voteStatus == null)
        {
            voteStatus = savedInstanceState.getParcelable(VOTE_STATUS_KEY);
        }

        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);
        toolbar.setTitle(getActivity().getString(R.string.menu_wa));

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
                                                            queryWorldAssembly(mainView);
                                                        }
                                                 });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        queryWorldAssembly(mainView);
        return mView;
    }

    // Wrapper function for the heavy-lifting query function
    // Starts PART 1 of the query process
    private void queryWorldAssembly(View view)
    {
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
    private void queryWorldAssemblyHeavy(final View view, final int chamberId)
    {
        String targetURL = String.format(Assembly.QUERY, chamberId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Assembly waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded())
                        {
                            return;
                        }
                        Persister serializer = new Persister();
                        try {
                            waResponse = serializer.read(Assembly.class, response);

                            if (chamberId == Assembly.GENERAL_ASSEMBLY)
                            {
                                // Once a response is obtained for the General Assembly,
                                // start querying for the Security Council
                                setGeneralAssembly(waResponse);
                                queryWorldAssemblyHeavy(view, Assembly.SECURITY_COUNCIL);
                            }
                            else if (chamberId == Assembly.SECURITY_COUNCIL)
                            {
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
                if (getActivity() == null || !isAdded())
                {
                    return;
                }
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    private void setGeneralAssembly(Assembly g)
    {
        genAssembly = g;
    }

    private void setSecurityCouncil(Assembly s)
    {
        secCouncil = s;
    }

    private void refreshRecycler()
    {
        mRecyclerAdapter = new AssemblyRecyclerAdapter(getContext(), genAssembly, secCouncil, voteStatus);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (voteStatus != null)
        {
            savedInstanceState.putParcelable(VOTE_STATUS_KEY, voteStatus);
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
