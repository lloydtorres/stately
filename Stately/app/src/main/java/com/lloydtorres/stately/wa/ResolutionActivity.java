package com.lloydtorres.stately.wa;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyActive;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 */
public class ResolutionActivity extends AppCompatActivity {
    private AssemblyActive mAssembly;
    private Resolution mResolution;
    private int councilId;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView title;
    private TextView target;
    private TextView proposedBy;
    private TextView voteStart;
    private TextView votesFor;
    private TextView votesAgainst;

    private TextView content;

    private PieChart votingBreakdown;
    private LineChart votingHistory;
    private TextView voteHistoryFor;
    private TextView voteHistoryAgainst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_council);

        if (getIntent() != null)
        {
            councilId = getIntent().getIntExtra("councilId", 1);
            mResolution = getIntent().getParcelableExtra("resolution");
        }
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
            mResolution = savedInstanceState.getParcelable("resolution");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_wa_council);
        setToolbar(toolbar);

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

        content = (TextView) findViewById(R.id.wa_resolution_content);

        votingBreakdown = (PieChart) findViewById(R.id.wa_voting_breakdown);
        votingHistory = (LineChart) findViewById(R.id.wa_voting_history);
        voteHistoryFor = (TextView) findViewById(R.id.wa_vote_history_for);
        voteHistoryAgainst = (TextView) findViewById(R.id.wa_vote_history_against);

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
        else
        {
            AssemblyActive tmp = new AssemblyActive();
            tmp.resolution = mResolution;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void queryResolution(int chamberId)
    {
        final View fView = findViewById(R.id.wa_council_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(AssemblyActive.QUERY, chamberId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    AssemblyActive waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            waResponse = serializer.read(AssemblyActive.class, response);
                            setResolution(waResponse);
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

        queue.add(stringRequest);
    }

    private void setResolution(AssemblyActive res)
    {
        mAssembly = res;
        mResolution = mAssembly.resolution;

        title.setText(mResolution.name);

        setTargetView(target, mResolution.category, mResolution.target);

        String proposer = SparkleHelper.getNameFromId(mResolution.proposedBy);
        String proposeTemplate = String.format(getString(R.string.wa_proposed), mResolution.proposedBy);
        SparkleHelper.activityLinkBuilder(this, proposedBy, proposeTemplate, mResolution.proposedBy, proposer, SparkleHelper.CLICKY_NATION_MODE);

        voteStart.setText(String.format(getString(R.string.wa_voting_time), SparkleHelper.getReadableDateFromUTC(mResolution.created)));
        votesFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        votesAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));
        voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));

        SparkleHelper.setBbCodeFormatting(this, content, mResolution.content);
        setVotingBreakdown(mResolution.votesFor, mResolution.votesAgainst);
        setVotingHistory(mResolution.voteHistoryFor, mResolution.voteHistoryAgainst);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setTargetView(TextView t, String category, String target)
    {
        String template = getString(R.string.wa_nominee_template);
        String[] pair = target.split(":");

        switch(pair[0])
        {
            case "N":
                String nationTarget = SparkleHelper.getNameFromId(pair[1]);
                String oldTemplate = String.format(template, category, pair[1]);
                SparkleHelper.activityLinkBuilder(this, t, oldTemplate, pair[1], nationTarget, SparkleHelper.CLICKY_NATION_MODE);
                break;
            default:
                t.setText(String.format(template, category, target));
                break;
        }
    }

    private void setVotingBreakdown(int voteFor, int voteAgainst)
    {
        float voteTotal = voteFor + voteAgainst;
        float votePercentFor = (((float) voteFor) * 100f)/voteTotal;
        float votePercentAgainst = (((float) voteAgainst) * 100f)/voteTotal;

        List<String> chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();

        int i = 0;
        chartLabels.add(getString(R.string.wa_for));
        chartEntries.add(new Entry((float) votePercentFor, i++));
        chartLabels.add(getString(R.string.wa_against));
        chartEntries.add(new Entry((float) votePercentAgainst, i++));

        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.waColours, this);
        PieData dataFull = new PieData(chartLabels, dataSet);

        // formatting
        Legend cLegend = votingBreakdown.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        votingBreakdown.setDrawSliceText(false);
        votingBreakdown.setDescription("");
        votingBreakdown.setHoleColorTransparent(true);
        votingBreakdown.setHoleRadius(60f);
        votingBreakdown.setTransparentCircleRadius(65f);
        votingBreakdown.setCenterTextSize(20);
        votingBreakdown.setRotationEnabled(false);

        votingBreakdown.setOnChartValueSelectedListener(new VotingBreakdownChartListener(this, votingBreakdown, chartLabels));
        votingBreakdown.setData(dataFull);
        votingBreakdown.invalidate();
    }

    private void setVotingHistory(List<Integer> votesFor, List<Integer> votesAgainst)
    {
        List<Entry> entryFor = new ArrayList<Entry>();
        List<Entry> entryAgainst = new ArrayList<Entry>();

        for (int i=0; i < votesFor.size(); i++)
        {
            entryFor.add(new Entry(votesFor.get(i), i));
            entryAgainst.add(new Entry(votesAgainst.get(i), i));
        }

        LineDataSet setFor = new LineDataSet(entryFor, getString(R.string.wa_for));
        setFor.setAxisDependency(YAxis.AxisDependency.LEFT);
        setFor.setColors(SparkleHelper.waColourFor, this);
        setFor.setDrawValues(false);
        setFor.setDrawVerticalHighlightIndicator(true);
        setFor.setDrawHorizontalHighlightIndicator(false);
        setFor.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setFor.setHighlightLineWidth(2.5f);
        setFor.setDrawCircles(false);
        setFor.setLineWidth(2.5f);

        LineDataSet setAgainst = new LineDataSet(entryAgainst, getString(R.string.wa_against));
        setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAgainst.setColors(SparkleHelper.waColourAgainst, this);
        setAgainst.setDrawValues(false);
        setAgainst.setDrawVerticalHighlightIndicator(true);
        setAgainst.setDrawHorizontalHighlightIndicator(false);
        setAgainst.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setAgainst.setHighlightLineWidth(2.5f);
        setAgainst.setDrawCircles(false);
        setAgainst.setLineWidth(2.5f);

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
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("councilId", councilId);
        if (mResolution != null)
        {
            savedInstanceState.putParcelable("resolution", mResolution);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
            if (mResolution == null)
            {
                mResolution = savedInstanceState.getParcelable("resolution");
            }
        }
    }
}
