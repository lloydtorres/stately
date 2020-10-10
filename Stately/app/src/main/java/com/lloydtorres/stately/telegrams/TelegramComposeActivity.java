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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
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
import com.lloydtorres.stately.core.SlidrActivity;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

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
public class TelegramComposeActivity extends SlidrActivity {
    // Keys for intent data
    public static final String REPLY_ID_DATA = "replyIdData";
    public static final String RECIPIENTS_DATA = "recipientsData";
    public static final String DEVELOPER_TG_DATA = "developerTgData";
    public static final String TG_CONTENT_DATA = "tgContentData";
    public static final int NO_REPLY_ID = -1;
    private static final String SENT_CONFIRM_1 = "Your telegram is being wired";
    private static final String SENT_CONFIRM_2 = "Your telegram has been wired";

    private int replyId = NO_REPLY_ID;
    private String recipients;
    private boolean isInProgress = false;
    private boolean isDeveloperTg = false;

    private View mView;
    private CardView headerCardView;
    private CardView developerCardView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AppCompatEditText recipientsField;
    private TextView senderField;
    private AppCompatEditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_compose);
        isInProgress = false;

        // Either get data from intent or restore state
        if (getIntent() != null) {
            replyId = getIntent().getIntExtra(REPLY_ID_DATA, NO_REPLY_ID);
            recipients = getIntent().getStringExtra(RECIPIENTS_DATA);
            isDeveloperTg = getIntent().getBooleanExtra(DEVELOPER_TG_DATA, false);
        }

        String savedContent = null;
        if (savedInstanceState != null) {
            replyId = savedInstanceState.getInt(REPLY_ID_DATA, NO_REPLY_ID);
            isDeveloperTg = savedInstanceState.getBoolean(DEVELOPER_TG_DATA, false);
            recipients = savedInstanceState.getString(RECIPIENTS_DATA);
            savedContent = savedInstanceState.getString(TG_CONTENT_DATA);
        }

        mView = findViewById(R.id.telegram_compose_main);

        headerCardView = findViewById(R.id.telegram_compose_header);
        developerCardView = findViewById(R.id.telegram_compose_developer_header);
        headerCardView.setVisibility(isDeveloperTg ? View.GONE : View.VISIBLE);
        developerCardView.setVisibility(isDeveloperTg ? View.VISIBLE : View.GONE);

        mSwipeRefreshLayout = findViewById(R.id.telegram_compose_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(RaraHelper.getThemeRefreshColours(this));
        mSwipeRefreshLayout.setEnabled(false);

        Toolbar toolbar = findViewById(R.id.telegram_compose_toolbar);
        setToolbar(toolbar);

        recipientsField = findViewById(R.id.telegram_compose_recipients);
        if (recipients != null && recipients.length() > 0) {
            recipientsField.setText(recipients);
            if (replyId != NO_REPLY_ID) {
                // If this is a reply telegram, don't let user edit this field
                recipientsField.setEnabled(false);
                recipientsField.setFocusable(false);
            }
        }

        senderField = findViewById(R.id.telegram_compose_sender);
        senderField.setText(PinkaHelper.getActiveUser(this).name);

        content = findViewById(R.id.telegram_compose_content);

        if (savedContent != null) {
            content.setText(savedContent);
        }

        content.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        String title = getString(R.string.telegrams_compose);
        if (replyId != NO_REPLY_ID) {
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
    private void checkTelegramRecipients() {
        // Clear focus then hide keyboard
        recipientsField.clearFocus();
        content.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);

        // Sanity checks
        String recipientsRaw = recipientsField.getText().toString();
        if (recipientsRaw.length() <= 0) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_empty_recipients));
            return;
        }

        String contentRaw = content.getText().toString();
        if (contentRaw.length() <= 0) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_empty_content));
            return;
        }

        // Check if recipients are valid names
        List<String> recipientIds = new ArrayList<String>();
        if (recipientsRaw.contains(",")) {
            List<String> recipientItems = Arrays.asList(recipientsRaw.split(","));
            for (String r : recipientItems) {
                String rawRecipient = r.trim();
                if (verifyRecipientEntry(rawRecipient)) {
                    recipientIds.add(SparkleHelper.getIdFromName(rawRecipient));
                } else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_invalid_recipient));
                    return;
                }
            }
        }
        else {
            String recipientCheck = recipientsRaw.trim();
            if (verifyRecipientEntry(recipientCheck)) {
                recipientIds.add(SparkleHelper.getIdFromName(recipientCheck));
            } else {
                SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_invalid_recipient));
                return;
            }
        }

        startTelegramSend(recipientIds);
    }

    private boolean verifyRecipientEntry(String entry) {
        return entry.length() > 0 && (SparkleHelper.isValidName(entry) || SparkleHelper.isValidName(entry.replace("region:", "")));
    }

    /**
     * Helper for starting the telegram sending process.
     * @param recipients List of verified recipients
     */
    private void startTelegramSend(final List<String> recipients) {
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
    private void getTelegramCheckValue(final List<String> recipients) {
        if (isInProgress) {
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

                        if (input == null) {
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
                } else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    private void sendTelegram(final List<String> recipients, final String chk) {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, Telegram.SEND_TELEGRAM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        isInProgress = false;
                        String textResponse = Jsoup.parse(response, SparkleHelper.BASE_URI).text();
                        if (textResponse.contains(SENT_CONFIRM_1) || textResponse.contains(SENT_CONFIRM_2)) {
                            finish();
                        } else {
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
                } else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("chk", chk);
        params.put("tgto", SparkleHelper.joinStringList(recipients, ", "));
        if (replyId == NO_REPLY_ID) {
            params.put("recruitregion", "region");
            params.put("recruitregionrealname", PinkaHelper.getRegionSessionData(getApplicationContext()));
            params.put("send", "1");
        } else {
            params.put("in_reply_to", String.valueOf(replyId));
            if (recipients.size() > 1) {
                params.put("send_to_all", "1");
            } else {
                params.put("send", "1");
            }
        }
        params.put("message", content.getText().toString());
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(REPLY_ID_DATA, replyId);
        savedInstanceState.putBoolean(DEVELOPER_TG_DATA, isDeveloperTg);

        if (recipients != null) {
            savedInstanceState.putString(RECIPIENTS_DATA, recipients);
        }
        if (content.getText() != null) {
            savedInstanceState.putString(TG_CONTENT_DATA, content.getText().toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            replyId = savedInstanceState.getInt(REPLY_ID_DATA, NO_REPLY_ID);
            isDeveloperTg = savedInstanceState.getBoolean(DEVELOPER_TG_DATA, false);
            recipients = savedInstanceState.getString(RECIPIENTS_DATA);
            content.setText(savedInstanceState.getString(TG_CONTENT_DATA));
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
