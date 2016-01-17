package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Sectors;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.atteo.evo.inflector.English;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 */
public class EconomySubFragment extends Fragment implements OnChartValueSelectedListener {
    private Nation mNation;

    private TextView econDesc;
    private TextView gdpTotal;
    private TextView gdpPerCapitaAvg;
    private TextView gdpPerCapitaPoor;
    private TextView gdpPerCapitaRich;
    private PieChart sectorChart;

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
        View view = inflater.inflate(R.layout.fragment_sub_economy, container, false);

        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNation");
        }

        if (mNation != null)
        {
            initEconDesc(view);
            initGDP(view);
            initSectorChart(view);
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

    private void initEconDesc(View view)
    {
        econDesc = (TextView) view.findViewById(R.id.nation_industrydesc);

        String descContent = mNation.industryDesc;
        descContent = descContent.replace(". ", ".<br /><br />");

        econDesc.setText(SparkleHelper.getHtmlFormatting(descContent));
    }

    private void initGDP(View view)
    {
        gdpTotal = (TextView) view.findViewById(R.id.nation_gdp_total);
        gdpTotal.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency));

        gdpPerCapitaAvg = (TextView) view.findViewById(R.id.nation_gdp_per_capita_avg);
        gdpPerCapitaAvg.setText(String.format(getString(R.string.avg_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.income, English.plural(mNation.currency))));

        gdpPerCapitaPoor = (TextView) view.findViewById(R.id.nation_gdp_per_capita_poor);
        gdpPerCapitaPoor.setText(String.format(getString(R.string.poor_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.poorest, mNation.currency)));

        gdpPerCapitaRich = (TextView) view.findViewById(R.id.nation_gdp_per_capita_rich);
        gdpPerCapitaRich.setText(String.format(getString(R.string.rich_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.richest, mNation.currency)));
    }

    private void initSectorChart(View view)
    {
        sectorChart = (PieChart) view.findViewById(R.id.nation_sectors);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        Sectors sectors = mNation.sectors;

        int i = 0;
        chartLabels.add(getString(R.string.government));
        chartEntries.add(new Entry((float) sectors.government, i++));
        chartLabels.add(getString(R.string.state_owned));
        chartEntries.add(new Entry((float) sectors.stateOwned, i++));
        chartLabels.add(getString(R.string.private_sector));
        chartEntries.add(new Entry((float) sectors.privateSector, i++));
        chartLabels.add(getString(R.string.black_market));
        chartEntries.add(new Entry((float) sectors.blackMarket, i++));

        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.sectorColours, getActivity());
        PieData dataFull = new PieData(chartLabels, dataSet);

        // formatting
        Legend cLegend = sectorChart.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        sectorChart.setDrawSliceText(false);
        sectorChart.setDescription("");
        sectorChart.setHoleColorTransparent(true);
        sectorChart.setHoleRadius(60f);
        sectorChart.setTransparentCircleRadius(65f);
        sectorChart.setCenterTextSize(20);
        sectorChart.setRotationEnabled(false);

        sectorChart.setOnChartValueSelectedListener(this);
        sectorChart.setData(dataFull);
        sectorChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (sectorChart != null)
        {
            sectorChart.setCenterText(String.format(getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (sectorChart != null)
        {
            sectorChart.setCenterText("");
        }
    }
}
