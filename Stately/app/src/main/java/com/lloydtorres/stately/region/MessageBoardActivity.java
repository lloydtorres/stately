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

package com.lloydtorres.stately.region;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.RegionMessages;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private static final int SCAN_SAME = 2;

    private static final String CONFIRM_DELETE = "self-deleted by";
    private AlertDialog.Builder dialogBuilder;

    private RegionMessages messages;
    private String regionName;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;
    private boolean postable = false;
    private Post replyTarget = null;

    private SwipyRefreshLayout mSwipeRefreshLayout;
    private LinearLayout messageResponder;
    private EditText messageContainer;
    private ImageView messagePostButton;
    private ImageView.OnClickListener postMessageListener;
    private RelativeLayout messageReplyContainer;
    private TextView messageReplyContent;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_board);

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

        dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);

        mRecyclerView = (RecyclerView) findViewById(R.id.message_board_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        postMessageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMessage();
            }
        };

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.message_board_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction == SwipyRefreshLayoutDirection.TOP) {
                    queryMessages(pastOffset, SCAN_BACKWARD, false);
                } else {
                    queryMessages(0, SCAN_FORWARD, false);
                }
            }
        });
        processRegionMembership();
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
     * Hack to make swipe refresh show up.
     */
    private void startSwipeRefresh()
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    /**
     * Enables message respond box if member of region, then calls on function to load messages.
     */
    private void processRegionMembership()
    {
        if (SparkleHelper.getRegionSessionData(getApplicationContext()).equals(SparkleHelper.getIdFromName(regionName)))
        {
            messageResponder = (LinearLayout) findViewById(R.id.message_board_responder);
            messageResponder.setVisibility(View.VISIBLE);
            messageContainer = (EditText) findViewById(R.id.responder_content);
            messageContainer.setCustomSelectionActionModeCallback(new NullActionCallback());
            messagePostButton = (ImageView) findViewById(R.id.responder_post_button);
            messagePostButton.setOnClickListener(postMessageListener);
            messageReplyContainer = (RelativeLayout) findViewById(R.id.responder_reply_container);
            messageReplyContent = (TextView) findViewById(R.id.responder_reply_content);
            postable = true;
        }

        if (messages.posts.size() <= 0)
        {
            startSwipeRefresh();
            startQueryMessages(SCAN_FORWARD);
        }
        // Otherwise just show it normally
        else
        {
            refreshRecycler(SCAN_FORWARD, 0);
        }
    }

    /**
     * Load swipe refresher and start loading messages.
     */
    private void startQueryMessages(int direction)
    {
        startSwipeRefresh();
        queryMessages(0, direction, true);
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
            pastOffset = offset;
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
            queryMessages(offset + 10, SCAN_FORWARD, false);
        }
        else
        {
            if (uniqueMessages <= 0)
            {
                if (!initialRun)
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
            
            // We've reached the point where we already have the messages, so put everything back together
            refreshRecycler(SCAN_FORWARD, 0);
        }
    }

    /**
     * Used for setting a reply message for the post.
     * @param p
     */
    public void setReplyMessage(Post p)
    {
        replyTarget = p;
        if (replyTarget != null)
        {
            messageReplyContainer.setVisibility(View.VISIBLE);
            messageReplyContent.setText(String.format(getString(R.string.rmb_reply), SparkleHelper.getNameFromId(p.name)));
            messageContainer.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(messageContainer, InputMethodManager.SHOW_IMPLICIT);
        }
        else
        {
            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setReplyIndex(MessageBoardRecyclerAdapter.NO_SELECTION);
            messageReplyContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Helper for scrolling to a certain position.
     * @param p Post
     * @param i Post index
     */
    public void setReplyMessage(Post p, int i)
    {
        setReplyMessage(p);
        mLayoutManager.scrollToPosition(i);
    }

    /**
     * It's called postMessage(), but this actually gets the chk value first before calling
     * the function that actually posts the message.
     */
    private void postMessage()
    {
        // Make sure there's actually a message to post first
        if (messageContainer.getText().length() <= 0)
        {
            return;
        }

        startSwipeRefresh();
        messagePostButton.setOnClickListener(null);
        final View view = findViewById(R.id.message_board_coordinator);
        String targetURL = String.format(Region.GET_QUERY, SparkleHelper.getIdFromName(regionName));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=chk]").first();

                        if (input == null)
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            return;
                        }

                        String chk = input.attr("value");
                        postActualMessage(chk);
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
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                params.put("Content-Type", "application/x-www-form-urlencoded");
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
     * The actual function that POSTs the message.
     * @param chk
     */
    private void postActualMessage(final String chk)
    {
        final View view = findViewById(R.id.message_board_coordinator);
        String targetURL = String.format(RegionMessages.POST_QUERY, SparkleHelper.getIdFromName(regionName));

        StringRequest stringRequest = new StringRequest(Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        messageContainer.setText("");
                        messagePostButton.setOnClickListener(postMessageListener);
                        setReplyMessage(null);
                        startQueryMessages(SCAN_FORWARD);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                messagePostButton.setOnClickListener(postMessageListener);
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
                params.put("chk", chk);

                String newMessage = messageContainer.getText().toString();
                if (replyTarget != null)
                {
                    String quoteMessage = replyTarget.message;
                    quoteMessage = SparkleHelper.regexRemove(quoteMessage, "(?s)\\[quote\\](.*?)\\[\\/quote\\]");
                    quoteMessage = SparkleHelper.regexRemove(quoteMessage, "(?s)\\[quote=(.*?);[0-9]+\\](.*?)\\[\\/quote\\]");
                    quoteMessage = SparkleHelper.regexRemove(quoteMessage, "(?s)\\[quote=(.*?)\\](.*?)\\[\\/quote\\]");
                    quoteMessage = String.format(getString(R.string.rmb_reply_format), replyTarget.name, replyTarget.id, quoteMessage);
                    newMessage = quoteMessage + newMessage;
                }
                params.put("message", newMessage);

                params.put("lodge_message", "1");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            messagePostButton.setOnClickListener(postMessageListener);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Public convenience class to confirm if post should be deleted.
     * @param pos Position of the deleted post
     * @param id
     */
    public void confirmDelete(final int pos, final int id)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSwipeRefresh();
                postMessageDelete(pos, id);
                dialog.dismiss();
            }
        };

        dialogBuilder.setTitle(R.string.rmb_delete_confirm)
                .setPositiveButton(R.string.rmb_delete, dialogClickListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
    }

    /**
     * POSTs a delete command to the NS servers.
     * @param pos Position of the deleted post
     * @param id Post ID
     */
    private void postMessageDelete(final int pos, final int id)
    {
        final View view = findViewById(R.id.message_board_coordinator);
        String targetURL = String.format(RegionMessages.DELETE_QUERY, SparkleHelper.getIdFromName(regionName), id);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (!response.contains(CONFIRM_DELETE))
                        {
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                        }
                        else
                        {
                            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setAsDeleted(pos);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                messagePostButton.setOnClickListener(postMessageListener);
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
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            messagePostButton.setOnClickListener(postMessageListener);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Refreshes the contents of the recycler
     */
    private void refreshRecycler(int direction, int newItems)
    {
        Collections.sort(messages.posts);
        if (mRecyclerAdapter == null)
        {
            mRecyclerAdapter = new MessageBoardRecyclerAdapter(this, messages.posts, postable);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        else
        {
            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setMessages(messages.posts);
        }
        mSwipeRefreshLayout.setRefreshing(false);

        switch (direction)
        {
            case SCAN_FORWARD:
                mLayoutManager.scrollToPosition(messages.posts.size()-1);
                break;
            case SCAN_BACKWARD:
                ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(newItems, 40);
                ((MessageBoardRecyclerAdapter) mRecyclerAdapter).addToReplyIndex(newItems);
                break;
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
