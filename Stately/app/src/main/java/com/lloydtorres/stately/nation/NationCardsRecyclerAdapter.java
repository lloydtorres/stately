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
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.census.TrendsOnClickListener;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.dto.GovBudget;
import com.lloydtorres.stately.dto.MortalityCause;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationFreedomCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.dto.NationOverviewCardData;
import com.lloydtorres.stately.dto.Sectors;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-07-24.
 * A RecyclerView adapter for the four main nation tabs.
 */
public class NationCardsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // different types of cards
    public static final int CARD_OVERVIEW = 0;
    public static final int CARD_FREEDOMS = 1;
    public static final int CARD_GENERIC = 2;
    public static final int CARD_CHART = 3;

    private String[] WORLD_CENSUS_ITEMS;

    private List<Parcelable> cards;
    private Context context;
    private FragmentManager fm;

    public NationCardsRecyclerAdapter(Context c, List<Parcelable> cds, FragmentManager f) {
        context = c;
        cards = cds;
        fm = f;

        WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case CARD_OVERVIEW:
                View overviewCard = inflater.inflate(R.layout.card_nation_overview, parent, false);
                viewHolder = new NationOverviewCard(overviewCard);
                break;
            case CARD_FREEDOMS:
                View freedomCard = inflater.inflate(R.layout.card_nation_freedom, parent, false);
                viewHolder = new NationFreedomCard(freedomCard);
                break;
            case CARD_GENERIC:
                View genericCard = inflater.inflate(R.layout.card_nation_generic, parent, false);
                viewHolder = new NationGenericCard(genericCard);
                break;
            case CARD_CHART:
                View chartCard = inflater.inflate(R.layout.card_nation_chart, parent, false);
                viewHolder = new NationChartCard(chartCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CARD_OVERVIEW:
                NationOverviewCard noc = (NationOverviewCard) holder;
                noc.init((NationOverviewCardData) cards.get(position));
                break;
            case CARD_FREEDOMS:
                NationFreedomCard nfc = (NationFreedomCard) holder;
                nfc.init((NationFreedomCardData) cards.get(position));
                break;
            case CARD_GENERIC:
                NationGenericCard ngc = (NationGenericCard) holder;
                ngc.init((NationGenericCardData) cards.get(position));
                break;
            case CARD_CHART:
                NationChartCard ncc = (NationChartCard) holder;
                ncc.init((NationChartCardData) cards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof NationOverviewCardData) {
            return CARD_OVERVIEW;
        }
        else if (cards.get(position) instanceof NationFreedomCardData) {
            return CARD_FREEDOMS;
        }
        else if (cards.get(position) instanceof NationGenericCardData) {
            return CARD_GENERIC;
        }
        else if (cards.get(position) instanceof NationChartCardData) {
            return CARD_CHART;
        }
        return -1;
    }

    // Card viewholders

    public class NationOverviewCard extends RecyclerView.ViewHolder {

        private HtmlTextView govType;
        private TextView region;
        private TextView influence;
        private TextView population;
        private TextView motto;
        private TextView time;

        // WA section
        private RelativeLayout waMember;
        private LinearLayout waSection;
        private TextView isWaMember;
        private View divider;
        private RelativeLayout endorsementsHolder;
        private TextView endorsementsCount;
        private RelativeLayout gaVoteHolder;
        private TextView gaVote;
        private RelativeLayout scVoteHolder;
        private TextView scVote;

        public NationOverviewCard(View view) {
            super(view);
            govType = (HtmlTextView) view.findViewById(R.id.nation_gov_type);
            region = (TextView) view.findViewById(R.id.nation_region);
            influence = (TextView) view.findViewById(R.id.nation_influence);
            population = (TextView) view.findViewById(R.id.nation_population);
            motto = (TextView) view.findViewById(R.id.nation_motto);
            time = (TextView) view.findViewById(R.id.nation_time);

            waMember = (RelativeLayout) view.findViewById(R.id.nation_wa_member);
            waSection = (LinearLayout) view.findViewById(R.id.card_overview_section_wa);
            isWaMember = (TextView) view.findViewById(R.id.nation_wa_status);
            divider = view.findViewById(R.id.view_divider);
            endorsementsHolder = (RelativeLayout) view.findViewById(R.id.nation_wa_endorsements);
            endorsementsCount = (TextView) view.findViewById(R.id.nation_wa_num_endorsements);
            gaVoteHolder = (RelativeLayout) view.findViewById(R.id.nation_wa_ga_vote);
            gaVote = (TextView) view.findViewById(R.id.card_overview_wa_vote_ga);
            scVoteHolder = (RelativeLayout) view.findViewById(R.id.nation_wa_sc_vote);
            scVote = (TextView) view.findViewById(R.id.card_overview_wa_vote_sc);
        }

        public void init(NationOverviewCardData data) {
            govType.setHtml(data.category);
            SparkleHelper.activityLinkBuilder(context, region, data.region, data.region, data.region, SparkleHelper.CLICKY_REGION_MODE);
            influence.setText(String.format(Locale.US, context.getString(R.string.nation_power_template), data.inflDesc, SparkleHelper.getPrettifiedNumber(data.inflScore)));
            population.setText(SparkleHelper.getPopulationFormatted(context, data.population));
            motto.setText(SparkleHelper.getHtmlFormatting(data.motto).toString());
            if (data.established.equals("0"))
            {
                time.setText(String.format(context.getString(R.string.nation_time_founded), context.getString(R.string.nation_time_immemorial), data.lastSeen.toLowerCase(Locale.US)));
            }
            else
            {
                time.setText(String.format(context.getString(R.string.nation_time_founded), data.established, data.lastSeen.toLowerCase(Locale.US)));
            }

            if (SparkleHelper.isWaMember(context, data.waState))
            {
                waSection.setVisibility(View.VISIBLE);
                waMember.setVisibility(View.VISIBLE);
                isWaMember.setText(data.waState);

                // Show endorsements section if endorsements exist
                if (data.endorsements != null && data.endorsements.length() > 0)
                {
                    endorsementsHolder.setVisibility(View.VISIBLE);

                    // Build endorsements list
                    String[] endorsements = data.endorsements.split(",");
                    ArrayList<String> properEndorsements = new ArrayList<String>();

                    for (String e : endorsements)
                    {
                        properEndorsements.add(SparkleHelper.getNameFromId(e));
                    }

                    endorsementsCount.setText(SparkleHelper.getPrettifiedNumber(properEndorsements.size()));

                    final ArrayList<String> fEndorsements = properEndorsements;

                    endorsementsHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NameListDialog nameListDialog = new NameListDialog();
                            nameListDialog.setTitle(context.getString(R.string.card_overview_wa_endorsements));
                            nameListDialog.setNames(fEndorsements);
                            nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
                            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                        }
                    });
                }
                else
                {
                    // disable divider
                    divider.setVisibility(View.GONE);
                }

                // Show vote state in General Assembly if exists
                if (data.gaVote != null)
                {
                    gaVoteHolder.setVisibility(View.VISIBLE);
                    setAssemblyVoteState(gaVoteHolder, gaVote, data.gaVote, Assembly.GENERAL_ASSEMBLY);
                }

                // Show vote state in Security council if exists
                if (data.scVote != null)
                {
                    scVoteHolder.setVisibility(View.VISIBLE);
                    setAssemblyVoteState(scVoteHolder, scVote, data.scVote, Assembly.SECURITY_COUNCIL);
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
        private void setAssemblyVoteState(RelativeLayout holder, TextView content, String vote, int councilId)
        {
            // Intent to open the ResolutionActivity
            Intent resolutionActivityLaunch = new Intent(context, ResolutionActivity.class);
            resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, councilId);
            final Intent fResolution = resolutionActivityLaunch;

            // Colour of the indicator as well as the assembly name
            int stateColour;
            String assemblyName;

            holder.setVisibility(View.VISIBLE);

            switch (councilId)
            {
                case Assembly.GENERAL_ASSEMBLY:
                    assemblyName = context.getString(R.string.wa_general_assembly);
                    break;
                case Assembly.SECURITY_COUNCIL:
                    assemblyName = context.getString(R.string.wa_security_council);
                    break;
                default:
                    assemblyName = "";
                    break;
            }

            // If voting FOR the resolution
            if (context.getString(R.string.wa_vote_state_for).equals(vote))
            {
                stateColour = SparkleHelper.waColours[0];
                content.setText(String.format(context.getString(R.string.card_overview_wa_vote), assemblyName, vote.toLowerCase(Locale.ENGLISH)));
            }
            // If voting AGAINST the resolution
            else if (context.getString(R.string.wa_vote_state_against).equals(vote))
            {
                stateColour = SparkleHelper.waColours[1];
                content.setText(String.format(context.getString(R.string.card_overview_wa_vote), assemblyName, vote.toLowerCase(Locale.ENGLISH)));
            }
            // If no vote yet
            else
            {
                stateColour = SparkleHelper.waColours[2];
                content.setText(String.format(context.getString(R.string.card_overview_wa_novote), assemblyName));
            }

            holder.setBackgroundColor(ContextCompat.getColor(context, stateColour));
            holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(fResolution);
                }
            });
        }
    }

    public class NationFreedomCard extends RecyclerView.ViewHolder {

        private CardView civilRightsCard;
        private TextView civilRightsDesc;
        private TextView civilRightsPts;

        private CardView economyCard;
        private TextView economyDesc;
        private TextView economyPts;

        private CardView politicalCard;
        private TextView politicalDesc;
        private TextView politicalPts;

        public NationFreedomCard(View view) {
            super(view);
            civilRightsCard = (CardView) view.findViewById(R.id.card_overview_civrights);
            civilRightsDesc = (TextView) view.findViewById(R.id.overview_civrights);
            civilRightsPts = (TextView) view.findViewById(R.id.overview_civrights_pts);
            economyCard = (CardView) view.findViewById(R.id.card_overview_economy);
            economyDesc = (TextView) view.findViewById(R.id.overview_economy);
            economyPts = (TextView) view.findViewById(R.id.overview_economy_pts);
            politicalCard = (CardView) view.findViewById(R.id.card_overview_polifree);
            politicalDesc = (TextView) view.findViewById(R.id.overview_polifree);
            politicalPts = (TextView) view.findViewById(R.id.overview_polifree_pts);
        }

        public void init(NationFreedomCardData data) {
            civilRightsDesc.setText(data.civDesc);
            int civilRightsScore = data.civScore;
            civilRightsPts.setText(String.valueOf(civilRightsScore));
            int civColInd = civilRightsScore / 7;
            civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(context, SparkleHelper.freedomColours[civColInd]));
            civilRightsCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_CIVIL_RIGHTS));

            economyDesc.setText(data.econDesc);
            int economyScore = data.econScore;
            economyPts.setText(String.valueOf(economyScore));
            int econColInd = economyScore / 7;
            economyCard.setCardBackgroundColor(ContextCompat.getColor(context, SparkleHelper.freedomColours[econColInd]));
            economyCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_ECONOMY));

            politicalDesc.setText(data.poliDesc);
            int politicalFreedomScore = data.poliScore;
            politicalPts.setText(String.valueOf(politicalFreedomScore));
            int polColInd = politicalFreedomScore / 7;
            politicalCard.setCardBackgroundColor(ContextCompat.getColor(context, SparkleHelper.freedomColours[polColInd]));
            politicalCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_POLITICAL_FREEDOM));
        }
    }

    private void inflateEntry(LayoutInflater inflater, LinearLayout targetLayout, String title, String content)
    {
        View entryView = inflater.inflate(R.layout.view_cardentry, null);
        TextView titleView = (TextView) entryView.findViewById(R.id.cardentry_label);
        TextView contentView = (TextView) entryView.findViewById(R.id.cardentry_content);
        titleView.setText(SparkleHelper.getHtmlFormatting(title));
        contentView.setText(SparkleHelper.getHtmlFormatting(content));
        targetLayout.addView(entryView);
    }

    public class NationGenericCard extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView content;
        private LinearLayout detailsHolder;
        private LinearLayout trendButton;
        private TextView trendContent;
        private LayoutInflater inflater;

        public NationGenericCard(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.card_nation_generic_title);
            content = (TextView) itemView.findViewById(R.id.card_nation_generic_content);
            detailsHolder = (LinearLayout) itemView.findViewById(R.id.card_nation_generic_details_holder);
            trendButton = (LinearLayout) itemView.findViewById(R.id.card_nation_generic_trend_button);
            trendContent = (TextView) itemView.findViewById(R.id.card_nation_generic_trend_content);
        }

        public void init(NationGenericCardData data) {
            inflater = LayoutInflater.from(context);

            title.setText(data.title);
            if (data.mainContent != null && data.mainContent.length() > 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(SparkleHelper.getHtmlFormatting(data.mainContent));
            }
            else {
                content.setVisibility(View.GONE);
            }
            if (data.items != null && data.items.size() > 0) {
                detailsHolder.setVisibility(View.VISIBLE);
                detailsHolder.removeAllViews();
                for (DataPair entry : data.items) {
                    inflateEntry(inflater, detailsHolder, entry.key, entry.value);
                }
            }
            else {
                detailsHolder.setVisibility(View.GONE);
                detailsHolder.removeAllViews();
            }

            if (data.nationCensusTarget != null) {
                trendButton.setVisibility(View.VISIBLE);
                trendButton.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationCensusTarget), data.idCensusTarget));

                // if census ID is out of bounds, set it as unknown
                int censusId = data.idCensusTarget;
                if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
                {
                    censusId = WORLD_CENSUS_ITEMS.length - 1;
                }
                String[] worldCensusItem = WORLD_CENSUS_ITEMS[censusId].split("##");
                trendContent.setText(String.format(context.getString(R.string.card_overview_census_button), worldCensusItem[0]));
            }
            else {
                trendButton.setVisibility(View.GONE);
                trendButton.setOnClickListener(null);
            }
        }
    }

    public class NationChartCard extends RecyclerView.ViewHolder {

        private TextView title;
        private LinearLayout details;
        private PieChart chart;
        private LayoutInflater inflater;

        public NationChartCard(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.card_nation_chart_title);
            details = (LinearLayout) itemView.findViewById(R.id.card_nation_chart_details_holder);
            chart = (PieChart) itemView.findViewById(R.id.card_nation_chart_chart);
        }

        public void init(NationChartCardData data) {
            inflater = LayoutInflater.from(context);

            List<String> chartLabels = new ArrayList<String>();
            List<Entry> chartEntries = new ArrayList<Entry>();
            List<Integer> chartColours = new ArrayList<Integer>();

            if (data.mode == NationChartCardData.MODE_PEOPLE) {
                details.setVisibility(View.GONE);
                details.removeAllViews();
            }
            else {
                details.setVisibility(View.VISIBLE);
                for (DataPair entry : data.details) {
                    inflateEntry(inflater, details, entry.key, entry.value);
                }
            }

            switch (data.mode) {
                case NationChartCardData.MODE_PEOPLE:
                    title.setText(context.getString(R.string.card_people_mortality_title));

                    // setup data
                    List<MortalityCause> causes = data.mortalityList;

                    for (int i=0; i < causes.size(); i++)
                    {
                        // NationStates API stores this as Animal Attack instead of
                        // using the actual national animal, so replace that
                        if (context.getString(R.string.animal_attack_original).equals(causes.get(i).type))
                        {
                            chartLabels.add(String.format(context.getString(R.string.animal_attack_madlibs), data.animal));
                        }
                        else
                        {
                            chartLabels.add(causes.get(i).type);
                        }
                        Entry n = new Entry(causes.get(i).value, i);
                        chartEntries.add(n);
                    }

                    for (int i=0; i<SparkleHelper.chartColours.length; i++) {
                        chartColours.add(ContextCompat.getColor(context, SparkleHelper.chartColours[i]));
                    }
                    break;
                case NationChartCardData.MODE_GOV:
                    title.setText(context.getString(R.string.card_government_expenditures_title));

                    // setup data
                    GovBudget budget = data.govBudget;

                    // Have to add it one by one, how horrifying
                    int i = 0;
                    if (budget.admin > 0f)
                    {
                        chartLabels.add(context.getString(R.string.administration));
                        chartEntries.add(new Entry(budget.admin, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart0));
                    }
                    if (budget.defense > 0f)
                    {
                        chartLabels.add(context.getString(R.string.defense));
                        chartEntries.add(new Entry(budget.defense, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart1));
                    }
                    if (budget.education > 0f)
                    {
                        chartLabels.add(context.getString(R.string.education));
                        chartEntries.add(new Entry(budget.education, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart2));
                    }
                    if (budget.environment > 0f)
                    {
                        chartLabels.add(context.getString(R.string.environment));
                        chartEntries.add(new Entry(budget.environment, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart3));
                    }
                    if (budget.healthcare > 0f)
                    {
                        chartLabels.add(context.getString(R.string.healthcare));
                        chartEntries.add(new Entry(budget.healthcare, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart4));
                    }
                    if (budget.industry > 0f)
                    {
                        chartLabels.add(context.getString(R.string.industry));
                        chartEntries.add(new Entry(budget.industry, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart5));
                    }
                    if (budget.internationalAid > 0f)
                    {
                        chartLabels.add(context.getString(R.string.international_aid));
                        chartEntries.add(new Entry(budget.internationalAid, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart6));
                    }
                    if (budget.lawAndOrder > 0f)
                    {
                        chartLabels.add(context.getString(R.string.law_and_order));
                        chartEntries.add(new Entry(budget.lawAndOrder, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart7));
                    }
                    if (budget.publicTransport > 0f)
                    {
                        chartLabels.add(context.getString(R.string.public_transport));
                        chartEntries.add(new Entry(budget.publicTransport, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart8));
                    }
                    if (budget.socialPolicy > 0f)
                    {
                        chartLabels.add(context.getString(R.string.social_policy));
                        chartEntries.add(new Entry(budget.socialPolicy, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart9));
                    }
                    if (budget.spirituality > 0f)
                    {
                        chartLabels.add(context.getString(R.string.spirituality));
                        chartEntries.add(new Entry(budget.spirituality, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart10));
                    }
                    if (budget.welfare > 0f)
                    {
                        chartLabels.add(context.getString(R.string.welfare));
                        chartEntries.add(new Entry(budget.welfare, i++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart11));
                    }
                    break;
                case NationChartCardData.MODE_ECON:
                    title.setText(context.getString(R.string.card_economy_analysis_title));

                    // setup data
                    Sectors sectors = data.sectors;

                    int j = 0;
                    if (sectors.government > 0f)
                    {
                        chartLabels.add(context.getString(R.string.government));
                        chartEntries.add(new Entry(sectors.government, j++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector0));
                    }
                    if (sectors.stateOwned > 0f)
                    {
                        chartLabels.add(context.getString(R.string.state_owned));
                        chartEntries.add(new Entry(sectors.stateOwned, j++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector1));
                    }
                    if (sectors.privateSector > 0f)
                    {
                        chartLabels.add(context.getString(R.string.private_sector));
                        chartEntries.add(new Entry(sectors.privateSector, j++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector2));
                    }
                    if (sectors.blackMarket > 0f)
                    {
                        chartLabels.add(context.getString(R.string.black_market));
                        chartEntries.add(new Entry(sectors.blackMarket, j++));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector3));
                    }
                    break;
            }

            // Disable data labels, set colours and data
            PieDataSet dataSet = new PieDataSet(chartEntries, "");
            dataSet.setDrawValues(false);
            dataSet.setColors(chartColours);
            PieData dataFull = new PieData(chartLabels, dataSet);

            chart = SparkleHelper.getFormattedPieChart(context, chart, chartLabels);
            chart.setData(dataFull);
            chart.invalidate();
        }
    }
}
