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
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.SlidrActivity;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.RegionMessages;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Lloyd on 2016-01-24.
 * An activity that displays the contents of the regional message board.
 */
public class MessageBoardActivity extends SlidrActivity {
    // Uri to invoke MessageBoardActivity
    public static final String RMB_PROTOCOL = "com.lloydtorres.stately.rmb";
    public static final String RMB_TARGET = RMB_PROTOCOL + "://";

    // Keys for Intent data
    public static final String BOARD_REGION_NAME = "regionName";
    public static final String BOARD_TARGET_ID = "targetId";
    public static final String BOARD_MESSAGES = "messages";
    public static final String BOARD_PAST_OFFSET = "pastOffset";

    public static final String PARAM_LIKE = "rmblike";
    public static final String PARAM_UNLIKE = "rmbunlike";

    public static final int NO_LATEST = -1;

    // Direction to scan for messages
    private static final int SCAN_BACKWARD = 0;
    private static final int SCAN_FORWARD = 1;
    private static final int SCAN_SAME = 2;

    private static final int RMB_LOAD_COUNT = 100;

    private static final String CONFIRM_DELETE = "self-deleted by";
    private AlertDialog.Builder dialogBuilder;

    private RegionMessages messages;
    private String regionName;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;
    private int latestId = NO_LATEST;
    private boolean postable = false;
    private boolean likable = false;
    private Post replyTarget = null;
    private boolean isInProgress;

    @BindView(R.id.message_board_refresher)
    SwipyRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.message_board_responder)
    LinearLayout messageResponder;
    @BindView(R.id.responder_content)
    AppCompatEditText messageContainer;
    @BindView(R.id.responder_reply_container)
    RelativeLayout messageReplyContainer;
    @BindView(R.id.responder_reply_content)
    TextView messageReplyContent;
    @BindView(R.id.message_board_toolbar)
    Toolbar toolbar;
    @BindView(R.id.message_board_coordinator)
    View view;

    @BindView(R.id.message_board_recycler)
    RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_board);
        ButterKnife.bind(this);

        isInProgress = false;

        if (getIntent() != null) {
            messages = new RegionMessages();
            messages.posts = new ArrayList<Post>();
            uniqueEnforcer = new HashSet<Integer>();

            if (getIntent().getData() != null) {
                regionName = SparkleHelper.getNameFromId(getIntent().getData().getHost());
                latestId = Integer.valueOf(getIntent().getData().getLastPathSegment());
            } else {
                regionName = getIntent().getStringExtra(BOARD_REGION_NAME);
                latestId = getIntent().getIntExtra(BOARD_TARGET_ID, NO_LATEST);
            }
        }
        if (savedInstanceState != null) {
            messages = savedInstanceState.getParcelable(BOARD_MESSAGES);
            regionName = savedInstanceState.getString(BOARD_REGION_NAME);
            pastOffset = savedInstanceState.getInt(BOARD_PAST_OFFSET, RMB_LOAD_COUNT);
            latestId = savedInstanceState.getInt(BOARD_TARGET_ID, NO_LATEST);
            rebuildUniqueEnforcer();
        }

        setToolbar(toolbar);

        dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));

        mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout.setColorSchemeResources(RaraHelper.getThemeRefreshColours(this));
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
        queryPostingRights();
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(Locale.US, getString(R.string.region_rmb), regionName));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Hack to make swipe refresh show up.
     */
    private void startSwipeRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    /**
     * Enables message respond box if user has posting rights, then calls on function to load messages.
     */
    private void queryPostingRights() {
        // If user is a member of this region, enable posting rights immediately without querying
        if (PinkaHelper.getRegionSessionData(getApplicationContext()).equals(SparkleHelper.getIdFromName(regionName))) {
            enablePostingRights();
            queryPostingRightsCallback();
            likable = true;
            markBoardAsRead();
            return;
        }

        startSwipeRefresh();
        String targetURL = String.format(Locale.US, RegionMessages.RAW_QUERY, SparkleHelper.getIdFromName(regionName));

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        // If the textbox exists in the page, it means that the user has posting rights
                        if (d.select("textarea[name=message]").first() != null) {
                            enablePostingRights();
                        }
                        queryPostingRightsCallback();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                // If an error occurs, fail gracefully and query messages anyway
                queryPostingRightsCallback();
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * When this is called, it enables a bunch of views that lets the user post in the RMB.
     */
    private void enablePostingRights() {
        messageResponder.setVisibility(View.VISIBLE);
        postable = true;
    }

    /**
     * Called after checking a user's posting rights.
     * If there are no messages stored, start query. If there's messages available
     * (meaning they were restored from an old session), just show those messages.
     */
    private void queryPostingRightsCallback() {
        // If there are no messages, load them from NS
        if (messages.posts.size() <= 0) {
            startSwipeRefresh();
            startQueryMessages(SCAN_FORWARD, true);
        }
        // Otherwise, they already here so just show it normally
        else {
            refreshRecycler(SCAN_FORWARD, 0, false);
        }
    }

    /**
     * Performs a GET request on the NS region page, which should mark the RMB as read and clear the unread count.
     */
    private void markBoardAsRead() {
        String targetURL = String.format(Locale.US, Region.QUERY_HTML, SparkleHelper.getIdFromName(regionName));
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
            }
        });
        DashHelper.getInstance(this).addRequest(stringRequest);
    }

    /**
     * Load swipe refresher and start loading messages.
     */
    private void startQueryMessages(int direction, boolean firstRun) {
        startSwipeRefresh();
        queryMessages(0, direction, firstRun);
    }

    /**
     * Get RMB messages from NationStates
     * @param offset the number of messages to skip
     */
    private void queryMessages(final int offset, final int direction, final boolean initialRun) {
        String targetURL;
        if (direction == SCAN_FORWARD && latestId != NO_LATEST) {
            targetURL = String.format(Locale.US, RegionMessages.QUERY_ID, SparkleHelper.getIdFromName(regionName), latestId, RMB_LOAD_COUNT);
        }
        else {
            targetURL = String.format(Locale.US, RegionMessages.QUERY, SparkleHelper.getIdFromName(regionName), offset, RMB_LOAD_COUNT);
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    RegionMessages messageResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            messageResponse = serializer.read(RegionMessages.class, response);
                            switch (direction) {
                                case SCAN_BACKWARD:
                                    processMessageResponseBackward(messageResponse);
                                    break;
                                default:
                                    processMessageResponseForward(messageResponse, initialRun);
                                    break;
                            }
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
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
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Used to scan for previous messages.
     * @param m Message response
     */
    private void processMessageResponseBackward(RegionMessages m) {
        // If there's nothing in the current messages, then there's probably nothing in the past
        if (messages.posts.size() <= 0 || m.posts.size() <= 0) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
            return;
        }

        Collections.sort(m.posts);

        // Count the number of obtained posts that were posted earlier than the current earliest
        long earliestCurrentDate = messages.posts.get(0).timestamp;
        int timeCounter = 0;
        // Only add posts that are older than the current oldest post
        for (Post p : m.posts) {
            if (p.timestamp < earliestCurrentDate && !uniqueEnforcer.contains(p.id)) {
                timeCounter++;
                messages.posts.add(p);
                uniqueEnforcer.add(p.id);
            }
        }

        // If at least 25% of messages were from the past, we're good
        int quarterMessages = (int)(m.posts.size() * 0.25);
        if (timeCounter >= quarterMessages) {
            pastOffset += timeCounter;
            refreshRecycler(SCAN_BACKWARD, m.posts.size(), false);
        }
        // If less than a quarter were from the past, adjust the offset and try again
        else if (timeCounter < quarterMessages && timeCounter >= 1) {
            // Since we already added the 25% of posts that are guaranteed to be old, we can
            // shift the offset by the size of the sample received and get the next batch
            pastOffset += m.posts.size();
            queryMessages(pastOffset, SCAN_FORWARD, false);
        }
        // If all messages are not from the past, stop and complain
        else {
            SparkleHelper.makeSnackbar(view, getString(R.string.rmb_backtrack_error));
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Process the response after querying for RMB messages (scanning forward).
     * Depending on the response, we may have to check again if there's more messages to get.
     * @param m The response
     * @param initialRun if this is the first time the process is being run
     */
    private void processMessageResponseForward(RegionMessages m, boolean initialRun) {
        // Only add unique posts to our list
        int uniqueMessages = 0;
        for (Post p : m.posts) {
            if (!uniqueEnforcer.contains(p.id)) {
                messages.posts.add(p);
                uniqueEnforcer.add(p.id);
                uniqueMessages++;
            }
        }
        pastOffset += uniqueMessages;
        if (uniqueMessages <= 0) {
            if (!initialRun) {
                SparkleHelper.makeSnackbar(view, getString(R.string.rmb_caught_up));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        // If this is the first run and the latest ID was specified, then jump to top since
        // that's what the user should want.
        boolean shouldJumpToTop = initialRun && latestId != NO_LATEST;
        // Figure out the new latest value.
        Collections.sort(messages.posts);
        if (messages.posts.size()-1 >= 0) {
            latestId = messages.posts.get(messages.posts.size()-1).id;
        }

        // We've reached the point where we already have the messages, so put everything back together
        refreshRecycler(SCAN_FORWARD, 0, shouldJumpToTop);

        // Mark board as read if this is the user's region's RMB
        if (PinkaHelper.getRegionSessionData(getApplicationContext()).equals(SparkleHelper.getIdFromName(regionName))) {
            markBoardAsRead();
        }
    }

    /**
     * Used for setting a reply message for the post.
     * @param p
     */
    public void setReplyMessage(Post p) {
        replyTarget = p;
        if (replyTarget != null) {
            messageReplyContainer.setVisibility(View.VISIBLE);
            messageReplyContent.setText(String.format(Locale.US, getString(R.string.rmb_reply), SparkleHelper.getNameFromId(p.name)));
            messageContainer.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(messageContainer, InputMethodManager.SHOW_IMPLICIT);
        }
        else if (mRecyclerAdapter != null) {
            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setReplyIndex(MessageBoardRecyclerAdapter.NO_SELECTION);
            messageReplyContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Helper for scrolling to a certain position.
     * @param p Post
     * @param i Post index
     */
    public void setReplyMessage(Post p, int i) {
        setReplyMessage(p);
        mLayoutManager.scrollToPosition(i);
    }

    /**
     * It's called postMessage(), but this actually gets the chk value first before calling
     * the function that actually posts the message.
     */
    @OnClick(R.id.responder_post_button)
    public void postMessage() {
        // Make sure there's actually a message to post first
        if (messageContainer.getText().length() <= 0) {
            return;
        }

        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        startSwipeRefresh();
        String targetURL = String.format(Locale.US, Region.QUERY_HTML, SparkleHelper.getIdFromName(regionName));

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=chk]").first();

                        if (input == null) {
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
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * The actual function that POSTs the message.
     * @param chk
     */
    private void postActualMessage(final String chk) {
        String targetURL = String.format(Locale.US, RegionMessages.POST_QUERY, SparkleHelper.getIdFromName(regionName));

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        isInProgress = false;
                        messageContainer.setText("");
                        setReplyMessage(null);
                        startQueryMessages(SCAN_FORWARD, false);
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
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("chk", chk);
        String newMessage = messageContainer.getText().toString();
        if (replyTarget != null) {
            String quoteMessage = replyTarget.message;
            quoteMessage = SparkleHelper.regexRemove(quoteMessage, SparkleHelper.BBCODE_QUOTE);
            quoteMessage = SparkleHelper.regexRemove(quoteMessage, SparkleHelper.BBCODE_QUOTE_1);
            quoteMessage = SparkleHelper.regexRemove(quoteMessage, SparkleHelper.BBCODE_QUOTE_2);
            quoteMessage = String.format(Locale.US, getString(R.string.rmb_reply_format), replyTarget.name, replyTarget.id, quoteMessage);
            newMessage = quoteMessage + newMessage;
        }
        params.put("message", SparkleHelper.escapeHtml(newMessage));
        params.put("lodge_message", "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Called if user tries to like their own post.
     */
    public void selfLikeStatus() {
        SparkleHelper.makeSnackbar(view, getString(R.string.rmb_self_like));
    }

    /**
     * Sends a request to like or unlike a specified post.
     * @param pos Position of the post in the adapter.
     * @param id ID of the post to un/like
     * @param sendLike True if like should be sent, false if unlike
     */
    public void setLikeStatus(final int pos, final int id, final boolean sendLike) {
        if (!likable) {
            SparkleHelper.makeSnackbar(view, getString(R.string.rmb_cant_like));
            return;
        }

        // Set like status in UI immediately for user friendliness
        ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setLikeStatus(pos, sendLike);

        String targetURL = String.format(Locale.US, RegionMessages.LIKE_QUERY, sendLike ? PARAM_LIKE : PARAM_UNLIKE, id);
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // If success, do nothing
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                // Undo action on error
                ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setLikeStatus(pos, !sendLike);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            // Undo action on error
            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setLikeStatus(pos, !sendLike);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Public convenience class to confirm if post should be deleted.
     * @param pos Position of the deleted post
     * @param id
     */
    public void confirmDelete(final int pos, final int id) {
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
    private void postMessageDelete(final int pos, final int id) {
        String targetURL = String.format(Locale.US, RegionMessages.DELETE_QUERY, SparkleHelper.getIdFromName(regionName), id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (!response.contains(CONFIRM_DELETE)) {
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                        }
                        else {
                            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setAsDeleted(pos);
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
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Refreshes the contents of the recycler
     */
    private void refreshRecycler(int direction, int newItems, boolean jumpToTop) {
        Collections.sort(messages.posts);
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new MessageBoardRecyclerAdapter(this, messages.posts, postable, getSupportFragmentManager());
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        else {
            ((MessageBoardRecyclerAdapter) mRecyclerAdapter).setMessages(messages.posts);
        }
        mSwipeRefreshLayout.setRefreshing(false);

        if (jumpToTop) {
            mLayoutManager.scrollToPosition(0);
        } else {
            switch (direction) {
                case SCAN_FORWARD:
                    mLayoutManager.scrollToPosition(messages.posts.size()-1);
                    break;
                case SCAN_BACKWARD:
                    ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(newItems, 40);
                    ((MessageBoardRecyclerAdapter) mRecyclerAdapter).addToReplyIndex(newItems);
                    break;
            }
        }
    }

    /**
     * This function rebuilds the set used to track unique messages after a restart.
     * Because set isn't parcelable :(
     */
    private void rebuildUniqueEnforcer() {
        uniqueEnforcer = new HashSet<Integer>();
        for (Post p : messages.posts) {
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(BOARD_PAST_OFFSET, pastOffset);
        if (messages != null) {
            savedInstanceState.putParcelable(BOARD_MESSAGES, messages);
        }
        if (regionName != null) {
            savedInstanceState.putString(BOARD_REGION_NAME, regionName);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            pastOffset = savedInstanceState.getInt(BOARD_PAST_OFFSET, RMB_LOAD_COUNT);
            if (messages == null) {
                messages = savedInstanceState.getParcelable(BOARD_MESSAGES);
                rebuildUniqueEnforcer();
            }
            if (regionName == null) {
                regionName = savedInstanceState.getString(BOARD_REGION_NAME);
            }
        }
    }
}
