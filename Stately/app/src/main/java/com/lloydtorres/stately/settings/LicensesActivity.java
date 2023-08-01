/**
 * Copyright 2017 Lloyd Torres
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

package com.lloydtorres.stately.settings;

import android.os.Bundle;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.DataPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lloyd on 2017-03-16.
 * Activity that shows licenses for Stately and its open source dependencies.
 */
public class LicensesActivity extends RefreshviewActivity {
    private final List<DataPair> licenses = new ArrayList<DataPair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSwipeRefreshLayout.setEnabled(false);
        removePaddingTop();

        String[] licenseTitles = getResources().getStringArray(R.array.libraries);
        String[] licenseContents = getResources().getStringArray(R.array.libraries_licenses);

        DataPair header = new DataPair(getString(R.string.licenses_desc_title),
                getString(R.string.licenses_desc));
        licenses.add(header);
        for (int i = 0; i < licenseTitles.length; i++) {
            DataPair licenseData = new DataPair(licenseTitles[i], licenseContents[i]);
            licenses.add(licenseData);
        }

        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new LicensesAdapter(licenses);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((LicensesAdapter) mRecyclerAdapter).setContent(licenses);
        }
    }
}
