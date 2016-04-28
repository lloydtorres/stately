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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.census.UserTrendsOnClickListener;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.NameListDialog;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.wa.ResolutionActivity;

import java.util.ArrayList;
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

    // main card
    private TextView govType;
    private TextView region;
    private TextView influence;
    private TextView population;
    private TextView motto;
    private TextView time;

    // freedom cards
    private CardView civilRightsCard;
    private TextView civilRightsDesc;
    private TextView civilRightsPts;

    private CardView economyCard;
    private TextView economyDesc;
    private TextView economyPts;

    private CardView politicalCard;
    private TextView politicalDesc;
    private TextView politicalPts;

    // government cards
    private LinearLayout leaderLayout;
    private TextView leader;
    private LinearLayout capitalLayout;
    private TextView capital;
    private TextView priority;
    private TextView tax;
    private LinearLayout censusTaxation;

    // economy cards
    private TextView currency;
    private TextView gdp;
    private TextView industry;
    private TextView income;
    private LinearLayout censusEconomicOutput;

    // wa section
    private RelativeLayout waMember;
    private LinearLayout waSection;
    private TextView isWaMember;
    private LinearLayout endorsementsHolder;
    private TextView endorsementsCount;
    private LinearLayout gaVoteHolder;
    private TextView gaVote;
    private LinearLayout scVoteHolder;
    private TextView scVote;

    // other cards
    private TextView demonym;
    private LinearLayout religionLayout;
    private TextView religion;
    private TextView animal;
    private TextView censusTitle;
    private TextView censusContent;
    private LinearLayout censusButton;
    private TextView censusButtonLabel;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WORLD_CENSUS_ITEMS = getResources().getStringArray(R.array.census);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_overview, container, false);

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        if (mNation != null)
        {
            initMainCard(view);
            initFreedomCards(view);
            initAssemblySection(view);
            initGovernmentCard(view);
            initEconomyCard(view);
            initOtherCard(view);
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
     * Initialize first card with general data (e.g. government type, region)
     * @param view
     */
    private void initMainCard(View view)
    {
        govType = (TextView) view.findViewById(R.id.nation_gov_type);
        govType.setText(mNation.govType);

        region = (TextView) view.findViewById(R.id.nation_region);
        SparkleHelper.activityLinkBuilder(getContext(), region, mNation.region, mNation.region, mNation.region, SparkleHelper.CLICKY_REGION_MODE);

        influence = (TextView) view.findViewById(R.id.nation_influence);
        influence.setText(String.format(Locale.US, getString(R.string.nation_power_template), mNation.influence, SparkleHelper.getPrettifiedNumber(mNation.census.get(TrendsActivity.CENSUS_INFLUENCE).score)));

        population = (TextView) view.findViewById(R.id.nation_population);
        population.setText(SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase));

        motto = (TextView) view.findViewById(R.id.nation_motto);
        motto.setText(SparkleHelper.getHtmlFormatting(mNation.motto).toString());

        // Testlandia returns a value of 0 for this, so replace with some pretty text instead
        time = (TextView) view.findViewById(R.id.nation_time);
        if (mNation.foundedAgo.equals("0"))
        {
            time.setText(String.format(getString(R.string.nation_time_founded), getString(R.string.nation_time_immemorial), mNation.lastActivityAgo.toLowerCase(Locale.US)));
        }
        else
        {
            time.setText(String.format(getString(R.string.nation_time_founded), mNation.foundedAgo, mNation.lastActivityAgo.toLowerCase(Locale.US)));
        }
    }

    /**
     * Initialize the freedom indicators, use point value divided by 7 as a colour index.
     * @param view
     */
    private void initFreedomCards(View view)
    {
        civilRightsCard = (CardView) view.findViewById(R.id.card_overview_civrights);
        civilRightsDesc = (TextView) view.findViewById(R.id.overview_civrights);
        civilRightsPts = (TextView) view.findViewById(R.id.overview_civrights_pts);

        civilRightsDesc.setText(mNation.freedomDesc.civilRightsDesc);
        int civilRightsScore = (int) mNation.census.get(TrendsActivity.CENSUS_CIVIL_RIGHTS).score;
        civilRightsPts.setText(String.valueOf(civilRightsScore));
        int civColInd = civilRightsScore / 7;
        civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[civColInd]));
        civilRightsCard.setOnClickListener(new UserTrendsOnClickListener(getContext(), TrendsActivity.CENSUS_CIVIL_RIGHTS));

        economyCard = (CardView) view.findViewById(R.id.card_overview_economy);
        economyDesc = (TextView) view.findViewById(R.id.overview_economy);
        economyPts = (TextView) view.findViewById(R.id.overview_economy_pts);

        economyDesc.setText(mNation.freedomDesc.economyDesc);
        int economyScore = (int) mNation.census.get(TrendsActivity.CENSUS_ECONOMY).score;
        economyPts.setText(String.valueOf(economyScore));
        int econColInd = economyScore / 7;
        economyCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[econColInd]));
        economyCard.setOnClickListener(new UserTrendsOnClickListener(getContext(), TrendsActivity.CENSUS_ECONOMY));

        politicalCard = (CardView) view.findViewById(R.id.card_overview_polifree);
        politicalDesc = (TextView) view.findViewById(R.id.overview_polifree);
        politicalPts = (TextView) view.findViewById(R.id.overview_polifree_pts);

        politicalDesc.setText(mNation.freedomDesc.politicalDesc);
        int politicalFreedomScore = (int) mNation.census.get(TrendsActivity.CENSUS_POLITICAL_FREEDOM).score;
        politicalPts.setText(String.valueOf(politicalFreedomScore));
        int polColInd = politicalFreedomScore / 7;
        politicalCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[polColInd]));
        politicalCard.setOnClickListener(new UserTrendsOnClickListener(getContext(), TrendsActivity.CENSUS_POLITICAL_FREEDOM));
    }

    /**
     * Initialize the World Assembly card if needed.
     * @param view
     */
    private void initAssemblySection(View view)
    {
        waMember = (RelativeLayout) view.findViewById(R.id.nation_wa_member);
        waSection = (LinearLayout) view.findViewById(R.id.card_overview_section_wa);

        // Only show if member of the WA (or delegate)
        if (SparkleHelper.isWaMember(getContext(), mNation.waState))
        {
            waSection.setVisibility(View.VISIBLE);
            waMember.setVisibility(View.VISIBLE);

            isWaMember = (TextView) view.findViewById(R.id.nation_wa_status);
            isWaMember.setText(mNation.waState);

            // Show endorsements section if endorsements exist
            if (mNation.endorsements != null && mNation.endorsements.length() > 0)
            {
                endorsementsHolder = (LinearLayout) view.findViewById(R.id.nation_wa_endorsements);
                endorsementsHolder.setVisibility(View.VISIBLE);

                // Build endorsements list
                String[] endorsements = mNation.endorsements.split(",");
                ArrayList<String> properEndorsements = new ArrayList<String>();

                for (String e : endorsements)
                {
                    properEndorsements.add(SparkleHelper.getNameFromId(e));
                }

                endorsementsCount = (TextView) view.findViewById(R.id.nation_wa_num_endorsements);
                endorsementsCount.setText(SparkleHelper.getPrettifiedNumber(properEndorsements.size()));

                final ArrayList<String> fEndorsements = properEndorsements;

                endorsementsHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        NameListDialog nameListDialog = new NameListDialog();
                        nameListDialog.setTitle(getString(R.string.card_overview_wa_endorsements));
                        nameListDialog.setNames(fEndorsements);
                        nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
                        nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                    }
                });
            }
            else
            {
                // disable divider
                view.findViewById(R.id.view_divider).setVisibility(View.GONE);
            }

            // Show vote state in General Assembly if exists
            if (mNation.gaVote != null)
            {
                gaVoteHolder = (LinearLayout) view.findViewById(R.id.nation_wa_ga_vote);
                gaVoteHolder.setVisibility(View.VISIBLE);

                gaVote = (TextView) view.findViewById(R.id.card_overview_wa_vote_ga);
                setAssemblyVoteState(gaVoteHolder, gaVote, mNation.gaVote, Assembly.GENERAL_ASSEMBLY);
            }

            // Show vote state in Security council if exists
            if (mNation.scVote != null)
            {
                scVoteHolder = (LinearLayout) view.findViewById(R.id.nation_wa_sc_vote);
                scVoteHolder.setVisibility(View.VISIBLE);

                scVote = (TextView) view.findViewById(R.id.card_overview_wa_vote_sc);
                setAssemblyVoteState(scVoteHolder, scVote, mNation.scVote, Assembly.SECURITY_COUNCIL);
            }
        }
    }

    /**
     * Sets up colours, etc. for the WA voting indicators, which are identical in all but name.
     * @param holder
     * @param content
     * @param vote
     * @param councilId
     */
    private void setAssemblyVoteState(LinearLayout holder, TextView content, String vote, int councilId)
    {
        // Intent to open the ResolutionActivity
        Intent resolutionActivityLaunch = new Intent(getContext(), ResolutionActivity.class);
        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, councilId);
        final Intent fResolution = resolutionActivityLaunch;

        // Colour of the indicator as well as the assembly name
        int stateColour;
        String assemblyName;

        holder.setVisibility(View.VISIBLE);

        switch (councilId)
        {
            case Assembly.GENERAL_ASSEMBLY:
                assemblyName = getString(R.string.wa_general_assembly);
                break;
            case Assembly.SECURITY_COUNCIL:
                assemblyName = getString(R.string.wa_security_council);
                break;
            default:
                assemblyName = "";
                break;
        }

        // If voting FOR the resolution
        if (getString(R.string.wa_vote_state_for).equals(vote))
        {
            stateColour = SparkleHelper.waColours[0];
            content.setText(String.format(getString(R.string.card_overview_wa_vote), assemblyName, vote));
        }
        // If voting AGAINST the resolution
        else if (getString(R.string.wa_vote_state_against).equals(vote))
        {
            stateColour = SparkleHelper.waColours[1];
            content.setText(String.format(getString(R.string.card_overview_wa_vote), assemblyName, vote));
        }
        // If no vote yet
        else
        {
            stateColour = SparkleHelper.waColours[2];
            content.setText(String.format(getString(R.string.card_overview_wa_novote), assemblyName));
        }

        holder.setBackgroundColor(ContextCompat.getColor(getContext(), stateColour));
        holder.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(fResolution);
                                        }
                                    });
    }

    /**
     * Initialize the government card. Lots of if statements here since these fields are optional.
     * @param view
     */
    private void initGovernmentCard(View view)
    {
        if (mNation.leader != null)
        {
            leader = (TextView) view.findViewById(R.id.nation_leader);
            leader.setText(SparkleHelper.getHtmlFormatting(mNation.leader).toString());
        }
        else
        {
            leaderLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_leader);
            leaderLayout.setVisibility(View.GONE);
        }

        if (mNation.capital != null)
        {
            capital = (TextView) view.findViewById(R.id.nation_capital);
            capital.setText(SparkleHelper.getHtmlFormatting(mNation.capital).toString());
        }
        else
        {
            capitalLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_capital);
            capitalLayout.setVisibility(View.GONE);
        }

        priority = (TextView) view.findViewById(R.id.nation_priority);
        priority.setText(mNation.govtPriority);

        tax = (TextView) view.findViewById(R.id.nation_tax);
        tax.setText(String.format(Locale.US, getString(R.string.percent), mNation.tax));

        censusTaxation = (LinearLayout) view.findViewById(R.id.card_overview_gov_tax_census);
        censusTaxation.setOnClickListener(new UserTrendsOnClickListener(getContext(), TrendsActivity.CENSUS_TAXATION));
    }

    /**
     * Initialize the economy card.
     * @param view
     */
    private void initEconomyCard(View view)
    {
        currency = (TextView) view.findViewById(R.id.nation_currency);
        currency.setText(mNation.currency);

        gdp = (TextView) view.findViewById(R.id.nation_gdp);
        gdp.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency));

        industry = (TextView) view.findViewById(R.id.nation_industry);
        industry.setText(mNation.industry);

        income = (TextView) view.findViewById(R.id.nation_income);
        income.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency));

        censusEconomicOutput = (LinearLayout) view.findViewById(R.id.card_overview_econ_gdp_census);
        censusEconomicOutput.setOnClickListener(new UserTrendsOnClickListener(getContext(), TrendsActivity.CENSUS_ECONOMIC_OUTPUT));
    }

    /**
     * Initialize the 'other' card (demonym and religion data)
     * @param view
     */
    private void initOtherCard(View view)
    {
        demonym = (TextView) view.findViewById(R.id.nation_demonym);
        // Determine if the adjective is different from the noun (different flavour text)
        if (mNation.demAdjective.equals(mNation.demNoun))
        {
            demonym.setText(String.format(getString(R.string.card_overview_other_demonym_txt2), mNation.demNoun, mNation.demPlural));
        }
        else
        {
            demonym.setText(String.format(getString(R.string.card_overview_other_demonym_txt1), mNation.demNoun, mNation.demPlural, mNation.demAdjective));
        }

        // This is an optional field
        if (mNation.religion != null)
        {
            religion = (TextView) view.findViewById(R.id.nation_religion);
            religion.setText(mNation.religion);
        }
        else
        {
            religionLayout = (LinearLayout) view.findViewById(R.id.card_overview_other_religion);
            religionLayout.setVisibility(View.GONE);
        }

        animal = (TextView) view.findViewById(R.id.nation_animal);
        animal.setText(mNation.animal);

        censusTitle = (TextView) view.findViewById(R.id.nation_census_title);
        int censusId = mNation.wCensus.id;
        int censusRawId = mNation.wCensus.id;
        // if census ID is out of bounds, set it as unknown
        if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
        {
            censusId = WORLD_CENSUS_ITEMS.length - 1;
        }
        String[] worldCensusItem = WORLD_CENSUS_ITEMS[censusId].split("##");

        censusTitle.setText(String.format(getString(R.string.card_overview_other_census_title), worldCensusItem[0]));
        censusContent = (TextView) view.findViewById(R.id.nation_census_content);
        CensusDetailedRank detailedRank = mNation.census.get(censusRawId);
        censusContent.setText(String.format(getString(R.string.card_overview_other_census_content), SparkleHelper.getPrettifiedSuffixedNumber(getContext(), detailedRank.score), worldCensusItem[1]));
        if (detailedRank.regionRank > 0)
        {
            censusContent.append(String.format(Locale.US, getString(R.string.card_overview_other_census_region), SparkleHelper.getPrettifiedNumber(detailedRank.regionRank), mNation.region, SparkleHelper.getPrettifiedNumber(detailedRank.regionRankPercent)));
        }
        if (detailedRank.worldRank > 0)
        {
            censusContent.append(String.format(Locale.US, getString(R.string.card_overview_other_census_world), SparkleHelper.getPrettifiedNumber(detailedRank.worldRank), SparkleHelper.getPrettifiedNumber(detailedRank.worldRankPercent)));
        }
        
        censusButtonLabel = (TextView) view.findViewById(R.id.nation_census_button_text);
        censusButtonLabel.setText(String.format(getString(R.string.card_overview_census_button), worldCensusItem[0]));
        censusButton = (LinearLayout) view.findViewById(R.id.nation_census_button);
        censusButton.setOnClickListener(new UserTrendsOnClickListener(getContext(), mNation.wCensus.id));
    }
}
