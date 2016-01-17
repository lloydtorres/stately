package com.lloydtorres.stately.wa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyActive;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_council);

        if (getIntent() != null)
        {
            councilId = getIntent().getIntExtra("councilId", 1);
        }
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
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

        // hack to get swiperefreshlayout to show
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
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

        queryResolution(councilId);
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
                            setContent(waResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
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

    private void setContent(AssemblyActive res)
    {
        mAssembly = res;
        mResolution = mAssembly.resolution;

        title.setText(mResolution.name);
        target.setText(String.format(getString(R.string.wa_nominee_template), mResolution.category, mResolution.target));
        proposedBy.setText(String.format(getString(R.string.wa_proposed), proposedBy));
        voteStart.setText(String.format(getString(R.string.wa_voting_time), SparkleHelper.getReadableDateFromUTC(mResolution.created)));
        votesFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        votesAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));

        content.setText(mResolution.content);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("councilId", councilId);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
        }
    }
}
