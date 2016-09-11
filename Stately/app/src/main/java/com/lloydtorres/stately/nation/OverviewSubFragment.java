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
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.NationFreedomCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.dto.NationOverviewCardData;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-10.
 * A sub-fragment of the Nation fragment showing overview stats about a nation.
 * Takes in a Nation object.
 */
public class OverviewSubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";
    private String[] WORLD_CENSUS_ITEMS;

    private Nation mNation;

    private final HashMap<String, String> waCategoryConservative = new HashMap<String, String>();
    private final HashMap<String, String> waCategoryLiberal = new HashMap<String, String>();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private List<Object> cards;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WORLD_CENSUS_ITEMS = getResources().getStringArray(R.array.census);

        String[] govTypes = getResources().getStringArray(R.array.gov_types);
        String[] govConservative = getResources().getStringArray(R.array.gov_conservative);
        String[] govLiberal = getResources().getStringArray(R.array.gov_liberal);

        for (int i = 0; i < govTypes.length; i++) {
            waCategoryConservative.put(govTypes[i], govConservative[i]);
            waCategoryLiberal.put(govTypes[i], govLiberal[i]);
        }
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

            NationOverviewCardData nocData = new NationOverviewCardData();

            // Set up custom government category depending on user preferences
            String waCategory = mNation.govType.toLowerCase(Locale.US).replace(" ", "_").replace("-", "_");
            String customCategory = null;
            if (SettingsActivity.getGovernmentSetting(getContext()) == SettingsActivity.GOV_CONSERVATIVE
                    && waCategoryConservative.containsKey(waCategory)) {
                customCategory = waCategoryConservative.get(waCategory);

            }
            else if (SettingsActivity.getGovernmentSetting(getContext()) == SettingsActivity.GOV_LIBERAL
                    && waCategoryLiberal.containsKey(waCategory)) {
                customCategory = waCategoryLiberal.get(waCategory);
            }

            if (customCategory != null) {
                nocData.category = String.format(Locale.US, getString(R.string.nation_government_custom),
                        mNation.govType, customCategory);
                SparkleHelper.logError(nocData.category);
            }
            else {
                nocData.category = mNation.govType;
            }

            nocData.region = mNation.region;
            nocData.inflDesc = mNation.influence;
            nocData.inflScore = mNation.census.get(TrendsActivity.CENSUS_INFLUENCE).score;
            nocData.population = mNation.popBase;
            nocData.motto = mNation.motto;
            nocData.established = mNation.foundedAgo;
            nocData.lastSeen = mNation.lastActivityAgo;
            nocData.waState = mNation.waState;
            nocData.endorsements = mNation.endorsements;
            nocData.gaVote = mNation.gaVote;
            nocData.scVote = mNation.scVote;
            cards.add(nocData);

            NationFreedomCardData nfcData = new NationFreedomCardData();
            nfcData.nationTarget = mNation.name;
            nfcData.civDesc = mNation.freedomDesc.civilRightsDesc;
            nfcData.civScore = (int) mNation.census.get(TrendsActivity.CENSUS_CIVIL_RIGHTS).score;
            nfcData.econDesc = mNation.freedomDesc.economyDesc;
            nfcData.econScore = (int) mNation.census.get(TrendsActivity.CENSUS_ECONOMY).score;
            nfcData.poliDesc = mNation.freedomDesc.politicalDesc;
            nfcData.poliScore = (int) mNation.census.get(TrendsActivity.CENSUS_POLITICAL_FREEDOM).score;
            cards.add(nfcData);

            NationGenericCardData ngcGov = new NationGenericCardData();
            ngcGov.title = getString(R.string.card_overview_gov_title);
            ngcGov.items = new LinkedHashMap<String, String>();
            if (mNation.leader != null) {
                ngcGov.items.put(getString(R.string.card_overview_gov_leader), mNation.leader);
            }
            if (mNation.capital != null)
            {
                ngcGov.items.put(getString(R.string.card_overview_gov_capital), mNation.capital);
            }
            if (mNation.govtPriority != null)
            {
                ngcGov.items.put(getString(R.string.card_overview_gov_priority), mNation.govtPriority);
            }
            ngcGov.items.put(getString(R.string.card_overview_gov_tax), String.format(Locale.US, getString(R.string.percent), mNation.tax));
            ngcGov.nationCensusTarget = mNation.name;
            ngcGov.idCensusTarget = TrendsActivity.CENSUS_TAXATION;
            cards.add(ngcGov);

            NationGenericCardData ngcEconomy = new NationGenericCardData();
            ngcEconomy.title = getString(R.string.card_overview_econ_title);
            ngcEconomy.items = new LinkedHashMap<String, String>();
            ngcEconomy.items.put(getString(R.string.card_overview_econ_currency), mNation.currency);
            ngcEconomy.items.put(getString(R.string.card_overview_econ_gdp), SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency));
            ngcEconomy.items.put(getString(R.string.card_overview_econ_income), SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency));
            ngcEconomy.items.put(getString(R.string.card_overview_econ_industry), mNation.industry);
            ngcEconomy.nationCensusTarget = mNation.name;
            ngcEconomy.idCensusTarget = TrendsActivity.CENSUS_ECONOMIC_OUTPUT;
            cards.add(ngcEconomy);

            NationGenericCardData ngcOther = new NationGenericCardData();
            ngcOther.title = getString(R.string.card_overview_other_title);
            ngcOther.items = new LinkedHashMap<String, String>();
            // Determine if the adjective is different from the noun (different flavour text)
            String demonymText;
            if (mNation.demAdjective.equals(mNation.demNoun))
            {
                demonymText = String.format(getString(R.string.card_overview_other_demonym_txt2), mNation.demNoun, mNation.demPlural);
            }
            else
            {
                demonymText = String.format(getString(R.string.card_overview_other_demonym_txt1), mNation.demNoun, mNation.demPlural, mNation.demAdjective);
            }
            ngcOther.items.put(getString(R.string.card_overview_other_demonym), demonymText);
            if (mNation.religion != null)
            {
                ngcOther.items.put(getString(R.string.card_overview_other_religion), mNation.religion);
            }
            String animalText;
            if (mNation.animalTrait != null)
            {
                animalText = String.format(getString(R.string.card_overview_other_animal_template), mNation.animal, mNation.animalTrait);
            }
            else
            {
                animalText = mNation.animal;
            }
            ngcOther.items.put(getString(R.string.card_overview_other_animal), animalText);
            int censusId = mNation.wCensus.id;
            int censusRawId = mNation.wCensus.id;
            // if census ID is out of bounds, set it as unknown
            if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
            {
                censusId = WORLD_CENSUS_ITEMS.length - 1;
            }
            String[] worldCensusItem = WORLD_CENSUS_ITEMS[censusId].split("##");
            String todayCensusTitle = String.format(getString(R.string.card_overview_other_census_title), worldCensusItem[0]);
            CensusDetailedRank detailedRank = mNation.census.get(censusRawId);
            StringBuilder todayCensusContent = new StringBuilder(String.format(getString(R.string.card_overview_other_census_content), SparkleHelper.getPrettifiedSuffixedNumber(getContext(), detailedRank.score), worldCensusItem[1]));
            if (detailedRank.regionRank > 0)
            {
                todayCensusContent.append("<br>").append(String.format(Locale.US, getString(R.string.card_overview_other_census_region), SparkleHelper.getPrettifiedNumber(detailedRank.regionRank), mNation.region, SparkleHelper.getPrettifiedNumber(detailedRank.regionRankPercent)));
            }
            if (detailedRank.worldRank > 0)
            {
                todayCensusContent.append("<br>").append(String.format(Locale.US, getString(R.string.card_overview_other_census_world), SparkleHelper.getPrettifiedNumber(detailedRank.worldRank), SparkleHelper.getPrettifiedNumber(detailedRank.worldRankPercent)));
            }
            ngcOther.items.put(todayCensusTitle, todayCensusContent.toString());
            ngcOther.nationCensusTarget = mNation.name;
            ngcOther.idCensusTarget = censusId;
            cards.add(ngcOther);

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
