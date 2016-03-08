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
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-28.
 * A fragment to display current issues.
 */
public class IssuesFragment extends Fragment {
    private static final String NEXT_ISSUE_REGEX = "\\$\\('#nextdilemmacountdown'\\)\\.countdown\\(\\{timestamp:new Date\\(([0-9]*?)\\)\\}\\);";

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private List<Object> issues;
    private Nation mNation;

    public void setNationData(Nation n)
    {
        mNation = n;
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_refreshview, container, false);

        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);
        toolbar.setTitle(getActivity().getString(R.string.menu_issues));

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
                queryIssues(mView);
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return mView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Refresh on resume
        startQueryIssues();
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
                queryIssues(mView);
            }
        });
    }

    /**
     * Scrape the issues from the actual NationStates site
     * @param view
     */
    private void queryIssues(final View view)
    {
        String targetURL = Issue.QUERY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded())
                        {
                            return;
                        }
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processIssues(view, d);
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
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Process the HTML contents of the issues into actual Issue objects
     * @param d
     */
    private void processIssues(View v, Document d)
    {
        issues = new ArrayList<Object>();

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

            issues.add(issueCore);
        }

        Element nextIssueUpdate = d.select("p.dilemmanextupdate").first();
        if (nextIssueUpdate != null)
        {
            String nextUpdate = nextIssueUpdate.text();
            issues.add(nextUpdate);
        }

        if (issuesRaw.size() <= 0)
        {
            String nextUpdate = getString(R.string.no_issues);

            Matcher m = Pattern.compile(NEXT_ISSUE_REGEX).matcher(d.html());
            if (m.find())
            {
                long nextUpdateTime = Long.valueOf(m.group(1)) / 1000L;
                nextUpdate = String.format(getString(R.string.next_issue), SparkleHelper.getReadableDateFromUTC(nextUpdateTime));
            }

            issues.add(nextUpdate);
        }

        mRecyclerAdapter = new IssuesRecyclerAdapter(getContext(), issues, mNation);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
