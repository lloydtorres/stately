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

package com.lloydtorres.stately.wa;

import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 * This is a listener that shows historical data on the two TextViews when clicking
 * on the vote history chart in ResolutionActivity. A separate class is needed since
 * there's two charts in the activity.
 */
public class VotingHistoryChartListener implements OnChartValueSelectedListener {
    private TextView labelFor;
    private TextView labelAgainst;
    private List<Integer> votesFor;
    private List<Integer> votesAgainst;

    public VotingHistoryChartListener(TextView tF, TextView tA, List<Integer> f, List<Integer> a)
    {
        labelFor = tF;
        labelAgainst = tA;
        votesFor = f;
        votesAgainst = a;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        // Set the for and against TextViews to their historical state.
        labelFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get(e.getXIndex())));
        labelAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get(e.getXIndex())));
    }

    @Override
    public void onNothingSelected() {
        // Set them back to their current state.
        labelFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get(votesFor.size()-1)));
        labelAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get(votesAgainst.size()-1)));
    }
}
