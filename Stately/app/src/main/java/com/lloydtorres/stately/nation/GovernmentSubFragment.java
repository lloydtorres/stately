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
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.GovBudget;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment within the Nation fragment that displays government data.
 * Takes in a Nation object.
 */
public class GovernmentSubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";

    private Nation mNation;

    private TextView govDesc;
    private TextView budgetTotal;
    private PieChart budgetChart;

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
        View view = inflater.inflate(R.layout.fragment_sub_government, container, false);

        // Restore save state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        if (mNation != null)
        {
            initGovDesc(view);
            initBudgetTotal(view);
            initBudgetChart(view);
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
     * Initializes the first card containing government description from NationStates.
     * @param view
     */
    private void initGovDesc(View view)
    {
        govDesc = (TextView) view.findViewById(R.id.nation_govtdesc);

        String descContent = mNation.govtDesc;
        descContent = descContent.replace(". ", ".<br /><br />");

        govDesc.setText(SparkleHelper.getHtmlFormatting(descContent));
    }

    /**
     * Initializes the text content for the budget card.
     * @param view
     */
    private void initBudgetTotal(View view)
    {
        budgetTotal = (TextView) view.findViewById(R.id.nation_expenditures_total);

        long budgetHolder = (long) (mNation.gdp * (mNation.sectors.government/100d));
        budgetTotal.setText(String.format(getString(R.string.card_government_expenditures_budget_flavour), SparkleHelper.getMoneyFormatted(getContext(), budgetHolder, mNation.currency), mNation.sectors.government));
    }

    /**
     * Initializes the pie chart used to display government budget breakdown.
     * @param view
     */
    private void initBudgetChart(View view)
    {
        budgetChart = (PieChart) view.findViewById(R.id.nation_govspending);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        GovBudget budget = mNation.govBudget;

        List<Integer> budgetColours = new ArrayList<Integer>();
        Context context = getContext();

        // Have to add it one by one, how horrifying
        int i = 0;
        if (budget.admin > 0D)
        {
            chartLabels.add(getString(R.string.administration));
            chartEntries.add(new Entry((float) budget.admin, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart0));
        }
        if (budget.defense > 0D)
        {
            chartLabels.add(getString(R.string.defense));
            chartEntries.add(new Entry((float) budget.defense, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart1));
        }
        if (budget.education > 0D)
        {
            chartLabels.add(getString(R.string.education));
            chartEntries.add(new Entry((float) budget.education, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart2));
        }
        if (budget.environment > 0D)
        {
            chartLabels.add(getString(R.string.environment));
            chartEntries.add(new Entry((float) budget.environment, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart3));
        }
        if (budget.healthcare > 0D)
        {
            chartLabels.add(getString(R.string.healthcare));
            chartEntries.add(new Entry((float) budget.healthcare, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart4));
        }
        if (budget.industry > 0D)
        {
            chartLabels.add(getString(R.string.industry));
            chartEntries.add(new Entry((float) budget.industry, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart5));
        }
        if (budget.internationalAid > 0D)
        {
            chartLabels.add(getString(R.string.international_aid));
            chartEntries.add(new Entry((float) budget.internationalAid, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart6));
        }
        if (budget.lawAndOrder > 0D)
        {
            chartLabels.add(getString(R.string.law_and_order));
            chartEntries.add(new Entry((float) budget.lawAndOrder, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart7));
        }
        if (budget.publicTransport > 0D)
        {
            chartLabels.add(getString(R.string.public_transport));
            chartEntries.add(new Entry((float) budget.publicTransport, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart8));
        }
        if (budget.socialPolicy > 0D)
        {
            chartLabels.add(getString(R.string.social_policy));
            chartEntries.add(new Entry((float) budget.socialPolicy, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart9));
        }
        if (budget.spirituality > 0D)
        {
            chartLabels.add(getString(R.string.spirituality));
            chartEntries.add(new Entry((float) budget.spirituality, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart10));
        }
        if (budget.welfare > 0D)
        {
            chartLabels.add(getString(R.string.welfare));
            chartEntries.add(new Entry((float) budget.welfare, i++));
            budgetColours.add(ContextCompat.getColor(context, R.color.colorChart11));
        }

        // Disable chart labels, set colours, set data
        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(budgetColours);
        PieData dataFull = new PieData(chartLabels, dataSet);

        budgetChart = SparkleHelper.getFormattedPieChart(context, budgetChart, chartLabels);
        budgetChart.setData(dataFull);
        budgetChart.invalidate();
    }

    @Override
    public void onPause()
    {
        if (budgetChart != null)
        {
            budgetChart = null;
        }
        super.onPause();
    }
}
