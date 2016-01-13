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

import org.atteo.evo.inflector.English;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 */
public class EconomyFragment extends Fragment implements OnChartValueSelectedListener {
    private final int[] chartColours = {    R.color.colorSector0,
                                            R.color.colorSector1,
                                            R.color.colorSector2,
                                            R.color.colorSector3,
                                        };

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
        View view = inflater.inflate(R.layout.fragment_economy, container, false);

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

        econDesc.setText(Html.fromHtml(descContent).toString());
    }

    private void initGDP(View view)
    {
        gdpTotal = (TextView) view.findViewById(R.id.nation_gdp_total);
        gdpPerCapitaAvg = (TextView) view.findViewById(R.id.nation_gdp_per_capita_avg);
        gdpPerCapitaPoor = (TextView) view.findViewById(R.id.nation_gdp_per_capita_poor);
        gdpPerCapitaRich = (TextView) view.findViewById(R.id.nation_gdp_per_capita_rich);

        String suffix = getString(R.string.thousand);
        long gdpHolder = mNation.gdp;
        if (gdpHolder >= 1000000L && gdpHolder < 1000000000L)
        {
            suffix = getString(R.string.million);
            gdpHolder /= 1000000L;
        }
        else if (gdpHolder >= 1000000000L && gdpHolder < 1000000000000L)
        {
            suffix = getString(R.string.billion);
            gdpHolder /= 1000000000L;
        }
        else if (gdpHolder >= 1000000000000L)
        {
            suffix = getString(R.string.trillion);
            gdpHolder /= 1000000000000L;
        }

        gdpTotal.setText(String.format(getString(R.string.val_suffix_currency), NumberFormat.getInstance(Locale.US).format(gdpHolder).toString(), suffix, English.plural(mNation.currency)));

        gdpPerCapitaAvg.setText(String.format(getString(R.string.avg_val_currency), NumberFormat.getInstance(Locale.US).format(mNation.income), English.plural(mNation.currency)));
        gdpPerCapitaPoor.setText(String.format(getString(R.string.poor_val_currency), NumberFormat.getInstance(Locale.US).format(mNation.poorest), English.plural(mNation.currency)));
        gdpPerCapitaRich.setText(String.format(getString(R.string.rich_val_currency), NumberFormat.getInstance(Locale.US).format(mNation.richest), English.plural(mNation.currency)));
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
        dataSet.setColors(chartColours, getActivity());
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
