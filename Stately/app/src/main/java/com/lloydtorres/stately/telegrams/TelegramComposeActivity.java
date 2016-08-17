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

package com.lloydtorres.stately.telegrams;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.r0adkll.slidr.Slidr;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lloyd on 2016-03-12.
 * This activity lets users compose or reply to telegrams.
 */
public class TelegramComposeActivity extends AppCompatActivity {
    // Keys for intent data
    public static final String REPLY_ID_DATA = "replyIdData";
    public static final String RECIPIENTS_DATA = "recipientsData";
    public static final int NO_REPLY_ID = -1;
    private static final String SENT_CONFIRM_1 = "Your telegram is being wired";
    private static final String SENT_CONFIRM_2 = "Your telegram has been wired";

    private int replyId = NO_REPLY_ID;
    private String recipients;
    private boolean isInProgress = false;

    private View mView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppCompatEditText recipientsField;
    private TextView senderField;
    private AppCompatEditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_compose);
        Slidr.attach(this, SparkleHelper.slidrConfig);
        isInProgress = false;

        // Either get data from intent or restore state
        if (getIntent() != null) {
            replyId = getIntent().getIntExtra(REPLY_ID_DATA, NO_REPLY_ID);
            recipients = getIntent().getStringExtra(RECIPIENTS_DATA);
        }

        mView = findViewById(R.id.telegram_compose_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.telegram_compose_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setEnabled(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.telegram_compose_toolbar);
        setToolbar(toolbar);

        recipientsField = (AppCompatEditText) findViewById(R.id.telegram_compose_recipients);
        recipientsField.setCustomSelectionActionModeCallback(new NullActionCallback());
        if (recipients != null && recipients.length() > 0)
        {
            recipientsField.setText(recipients);
            if (replyId != NO_REPLY_ID)
            {
                // If this is a reply telegram, don't let user edit this field
                recipientsField.setEnabled(false);
                recipientsField.setFocusable(false);
            }
        }

        senderField = (TextView) findViewById(R.id.telegram_compose_sender);
        senderField.setText(SparkleHelper.getActiveUser(this).name);

        content = (AppCompatEditText) findViewById(R.id.telegram_compose_content);
        content.setCustomSelectionActionModeCallback(new NullActionCallback());
        content.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        String title = getString(R.string.telegrams_compose);
        if (replyId != NO_REPLY_ID)
        {
            title = getString(R.string.telegrams_reply);
        }
        getSupportActionBar().setTitle(title);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * This makes sure that the recipients entered by the user are valid.
     */
    private void checkTelegramRecipients()
    {
        // Clear focus then hide keyboard
        recipientsField.clearFocus();
        content.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);

        // Sanity checks
        String recipientsRaw = recipientsField.getText().toString();
        if (recipientsRaw.length() <= 0)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_empty_recipients));
            return;
        }

        String contentRaw = content.getText().toString();
        if (contentRaw.length() <= 0)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_empty_content));
            return;
        }

        // Check if recipients are valid names
        List<String> recipientIds = new ArrayList<String>();
        if (recipientsRaw.contains(","))
        {
            List<String> recipientItems = Arrays.asList(recipientsRaw.split(","));
            for (String r : recipientItems)
            {
                String rawRecipient = r.trim();
                if (SparkleHelper.isValidName(rawRecipient) && rawRecipient.length() > 0)
                {
                    recipientIds.add(SparkleHelper.getIdFromName(rawRecipient));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_invalid_recipient));
                    return;
                }
            }
        }
        else
        {
            String recipientCheck = recipientsRaw.trim();
            if (SparkleHelper.isValidName(recipientCheck) && recipientCheck.length() > 0)
            {
                recipientIds.add(SparkleHelper.getIdFromName(recipientCheck));
            }
            else
            {
                SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_invalid_recipient));
                return;
            }
        }

        startTelegramSend(recipientIds);
    }

    /**
     * Helper for starting the telegram sending process.
     * @param recipients List of verified recipients
     */
    private void startTelegramSend(final List<String> recipients)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                getTelegramCheckValue(recipients);
            }
        });
    }

    /**
     * Gets the check value needed to send a telegram.
     * @param recipients List of verified recipients
     */
    private void getTelegramCheckValue(final List<String> recipients)
    {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, Telegram.SEND_TELEGRAM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=chk]").first();

                        if (input == null)
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
                            return;
                        }

                        String chk = input.attr("value");
                        sendTelegram(recipients, chk);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    private void sendTelegram(final List<String> recipients, final String chk)
    {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, Telegram.SEND_TELEGRAM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        isInProgress = false;
                        String textResponse = Jsoup.parse(response, SparkleHelper.BASE_URI).text();
                        if (textResponse.contains(SENT_CONFIRM_1) || textResponse.contains(SENT_CONFIRM_2))
                        {
                            finish();
                        }
                        else
                        {
                            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_fail));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("chk", chk);
        params.put("tgto", SparkleHelper.joinStringList(recipients, ", "));
        if (replyId == NO_REPLY_ID) {
            params.put("recruitregion", "region");
            params.put("recruitregionrealname", SparkleHelper.getRegionSessionData(getApplicationContext()));
            params.put("send", "1");
        }
        else {
            params.put("in_reply_to", String.valueOf(replyId));
            if (recipients.size() > 1) {
                params.put("send_to_all", "1");
            }
            else {
                params.put("send", "1");
            }
        }
        params.put("message", Html.escapeHtml(content.getText().toString()));
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_telegram_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_send_telegram:
                checkTelegramRecipients();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
