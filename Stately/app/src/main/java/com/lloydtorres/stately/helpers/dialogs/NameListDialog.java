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

package com.lloydtorres.stately.helpers.dialogs;

import android.os.Bundle;
import android.view.View;

import com.lloydtorres.stately.core.RecyclerDialogFragment;
import com.lloydtorres.stately.dto.DelegateVote;
import com.lloydtorres.stately.explore.ExploreActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-19.
 * A dialog showing a list of names a nation has.
 */
public class NameListDialog extends RecyclerDialogFragment {
    public static final String DIALOG_TAG = "fragment_endorsement_dialog";
    public static final String TITLE_KEY = "title";
    public static final String NAMES_KEY = "names";
    public static final String DELEGATE_VOTES_KEY = "delegateVotes";
    public static final String TARGET_KEY = "target";

    private ArrayList<String> names;
    private ArrayList<DelegateVote> delegateVotes;
    private String title;
    private int target;

    public void setTitle(String s) {
        title = s;
    }

    public void setNames(ArrayList<String> ends) {
        names = ends;
    }

    public void setTarget(int i) {
        target = i;
    }

    public void setDelegateVotes(List<DelegateVote> dvs) {
        delegateVotes = new ArrayList<DelegateVote>(dvs);
        target = ExploreActivity.EXPLORE_NATION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore saved state
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(TITLE_KEY);
            names = savedInstanceState.getStringArrayList(NAMES_KEY);
            delegateVotes = savedInstanceState.getParcelableArrayList(DELEGATE_VOTES_KEY);
            target = savedInstanceState.getInt(TARGET_KEY);
        }
    }

    @Override
    protected void initRecycler(View view) {
        super.initRecycler(view);
        setDialogTitle(title);
        if (delegateVotes != null) {
            Collections.sort(delegateVotes);
            mRecyclerAdapter = new NameListRecyclerAdapter(getContext(), this, delegateVotes);
        } else {
            Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
            mRecyclerAdapter = new NameListRecyclerAdapter(getContext(), this, names, target);
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putString(TITLE_KEY, title);
        outState.putStringArrayList(NAMES_KEY, names);
        outState.putParcelableArrayList(DELEGATE_VOTES_KEY, delegateVotes);
        outState.putInt(TARGET_KEY, target);
    }
}
