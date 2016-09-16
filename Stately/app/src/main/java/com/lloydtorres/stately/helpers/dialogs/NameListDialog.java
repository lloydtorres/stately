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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerDialogFragment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-01-19.
 * A dialog showing a list of names a nation has.
 */
public class NameListDialog extends RecyclerDialogFragment {
    public static final String DIALOG_TAG = "fragment_endorsement_dialog";
    public static final String TITLE_KEY = "title";
    public static final String NAMES_KEY = "names";
    public static final String TARGET_KEY = "target";

    private ArrayList<String> names;
    private String title;
    private int target;

    public void setTitle(String s)
    {
        title = s;
    }

    public void setNames(ArrayList<String> ends)
    {
        names = ends;
    }

    public void setTarget(int i)
    {
        target = i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore saved state
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(TITLE_KEY);
            names = savedInstanceState.getStringArrayList(NAMES_KEY);
            target = savedInstanceState.getInt(TARGET_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        getDialog().setTitle(title);

        return view;
    }

    @Override
    protected void initRecycler(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(names);
        mRecyclerAdapter = new NameListRecyclerAdapter(getContext(), this, names, target);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putString(TITLE_KEY, title);
        outState.putStringArrayList(NAMES_KEY, names);
        outState.putInt(TARGET_KEY, target);
    }
}
