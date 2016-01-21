package com.lloydtorres.stately.wa;

import android.content.Context;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 * A listener for the voting breakdown piechart in ResolutionActivity.
 * A separate class is needed since there's two charts there.
 */
public class VotingBreakdownChartListener implements OnChartValueSelectedListener {
    private Context mContext;
    private PieChart votingBreakdown;
    private List<String> chartLabels;

    public VotingBreakdownChartListener(Context c, PieChart p, List<String> l)
    {
        mContext = c;
        votingBreakdown = p;
        chartLabels = l;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        // Show item label and percentage on click.
        if (votingBreakdown != null)
        {
            votingBreakdown.setCenterText(String.format(mContext.getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (votingBreakdown != null)
        {
            votingBreakdown.setCenterText("");
        }
    }
}
