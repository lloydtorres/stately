package com.lloydtorres.stately.helpers;

import android.content.Context;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;

import java.util.List;

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
            pieChart.setCenterText(String.format(mContext.getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
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
