package com.lloydtorres.stately.issues;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-28.
 * This activity displays options for a particular issue.
 */
public class IssueDecisionActivity extends AppCompatActivity {
    // Keys for Intent data
    public static final String ISSUE_DATA = "issueData";

    private Issue issue;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_decision);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            issue = getIntent().getParcelableExtra(ISSUE_DATA);
        }
        if (savedInstanceState != null)
        {
            issue = savedInstanceState.getParcelable(ISSUE_DATA);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.issue_decision_toolbar);
        setToolbar(toolbar);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.issue_decision_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //@TODO
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.issue_decision_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        if (issue != null)
        {
            savedInstanceState.putParcelable(ISSUE_DATA, issue);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (issue == null)
            {
                issue = savedInstanceState.getParcelable(ISSUE_DATA);
            }
        }
    }
}
