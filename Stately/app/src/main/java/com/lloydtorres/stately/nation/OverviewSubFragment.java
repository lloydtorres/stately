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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.core.BroadcastableActivity;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.dto.NationFreedomCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.dto.NationOverviewCardData;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.wa.ResolutionActivity;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-10.
 * A sub-fragment of the Nation fragment showing overview stats about a nation.
 * Takes in a Nation object.
 */
public class OverviewSubFragment extends NationSubFragment {
    public static final String TAX_TEMPLATE = "%s%%";
    private final static String CUSTOM_GOVERNMENT_CATEGORY_TEMPLATE = "<font color=\"#727272" +
            "\"><strike>%s</strike></font><br>%s";
    private static final String ANIMAL_TEMPLATE = "%s — %s.";
    private static final String CENSUS_TEMPLATE = "%s — %s";
    private final HashMap<String, String> waCategoryConservative = new HashMap<String, String>();
    private final HashMap<String, String> waCategoryLiberal = new HashMap<String, String>();
    private LinkedHashMap<Integer, CensusScale> censusScales;
    // Receiver for WA vote broadcasts
    private BroadcastReceiver resolutionVoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || !isAdded() || mNation == null) {
                return;
            }

            // Only update if user's nation
            String openNationName = SparkleHelper.getIdFromName(mNation.name);
            String userNationName =
                    SparkleHelper.getIdFromName(PinkaHelper.getActiveUser(context).name);
            if (!userNationName.equals(openNationName)) {
                return;
            }

            WaVoteStatus voteStatus =
                    intent.getParcelableExtra(ResolutionActivity.TARGET_VOTE_STATUS);
            mNation.waState = voteStatus.waState;
            mNation.gaVote = voteStatus.gaVote;
            mNation.scVote = voteStatus.scVote;
            forceRefreshData();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] WORLD_CENSUS_ITEMS = getResources().getStringArray(R.array.census);
        censusScales = SparkleHelper.getCensusScales(WORLD_CENSUS_ITEMS);

        // Register resolution vote receiver
        IntentFilter resolutionVoteFilter = new IntentFilter();
        resolutionVoteFilter.addAction(ResolutionActivity.RESOLUTION_BROADCAST);
        ((BroadcastableActivity) getActivity()).registerBroadcastReceiver(resolutionVoteReceiver,
                resolutionVoteFilter);

        String[] govTypes = getResources().getStringArray(R.array.gov_types);
        String[] govConservative = getResources().getStringArray(R.array.gov_conservative);
        String[] govLiberal = getResources().getStringArray(R.array.gov_liberal);

        for (int i = 0; i < govTypes.length; i++) {
            waCategoryConservative.put(govTypes[i], govConservative[i]);
            waCategoryLiberal.put(govTypes[i], govLiberal[i]);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        if (NightmareHelper.getIsZDayActive(getContext())
                && mNation.zombieData != null) {
            cards.add(mNation.zombieData);
        }

        if (mNation.waBadges != null) {
            cards.addAll(mNation.waBadges);
        }

        NationOverviewCardData nocData = new NationOverviewCardData();

        // Set up custom government category depending on user preferences
        String waCategory = mNation.govType.toLowerCase(Locale.US).replace(" ", "_").replace("-",
                "_");
        String customCategory = null;
        int governmentSetting = SettingsActivity.getGovernmentSetting(getContext());
        if (governmentSetting == SettingsActivity.GOV_CONSERVATIVE
                && waCategoryConservative.containsKey(waCategory)) {
            customCategory = waCategoryConservative.get(waCategory);

        } else if (governmentSetting == SettingsActivity.GOV_LIBERAL
                && waCategoryLiberal.containsKey(waCategory)) {
            customCategory = waCategoryLiberal.get(waCategory);
        }

        if (customCategory != null) {
            nocData.category = String.format(Locale.US, CUSTOM_GOVERNMENT_CATEGORY_TEMPLATE,
                    mNation.govType, customCategory);
        } else {
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
        if (mNation.leader != null) {
            ngcGov.items.add(new DataPair(getString(R.string.card_overview_gov_leader),
                    mNation.leader));
        }
        if (mNation.capital != null) {
            ngcGov.items.add(new DataPair(getString(R.string.card_overview_gov_capital),
                    mNation.capital));
        }
        if (mNation.govtPriority != null) {
            ngcGov.items.add(new DataPair(getString(R.string.card_overview_gov_priority),
                    mNation.govtPriority));
        }
        ngcGov.items.add(new DataPair(getString(R.string.card_overview_gov_tax),
                String.format(Locale.US, TAX_TEMPLATE,
                        SparkleHelper.getPrettifiedNumber(mNation.tax))));
        ngcGov.nationCensusTarget = mNation.name;
        ngcGov.idCensusTarget = TrendsActivity.CENSUS_TAXATION;
        cards.add(ngcGov);

        NationGenericCardData ngcEconomy = new NationGenericCardData();
        ngcEconomy.title = getString(R.string.card_overview_econ_title);
        ngcEconomy.items.add(new DataPair(getString(R.string.card_overview_econ_currency),
                mNation.currency));
        ngcEconomy.items.add(new DataPair(getString(R.string.card_overview_econ_gdp),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency)));
        ngcEconomy.items.add(new DataPair(getString(R.string.card_overview_econ_income),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency)));
        ngcEconomy.items.add(new DataPair(getString(R.string.card_overview_econ_industry),
                mNation.industry));
        ngcEconomy.nationCensusTarget = mNation.name;
        ngcEconomy.idCensusTarget = TrendsActivity.CENSUS_ECONOMIC_OUTPUT;
        cards.add(ngcEconomy);

        NationGenericCardData ngcOther = new NationGenericCardData();
        ngcOther.title = getString(R.string.card_overview_other_title);
        // Determine if the adjective is different from the noun (different flavour text)
        String demonymText;
        if (mNation.demAdjective.equals(mNation.demNoun)) {
            demonymText = String.format(Locale.US,
                    getString(R.string.card_overview_other_demonym_txt2), mNation.demNoun,
                    mNation.demPlural);
        } else {
            demonymText = String.format(Locale.US,
                    getString(R.string.card_overview_other_demonym_txt1), mNation.demNoun,
                    mNation.demPlural, mNation.demAdjective);
        }
        ngcOther.items.add(new DataPair(getString(R.string.card_overview_other_demonym),
                demonymText));
        if (mNation.religion != null) {
            ngcOther.items.add(new DataPair(getString(R.string.card_overview_other_religion),
                    mNation.religion));
        }
        String animalText;
        if (mNation.animalTrait != null) {
            animalText = String.format(Locale.US, ANIMAL_TEMPLATE, mNation.animal,
                    mNation.animalTrait);
        } else {
            animalText = mNation.animal;
        }
        ngcOther.items.add(new DataPair(getString(R.string.card_overview_other_animal),
                animalText));
        int censusRawId = mNation.wCensus.id;
        CensusScale worldCensusItem = SparkleHelper.getCensusScale(censusScales,
                mNation.wCensus.id);
        String todayCensusTitle = String.format(Locale.US,
                getString(R.string.card_overview_other_census_title), worldCensusItem.name);
        CensusDetailedRank detailedRank = mNation.census.get(censusRawId);
        StringBuilder todayCensusContent = new StringBuilder(String.format(Locale.US,
                CENSUS_TEMPLATE, SparkleHelper.getPrettifiedSuffixedNumber(getContext(),
                        detailedRank.score), worldCensusItem.unit));
        if (detailedRank.regionRank > 0) {
            todayCensusContent.append("<br>").append(String.format(Locale.US,
                    getString(R.string.card_overview_other_census_region),
                    SparkleHelper.getPrettifiedNumber(detailedRank.regionRank), mNation.region,
                    SparkleHelper.getPrettifiedNumber(detailedRank.regionRankPercent)));
        }
        if (detailedRank.worldRank > 0) {
            todayCensusContent.append("<br>").append(String.format(Locale.US,
                    getString(R.string.card_overview_other_census_world),
                    SparkleHelper.getPrettifiedNumber(detailedRank.worldRank),
                    SparkleHelper.getPrettifiedNumber(detailedRank.worldRankPercent)));
        }
        ngcOther.items.add(new DataPair(todayCensusTitle, todayCensusContent.toString()));
        ngcOther.nationCensusTarget = mNation.name;
        ngcOther.idCensusTarget = censusRawId;
        cards.add(ngcOther);
    }
}
