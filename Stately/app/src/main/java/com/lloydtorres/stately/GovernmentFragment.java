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
public class GovernmentFragment extends Fragment implements OnChartValueSelectedListener {
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

    private TextView govDesc;
    private TextView budgetTotal;
    private PieChart budgetChart;

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
        View view = inflater.inflate(R.layout.fragment_government, container, false);

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

    private void initGovDesc(View view)
    {
        govDesc = (TextView) view.findViewById(R.id.nation_govtdesc);

        String descContent = mNation.govtDesc;
        descContent = descContent.replace(". ", ".<br /><br />");

        govDesc.setText(Html.fromHtml(descContent).toString());
    }

    private void initBudgetTotal(View view)
    {
        budgetTotal = (TextView) view.findViewById(R.id.nation_expenditures_total);

        String suffix = getString(R.string.thousand);
        long budgetHolder = (long) (mNation.gdp * (mNation.sectors.government/100d));
        if (budgetHolder >= 1000000L && budgetHolder < 1000000000L)
        {
            suffix = getString(R.string.million);
            budgetHolder /= 1000000L;
        }
        else if (budgetHolder >= 1000000000L && budgetHolder < 1000000000000L)
        {
            suffix = getString(R.string.billion);
            budgetHolder /= 1000000000L;
        }
        else if (budgetHolder >= 1000000000000L)
        {
            suffix = getString(R.string.trillion);
            budgetHolder /= 1000000000000L;
        }

        budgetTotal.setText(String.format(getString(R.string.card_government_expenditures_budget_flavour), NumberFormat.getInstance(Locale.US).format(budgetHolder).toString(), suffix, English.plural(mNation.currency), mNation.sectors.government));
    }

    private void initBudgetChart(View view)
    {
        budgetChart = (PieChart) view.findViewById(R.id.nation_govspending);

        // setup data
        List<String> chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        GovBudget budget = mNation.govBudget;

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

        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(chartColours, getActivity());
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
        budgetChart.setCenterTextSize(30);

        budgetChart.setOnChartValueSelectedListener(this);
        budgetChart.setData(dataFull);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (budgetChart != null)
        {
            budgetChart.setCenterText(String.format(getString(R.string.percent), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (budgetChart != null)
        {
            budgetChart.setCenterText("");
        }
    }
}
