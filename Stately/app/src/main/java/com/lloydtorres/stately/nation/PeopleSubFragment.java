package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.MortalityCause;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment of the Nation fragment showing data on people.
 * Takes in a Nation object.
 */
public class PeopleSubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";

    private HashMap<String, Integer> waCategoryDescriptors;

    private Nation mNation;

    private TextView summaryDesc;
    private PieChart mortalityChart;

    // Labels on the mortality chart
    private List<String> chartLabels;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create hash map for WA category descriptors
        waCategoryDescriptors = new HashMap<String, Integer>();
        waCategoryDescriptors.put("anarchy", R.string.wa_anarchy);
        waCategoryDescriptors.put("authoritarian_democracy", R.string.wa_authoritarian_democracy);
        waCategoryDescriptors.put("benevolent_dictatorship", R.string.wa_benevolent_dictatorship);
        waCategoryDescriptors.put("capitalist_paradise", R.string.wa_capitalist_paradise);
        waCategoryDescriptors.put("capitalizt", R.string.wa_capitalizt);
        waCategoryDescriptors.put("civil_rights_lovefest", R.string.wa_civil_rights_lovefest);
        waCategoryDescriptors.put("compulsory_consumerist_state", R.string.wa_compulsory_consumerist_state);
        waCategoryDescriptors.put("conservative_democracy", R.string.wa_conservative_democracy);
        waCategoryDescriptors.put("corporate_bordello", R.string.wa_corporate_bordello);
        waCategoryDescriptors.put("corporate_police_state", R.string.wa_corporate_police_state);
        waCategoryDescriptors.put("corrupt_dictatorship", R.string.wa_corrupt_dictatorship);
        waCategoryDescriptors.put("democratic_socialists", R.string.wa_democratic_socialists);
        waCategoryDescriptors.put("father_knows_best_state", R.string.wa_father_knows_best_state);
        waCategoryDescriptors.put("free_market_paradise", R.string.wa_free_market_paradise);
        waCategoryDescriptors.put("inoffensive_centrist_democracy", R.string.wa_inoffensive_centrist_democracy);
        waCategoryDescriptors.put("iron_fist_consumerists", R.string.wa_iron_fist_consumerists);
        waCategoryDescriptors.put("iron_fist_socialists", R.string.wa_iron_fist_socialists);
        waCategoryDescriptors.put("left_leaning_college_state", R.string.wa_left_leaning_college_state);
        waCategoryDescriptors.put("left_wing_utopia", R.string.wa_left_wing_utopia);
        waCategoryDescriptors.put("liberal_democratic_socialists", R.string.wa_liberal_democratic_socialists);
        waCategoryDescriptors.put("libertarian_police_state", R.string.wa_libertarian_police_state);
        waCategoryDescriptors.put("moralistic_democracy", R.string.wa_moralistic_democracy);
        waCategoryDescriptors.put("new_york_times_democracy", R.string.wa_new_york_times_democracy);
        waCategoryDescriptors.put("psychotic_dictatorship", R.string.wa_psychotic_dictatorship);
        waCategoryDescriptors.put("right_wing_utopia", R.string.wa_right_wing_utopia);
        waCategoryDescriptors.put("scandinavian_liberal_paradise", R.string.wa_scandinavian_liberal_paradise);
        waCategoryDescriptors.put("tyranny_by_majority", R.string.wa_tyranny_by_majority);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_people, container, false);

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
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
        // Save state
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable(NATION_DATA_KEY, mNation);
        }
    }

    /**
     * Initialize the first card showing a mockup of the people descriptors in NationStates.
     * @param view
     */
    private void initSummaryDesc(View view)
    {
        summaryDesc = (TextView) view.findViewById(R.id.nation_summarydesc);

        String summaryContent = String.format(getString(R.string.card_people_summarydesc_flavour),
                mNation.prename,
                mNation.name,
                mNation.notable,
                mNation.sensible,
                SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase),
                mNation.demPlural);

        String waCategory = mNation.govType.toLowerCase().replace(" ", "_").replace("-", "_");
        if (waCategoryDescriptors.containsKey(waCategory))
        {
            summaryContent += "<br /><br />" + getString(waCategoryDescriptors.get(waCategory));
        }

        summaryContent += "<br /><br />" + mNation.crime;

        summaryDesc.setText(SparkleHelper.getHtmlFormatting(summaryContent));
    }

    /**
     * Initialize the mortality pie chart.
     * @param view
     */
    private void initMortalityChart(View view)
    {
        mortalityChart = (PieChart) view.findViewById(R.id.nation_mortality_chart);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        List<MortalityCause> causes = mNation.mortalityRoot.causes;

        for (int i=0; i < causes.size(); i++)
        {
            // NationStates API stores this as Animal Attack instead of
            // using the actual national animal, so replace that
            if (getString(R.string.animal_attack_original).equals(causes.get(i).type))
            {
                chartLabels.add(String.format(getString(R.string.animal_attack_madlibs), mNation.animal));
            }
            else
            {
                chartLabels.add(causes.get(i).type);
            }
            Entry n = new Entry((float) causes.get(i).value, i);
            chartEntries.add(n);
        }

        // Disable labels, set values and colours
        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.chartColours, getActivity());
        PieData dataFull = new PieData(chartLabels, dataSet);

        mortalityChart = SparkleHelper.getFormattedPieChart(getContext(), mortalityChart, chartLabels);
        mortalityChart.setData(dataFull);
        mortalityChart.invalidate();
    }

    @Override
    public void onPause()
    {
        if (mortalityChart != null)
        {
            mortalityChart = null;
        }
        super.onPause();
    }
}
