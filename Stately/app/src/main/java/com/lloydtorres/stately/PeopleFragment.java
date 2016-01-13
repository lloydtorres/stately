package com.lloydtorres.stately;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 */
public class PeopleFragment extends Fragment implements OnChartValueSelectedListener {
    private final int[] chartColours = {    R.color.colorChart0,
                                            R.color.colorChart1,
                                            R.color.colorChart2,
                                            R.color.colorChart3,
                                            R.color.colorChart4,
                                            R.color.colorChart5,
                                            R.color.colorChart6,
                                            R.color.colorChart7,
                                            R.color.colorChart8,
                                            R.color.colorChart9,
                                            R.color.colorChart10,
                                            R.color.colorChart11,
                                            R.color.colorChart12
                                        };

    private Nation mNation;

    private TextView summaryDesc;
    private PieChart mortalityChart;

    private List<String> chartLabels;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNation");
        }

        if (mNation != null)
        {
            initSummaryDesc(view);
            initMortalityChart(view);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable("mNation", mNation);
        }
    }

    private void initSummaryDesc(View view)
    {
        summaryDesc = (TextView) view.findViewById(R.id.nation_summarydesc);

        String suffix = getString(R.string.million);
        double popHolder = mNation.popBase;
        if (mNation.popBase >= 1000D && mNation.popBase < 10000D)
        {
            suffix = getString(R.string.billion);
            popHolder /= 1000D;
        }
        else if (mNation.popBase >= 10000D)
        {
            suffix = getString(R.string.trillion);
            popHolder /= 1000000D;
        }

        String summaryContent = String.format(getString(R.string.card_people_summarydesc_flavour),
                mNation.prename,
                mNation.name,
                mNation.notable,
                mNation.sensible,
                String.format(getString(R.string.val_currency), NumberFormat.getInstance(Locale.US).format(popHolder).toString(), suffix),
                mNation.demPlural);

        summaryContent += "<br /><br />" + mNation.crime;

        summaryDesc.setText(Html.fromHtml(summaryContent).toString());
    }

    private void initMortalityChart(View view)
    {
        mortalityChart = (PieChart) view.findViewById(R.id.nation_mortality_chart);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        List<MortalityCause> causes = mNation.mortalityRoot.causes;

        for (int i=0; i < causes.size(); i++)
        {
            chartLabels.add(causes.get(i).type);
            Entry n = new Entry((float) causes.get(i).value, i);
            chartEntries.add(n);
        }

        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(chartColours, getActivity());
        PieData dataFull = new PieData(chartLabels, dataSet);

        // formatting
        Legend cLegend = mortalityChart.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        mortalityChart.setDrawSliceText(false);
        mortalityChart.setDescription("");
        mortalityChart.setHoleColorTransparent(true);
        mortalityChart.setHoleRadius(60f);
        mortalityChart.setTransparentCircleRadius(65f);
        mortalityChart.setCenterTextSize(20);
        mortalityChart.setRotationEnabled(false);

        mortalityChart.setOnChartValueSelectedListener(this);
        mortalityChart.setData(dataFull);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (mortalityChart != null)
        {
            mortalityChart.setCenterText(String.format(getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (mortalityChart != null)
        {
            mortalityChart.setCenterText("");
        }
    }
}
