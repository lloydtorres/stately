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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment of the Nation fragment showing data on people.
 * Takes in a Nation object.
 */
public class PeopleSubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";

    private static final HashMap<String, Integer> waCategoryDescriptors;

    private Nation mNation;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private List<Object> cards;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    static {
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
        waCategoryDescriptors.put("mother_knows_best_state", R.string.wa_father_knows_best_state);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        if (mNation != null)
        {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            cards = new ArrayList<Object>();

            NationGenericCardData ngcSummary = new NationGenericCardData();
            ngcSummary.title = getString(R.string.card_main_title_summary);
            StringBuilder summaryContent = new StringBuilder(String.format(getString(R.string.card_people_summarydesc_flavour),
                    mNation.prename,
                    mNation.name,
                    mNation.notable,
                    mNation.sensible,
                    SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase),
                    mNation.demPlural));

            String waCategory = mNation.govType.toLowerCase(Locale.US).replace(" ", "_").replace("-", "_");
            if (waCategoryDescriptors.containsKey(waCategory))
            {
                summaryContent.append("<br /><br />").append(String.format(getString(waCategoryDescriptors.get(waCategory)), mNation.demPlural));
            }
            summaryContent.append("<br /><br />").append(mNation.crime);
            ngcSummary.mainContent = summaryContent.toString();
            ngcSummary.nationCensusTarget = mNation.name;
            ngcSummary.idCensusTarget = TrendsActivity.CENSUS_CRIME;
            cards.add(ngcSummary);

            NationChartCardData nccMortality = new NationChartCardData();
            nccMortality.mode = NationChartCardData.MODE_PEOPLE;
            nccMortality.mortalityList = mNation.causes;
            nccMortality.animal = mNation.animal;
            cards.add(nccMortality);

            mRecyclerAdapter = new NationCardsRecyclerAdapter(getContext(), cards, getFragmentManager());
            mRecyclerView.setAdapter(mRecyclerAdapter);
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
}
