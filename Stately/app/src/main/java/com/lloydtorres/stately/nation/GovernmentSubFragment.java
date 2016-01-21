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
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.GovBudget;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.PieChartListener;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment within the Nation fragment that displays government data.
 * Takes in a Nation object.
 */
public class GovernmentSubFragment extends Fragment {
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
            mNation = savedInstanceState.getParcelable("mNation");
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
            outState.putParcelable("mNation", mNation);
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

        // Have to add it one by one, how horrifying
        int i = 0;
        chartLabels.add(getString(R.string.administration));
        chartEntries.add(new Entry((float) budget.admin, i++));
        chartLabels.add(getString(R.string.defense));
        chartEntries.add(new Entry((float) budget.defense, i++));
        chartLabels.add(getString(R.string.education));
        chartEntries.add(new Entry((float) budget.education, i++));
        chartLabels.add(getString(R.string.environment));
        chartEntries.add(new Entry((float) budget.environment, i++));
        chartLabels.add(getString(R.string.healthcare));
        chartEntries.add(new Entry((float) budget.healthcare, i++));
        chartLabels.add(getString(R.string.industry));
        chartEntries.add(new Entry((float) budget.industry, i++));
        chartLabels.add(getString(R.string.international_aid));
        chartEntries.add(new Entry((float) budget.internationalAid, i++));
        chartLabels.add(getString(R.string.law_and_order));
        chartEntries.add(new Entry((float) budget.lawAndOrder, i++));
        chartLabels.add(getString(R.string.public_transport));
        chartEntries.add(new Entry((float) budget.publicTransport, i++));
        chartLabels.add(getString(R.string.social_policy));
        chartEntries.add(new Entry((float) budget.socialPolicy, i++));
        chartLabels.add(getString(R.string.spirituality));
        chartEntries.add(new Entry((float) budget.spirituality, i++));
        chartLabels.add(getString(R.string.welfare));
        chartEntries.add(new Entry((float) budget.welfare, i++));

        // Disable chart labels, set colours, set data
        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.chartColours, getActivity());
        PieData dataFull = new PieData(chartLabels, dataSet);

        // formatting
        Legend cLegend = budgetChart.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        budgetChart.setDrawSliceText(false);
        budgetChart.setDescription("");
        budgetChart.setHoleColorTransparent(true);
        budgetChart.setHoleRadius(60f);
        budgetChart.setTransparentCircleRadius(65f);
        budgetChart.setCenterTextSize(20);
        budgetChart.setRotationEnabled(false);

        budgetChart.setOnChartValueSelectedListener(new PieChartListener(getContext(), budgetChart, chartLabels));
        budgetChart.setData(dataFull);
        budgetChart.invalidate();
    }
}
