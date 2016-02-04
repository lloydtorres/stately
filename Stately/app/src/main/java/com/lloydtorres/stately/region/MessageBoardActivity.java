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
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.dto.RegionMessages;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

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
    public static final String BOARD_PAST_OFFSET = "pastOffset";

    // Direction to scan for messages
    private static final int SCAN_BACKWARD = 0;
    private static final int SCAN_FORWARD = 1;

    private RegionMessages messages;
    private String regionName;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;

    private SwipyRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_message_board);

        // SparkleHelper.initAd(findViewById(R.id.message_board_main), R.id.ad_message_board_activity);

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
            pastOffset = savedInstanceState.getInt(BOARD_PAST_OFFSET, 10);
            rebuildUniqueEnforcer();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_board_toolbar);
        setToolbar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.message_board_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.message_board_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP)
                {
                    queryMessages(pastOffset, SCAN_BACKWARD, false);
                }
                else
                {
                    queryMessages(0, SCAN_FORWARD, false);
                }
            }
        });

        if (messages.posts.size() <= 0)
        {
            startQueryMessages();
        }
        // Otherwise just show it normally
        else
        {
            refreshRecycler(SCAN_FORWARD, 0);
        }
    }

    public void startQueryMessages()
    {
        // hack to get swipyrefreshlayout to show
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        queryMessages(0, SCAN_FORWARD, true);
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
    private void queryMessages(final int offset, final int direction, final boolean initialRun)
    {
        final View fView = findViewById(R.id.message_board_coordinator);

        // stop if this is the 11th time the query has been called moving forward
        if (direction == SCAN_FORWARD && offset >= 110)
        {
            pastOffset = 10;
            SparkleHelper.makeSnackbar(fView, getString(R.string.rmb_backload_error));
            refreshRecycler(SCAN_FORWARD, 0);
            return;
        }

        String targetURL = String.format(RegionMessages.QUERY, SparkleHelper.getIdFromName(regionName), offset);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    RegionMessages messageResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            messageResponse = serializer.read(RegionMessages.class, response);
                            switch (direction)
                            {
                                case SCAN_BACKWARD:
                                    processMessageResponseBackward(fView, messageResponse);
                                    break;
                                default:
                                    processMessageResponseForward(fView, messageResponse, offset, initialRun);
                                    break;
                            }
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

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(fView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Used to scan for previous messages.
     * @param view Activity view
     * @param m Message response
     */
    private void processMessageResponseBackward(View view, RegionMessages m)
    {
        // If there's nothing in the current messages, then there's probably nothing in the past
        if (messages.posts.size() <= 0 || m.posts.size() <= 0)
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
            return;
        }

        Collections.sort(m.posts);

        // Count the number of obtained posts that were posted earlier than the current earliest
        long earliestCurrentDate = messages.posts.get(0).timestamp;
        int timeCounter = 0;
        for (Post p : m.posts)
        {
            if (p.timestamp < earliestCurrentDate && !uniqueEnforcer.contains(p.id))
            {
                timeCounter++;
            }
        }

        // If all messages were from the past, we're good
        if (timeCounter >= m.posts.size())
        {
            pastOffset += m.posts.size();
            messages.posts.addAll(m.posts);
            for (Post p : m.posts)
            {
                uniqueEnforcer.add(p.id);
            }
            refreshRecycler(SCAN_BACKWARD, m.posts.size());
        }
        // If only some messages were from the past, adjust the offset and try again
        else if (timeCounter < 10 && timeCounter >= 1)
        {
            // If only n/10 messages were older than the earliest, then we should move
            // our offset by 10 - n to get all 10 old messages
            pastOffset += (10 - timeCounter);
            queryMessages(pastOffset, SCAN_FORWARD, false);
        }
        // If all messages are not from the past, stop and complain
        else
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.rmb_backtrack_error));
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Process the response after querying for RMB messages (scanning forward).
     * Depending on the response, we may have to check again if there's more messages to get.
     * @param m The response
     * @param offset The current offset
     * @param initialRun if this is the first time the process is being run
     */
    private void processMessageResponseForward(View view, RegionMessages m, int offset, boolean initialRun)
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

        pastOffset += uniqueMessages;

        // If this is the initial run, don't keep going
        if (!initialRun && uniqueMessages >= 10)
        {
            // In this case, all the messages were unique, so there may be more messages to load
            queryMessages(offset+10, SCAN_FORWARD, false);
        }
        else
        {
            if (uniqueMessages <= 0)
            {
                SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
                mSwipeRefreshLayout.setRefreshing(false);
            }
            else
            {
                // We've reached the point where we already have the messages, so put everything back together
                refreshRecycler(SCAN_FORWARD, 0);
            }
        }
    }

    /**
     * Refreshes the contents of the recycler
     */
    private void refreshRecycler(int direction, int newItems)
    {
        Collections.sort(messages.posts);
        mRecyclerAdapter = new MessageBoardRecyclerAdapter(this, messages.posts);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);

        // go back to user position if scanning backward
        if (direction == SCAN_BACKWARD)
        {
            ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(newItems, 40);
        }
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
        savedInstanceState.putInt(BOARD_PAST_OFFSET, pastOffset);
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
            pastOffset = savedInstanceState.getInt(BOARD_PAST_OFFSET, 10);
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
