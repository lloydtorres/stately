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

package com.lloydtorres.stately.core;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-09-16.
 * Skeleton for dialogs using the refreshview layout.
 */
public abstract class RecyclerDialogFragment extends DialogFragment {
    // RecyclerView variables
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView.Adapter mRecyclerAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AppCompatDialog(getActivity(), SparkleHelper.getThemeLollipopDialog(getContext()));
        }
        else {
            dialog = new AppCompatDialog(getActivity(), SparkleHelper.getThemeMaterialDialog(getContext()));
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_dialog_recycler, container, false);

        initRecycler(mView);

        return mView;
    }

    protected abstract void initRecycler(View view);

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
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (screenHeight * 0.5));
                mRecyclerView.setLayoutParams(lp);
            }
        }
    }
}
