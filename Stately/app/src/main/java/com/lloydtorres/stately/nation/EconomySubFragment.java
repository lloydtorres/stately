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

package com.lloydtorres.stately.nation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.census.TrendsOnClickListener;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Sectors;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment within the Nation fragment showing economic data.
 * Takes in nation object.
 */
public class EconomySubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";

    private Nation mNation;

    private TextView econDesc;
    private LinearLayout censusAvgIncome;
    private TextView gdpTotal;
    private TextView gdpPerCapitaAvg;
    private TextView gdpPerCapitaPoor;
    private TextView gdpPerCapitaRich;
    private PieChart sectorChart;

    // Labels used for the pie chart
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

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
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
            outState.putParcelable(NATION_DATA_KEY, mNation);
        }
    }

    /**
     * Set up first card with economic description from NationStates.
     * @param view
     */
    private void initEconDesc(View view)
    {
        econDesc = (TextView) view.findViewById(R.id.nation_industrydesc);

        String descContent = mNation.industryDesc;
        descContent = descContent.replace(". ", ".<br /><br />");

        econDesc.setText(SparkleHelper.getHtmlFormatting(descContent));

        censusAvgIncome = (LinearLayout) view.findViewById(R.id.card_economy_avg_income_census);
        censusAvgIncome.setOnClickListener(new TrendsOnClickListener(getContext(), SparkleHelper.getIdFromName(mNation.name), TrendsActivity.CENSUS_AVERAGE_INCOME));
    }

    /**
     * Initialize the card text content on GDP data from NationStates
     * @param view
     */
    private void initGDP(View view)
    {
        gdpTotal = (TextView) view.findViewById(R.id.nation_gdp_total);
        gdpTotal.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency));

        gdpPerCapitaAvg = (TextView) view.findViewById(R.id.nation_gdp_per_capita_avg);
        gdpPerCapitaAvg.setText(String.format(getString(R.string.avg_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency)));

        gdpPerCapitaPoor = (TextView) view.findViewById(R.id.nation_gdp_per_capita_poor);
        gdpPerCapitaPoor.setText(String.format(getString(R.string.poor_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.poorest, mNation.currency)));

        gdpPerCapitaRich = (TextView) view.findViewById(R.id.nation_gdp_per_capita_rich);
        gdpPerCapitaRich.setText(String.format(getString(R.string.rich_val_currency), SparkleHelper.getMoneyFormatted(getContext(), mNation.richest, mNation.currency)));
    }

    /**
     * Initialize the pie chart showing the economic sector breakdown
     * @param view
     */
    private void initSectorChart(View view)
    {
        sectorChart = (PieChart) view.findViewById(R.id.nation_sectors);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        Sectors sectors = mNation.sectors;

        List<Integer> sectorColours = new ArrayList<Integer>();
        Context context = getContext();

        int i = 0;
        if (sectors.government > 0f)
        {
            chartLabels.add(getString(R.string.government));
            chartEntries.add(new Entry(sectors.government, i++));
            sectorColours.add(ContextCompat.getColor(context, R.color.colorSector0));
        }
        if (sectors.stateOwned > 0f)
        {
            chartLabels.add(getString(R.string.state_owned));
            chartEntries.add(new Entry(sectors.stateOwned, i++));
            sectorColours.add(ContextCompat.getColor(context, R.color.colorSector1));
        }
        if (sectors.privateSector > 0f)
        {
            chartLabels.add(getString(R.string.private_sector));
            chartEntries.add(new Entry(sectors.privateSector, i++));
            sectorColours.add(ContextCompat.getColor(context, R.color.colorSector2));
        }
        if (sectors.blackMarket > 0f)
        {
            chartLabels.add(getString(R.string.black_market));
            chartEntries.add(new Entry(sectors.blackMarket, i++));
            sectorColours.add(ContextCompat.getColor(context, R.color.colorSector3));
        }

        // Disable data labels, set colours and data
        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(sectorColours);
        PieData dataFull = new PieData(chartLabels, dataSet);

        sectorChart = SparkleHelper.getFormattedPieChart(context, sectorChart, chartLabels);
        sectorChart.setData(dataFull);
        sectorChart.invalidate();
    }

    @Override
    public void onPause()
    {
        if (sectorChart != null)
        {
            sectorChart = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        if (sectorChart != null)
        {
            sectorChart = null;
        }
        super.onDestroy();
    }
}
