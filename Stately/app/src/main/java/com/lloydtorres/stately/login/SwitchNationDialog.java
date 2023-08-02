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

package com.lloydtorres.stately.login;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-02-03.
 * This dialog is shown for switching active nations.
 */
public class SwitchNationDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_switch_dialog";
    public static final String LOGINS_KEY = "logins";

    // RecyclerView variables
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<UserLogin> logins;

    public void setLogins(ArrayList<UserLogin> l) {
        logins = l;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, RaraHelper.getThemeLollipopDialog(getContext()));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_recycler, null);

        // Restore saved state
        if (savedInstanceState != null) {
            logins = savedInstanceState.getParcelableArrayList(LOGINS_KEY);
        }

        initRecycler(view);

        // Build actual dialog
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SparkleHelper.startAddNation(getContext());
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),
                RaraHelper.getThemeMaterialDialog(getContext()));
        dialogBuilder.setTitle(R.string.menu_switch)
                .setView(view)
                .setPositiveButton(R.string.add_nation, dialogListener);

        return dialogBuilder.create();
    }

    private void initRecycler(View view) {
        // Base recycler stuff
        mRecyclerView = view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(logins);
        mRecyclerAdapter = new SwitchNationRecyclerAdapter(getContext(), this, logins);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        // Add swipe to delete
        ItemTouchHelper.SimpleCallback deleteCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                // not needed
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // remove on swipe
                int pos = viewHolder.getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION) {
                    logins.get(pos).delete();
                    logins.remove(pos);
                    mRecyclerAdapter.notifyItemRemoved(pos);
                    mRecyclerAdapter.notifyItemRangeChanged(pos, logins.size());
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deleteCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // If the recyclerview is larger than 75% of the screen height, resize
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (getActivity() != null && isAdded()) {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int screenHeight = displaymetrics.heightPixels;
            int recyclerHeight = mRecyclerView.getLayoutParams().height;
            if (recyclerHeight > screenHeight * 0.75) {
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                (int) (screenHeight * 0.5));
                mRecyclerView.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LOGINS_KEY, logins);
    }
}
