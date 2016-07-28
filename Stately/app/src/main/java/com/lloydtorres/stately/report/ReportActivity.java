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

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.r0adkll.slidr.Slidr;

/**
 * Created by Lloyd on 2016-07-28.
 * Activity to report user content to the NS moderators.
 */
public class ReportActivity extends AppCompatActivity {
    // Keys for intent data and saved preferences
    public static final String REPORT_ID = "reportId";
    public static final String REPORT_TYPE = "reportType";
    public static final String REPORT_USER = "reportUser";
    private static final String REPORT_CATEGORY = "reportCategory";
    private static final String REPORT_CONTENT = "reportContent";

    public static final int REPORT_TYPE_TASK = 0;
    public static final int REPORT_TYPE_RMB = 1;
    public static final int REPORT_TYPE_TELEGRAM = 2;

    private static final int CATEGORY_NONE = -1;

    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout targetHolder;
    private TextView reportTarget;
    private RadioGroup reportCategorySelect;
    private EditText reportContent;

    private int targetId;
    private String targetName;
    private int type;
    private int categoryHolder = CATEGORY_NONE;
    private String contentHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Slidr.attach(this, SparkleHelper.slidrConfig);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            targetId = getIntent().getIntExtra(REPORT_ID, 0);
            targetName = SparkleHelper.getNameFromId(getIntent().getStringExtra(REPORT_USER));
            type = getIntent().getIntExtra(REPORT_TYPE, REPORT_TYPE_TASK);
            // @TODO: Handle URL intents.
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

        view = findViewById(R.id.report_main);
        targetHolder = (RelativeLayout) findViewById(R.id.report_target_holder);
        reportTarget = (TextView) findViewById(R.id.report_target);
        reportCategorySelect = (RadioGroup) findViewById(R.id.report_category);
        reportContent = (EditText) findViewById(R.id.report_content);

        String reportType = "";
        targetHolder.setVisibility(View.VISIBLE);
        switch (type) {
            case REPORT_TYPE_TASK:
                targetHolder.setVisibility(View.GONE);
                break;
            case REPORT_TYPE_RMB:
                reportType = getString(R.string.report_rmb_post);
                break;
            case REPORT_TYPE_TELEGRAM:
                reportType = getString(R.string.report_telegram);
                break;
        }
        reportTarget.setText(String.format(getString(R.string.report_target), reportType, targetId, targetName));

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
                // @TODO: Actually send report
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
