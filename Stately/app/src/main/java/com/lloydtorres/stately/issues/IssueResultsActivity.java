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

package com.lloydtorres.stately.issues;

import android.os.Bundle;

import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.IssueResultContainer;
import com.lloydtorres.stately.dto.Nation;

/**
 * Created by Lloyd on 2016-02-29.
 * This activity shows the results of an issue decision.
 */
public class IssueResultsActivity extends RefreshviewActivity {
    public static final String ISSUE_RESULTS_DATA = "issueResultsData";
    public static final String NATION_DATA = "nationData";

    private IssueResultContainer issueResult;
    private Nation mNation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Either get data from intent or restore state
        if (getIntent() != null) {
            issueResult = getIntent().getParcelableExtra(ISSUE_RESULTS_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null) {
            issueResult = savedInstanceState.getParcelable(ISSUE_RESULTS_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        mSwipeRefreshLayout.setEnabled(false);

        setRecyclerAdapter();
    }

    /**
     * Helper class for initializing the recycler adapter.
     */
    private void setRecyclerAdapter() {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssueResultsRecyclerAdapter(this, issueResult, mNation);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssueResultsRecyclerAdapter) mRecyclerAdapter).setContent(issueResult, mNation);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issueResult != null) {
            savedInstanceState.putParcelable(ISSUE_RESULTS_DATA, issueResult);
        }
        if (mNation != null) {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (issueResult == null) {
                issueResult = savedInstanceState.getParcelable(ISSUE_RESULTS_DATA);
            }
            if (mNation == null) {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
