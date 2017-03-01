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

package com.lloydtorres.stately.wa;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.DelegateVote;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-10-01.
 * RecyclerView adapter for the ResolutionActivity. Shows a given WA resolution as cards.
 */

public class ResolutionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Types of cards
    public static final int CARD_HEADER = 0;
    public static final int CARD_CONTENT = 1;
    public static final int CARD_HISTORY = 2;
    public static final int CARD_BREAKDOWN = 3;

    private ResolutionActivity resolutionActivity;
    private Context context;
    private FragmentManager fragmentManager;
    private AlertDialog.Builder dialogBuilder;
    private Resolution resolution;
    private String voteStatus;
    private int councilId;
    private int prefixId;
    private boolean isActive;

    public ResolutionRecyclerAdapter(ResolutionActivity activity, Resolution res, String vs, int cId) {
        resolutionActivity = activity;
        context = resolutionActivity;
        fragmentManager = resolutionActivity.getSupportFragmentManager();
        dialogBuilder = new AlertDialog.Builder(context, RaraHelper.getThemeMaterialDialog(context));
        resolution = res;
        voteStatus = vs;
        councilId = cId;
        prefixId = councilId == Assembly.GENERAL_ASSEMBLY ? R.string.wa_ga_prefix : R.string.wa_sc_prefix;
        isActive = voteStatus != null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case CARD_HEADER:
                View headerCard = inflater.inflate(R.layout.card_wa_resolution_header, parent, false);
                viewHolder = new ResolutionHeaderCard(headerCard);
                break;
            case CARD_CONTENT:
                View contentCard = inflater.inflate(R.layout.card_wa_resolution_content, parent, false);
                viewHolder = new ResolutionContentCard(contentCard);
                break;
            case CARD_BREAKDOWN:
                View breakdownCard = inflater.inflate(R.layout.card_wa_resolution_breakdown, parent, false);
                viewHolder = new ResolutionBreakdownCard(breakdownCard);
                break;
            case CARD_HISTORY:
                View historyCard = inflater.inflate(R.layout.card_wa_resolution_history, parent, false);
                viewHolder = new ResolutionHistoryCard(historyCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ResolutionCard resCardHolder = (ResolutionCard) holder;
        resCardHolder.init();
    }

    private static final int COUNT_ACTIVE = 4;
    private static final int COUNT_INACTIVE = 2;

    @Override
    public int getItemCount() {
        return isActive ? COUNT_ACTIVE : COUNT_INACTIVE;
    }

    public void setUpdatedResolutionData(Resolution res, String vs) {
        resolution = res;
        voteStatus = vs;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // Card viewholders
    public abstract class ResolutionCard extends RecyclerView.ViewHolder {

        public ResolutionCard(View itemView) {
            super(itemView);
        }

        public abstract void init();
    }

    public class ResolutionHeaderCard extends ResolutionCard {
        private TextView title;
        private TextView target;
        private TextView proposedBy;
        private TextView voteStart;
        private TextView repealed;
        private TextView votesFor;
        private TextView votesAgainst;
        private ImageView iconVoteFor;
        private ImageView iconVoteAgainst;

        public ResolutionHeaderCard(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.wa_resolution_title);
            target = (TextView) itemView.findViewById(R.id.wa_nominee);
            proposedBy = (TextView) itemView.findViewById(R.id.wa_proposed_by);
            voteStart = (TextView) itemView.findViewById(R.id.wa_activetime);
            repealed = (TextView) itemView.findViewById(R.id.wa_repealed);
            votesFor = (TextView) itemView.findViewById(R.id.wa_resolution_for);
            votesAgainst = (TextView) itemView.findViewById(R.id.wa_resolution_against);
            iconVoteFor = (ImageView) itemView.findViewById(R.id.content_icon_vote_for);
            iconVoteAgainst = (ImageView) itemView.findViewById(R.id.content_icon_vote_against);
        }

        public void init() {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            title.setText(resolution.name);
            setTargetView(target, resolution.category, resolution.target, resolution.repealTarget);

            String proposer = SparkleHelper.getNameFromId(resolution.proposedBy);
            String proposeTemplate = String.format(Locale.US, context.getString(R.string.wa_proposed), resolution.proposedBy);
            proposeTemplate = SparkleHelper.addExploreActivityLink(proposeTemplate, resolution.proposedBy, proposer, ExploreActivity.EXPLORE_NATION);
            SparkleHelper.setStyledTextView(context, proposedBy, proposeTemplate);

            if (isActive) {
                voteStart.setText(String.format(Locale.US, context.getString(R.string.wa_voting_time), SparkleHelper.calculateResolutionEnd(context, resolution.voteHistoryFor.size())));
            } else {
                voteStart.setText(String.format(Locale.US, context.getString(R.string.wa_implemented),
                        context.getString(prefixId),
                        resolution.id,
                        SparkleHelper.sdf.format(new Date(resolution.implemented * 1000L))));

                if (resolution.repealed > 0) {
                    repealed.setVisibility(View.VISIBLE);
                    repealed.setText(String.format(Locale.US,
                            context.getString(R.string.wa_repealed),
                            context.getString(prefixId),
                            resolution.repealed));
                    repealed.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent resolutionActivityIntent = new Intent(context, ResolutionActivity.class);
                            resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, councilId);
                            resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_OVERRIDE_RES_ID, resolution.repealed);
                            context.startActivity(resolutionActivityIntent);
                        }
                    });
                }
            }

            votesFor.setText(SparkleHelper.getPrettifiedNumber(resolution.votesFor));
            votesAgainst.setText(SparkleHelper.getPrettifiedNumber(resolution.votesAgainst));

            iconVoteFor.setVisibility(WaVoteStatus.VOTE_FOR.equals(voteStatus) ? View.VISIBLE : View.GONE);
            iconVoteAgainst.setVisibility(WaVoteStatus.VOTE_AGAINST.equals(voteStatus) ? View.VISIBLE : View.GONE);
        }

        /**
         * This formats the topmost TextView with information on the category and resolution target.
         * @param t
         * @param category
         * @param target
         */
        private void setTargetView(TextView t, String category, String target, int repealTarget) {
            String template = context.getString(R.string.wa_nominee_template);
            String[] pair = target.split(":");

            if (pair.length <= 1) {
                if (repealTarget > 0) {
                    StringBuilder linkBuilder = new StringBuilder("<a href=\"");
                    linkBuilder.append(ResolutionActivity.RESOLUTION_TARGET);
                    linkBuilder.append("%d/%d\">%s #%d</a>");
                    String link = String.format(Locale.US, linkBuilder.toString(),
                            councilId, repealTarget - 1,
                            context.getString(prefixId),
                            repealTarget);
                    SparkleHelper.setStyledTextView(context, t, context.getString(R.string.wa_repeal_target, link));
                } else {
                    t.setText(category);
                }
            }
            else {
                switch(pair[0]) {
                    case "N":
                        // If target is a nation, linkify it.
                        String nationTarget = SparkleHelper.getNameFromId(pair[1]);
                        String oldTemplate = String.format(Locale.US, template, category, pair[1]);
                        oldTemplate = SparkleHelper.addExploreActivityLink(oldTemplate, pair[1], nationTarget, ExploreActivity.EXPLORE_NATION);
                        SparkleHelper.setStyledTextView(context, t, oldTemplate);
                        break;
                    case "R":
                        // If target is a nation, linkify it.
                        String regionTarget = SparkleHelper.getNameFromId(pair[1]);
                        String oldRegionTemplate = String.format(Locale.US, template, category, pair[1]);
                        oldRegionTemplate = SparkleHelper.addExploreActivityLink(oldRegionTemplate, pair[1], regionTarget, ExploreActivity.EXPLORE_REGION);
                        SparkleHelper.setStyledTextView(context, t, oldRegionTemplate);
                        break;
                    default:
                        t.setText(String.format(Locale.US, template, category, target));
                        break;
                }
            }
        }
    }

    public class ResolutionContentCard extends ResolutionCard {
        private HtmlTextView content;
        private ImageView voteButtonIcon;
        private View voteButtonDivider;
        private LinearLayout voteButton;
        private TextView voteButtonContent;

        public ResolutionContentCard(View itemView) {
            super(itemView);
            content = (HtmlTextView) itemView.findViewById(R.id.wa_resolution_content);
            voteButtonIcon = (ImageView) itemView.findViewById(R.id.wa_resolution_button_icon);
            voteButtonDivider = itemView.findViewById(R.id.view_divider);
            voteButton = (LinearLayout) itemView.findViewById(R.id.wa_resolution_vote);
            voteButtonContent = (TextView) itemView.findViewById(R.id.wa_resolution_vote_content);
        }

        public void init() {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            SparkleHelper.setStyledTextView(context, content, resolution.content, resolutionActivity.getSupportFragmentManager());

            if (isActive && PinkaHelper.getWaSessionData(context)) {
                voteButton.setVisibility(View.VISIBLE);
                voteButtonDivider.setVisibility(View.VISIBLE);
                final int voteChoice;

                // If voting FOR the resolution
                if (WaVoteStatus.VOTE_FOR.equals(voteStatus)) {
                    voteButtonDivider.setVisibility(View.GONE);
                    voteButtonIcon.setImageResource(R.drawable.ic_wa_white);
                    voteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChart0));
                    voteButtonContent.setTextColor(ContextCompat.getColor(context, R.color.white));
                    voteButtonContent.setText(context.getString(R.string.wa_resolution_vote_for));
                    voteChoice = VoteDialog.VOTE_FOR;
                }
                // If voting AGAINST the resolution
                else if (WaVoteStatus.VOTE_AGAINST.equals(voteStatus)) {
                    voteButtonDivider.setVisibility(View.GONE);
                    voteButtonIcon.setImageResource(R.drawable.ic_wa_white);
                    voteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChart1));
                    voteButtonContent.setTextColor(ContextCompat.getColor(context, R.color.white));
                    voteButtonContent.setText(context.getString(R.string.wa_resolution_vote_against));
                    voteChoice = VoteDialog.VOTE_AGAINST;
                }
                else {
                    voteButtonDivider.setVisibility(View.VISIBLE);
                    switch (SettingsActivity.getTheme(context)) {
                        case SettingsActivity.THEME_VERT:
                            voteButtonIcon.setImageResource(R.drawable.ic_wa_green);
                            break;
                        case SettingsActivity.THEME_NOIR:
                            voteButtonIcon.setImageResource(R.drawable.ic_wa_white);
                            break;
                        case SettingsActivity.THEME_BLEU:
                            voteButtonIcon.setImageResource(R.drawable.ic_wa_blue);
                            break;
                        case SettingsActivity.THEME_ROUGE:
                            voteButtonIcon.setImageResource(R.drawable.ic_wa_red);
                            break;
                        case SettingsActivity.THEME_VIOLET:
                            voteButtonIcon.setImageResource(R.drawable.ic_wa_violet);
                            break;
                    }
                    voteButton.setBackgroundColor(RaraHelper.getThemeCardColour(context));
                    voteButtonContent.setTextColor(RaraHelper.getThemeButtonColour(context));
                    voteButtonContent.setText(context.getString(R.string.wa_resolution_vote_default));
                    voteChoice = VoteDialog.VOTE_UNDECIDED;
                }

                voteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resolutionActivity.showVoteDialog(voteChoice);
                    }
                });
            }
            else {
                voteButtonDivider.setVisibility(View.GONE);
                voteButton.setVisibility(View.GONE);
                voteButton.setOnClickListener(null);
            }
        }
    }

    public class ResolutionBreakdownCard extends ResolutionCard {
        // State constants
        private final int DELEGATE_VOTES_FOR = 0;
        private final int DELEGATE_VOTES_AGAINST = 1;

        // Views
        private PieChart votingBreakdown;
        private TextView nullVote;
        private RelativeLayout nationVotesForHolder;
        private TextView nationVotesFor;
        private ImageView nationVotesForIcon;
        private RelativeLayout nationVotesAgainstHolder;
        private TextView nationVotesAgainst;
        private ImageView nationVotesAgainstIcon;
        private RelativeLayout delegateVotesForButton;
        private TextView delegateVotesFor;
        private RelativeLayout delegateVotesAgainstButton;
        private TextView delegateVotesAgainst;

        public ResolutionBreakdownCard(View itemView) {
            super(itemView);
            votingBreakdown = (PieChart) itemView.findViewById(R.id.wa_voting_breakdown);
            nullVote = (TextView) itemView.findViewById(R.id.resolution_null_vote);
            nationVotesForHolder = (RelativeLayout) itemView.findViewById(R.id.resolution_nations_for_holder);
            nationVotesFor = (TextView) itemView.findViewById(R.id.resolution_nations_for_count);
            nationVotesForIcon = (ImageView) itemView.findViewById(R.id.resolution_nations_for_icon);
            nationVotesAgainstHolder = (RelativeLayout) itemView.findViewById(R.id.resolution_nations_against_holder);
            nationVotesAgainst = (TextView) itemView.findViewById(R.id.resolution_nations_against_count);
            nationVotesAgainstIcon = (ImageView) itemView.findViewById(R.id.resolution_nations_against_icon);
            delegateVotesForButton = (RelativeLayout) itemView.findViewById(R.id.resolution_delegates_for);
            delegateVotesFor = (TextView) itemView.findViewById(R.id.resolution_delegates_for_count);
            delegateVotesAgainstButton = (RelativeLayout) itemView.findViewById(R.id.resolution_delegates_against);
            delegateVotesAgainst = (TextView) itemView.findViewById(R.id.resolution_delegates_against_count);
        }

        public void init() {
            int voteForTotal = resolution.votesFor;
            int voteAgainstTotal = resolution.votesAgainst;
            int voteTotal = voteForTotal + voteAgainstTotal;

            int voteForDelegates = 0;
            if (resolution.delegateVotesFor != null) {
                for (DelegateVote dv : resolution.delegateVotesFor) {
                    voteForDelegates += dv.votes;
                }
            }

            int voteAgainstDelegates = 0;
            if (resolution.delegateVotesAgainst != null) {
                for (DelegateVote dv : resolution.delegateVotesAgainst) {
                    voteAgainstDelegates += dv.votes;
                }
            }

            int voteForNations = voteForTotal - voteForDelegates;
            int voteAgainstNations = voteAgainstTotal - voteAgainstDelegates;

            if (voteTotal > 0) {
                // Make the necessary views visible again
                votingBreakdown.setVisibility(View.VISIBLE);
                nullVote.setVisibility(View.GONE);
                nationVotesForHolder.setVisibility(View.VISIBLE);
                nationVotesAgainstHolder.setVisibility(View.VISIBLE);
                delegateVotesForButton.setVisibility(View.VISIBLE);
                delegateVotesAgainstButton.setVisibility(View.VISIBLE);

                // Calculate percentages
                float votePercentForIndividual = (voteForNations * 100f)/voteTotal;
                float votePercentForDelegates = (voteForDelegates * 100f)/voteTotal;
                float votePercentAgainstIndividual = (voteAgainstNations * 100f)/voteTotal;
                float votePercentAgainstDelegates = (voteAgainstDelegates * 100f)/voteTotal;

                List<PieEntry> chartEntries = new ArrayList<PieEntry>();
                List<Integer> chartColours = new ArrayList<Integer>();

                // Set data
                // It's in this order so that the values that are displayed, from left to right (counter-clockwise) are:
                // [Nations For] [Delegate Votes For] [Delegate Votes Against] [Nations Against]
                // This puts the fors and againsts together and in the order they show up in the rest of the UI
                if (votePercentAgainstIndividual > 0f) {
                    chartEntries.add(new PieEntry(votePercentAgainstIndividual, context.getString(R.string.wa_individual_nations_against_newline)));
                    chartColours.add(ContextCompat.getColor(context, R.color.colorChart1));
                }
                if (votePercentAgainstDelegates > 0f) {
                    chartEntries.add(new PieEntry(votePercentAgainstDelegates, context.getString(R.string.wa_delegate_votes_against_newline)));
                    chartColours.add(ContextCompat.getColor(context, R.color.waDelegateAgainst));
                }
                if (votePercentForDelegates > 0f) {
                    chartEntries.add(new PieEntry(votePercentForDelegates, context.getString(R.string.wa_delegate_votes_for_newline)));
                    chartColours.add(ContextCompat.getColor(context, R.color.waDelegateFor));
                }
                if (votePercentForIndividual > 0f) {
                    chartEntries.add(new PieEntry(votePercentForIndividual, context.getString(R.string.wa_individual_nations_for_newline)));
                    chartColours.add(ContextCompat.getColor(context, R.color.colorChart0));
                }

                // Set colour and disable chart labels
                PieDataSet dataSet = new PieDataSet(chartEntries, "");
                dataSet.setDrawValues(false);
                dataSet.setColors(chartColours);
                PieData dataFull = new PieData(dataSet);

                // Format chart
                votingBreakdown = RaraHelper.getFormattedPieChart(context, votingBreakdown, false);
                votingBreakdown.setData(dataFull);
                votingBreakdown.invalidate();

                // Set nation vote counts and voted icon
                nationVotesFor.setText(SparkleHelper.getPrettifiedNumber(voteForNations));
                nationVotesAgainst.setText(SparkleHelper.getPrettifiedNumber(voteAgainstNations));
                nationVotesForIcon.setVisibility(WaVoteStatus.VOTE_FOR.equals(voteStatus) ? View.VISIBLE : View.GONE);
                nationVotesAgainstIcon.setVisibility(WaVoteStatus.VOTE_AGAINST.equals(voteStatus) ? View.VISIBLE : View.GONE);

                // Set delegate vote counts
                delegateVotesFor.setText(SparkleHelper.getPrettifiedNumber(voteForDelegates));
                delegateVotesForButton.setOnClickListener(getDelegateVotesOnClickListener(DELEGATE_VOTES_FOR, voteForDelegates, resolution.delegateVotesFor));
                delegateVotesAgainst.setText(SparkleHelper.getPrettifiedNumber(voteAgainstDelegates));
                delegateVotesAgainstButton.setOnClickListener(getDelegateVotesOnClickListener(DELEGATE_VOTES_AGAINST, voteAgainstDelegates, resolution.delegateVotesAgainst));
            } else {
                votingBreakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
                nationVotesForHolder.setVisibility(View.GONE);
                nationVotesAgainstHolder.setVisibility(View.GONE);
                delegateVotesForButton.setVisibility(View.GONE);
                delegateVotesForButton.setOnClickListener(null);
                delegateVotesAgainstButton.setVisibility(View.GONE);
                delegateVotesAgainstButton.setOnClickListener(null);
            }
        }

        /**
         * Returns the proper onClickListener for each type of delegate vote button.
         * @param mode
         * @param delegateVotes
         */
        private View.OnClickListener getDelegateVotesOnClickListener(final int mode, final int numVotes, final List<DelegateVote> delegateVotes) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int titleStringResource = mode == DELEGATE_VOTES_FOR ? R.string.wa_delegate_votes_for : R.string.wa_delegate_votes_against;
                    if (numVotes > 0) {
                        NameListDialog nameListDialog = new NameListDialog();
                        nameListDialog.setTitle(context.getString(titleStringResource));
                        nameListDialog.setDelegateVotes(delegateVotes);
                        nameListDialog.show(fragmentManager, NameListDialog.DIALOG_TAG);
                    } else {
                        dialogBuilder
                                .setTitle(context.getString(titleStringResource))
                                .setMessage(context.getString(mode == DELEGATE_VOTES_FOR ? R.string.wa_delegate_no_votes_for : R.string.wa_delegate_no_votes_against))
                                .setPositiveButton(context.getString(R.string.got_it), null)
                                .show();
                    }
                }
            };
        }
    }

    public class ResolutionHistoryCard extends ResolutionCard implements OnChartValueSelectedListener {
        private List<Integer> votesFor;
        private List<Integer> votesAgainst;

        private LineChart votingHistory;
        private TextView voteHistoryFor;
        private TextView voteHistoryAgainst;
        private ImageView histIconVoteFor;
        private ImageView histIconVoteAgainst;

        public ResolutionHistoryCard(View itemView) {
            super(itemView);
            votingHistory = (LineChart) itemView.findViewById(R.id.wa_voting_history);
            voteHistoryFor = (TextView) itemView.findViewById(R.id.wa_vote_history_for);
            voteHistoryAgainst = (TextView) itemView.findViewById(R.id.wa_vote_history_against);
            histIconVoteFor = (ImageView) itemView.findViewById(R.id.history_icon_vote_for);
            histIconVoteAgainst = (ImageView) itemView.findViewById(R.id.history_icon_vote_against);
        }

        public void init() {
            setVotingHistory(resolution.voteHistoryFor, resolution.voteHistoryAgainst);
            voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(resolution.votesFor));
            voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(resolution.votesAgainst));
            histIconVoteFor.setVisibility(WaVoteStatus.VOTE_FOR.equals(voteStatus) ? View.VISIBLE : View.GONE);
            histIconVoteAgainst.setVisibility(WaVoteStatus.VOTE_AGAINST.equals(voteStatus) ? View.VISIBLE : View.GONE);
        }

        /**
         * Initialize the line graph to show voting history
         * @param vF
         * @param vA
         */
        private void setVotingHistory(List<Integer> vF, List<Integer> vA) {
            votesFor = vF;
            votesAgainst = vA;

            final float lineWidth = 2.5f;

            List<Entry> entryFor = new ArrayList<Entry>();
            List<Entry> entryAgainst = new ArrayList<Entry>();

            // Build data
            for (int i=0; i < votesFor.size(); i++) {
                entryFor.add(new Entry(i, votesFor.get(i)));
                entryAgainst.add(new Entry(i, votesAgainst.get(i)));
            }

            // lots of formatting for the FOR and AGAINST lines
            LineDataSet setFor = new LineDataSet(entryFor, context.getString(R.string.wa_for));
            setFor.setAxisDependency(YAxis.AxisDependency.LEFT);
            setFor.setColors(ContextCompat.getColor(context, R.color.colorChart0));
            setFor.setDrawValues(false);
            setFor.setDrawVerticalHighlightIndicator(true);
            setFor.setDrawHorizontalHighlightIndicator(false);
            setFor.setHighLightColor(RaraHelper.getThemeButtonColour(context));
            setFor.setHighlightLineWidth(lineWidth);
            setFor.setDrawCircles(false);
            setFor.setLineWidth(lineWidth);

            LineDataSet setAgainst = new LineDataSet(entryAgainst, context.getString(R.string.wa_against));
            setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
            setAgainst.setColors(ContextCompat.getColor(context, R.color.colorChart1));
            setAgainst.setDrawValues(false);
            setAgainst.setDrawVerticalHighlightIndicator(true);
            setAgainst.setDrawHorizontalHighlightIndicator(false);
            setAgainst.setHighLightColor(RaraHelper.getThemeButtonColour(context));
            setAgainst.setHighlightLineWidth(lineWidth);
            setAgainst.setDrawCircles(false);
            setAgainst.setLineWidth(lineWidth);

            // Match data with x-axis labels
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setFor);
            dataSets.add(setAgainst);

            LineData data = new LineData(dataSets);
            List<String> xLabels = new ArrayList<String>();
            for (int i=0; i < votesFor.size(); i++) {
                // Only add labels for each day
                if (i%24 == 0) {
                    xLabels.add(String.format(Locale.US, context.getString(R.string.wa_x_axis_d), (i/24)+1));
                } else {
                    xLabels.add(String.format(Locale.US, context.getString(R.string.wa_x_axis_h), i));
                }
            }

            // formatting
            votingHistory = RaraHelper.getFormattedLineChart(context, votingHistory, this, xLabels, true, 24, false);

            votingHistory.setData(data);
            votingHistory.invalidate();
        }

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get((int) e.getX())));
            voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get((int) e.getX())));
        }

        @Override
        public void onNothingSelected() {
            voteHistoryFor.setText(SparkleHelper.getPrettifiedNumber(votesFor.get(votesFor.size()-1)));
            voteHistoryAgainst.setText(SparkleHelper.getPrettifiedNumber(votesAgainst.get(votesAgainst.size()-1)));
        }
    }
}
