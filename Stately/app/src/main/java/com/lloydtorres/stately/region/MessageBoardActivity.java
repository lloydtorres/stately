package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

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
import com.lloydtorres.stately.dto.RegionMessages;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-24.
 * An activity that displays the contents of the regional message board.
 */
public class MessageBoardActivity extends AppCompatActivity {
    private RegionMessages messages;
    private String regionName;
    private int offsetCount;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshviewdouble);

        if (getIntent() != null)
        {
            regionName = getIntent().getStringExtra("regionName");
        }
        if (savedInstanceState != null)
        {
            messages = savedInstanceState.getParcelable("messages");
            regionName = savedInstanceState.getString("regionName");
            offsetCount = savedInstanceState.getInt("offsetCount");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshviewdouble_toolbar);
        setToolbar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.refreshviewdouble_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setup refresher to requery for resolution on swipe
        mSwipyRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.refreshviewdouble_refresher);
        mSwipyRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.region_rmb), regionName));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void queryMessages(int offset)
    {
        final View fView = findViewById(R.id.wa_council_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(RegionMessages.QUERY, SparkleHelper.getIdFromName(regionName), offset);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    RegionMessages messageResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            messageResponse = serializer.read(RegionMessages.class, response);
                            // @TODO
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
        savedInstanceState.putInt("offsetCount", offsetCount);
        if (messages != null)
        {
            savedInstanceState.putParcelable("messages", messages);
        }
        if (regionName != null)
        {
            savedInstanceState.putString("regionName", regionName);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            offsetCount = savedInstanceState.getInt("offsetCount");
            if (messages == null)
            {
                messages = savedInstanceState.getParcelable("messages");
            }
            if (regionName == null)
            {
                regionName = savedInstanceState.getString("regionName");
            }
        }
    }
}
