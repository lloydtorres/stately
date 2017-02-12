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
import com.lloydtorres.stately.dto.CensusDelta;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResult;
import com.lloydtorres.stately.dto.IssueResultHeadline;
import com.lloydtorres.stately.dto.Nation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-02-29.
 * This activity shows the results of an issue decision.
 */
public class IssueResultsActivity extends RefreshviewActivity {
    public static final String ISSUE_RESULTS_DATA = "issueResultsData";
    public static final String HEADLINES_DATA = "headlinesData";
    public static final String POSTCARD_DATA = "postcardData";
    public static final String CENSUSDELTA_DATA = "censusDeltaData";
    public static final String NATION_DATA = "nationData";

    private IssueResult issueResult;
    private ArrayList<IssueResultHeadline> headlines;
    private ArrayList<IssuePostcard> postcards;
    private ArrayList<CensusDelta> censusDeltas;
    private Nation mNation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String response = null;
        // Either get data from intent or restore state
        if (getIntent() != null) {
            issueResult = getIntent().getParcelableExtra(ISSUE_RESULTS_DATA);
            headlines = getIntent().getParcelableArrayListExtra(HEADLINES_DATA);
            postcards = getIntent().getParcelableArrayListExtra(POSTCARD_DATA);
            censusDeltas = getIntent().getParcelableArrayListExtra(CENSUSDELTA_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null) {
            issueResult = savedInstanceState.getParcelable(ISSUE_RESULTS_DATA);
            headlines = savedInstanceState.getParcelableArrayList(HEADLINES_DATA);
            postcards = savedInstanceState.getParcelableArrayList(POSTCARD_DATA);
            censusDeltas = savedInstanceState.getParcelableArrayList(CENSUSDELTA_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        mSwipeRefreshLayout.setEnabled(false);

        setRecyclerAdapter();
    }

    /**
     * Helper class for initializing the recycler adapter.
     */
    private void setRecyclerAdapter() {
        List<Object> resultsContent = new ArrayList<Object>();
        if (issueResult != null) {
            resultsContent.add(issueResult);
        }
        resultsContent.addAll(headlines);
        if (postcards != null) {
            resultsContent.addAll(postcards);
        }
        if (censusDeltas != null) {
            resultsContent.addAll(censusDeltas);
        }

        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssueResultsRecyclerAdapter(this, resultsContent, mNation);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssueResultsRecyclerAdapter) mRecyclerAdapter).setContent(resultsContent, mNation);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issueResult != null) {
            savedInstanceState.putParcelable(ISSUE_RESULTS_DATA, issueResult);
        }
        if (headlines != null) {
            savedInstanceState.putParcelableArrayList(HEADLINES_DATA, headlines);
        }
        if (postcards != null) {
            savedInstanceState.putParcelableArrayList(POSTCARD_DATA, postcards);
        }
        if (censusDeltas != null) {
            savedInstanceState.putParcelableArrayList(CENSUSDELTA_DATA, censusDeltas);
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
            if (headlines == null) {
                headlines = savedInstanceState.getParcelableArrayList(HEADLINES_DATA);
            }
            if (postcards == null) {
                postcards = savedInstanceState.getParcelableArrayList(POSTCARD_DATA);
            }
            if (censusDeltas == null) {
                censusDeltas = savedInstanceState.getParcelableArrayList(CENSUSDELTA_DATA);
            }
            if (mNation == null) {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
