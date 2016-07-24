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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyActive;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-17.
 * This activity shows an active resolution from either WA chamber.
 * Takes in the chamber (council) ID to get the right chamber, as well as a
 * Resolution object (optional). If Resolution is null, it can get it on its own.
 * Also has refreshing!
 */
public class ResolutionActivity extends AppCompatActivity {
    // Keys for Intent data
    public static final String TARGET_COUNCIL_ID = "councilId";
    public static final String TARGET_RESOLUTION = "resolution";
    public static final String TARGET_VOTE_STATUS = "voteStatus";

    private AssemblyActive mAssembly;
    private Resolution mResolution;
    private WaVoteStatus voteStatus;
    private int councilId;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isInProgress;

    private TextView title;
    private TextView target;
    private TextView proposedBy;
    private TextView voteStart;
    private TextView votesFor;
    private TextView votesAgainst;

    private HtmlTextView content;
    private ImageView voteButtonIcon;
    private LinearLayout voteButton;
    private TextView voteButtonContent;

    private PieChart votingBreakdown;
    private TextView nullVote;
    private LineChart votingHistory;
    private TextView voteHistoryFor;
    private TextView voteHistoryAgainst;
    private View view;

    private ImageView iconVoteFor;
    private ImageView iconVoteAgainst;
    private ImageView histIconVoteFor;
    private ImageView histIconVoteAgainst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_council);
        isInProgress = false;
        view = findViewById(R.id.wa_council_main);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            councilId = getIntent().getIntExtra(TARGET_COUNCIL_ID, 1);
            mResolution = getIntent().getParcelableExtra(TARGET_RESOLUTION);
            voteStatus = getIntent().getParcelableExtra(TARGET_VOTE_STATUS);
        }
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt(TARGET_COUNCIL_ID);
            mResolution = savedInstanceState.getParcelable(TARGET_RESOLUTION);
            voteStatus = savedInstanceState.getParcelable(TARGET_VOTE_STATUS);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_wa_council);
        setToolbar(toolbar);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.wa_resolution_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryResolution(councilId);
            }
        });

        title = (TextView) findViewById(R.id.wa_resolution_title);
        target = (TextView) findViewById(R.id.wa_nominee);
        proposedBy = (TextView) findViewById(R.id.wa_proposed_by);
        voteStart = (TextView) findViewById(R.id.wa_activetime);
        votesFor = (TextView) findViewById(R.id.wa_resolution_for);
        votesAgainst = (TextView) findViewById(R.id.wa_resolution_against);

        content = (HtmlTextView) findViewById(R.id.wa_resolution_content);
        voteButtonIcon = (ImageView) findViewById(R.id.wa_resolution_button_icon);
        voteButton = (LinearLayout) findViewById(R.id.wa_resolution_vote);
        voteButtonContent = (TextView) findViewById(R.id.wa_resolution_vote_content);

        votingBreakdown = (PieChart) findViewById(R.id.wa_voting_breakdown);
        nullVote = (TextView) findViewById(R.id.resolution_null_vote);
        votingHistory = (LineChart) findViewById(R.id.wa_voting_history);
        voteHistoryFor = (TextView) findViewById(R.id.wa_vote_history_for);
        voteHistoryAgainst = (TextView) findViewById(R.id.wa_vote_history_against);

        iconVoteFor = (ImageView) findViewById(R.id.content_icon_vote_for);
        iconVoteAgainst = (ImageView) findViewById(R.id.content_icon_vote_against);
        histIconVoteFor = (ImageView) findViewById(R.id.history_icon_vote_for);
        histIconVoteAgainst = (ImageView) findViewById(R.id.history_icon_vote_against);

        // if no resolution passed in, go get it from server.
        if (mResolution == null)
        {
            startQueryResolution();
        }
        // Otherwise just show it normally
        else
        {
            AssemblyActive tmp = new AssemblyActive();
            tmp.resolution = mResolution;
            setVoteStatus(voteStatus);
            setResolution(tmp);
        }
    }

    private void startQueryResolution()
    {
        // hack to get swiperefreshlayout to show
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryResolution(councilId);
            }
        });
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        switch (councilId)
        {
            case Assembly.GENERAL_ASSEMBLY:
                getSupportActionBar().setTitle(getString(R.string.wa_general_assembly));
                break;
            case Assembly.SECURITY_COUNCIL:
                getSupportActionBar().setTitle(getString(R.string.wa_security_council));
                break;
        }
        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Queries the resolution from the specified chamber, then calls to check the nation status.
     * @param chamberId Current WA chamber being checked
     */
    private void queryResolution(int chamberId)
    {
        String targetURL = String.format(AssemblyActive.QUERY, chamberId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    AssemblyActive waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            waResponse = serializer.read(AssemblyActive.class, response);
                            queryVoteStatus(waResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
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
                UserLogin u = SparkleHelper.getActiveUser(getApplicationContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Called from queryResolution(). Checks the current nation's WA voting rights.
     * @param a The resolution data to be passed to setResolution().
     */
    private void queryVoteStatus(final AssemblyActive a)
    {
        UserLogin u = SparkleHelper.getActiveUser(this);
        String targetURL = String.format(WaVoteStatus.QUERY, u.nationId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    WaVoteStatus vsResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            vsResponse = serializer.read(WaVoteStatus.class, response);
                            setVoteStatus(vsResponse);
                            setResolution(a);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
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
                UserLogin u = SparkleHelper.getActiveUser(getApplicationContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Convenience class to show voting dialog.
     * @param vote Current choice in voting
     */
    private void showVoteDialog(int vote)
    {
        FragmentManager fm = getSupportFragmentManager();
        VoteDialog voteDialog = new VoteDialog();
        voteDialog.setListener(this);
        voteDialog.setChoice(vote);
        voteDialog.show(fm, VoteDialog.DIALOG_TAG);
    }

    /**
     * Setter for the voteStatus object.
     * @param vs
     */
    private void setVoteStatus(WaVoteStatus vs)
    {
        voteStatus = vs;
        SparkleHelper.setWaSessionData(this, voteStatus.waState);

        String voteStats = "";
        switch(councilId)
        {
            case Assembly.GENERAL_ASSEMBLY:
                voteStats = voteStatus.gaVote;
                break;
            case Assembly.SECURITY_COUNCIL:
                voteStats = voteStatus.scVote;
                break;
        }

        if (SparkleHelper.isWaMember(this, voteStatus.waState))
        {
            voteButton.setVisibility(View.VISIBLE);
            final int voteChoice;

            // If voting FOR the resolution
            if (getString(R.string.wa_vote_state_for).equals(voteStats))
            {
                findViewById(R.id.view_divider).setVisibility(View.GONE);
                iconVoteFor.setVisibility(View.VISIBLE);
                histIconVoteFor.setVisibility(View.VISIBLE);
                iconVoteAgainst.setVisibility(View.GONE);
                histIconVoteAgainst.setVisibility(View.GONE);
                voteButtonIcon.setImageResource(R.drawable.ic_wa_white);
                voteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorChart0));
                voteButtonContent.setTextColor(ContextCompat.getColor(this, R.color.white));
                voteButtonContent.setText(getString(R.string.wa_resolution_vote_for));
                voteChoice = VoteDialog.VOTE_FOR;
            }
            // If voting AGAINST the resolution
            else if (getString(R.string.wa_vote_state_against).equals(voteStats))
            {
                findViewById(R.id.view_divider).setVisibility(View.GONE);
                iconVoteAgainst.setVisibility(View.VISIBLE);
                histIconVoteAgainst.setVisibility(View.VISIBLE);
                iconVoteFor.setVisibility(View.GONE);
                histIconVoteFor.setVisibility(View.GONE);
                voteButtonIcon.setImageResource(R.drawable.ic_wa_white);
                voteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorChart1));
                voteButtonContent.setTextColor(ContextCompat.getColor(this, R.color.white));
                voteButtonContent.setText(getString(R.string.wa_resolution_vote_against));
                voteChoice = VoteDialog.VOTE_AGAINST;
            }
            else
            {
                findViewById(R.id.view_divider).setVisibility(View.VISIBLE);
                iconVoteFor.setVisibility(View.GONE);
                histIconVoteFor.setVisibility(View.GONE);
                iconVoteAgainst.setVisibility(View.GONE);
                histIconVoteAgainst.setVisibility(View.GONE);
                voteButtonIcon.setImageResource(R.drawable.ic_wa_green);
                voteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                voteButtonContent.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                voteButtonContent.setText(getString(R.string.wa_resolution_vote_default));
                voteChoice = VoteDialog.VOTE_UNDECIDED;
            }

            voteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVoteDialog(voteChoice);
                }
            });
        }
        else
        {
            findViewById(R.id.view_divider).setVisibility(View.GONE);
            voteButton.setOnClickListener(null);
        }
    }

    /**
     * Starts the vote submission process.
     * @param choice Voting choice.
     */
    public void submitVote(int choice)
    {
        String url;
        switch(councilId)
        {
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
    private void getLocalId(final String url, final int p)
    {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=localid]").first();

                        if (input == null)
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
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
                UserLogin u = SparkleHelper.getActiveUser(getApplicationContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually post the user's vote
     * @param url Target URL.
     * @param localid Required localid
     * @param p Vote
     */
    private void postVote(final String url, final String localid, final int p)
    {
        final String votePost;
        switch (p)
        {
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch(p)
                        {
                            case VoteDialog.VOTE_FOR:
                                SparkleHelper.makeSnackbar(view, getString(R.string.wa_resolution_vote_for));
                                break;
                            case VoteDialog.VOTE_AGAINST:
                                SparkleHelper.makeSnackbar(view, getString(R.string.wa_resolution_vote_against));
                                break;
                            default:
                                SparkleHelper.makeSnackbar(view, getString(R.string.wa_resolution_vote_undecided));
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
                params.put("localid", localid);
                params.put("vote", votePost);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Fill in the resolution activity with the given data.
     * @param res
     */
    private void setResolution(AssemblyActive res)
    {
        mAssembly = res;
        mResolution = mAssembly.resolution;

        title.setText(mResolution.name);

        setTargetView(target, mResolution.category, mResolution.target);

        String proposer = SparkleHelper.getNameFromId(mResolution.proposedBy);
        String proposeTemplate = String.format(getString(R.string.wa_proposed), mResolution.proposedBy);
        SparkleHelper.activityLinkBuilder(this, proposedBy, proposeTemplate, mResolution.proposedBy, proposer, SparkleHelper.CLICKY_NATION_MODE);

        voteStart.setText(String.format(getString(R.string.wa_voting_time), SparkleHelper.calculateResolutionEnd(this, mResolution.voteHistoryFor.size()+1)));
        votesFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        votesAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));
        voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));

        SparkleHelper.setBbCodeFormatting(this, content, mResolution.content, getSupportFragmentManager());
        if (!SparkleHelper.setWaVotingBreakdown(this, votingBreakdown, mResolution.votesFor, mResolution.votesAgainst))
        {
            votingBreakdown.setVisibility(View.GONE);
            nullVote.setVisibility(View.VISIBLE);
        }
        setVotingHistory(mResolution.voteHistoryFor, mResolution.voteHistoryAgainst);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This formats the topmost TextView with information on the category and resolution target.
     * @param t
     * @param category
     * @param target
     */
    private void setTargetView(TextView t, String category, String target)
    {
        String template = getString(R.string.wa_nominee_template);
        String[] pair = target.split(":");

        if (pair.length <= 1)
        {
            t.setText(category);
        }
        else
        {
            switch(pair[0])
            {
                case "N":
                    // If target is a nation, linkify it.
                    String nationTarget = SparkleHelper.getNameFromId(pair[1]);
                    String oldTemplate = String.format(template, category, pair[1]);
                    SparkleHelper.activityLinkBuilder(this, t, oldTemplate, pair[1], nationTarget, SparkleHelper.CLICKY_NATION_MODE);
                    break;
                case "R":
                    // If target is a nation, linkify it.
                    String regionTarget = SparkleHelper.getNameFromId(pair[1]);
                    String oldRegionTemplate = String.format(template, category, pair[1]);
                    SparkleHelper.activityLinkBuilder(this, t, oldRegionTemplate, pair[1], regionTarget, SparkleHelper.CLICKY_REGION_MODE);
                    break;
                default:
                    t.setText(String.format(template, category, target));
                    break;
            }
        }
    }

    /**
     * Initialize the line graph to show voting history
     * @param votesFor
     * @param votesAgainst
     */
    private void setVotingHistory(List<Integer> votesFor, List<Integer> votesAgainst)
    {
        final float lineWidth = 2.5f;

        List<Entry> entryFor = new ArrayList<Entry>();
        List<Entry> entryAgainst = new ArrayList<Entry>();

        // Build data
        for (int i=0; i < votesFor.size(); i++)
        {
            entryFor.add(new Entry(votesFor.get(i), i));
            entryAgainst.add(new Entry(votesAgainst.get(i), i));
        }

        // lots of formatting for the FOR and AGAINST lines
        LineDataSet setFor = new LineDataSet(entryFor, getString(R.string.wa_for));
        setFor.setAxisDependency(YAxis.AxisDependency.LEFT);
        setFor.setColors(SparkleHelper.waColourFor, this);
        setFor.setDrawValues(false);
        setFor.setDrawVerticalHighlightIndicator(true);
        setFor.setDrawHorizontalHighlightIndicator(false);
        setFor.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setFor.setHighlightLineWidth(lineWidth);
        setFor.setDrawCircles(false);
        setFor.setLineWidth(lineWidth);

        LineDataSet setAgainst = new LineDataSet(entryAgainst, getString(R.string.wa_against));
        setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAgainst.setColors(SparkleHelper.waColourAgainst, this);
        setAgainst.setDrawValues(false);
        setAgainst.setDrawVerticalHighlightIndicator(true);
        setAgainst.setDrawHorizontalHighlightIndicator(false);
        setAgainst.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setAgainst.setHighlightLineWidth(lineWidth);
        setAgainst.setDrawCircles(false);
        setAgainst.setLineWidth(lineWidth);

        // Match data with x-axis labels
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setFor);
        dataSets.add(setAgainst);

        List<String> xLabels = new ArrayList<String>();
        for (int i=0; i < votesFor.size(); i++)
        {
            // Only add labels for each day
            if (i%24 == 0)
            {
                xLabels.add(String.format(getString(R.string.wa_x_axis_d), (i/24)+1));
            }
            else
            {
                xLabels.add(String.format(getString(R.string.wa_x_axis_h), i));
            }
        }
        LineData data = new LineData(xLabels, dataSets);

        // formatting
        votingHistory = SparkleHelper.getFormattedLineChart(votingHistory,
                new VotingHistoryChartListener(voteHistoryFor, voteHistoryAgainst, votesFor, votesAgainst),
                true, 23, false);

        votingHistory.setData(data);
        votingHistory.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TARGET_COUNCIL_ID, councilId);
        if (mResolution != null)
        {
            savedInstanceState.putParcelable(TARGET_RESOLUTION, mResolution);
        }
        if (voteStatus != null)
        {
            savedInstanceState.putParcelable(TARGET_VOTE_STATUS, voteStatus);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt(TARGET_COUNCIL_ID);
            if (mResolution == null)
            {
                mResolution = savedInstanceState.getParcelable(TARGET_RESOLUTION);
            }
            if (voteStatus == null)
            {
                voteStatus = savedInstanceState.getParcelable(TARGET_VOTE_STATUS);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        if (votingBreakdown != null)
        {
            votingBreakdown = null;
        }
        if (votingHistory != null)
        {
            votingHistory = null;
        }
        super.onDestroy();
    }
}
