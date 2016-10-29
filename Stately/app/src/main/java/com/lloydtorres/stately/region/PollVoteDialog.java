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

import android.os.Bundle;
import android.view.View;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerDialogFragment;
import com.lloydtorres.stately.dto.Poll;

/**
 * Created by Lloyd on 2016-10-02.
 * Dialog allows user to choose between different regional poll options.
 */

public class PollVoteDialog extends RecyclerDialogFragment {
    public static final String DIALOG_TAG = "fragment_poll_vote_dialog";
    public static final String POLL_DATA = "pollData";

    private RegionCommunitySubFragment fragment;
    private Poll pollData;

    public void setData(RegionCommunitySubFragment frag, Poll p) {
        fragment = frag;
        pollData = p;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore saved state
        if (savedInstanceState != null) {
            pollData = savedInstanceState.getParcelable(POLL_DATA);
        }
    }

    @Override
    protected void initRecycler(View view) {
        super.initRecycler(view);
        setDialogTitle(getString(R.string.card_region_poll));
        mRecyclerAdapter = new PollVoteRecyclerAdapter(fragment, this, pollData);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putParcelable(POLL_DATA, pollData);
    }
}
