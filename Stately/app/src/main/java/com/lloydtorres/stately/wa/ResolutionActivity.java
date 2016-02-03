package com.lloydtorres.stately.wa;

import android.os.Bundle;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyActive;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

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

    private TextView title;
    private TextView target;
    private TextView proposedBy;
    private TextView voteStart;
    private TextView votesFor;
    private TextView votesAgainst;

    private HtmlTextView content;
    private LinearLayout voteButton;
    private TextView voteButtonContent;

    private PieChart votingBreakdown;
    private TextView nullVote;
    private LineChart votingHistory;
    private TextView voteHistoryFor;
    private TextView voteHistoryAgainst;

    private ImageView iconVoteFor;
    private ImageView iconVoteAgainst;
    private ImageView histIconVoteFor;
    private ImageView histIconVoteAgainst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_council);

        SparkleHelper.initAd(findViewById(R.id.activity_wa_council_main), R.id.ad_resolution_activity);

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
            // hack to get swiperefreshlayout to show
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            queryResolution(councilId);
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
        final View fView = findViewById(R.id.wa_council_main);
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
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        DashHelper.getInstance(this).addRequest(stringRequest);
    }

    /**
     * Called from queryResolution(). Checks the current nation's WA voting rights.
     * @param a The resolution data to be passed to setResolution().
     */
    private void queryVoteStatus(final AssemblyActive a)
    {
        final View fView = findViewById(R.id.wa_council_main);
        UserLogin u = SparkleHelper.getActiveUser(this);
        String targetURL = String.format(WaVoteStatus.QUERY, SparkleHelper.getIdFromName(u.name));

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
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        DashHelper.getInstance(this).addRequest(stringRequest);
    }

    /**
     * Setter for the voteStatus object.
     * @param vs
     */
    private void setVoteStatus(WaVoteStatus vs)
    {
        voteStatus = vs;

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

            // If voting FOR the resolution
            if (getString(R.string.wa_vote_state_for).equals(voteStats))
            {
                findViewById(R.id.view_divider).setVisibility(View.GONE);
                iconVoteFor.setVisibility(View.VISIBLE);
                histIconVoteFor.setVisibility(View.VISIBLE);
                voteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorChart0));
                voteButtonContent.setTextColor(ContextCompat.getColor(this, R.color.white));
                voteButtonContent.setText(getString(R.string.wa_resolution_vote_for));
            }
            // If voting AGAINST the resolution
            else if (getString(R.string.wa_vote_state_against).equals(voteStats))
            {
                findViewById(R.id.view_divider).setVisibility(View.GONE);
                iconVoteAgainst.setVisibility(View.VISIBLE);
                histIconVoteAgainst.setVisibility(View.VISIBLE);
                voteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorChart1));
                voteButtonContent.setTextColor(ContextCompat.getColor(this, R.color.white));
                voteButtonContent.setText(getString(R.string.wa_resolution_vote_against));
            }
        }
        else
        {
            findViewById(R.id.view_divider).setVisibility(View.GONE);
            voteButton.setOnClickListener(null);
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

        voteStart.setText(String.format(getString(R.string.wa_voting_time), SparkleHelper.calculateResolutionEnd(mResolution.voteHistoryFor.size()+1)));
        votesFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        votesAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));
        voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));

        SparkleHelper.setBbCodeFormatting(this, content, mResolution.content);
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
        List<LineDataSet> dataSets = new ArrayList<LineDataSet>();
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
        Legend cLegend = votingHistory.getLegend();
        cLegend.setEnabled(false);

        XAxis xAxis = votingHistory.getXAxis();
        xAxis.setLabelsToSkip(23);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = votingHistory.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = votingHistory.getAxisLeft();
        yAxisLeft.setValueFormatter(new LargeValueFormatter());

        votingHistory.setDoubleTapToZoomEnabled(false);
        votingHistory.setDescription("");
        votingHistory.setDragEnabled(true);
        votingHistory.setScaleYEnabled(false);
        votingHistory.setDrawGridBackground(false);
        votingHistory.setOnChartValueSelectedListener(new VotingHistoryChartListener(voteHistoryFor, voteHistoryAgainst, votesFor, votesAgainst));

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
}
