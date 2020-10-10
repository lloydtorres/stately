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
import android.os.Parcelable;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.census.TrendsOnClickListener;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.GovBudget;
import com.lloydtorres.stately.dto.MortalityCause;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationFreedomCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.dto.NationOverviewCardData;
import com.lloydtorres.stately.dto.Policy;
import com.lloydtorres.stately.dto.Sectors;
import com.lloydtorres.stately.dto.WaBadge;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.feed.HappeningCard;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.issues.PolicyCard;
import com.lloydtorres.stately.wa.WaBadgeCard;
import com.lloydtorres.stately.zombie.ZombieChartCard;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-07-24.
 * A RecyclerView adapter for the four main nation tabs.
 */
public class NationCardsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // String container templates
    private static final String POWER_TEMPLATE = "%s (%s)";

    // different types of cards
    private static final int CARD_OVERVIEW = 0;
    private static final int CARD_FREEDOMS = 1;
    private static final int CARD_GENERIC = 2;
    private static final int CARD_CHART = 3;
    private static final int CARD_ZOMBIE = 4;
    private static final int CARD_WA_BADGE = 5;
    private static final int CARD_POLICY = 6;
    private static final int CARD_EMPTY = 7;

    private LinkedHashMap<Integer, CensusScale> censusScales;

    private List<Parcelable> cards;
    private String nationName;
    private boolean isSameRegion;
    private ExploreActivity exploreActivity;
    private Context context;
    private FragmentManager fm;

    public NationCardsRecyclerAdapter(List<Parcelable> cds, FragmentManager f, String n, boolean sameRegion, ExploreActivity act) {
        this(act, cds, f, n, sameRegion);
        exploreActivity = act;
    }

    public NationCardsRecyclerAdapter(Context c, List<Parcelable> cds, FragmentManager f, String n, boolean sameRegion) {
        context = c;
        fm = f;
        nationName = n;
        isSameRegion = sameRegion;

        String[] WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
        censusScales = SparkleHelper.getCensusScales(WORLD_CENSUS_ITEMS);

        setCards(cds);
    }

    public void setCards(List<Parcelable> cds) {
        cards = cds;
        notifyDataSetChanged();
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
            case CARD_ZOMBIE:
                View zombieCard = inflater.inflate(R.layout.card_zombie_chart, parent, false);
                viewHolder = new ZombieChartCard(zombieCard);
                break;
            case CARD_WA_BADGE:
                View waBadgeCard = inflater.inflate(R.layout.card_wa_badge, parent, false);
                viewHolder = new WaBadgeCard(context, waBadgeCard);
                break;
            case CARD_POLICY:
                View policyCard = inflater.inflate(R.layout.card_policy, parent, false);
                viewHolder = new PolicyCard(context, policyCard);
                break;
            case CARD_EMPTY:
                View emptyCard = inflater.inflate(R.layout.card_happening, parent, false);
                viewHolder = new HappeningCard(context, emptyCard);
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
            case CARD_ZOMBIE:
                ZombieChartCard zcc = (ZombieChartCard) holder;
                Zombie zombieData = (Zombie) cards.get(position);
                String curUserId = PinkaHelper.getActiveUser(context).nationId;
                // Only show superweapon button if in same region and not self
                if (!SparkleHelper.getIdFromName(nationName).equals(curUserId) && isSameRegion) {
                    zcc.initExplore(exploreActivity, zombieData, ZombieChartCard.MODE_NATION_SUPERWEAPON, nationName);
                } else {
                    zcc.init(context, zombieData, ZombieChartCard.MODE_NATION_DEFAULT, nationName);
                }
                break;
            case CARD_WA_BADGE:
                WaBadgeCard bc = (WaBadgeCard) holder;
                bc.init((WaBadge) cards.get(position));
                break;
            case CARD_POLICY:
                PolicyCard pc = (PolicyCard) holder;
                pc.init((Policy) cards.get(position));
                break;
            case CARD_EMPTY:
                HappeningCard hc = (HappeningCard) holder;
                hc.init((Event) cards.get(position));
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
        else if (cards.get(position) instanceof Zombie) {
            return CARD_ZOMBIE;
        }
        else if (cards.get(position) instanceof WaBadge) {
            return CARD_WA_BADGE;
        }
        else if (cards.get(position) instanceof Policy) {
            return CARD_POLICY;
        }
        else if (cards.get(position) instanceof Event) {
            return CARD_EMPTY;
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
            govType = view.findViewById(R.id.nation_gov_type);
            region = view.findViewById(R.id.nation_region);
            influence = view.findViewById(R.id.nation_influence);
            population = view.findViewById(R.id.nation_population);
            motto = view.findViewById(R.id.nation_motto);
            time = view.findViewById(R.id.nation_time);

            waMember = view.findViewById(R.id.nation_wa_member);
            waSection = view.findViewById(R.id.card_overview_section_wa);
            isWaMember = view.findViewById(R.id.nation_wa_status);
            divider = view.findViewById(R.id.view_divider);
            endorsementsHolder = view.findViewById(R.id.nation_wa_endorsements);
            endorsementsCount = view.findViewById(R.id.nation_wa_num_endorsements);
            gaVoteHolder = view.findViewById(R.id.nation_wa_ga_vote);
            gaVote = view.findViewById(R.id.card_overview_wa_vote_ga);
            scVoteHolder = view.findViewById(R.id.nation_wa_sc_vote);
            scVote = view.findViewById(R.id.card_overview_wa_vote_sc);
        }

        public void init(NationOverviewCardData data) {
            govType.setHtml(data.category);
            region.setText(data.region);
            region.setOnClickListener(SparkleHelper.getExploreOnClickListener(context, SparkleHelper.getIdFromName(data.region), ExploreActivity.EXPLORE_REGION));
            influence.setText(String.format(Locale.US, POWER_TEMPLATE, data.inflDesc, SparkleHelper.getPrettifiedNumber(data.inflScore)));
            population.setText(SparkleHelper.getPopulationFormatted(context, data.population));
            motto.setText(SparkleHelper.getHtmlFormatting(data.motto).toString());
            String lastLogin = SparkleHelper.getReadableDateFromUTC(context, data.lastSeen);
            if (data.established == 0) {
                time.setText(String.format(Locale.US, context.getString(R.string.nation_time_founded), context.getString(R.string.nation_time_immemorial), lastLogin));
            } else {
                time.setText(String.format(Locale.US, context.getString(R.string.nation_time_founded), SparkleHelper.getReadableDateFromUTC(context, data.established), lastLogin));
            }

            if (SparkleHelper.isWaMember(data.waState)) {
                waSection.setVisibility(View.VISIBLE);
                waMember.setVisibility(View.VISIBLE);
                isWaMember.setText(data.waState);

                // Show endorsements section if endorsements exist
                if (data.endorsements != null && data.endorsements.length() > 0) {
                    endorsementsHolder.setVisibility(View.VISIBLE);

                    // Build endorsements list
                    String[] endorsements = data.endorsements.split(",");
                    ArrayList<String> properEndorsements = new ArrayList<String>();

                    for (String e : endorsements) {
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
                            nameListDialog.setTarget(ExploreActivity.EXPLORE_NATION);
                            nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                        }
                    });
                } else {
                    // disable divider
                    divider.setVisibility(View.GONE);
                }

                // Show vote state in General Assembly if exists
                if (data.gaVote != null) {
                    gaVoteHolder.setVisibility(View.VISIBLE);
                    setAssemblyVoteState(gaVoteHolder, gaVote, data.gaVote, Assembly.GENERAL_ASSEMBLY);
                }

                // Show vote state in Security council if exists
                if (data.scVote != null) {
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
        private void setAssemblyVoteState(RelativeLayout holder, TextView content, String vote, final int councilId) {
            // Colour of the indicator as well as the assembly name
            int stateColour;
            String assemblyName;

            holder.setVisibility(View.VISIBLE);

            switch (councilId) {
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
            if (WaVoteStatus.VOTE_FOR.equals(vote)) {
                stateColour = R.color.colorChart0;
                content.setText(String.format(Locale.US, context.getString(R.string.card_overview_wa_vote), assemblyName, vote.toLowerCase(Locale.ENGLISH)));
            }
            // If voting AGAINST the resolution
            else if (WaVoteStatus.VOTE_AGAINST.equals(vote)) {
                stateColour = R.color.colorChart1;
                content.setText(String.format(Locale.US, context.getString(R.string.card_overview_wa_vote), assemblyName, vote.toLowerCase(Locale.ENGLISH)));
            }
            // If no vote yet
            else {
                stateColour = R.color.colorChart12;
                content.setText(String.format(Locale.US, context.getString(R.string.card_overview_wa_novote), assemblyName));
            }

            holder.setBackgroundColor(ContextCompat.getColor(context, stateColour));
            holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SparkleHelper.startResolution(context, councilId, null);
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
            civilRightsCard = view.findViewById(R.id.card_overview_civrights);
            civilRightsDesc = view.findViewById(R.id.overview_civrights);
            civilRightsPts = view.findViewById(R.id.overview_civrights_pts);
            economyCard = view.findViewById(R.id.card_overview_economy);
            economyDesc = view.findViewById(R.id.overview_economy);
            economyPts = view.findViewById(R.id.overview_economy_pts);
            politicalCard = view.findViewById(R.id.card_overview_polifree);
            politicalDesc = view.findViewById(R.id.overview_polifree);
            politicalPts = view.findViewById(R.id.overview_polifree_pts);
        }

        public void init(NationFreedomCardData data) {
            civilRightsDesc.setText(data.civDesc);
            int civilRightsScore = data.civScore;
            civilRightsPts.setText(String.valueOf(civilRightsScore));
            int civColInd = Math.min(Math.max(civilRightsScore / 7, 0), RaraHelper.freedomColours.length - 1);
            civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(context, RaraHelper.freedomColours[civColInd]));
            civilRightsCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_CIVIL_RIGHTS));

            economyDesc.setText(data.econDesc);
            int economyScore = data.econScore;
            economyPts.setText(String.valueOf(economyScore));
            int econColInd = Math.min(Math.max(economyScore / 7, 0), RaraHelper.freedomColours.length - 1);
            economyCard.setCardBackgroundColor(ContextCompat.getColor(context, RaraHelper.freedomColours[econColInd]));
            economyCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_ECONOMY));

            politicalDesc.setText(data.poliDesc);
            int politicalFreedomScore = data.poliScore;
            politicalPts.setText(String.valueOf(politicalFreedomScore));
            int polColInd = Math.min(Math.max(politicalFreedomScore / 7, 0), RaraHelper.freedomColours.length - 1);
            politicalCard.setCardBackgroundColor(ContextCompat.getColor(context, RaraHelper.freedomColours[polColInd]));
            politicalCard.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationTarget), TrendsActivity.CENSUS_POLITICAL_FREEDOM));
        }
    }

    private void inflateEntry(LayoutInflater inflater, LinearLayout targetLayout, String title, String content) {
        View entryView = inflater.inflate(R.layout.view_cardentry, null);
        TextView titleView = entryView.findViewById(R.id.cardentry_label);
        TextView contentView = entryView.findViewById(R.id.cardentry_content);
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
            title = itemView.findViewById(R.id.card_nation_generic_title);
            content = itemView.findViewById(R.id.card_nation_generic_content);
            detailsHolder = itemView.findViewById(R.id.card_nation_generic_details_holder);
            trendButton = itemView.findViewById(R.id.card_nation_generic_trend_button);
            trendContent = itemView.findViewById(R.id.card_nation_generic_trend_content);
        }

        public void init(NationGenericCardData data) {
            inflater = LayoutInflater.from(context);

            title.setText(data.title);
            if (data.mainContent != null && data.mainContent.length() > 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(SparkleHelper.getHtmlFormatting(data.mainContent));
            } else {
                content.setVisibility(View.GONE);
            }
            if (data.items != null && data.items.size() > 0) {
                detailsHolder.setVisibility(View.VISIBLE);
                detailsHolder.removeAllViews();
                for (DataPair entry : data.items) {
                    inflateEntry(inflater, detailsHolder, entry.key, entry.value);
                }
            } else {
                detailsHolder.setVisibility(View.GONE);
                detailsHolder.removeAllViews();
            }

            if (data.nationCensusTarget != null) {
                trendButton.setVisibility(View.VISIBLE);
                trendButton.setOnClickListener(new TrendsOnClickListener(context, SparkleHelper.getIdFromName(data.nationCensusTarget), data.idCensusTarget));

                // if census ID is out of bounds, set it as unknown
                CensusScale worldCensusItem = SparkleHelper.getCensusScale(censusScales, data.idCensusTarget);
                trendContent.setText(String.format(Locale.US, context.getString(R.string.card_overview_census_button), worldCensusItem.name));
            } else {
                trendButton.setVisibility(View.GONE);
                trendButton.setOnClickListener(null);
            }
        }
    }

    public static final String ANIMAL_ATTACK = "Animal Attack";

    public class NationChartCard extends RecyclerView.ViewHolder {

        private TextView title;
        private LinearLayout details;
        private PieChart chart;
        private LayoutInflater inflater;

        public NationChartCard(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_nation_chart_title);
            details = itemView.findViewById(R.id.card_nation_chart_details_holder);
            chart = itemView.findViewById(R.id.card_nation_chart_chart);
        }

        public void init(NationChartCardData data) {
            inflater = LayoutInflater.from(context);

            List<PieEntry> chartEntries = new ArrayList<PieEntry>();
            List<Integer> chartColours = new ArrayList<Integer>();

            if (data.mode == NationChartCardData.MODE_PEOPLE) {
                details.setVisibility(View.GONE);
                details.removeAllViews();
            } else {
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

                    for (int i=0; i < causes.size(); i++) {
                        // NationStates API stores this as Animal Attack instead of
                        // using the actual national animal, so replace that
                        String causeLabel = causes.get(i).type;
                        if (ANIMAL_ATTACK.equals(causes.get(i).type)) {
                            causeLabel = String.format(Locale.US, context.getString(R.string.animal_attack_madlibs), data.animal);
                        }
                        PieEntry n = new PieEntry(causes.get(i).value, causeLabel);
                        chartEntries.add(n);
                    }

                    for (int i=0; i<RaraHelper.chartColours.length; i++) {
                        chartColours.add(ContextCompat.getColor(context, RaraHelper.chartColours[i]));
                    }
                    break;
                case NationChartCardData.MODE_GOV:
                    title.setText(context.getString(R.string.card_government_expenditures_title));

                    // setup data
                    GovBudget budget = data.govBudget;

                    // Have to add it one by one, how horrifying
                    if (budget.admin > 0f) {
                        chartEntries.add(new PieEntry(budget.admin, context.getString(R.string.administration)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart0));
                    }
                    if (budget.defense > 0f) {
                        chartEntries.add(new PieEntry(budget.defense, context.getString(R.string.defense)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart1));
                    }
                    if (budget.education > 0f) {
                        chartEntries.add(new PieEntry(budget.education, context.getString(R.string.education)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart2));
                    }
                    if (budget.environment > 0f) {
                        chartEntries.add(new PieEntry(budget.environment, context.getString(R.string.environment)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart3));
                    }
                    if (budget.healthcare > 0f) {
                        chartEntries.add(new PieEntry(budget.healthcare, context.getString(R.string.healthcare)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart4));
                    }
                    if (budget.industry > 0f) {
                        chartEntries.add(new PieEntry(budget.industry, context.getString(R.string.industry)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart5));
                    }
                    if (budget.internationalAid > 0f) {
                        chartEntries.add(new PieEntry(budget.internationalAid, context.getString(R.string.international_aid)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart6));
                    }
                    if (budget.lawAndOrder > 0f) {
                        chartEntries.add(new PieEntry(budget.lawAndOrder, context.getString(R.string.law_and_order)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart7));
                    }
                    if (budget.publicTransport > 0f) {
                        chartEntries.add(new PieEntry(budget.publicTransport, context.getString(R.string.public_transport)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart8));
                    }
                    if (budget.socialPolicy > 0f) {
                        chartEntries.add(new PieEntry(budget.socialPolicy, context.getString(R.string.social_policy)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart9));
                    }
                    if (budget.spirituality > 0f) {
                        chartEntries.add(new PieEntry(budget.spirituality, context.getString(R.string.spirituality)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart10));
                    }
                    if (budget.welfare > 0f) {
                        chartEntries.add(new PieEntry(budget.welfare, context.getString(R.string.welfare)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorChart11));
                    }
                    break;
                case NationChartCardData.MODE_ECON:
                    title.setText(context.getString(R.string.card_economy_analysis_title));

                    // setup data
                    Sectors sectors = data.sectors;

                    if (sectors.government > 0f) {
                        chartEntries.add(new PieEntry(sectors.government, context.getString(R.string.government)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector0));
                    }
                    if (sectors.stateOwned > 0f) {
                        chartEntries.add(new PieEntry(sectors.stateOwned, context.getString(R.string.state_owned)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector1));
                    }
                    if (sectors.privateSector > 0f) {
                        chartEntries.add(new PieEntry(sectors.privateSector, context.getString(R.string.private_sector)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector2));
                    }
                    if (sectors.blackMarket > 0f) {
                        chartEntries.add(new PieEntry(sectors.blackMarket, context.getString(R.string.black_market)));
                        chartColours.add(ContextCompat.getColor(context, R.color.colorSector3));
                    }
                    break;
            }

            // Disable data labels, set colours and data
            PieDataSet dataSet = new PieDataSet(chartEntries, "");
            dataSet.setDrawValues(false);
            dataSet.setColors(chartColours);
            PieData dataFull = new PieData(dataSet);

            chart = RaraHelper.getFormattedPieChart(context, chart, true);
            chart.setData(dataFull);
            chart.invalidate();
        }
    }
}
