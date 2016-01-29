package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.dto.RegionMessages;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lloyd on 2016-01-24.
 * An activity that displays the contents of the regional message board.
 */
public class MessageBoardActivity extends AppCompatActivity {
    // Keys for Intent data
    public static final String BOARD_REGION_NAME = "regionName";
    public static final String BOARD_MESSAGES = "messages";

    private RegionMessages messages;
    private String regionName;
    private Set<Integer> uniqueEnforcer;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshview);

        if (getIntent() != null)
        {
            messages = new RegionMessages();
            messages.posts = new ArrayList<Post>();
            regionName = getIntent().getStringExtra(BOARD_REGION_NAME);
            uniqueEnforcer = new HashSet<Integer>();
        }
        if (savedInstanceState != null)
        {
            messages = savedInstanceState.getParcelable(BOARD_MESSAGES);
            regionName = savedInstanceState.getString(BOARD_REGION_NAME);
            rebuildUniqueEnforcer();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshview_toolbar);
        setToolbar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryMessages(0, false);
            }
        });

        if (messages.posts.size() <= 0)
        {
            // hack to get swipyrefreshlayout to show
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            queryMessages(0, true);
        }
        // Otherwise just show it normally
        else
        {
            refreshRecycler();
        }
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.region_rmb), regionName));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Get RMB messages from NationStates
     * @param offset the number of messages to skip
     */
    private void queryMessages(final int offset, final boolean initialRun)
    {
        final View fView = findViewById(R.id.refreshview_main);

        // stop if this is the 11th time the query has been called
        if (offset >= 110)
        {
            SparkleHelper.makeSnackbar(fView, getString(R.string.rmb_backload_error));
            refreshRecycler();
            return;
        }

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
                            processMessageResponse(messageResponse, offset, initialRun);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));

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

    /**
     * Process the response after querying for RMB messages.
     * Depending on the response, we may have to check again if there's more messages to get.
     * @param m The response
     * @param offset The current offset
     * @param initialRun if this is the first time the process is being run
     */
    private void processMessageResponse(RegionMessages m, int offset, boolean initialRun)
    {
        int uniqueMessages = 0;

        // Only add unique posts to our list
        for (Post p : m.posts)
        {
            if (!uniqueEnforcer.contains(p.id))
            {
                messages.posts.add(p);
                uniqueEnforcer.add(p.id);
                uniqueMessages++;
            }
        }

        // If this is the initial run, don't keep going
        if (!initialRun && uniqueMessages >= 10)
        {
            // In this case, all the messages were unique, so there may be more messages to load
            queryMessages(offset+10, false);
        }
        else
        {
            // We've reached the point where we already have the messages, so put everything back together
            refreshRecycler();
        }
    }

    /**
     * Refreshes the contents of the recycler
     */
    private void refreshRecycler()
    {
        Collections.sort(messages.posts);
        mRecyclerAdapter = new MessageBoardRecyclerAdapter(this, messages.posts);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This function rebuilds the set used to track unique messages after a restart.
     * Because set isn't parcelable :(
     */
    private void rebuildUniqueEnforcer()
    {
        uniqueEnforcer = new HashSet<Integer>();
        for (Post p : messages.posts)
        {
            uniqueEnforcer.add(p.id);
        }
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
        if (messages != null)
        {
            savedInstanceState.putParcelable(BOARD_MESSAGES, messages);
        }
        if (regionName != null)
        {
            savedInstanceState.putString(BOARD_REGION_NAME, regionName);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (messages == null)
            {
                messages = savedInstanceState.getParcelable(BOARD_MESSAGES);
                rebuildUniqueEnforcer();
            }
            if (regionName == null)
            {
                regionName = savedInstanceState.getString(BOARD_REGION_NAME);
            }
        }
    }
}
