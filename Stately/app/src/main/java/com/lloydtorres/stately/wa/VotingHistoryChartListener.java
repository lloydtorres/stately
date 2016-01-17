package com.lloydtorres.stately.wa;

import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
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
        labelFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get(e.getXIndex())));
        labelAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get(e.getXIndex())));
    }

    @Override
    public void onNothingSelected() {
        labelFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get(votesFor.size()-1)));
        labelAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get(votesAgainst.size()-1)));
    }
}
