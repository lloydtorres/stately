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

package com.lloydtorres.stately.census;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Dataset;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-04-10.
 * A recycler adapter for the dataset dialog.
 */
public class DatasetRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private FragmentActivity activity;
    private DatasetDialog selfDialog;
    private ArrayList<Dataset> datasets;

    public DatasetRecyclerAdapter(FragmentActivity a, DatasetDialog d, ArrayList<Dataset> ds)
    {
        activity = a;
        selfDialog = d;
        datasets = ds;
        Collections.sort(datasets);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new DatasetEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DatasetEntry datasetEntry = (DatasetEntry) holder;
        Dataset d = datasets.get(position);
        datasetEntry.init(d);
    }

    @Override
    public int getItemCount() {
        return datasets.size();
    }

    public class DatasetEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView datasetName;
        private Dataset dataset;

        public DatasetEntry(View v)
        {
            super(v);
            datasetName = (TextView) v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(Dataset d)
        {
            dataset = d;
            datasetName.setText(dataset.name);
        }

        @Override
        public void onClick(View v) {
            if (activity instanceof TrendsActivity)
            {
                ((TrendsActivity) activity).queryNewDataset(dataset.id);
            }
            selfDialog.dismiss();
        }

        public void select()
        {
            datasetName.setTypeface(datasetName.getTypeface(), Typeface.BOLD);
        }

        public void unselect()
        {
            datasetName.setTypeface(datasetName.getTypeface(), Typeface.NORMAL);
        }
    }
}
