package com.lloydtorres.stately.issues;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-28.
 * A fragment to display current issues.
 */
public class IssuesFragment extends Fragment {
    private static final String UNADDRESSED = "unaddressed";
    private static final String PENDING = "legislation pending";
    private static final String DISMISSED = "dismissed";

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;

    private static AlertDialog.Builder dialogBuilder;
    private static DialogInterface.OnClickListener dialogClickListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private List<Issue> issues;

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
        mView = inflater.inflate(R.layout.fragment_issues, container, false);
        toolbar = (Toolbar) mView.findViewById(R.id.issues_toolbar);
        toolbar.setTitle(getActivity().getString(R.string.menu_issues));

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.issues_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryIssues(mView);
            }
        });

        // dialog and listener for positive responses
        dialogBuilder = new AlertDialog.Builder(getContext());
        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissAllIssues(mView);
            }
        };

        // On press fab, show dialog
        mFloatingActionButton = (FloatingActionButton) mView.findViewById(R.id.issues_fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.setMessage(getString(R.string.issue_dismiss_all))
                        .setPositiveButton(getString(R.string.issue_dismiss_all_positive), dialogClickListener)
                        .setNegativeButton(getString(R.string.explore_negative), null).show();
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.issues_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        startQueryIssues();

        return mView;
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     */
    private void startQueryIssues()
    {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        queryIssues(mView);
    }

    /**
     * Scrape the issues from the actual NationStates site
     * @param view
     */
    private void queryIssues(final View view)
    {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String targetURL = Issue.QUERY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processIssues(view, d);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getContext());
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        queue.add(stringRequest);
    }

    /**
     * Process the HTML contents of the issues into actual Issue objects
     * @param d
     */
    private void processIssues(View v, Document d)
    {
        issues = new ArrayList<Issue>();

        Element issuesContainer = d.select("ul.dilemmalist").first();

        if (issuesContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(v, getString(R.string.login_error_parsing));
            return;
        }

        Elements issuesRaw = issuesContainer.children();

        for (Element i : issuesRaw)
        {
            Issue issueCore = new Issue();

            Elements issueContents = i.children();

            // Get issue ID and name
            Element issueMain = issueContents.select("a").first();
            String issueLink = issueMain.attr("href");
            int issueId = Integer.valueOf(issueLink.replace("page=show_dilemma/dilemma=", ""));
            issueCore.id = issueId;
            String issueName = issueMain.text();
            issueCore.title = issueName;

            // Get issue status
            String issueStat = i.text().trim().replaceAll(".* \\[(.*?)\\]", "$1").toLowerCase();
            switch (issueStat)
            {
                case PENDING:
                    issueCore.status = Issue.STATUS_PENDING;
                    break;
                case DISMISSED:
                    issueCore.status = Issue.STATUS_DISMISSED;
                    break;
                default:
                    issueCore.status = Issue.STATUS_UNADDRESSED;
                    break;
            }

            issues.add(issueCore);
        }

        mRecyclerAdapter = new IssuesRecyclerAdapter(getContext(), issues);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void dismissAllIssues(final View view)
    {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String targetURL = Issue.QUERY;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SparkleHelper.makeSnackbar(view, getString(R.string.issue_dismiss_all_response));
                        startQueryIssues();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("dismiss_all", "1");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getContext());
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        queue.add(stringRequest);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
