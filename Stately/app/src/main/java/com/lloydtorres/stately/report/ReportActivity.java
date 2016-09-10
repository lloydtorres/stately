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

package com.lloydtorres.stately.report;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
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
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-07-28.
 * Activity to report user content to the NS moderators.
 */
public class ReportActivity extends SlidrActivity {
    // Keys for intent data and saved preferences
    public static final String REPORT_ID = "reportId";
    public static final String REPORT_TYPE = "reportType";
    public static final String REPORT_USER = "reportUser";
    private static final String REPORT_CATEGORY = "reportCategory";
    private static final String REPORT_CONTENT = "reportContent";

    // Types of reports
    public static final int REPORT_TYPE_TASK = 0;
    public static final int REPORT_TYPE_RMB = 1;
    public static final int REPORT_TYPE_TELEGRAM = 2;

    // Target URL for reports
    public static final String REPORT_URL = "https://www.nationstates.net/page=help";

    // Headers to send when submitting report
    private static final int HEADER_GHR_INAPPROPRIATE = 1;
    private static final int HEADER_GHR_SPAMMER = 3;
    private static final int HEADER_GHR_MODREPLY = 8;
    private static final int HEADER_GHR_OTHER = 9;

    // If no category selected
    private static final int CATEGORY_NONE = -1;

    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout targetHolder;
    private TextView reportTarget;
    private CardView reportCategoryHolder;
    private RadioGroup reportCategorySelect;
    private AppCompatEditText reportContent;

    private int targetId;
    private String targetName;
    private int type;
    private int categoryHolder = CATEGORY_NONE;
    private String contentHolder;

    private boolean isInProgress = false;
    private DialogInterface.OnClickListener dialogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            if (getIntent().getData() != null) {
                targetId = Integer.valueOf(getIntent().getData().getHost());
                type = REPORT_TYPE_TASK;
            }
            else {
                targetId = getIntent().getIntExtra(REPORT_ID, 0);
                targetName = SparkleHelper.getNameFromId(getIntent().getStringExtra(REPORT_USER));
                type = getIntent().getIntExtra(REPORT_TYPE, REPORT_TYPE_TASK);
            }
        }
        if (savedInstanceState != null)
        {
            targetId = savedInstanceState.getInt(REPORT_ID);
            targetName = savedInstanceState.getString(REPORT_USER);
            type = savedInstanceState.getInt(REPORT_TYPE);
            categoryHolder = savedInstanceState.getInt(REPORT_CATEGORY, CATEGORY_NONE);
            contentHolder = savedInstanceState.getString(REPORT_CONTENT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_report);
        setToolbar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.report_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setEnabled(false);

        dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        sendReport();
                    }
                });
            }
        };

        view = findViewById(R.id.report_main);
        targetHolder = (RelativeLayout) findViewById(R.id.report_target_holder);
        reportTarget = (TextView) findViewById(R.id.report_target);
        reportCategoryHolder = (CardView) findViewById(R.id.report_category_holder);
        reportCategorySelect = (RadioGroup) findViewById(R.id.report_category);
        reportContent = (AppCompatEditText) findViewById(R.id.report_content);
        reportContent.setCustomSelectionActionModeCallback(new NullActionCallback());

        targetHolder.setVisibility(View.VISIBLE);
        reportCategoryHolder.setVisibility(View.VISIBLE);

        // If replying to mod mail, use this message instead
        if (type == REPORT_TYPE_TASK) {
            reportTarget.setText(String.format(getString(R.string.report_mod_reply), targetId));
            reportCategoryHolder.setVisibility(View.GONE);
        } else {
            String reportType = "";
            switch (type) {
                case REPORT_TYPE_RMB:
                    reportType = getString(R.string.report_rmb_post);
                    break;
                case REPORT_TYPE_TELEGRAM:
                    reportType = getString(R.string.report_telegram);
                    break;
                default:
                    targetHolder.setVisibility(View.GONE);
                    break;
            }
            reportTarget.setText(String.format(getString(R.string.report_target), reportType, targetId, targetName));
        }

        if (categoryHolder != CATEGORY_NONE) {
            reportCategorySelect.check(categoryHolder);
        }

        if (contentHolder != null && contentHolder.length() > 0) {
            reportContent.setText(contentHolder);
        }
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(getString(R.string.report_title));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Wrapper for sending the report to NS servers.
     */
    private void startSendReport() {
        String reportText = reportContent.getText().toString();
        if (reportText.length() <= 0) {
            SparkleHelper.makeSnackbar(view, getString(R.string.report_blank));
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle(R.string.report_confirm)
                .setPositiveButton(R.string.report_send_confirm, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
    }

    private void sendReport() {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        String typeHeader;
        String reasonHeader;
        switch (type) {
            case REPORT_TYPE_RMB:
                typeHeader = getString(R.string.report_header_rmb_post);
                reasonHeader = getString(R.string.report_header_reason);
                break;
            case REPORT_TYPE_TELEGRAM:
                typeHeader = getString(R.string.report_header_telegram);
                reasonHeader = getString(R.string.report_header_reason);
                break;
            case REPORT_TYPE_TASK:
                typeHeader = getString(R.string.report_header_task);
                reasonHeader = getString(R.string.report_header_response);
                break;
            default:
                typeHeader = "";
                reasonHeader = "";
                break;
        }
        final String commentHeader = String.format(Locale.US, getString(R.string.report_header_comment_template),
                typeHeader, targetId, reasonHeader, reportContent.getText().toString());

        final int problemHeader;
        if (type == REPORT_TYPE_TASK) {
            problemHeader = HEADER_GHR_MODREPLY;
        }
        else {
            switch (reportCategorySelect.getCheckedRadioButtonId()) {
                case R.id.report_inappropriate:
                    problemHeader = HEADER_GHR_INAPPROPRIATE;
                    break;
                case R.id.report_spam:
                    problemHeader = HEADER_GHR_SPAMMER;
                    break;
                default:
                    problemHeader = HEADER_GHR_OTHER;
                    break;
            }
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, REPORT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isInProgress = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        finish();
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
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("problem", Integer.toString(problemHeader));
        params.put("comment", Html.escapeHtml(commentHeader));
        params.put("submit", "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_send_report:
                startSendReport();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(REPORT_ID, targetId);
        savedInstanceState.putInt(REPORT_TYPE, type);
        savedInstanceState.putInt(REPORT_CATEGORY, reportCategorySelect.getCheckedRadioButtonId());

        if (targetName != null)
        {
            savedInstanceState.putString(REPORT_USER, targetName);
        }
        if (reportContent != null)
        {
            savedInstanceState.putString(REPORT_CONTENT, reportContent.getText().toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            targetId = savedInstanceState.getInt(REPORT_ID);
            categoryHolder = savedInstanceState.getInt(REPORT_CATEGORY, CATEGORY_NONE);
            type = savedInstanceState.getInt(REPORT_TYPE);
            targetName = savedInstanceState.getString(REPORT_USER);
            contentHolder = savedInstanceState.getString(REPORT_CONTENT);
        }
    }
}
