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

package com.lloydtorres.stately.helpers;

import android.content.Context;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-17.
 * A listener for pie charts being click. Sets the text in the centre to the label
 * and the percentage on click, clears it otherwise. Separate class for dat
 * modularization.
 */
public class PieChartListener implements OnChartValueSelectedListener {
    private Context mContext;
    private PieChart pieChart;
    private List<String> chartLabels;

    public PieChartListener(Context c, PieChart p, List<String> l)
    {
        mContext = c;
        pieChart = p;
        chartLabels = l;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        // Show item label and percentage on click.
        if (pieChart != null)
        {
            pieChart.setCenterText(String.format(Locale.US, mContext.getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (pieChart != null)
        {
            pieChart.setCenterText("");
        }
    }
}
