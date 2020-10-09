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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerDialogFragment;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.Dataset;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Lloyd on 2016-04-10.
 * A dialog showing the available trend datasets.
 */
public class DatasetDialog extends RecyclerDialogFragment {
    public static final String DIALOG_TAG = "fragment_dataset_dialog";
    public static final String DATASETS_KEY = "datasets_key";
    public static final String SELECTED_KEY = "selected_dataset_key";

    private LinkedHashMap<Integer, Dataset> datasets;
    private int selected;

    public void setDatasets(LinkedHashMap<Integer, CensusScale> rawDataset, int selected) {
        this.datasets = new LinkedHashMap<Integer, Dataset> ();
        ArrayList<CensusScale> rawScales = new ArrayList<CensusScale>(rawDataset.values());
        for (int i = 0; i < rawScales.size()-1; i++) {
            CensusScale scale = rawScales.get(i);

            Dataset d = new Dataset();
            d.name = scale.name;
            d.id = scale.id;
            d.selected = false;
            this.datasets.put(d.id, d);
        }

        setSelectedDataset(selected);
    }

    private void setSelectedDataset(final int selected) {
        this.selected = selected;
        if (this.datasets.containsKey(selected)) {
            this.datasets.get(selected).selected = true;
        }
    }

    @Override
    protected void initRecycler(View view) {
        super.initRecycler(view);
        setDialogTitle(getString(R.string.trends_datasets));

        mRecyclerAdapter = new DatasetRecyclerAdapter(getActivity(), this, datasets);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        int position = ((DatasetRecyclerAdapter) mRecyclerAdapter).getSelectedPosition();
        if (position != DatasetRecyclerAdapter.INVALID_POSITION) {
            mLayoutManager.scrollToPosition(position);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ArrayList<Dataset> rawDataset =
                    savedInstanceState.getParcelableArrayList(DATASETS_KEY);
            this.datasets = new LinkedHashMap<Integer, Dataset> ();
            for (Dataset dataset : rawDataset) {
                this.datasets.put(dataset.id, dataset);
            }
            
            setSelectedDataset(savedInstanceState.getInt(SELECTED_KEY));
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        final ArrayList<Dataset> rawDatasets = new ArrayList<>(datasets.values());
        outState.putParcelableArrayList(DATASETS_KEY, rawDatasets);
        outState.putInt(SELECTED_KEY, selected);
    }
}
