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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
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
    public static final int CARD_BREAKDOWN = 2;
    public static final int CARD_HISTORY = 3;

    private ResolutionActivity resolutionActivity;
    private Context context;
    private Resolution resolution;
    private String voteStatus;
    private boolean isActive;

    public ResolutionRecyclerAdapter(ResolutionActivity activity, Resolution res, String vs) {
        resolutionActivity = activity;
        context = resolutionActivity.getApplicationContext();
        resolution = res;
        voteStatus = vs;
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
            votesFor = (TextView) itemView.findViewById(R.id.wa_resolution_for);
            votesAgainst = (TextView) itemView.findViewById(R.id.wa_resolution_against);
            iconVoteFor = (ImageView) itemView.findViewById(R.id.content_icon_vote_for);
            iconVoteAgainst = (ImageView) itemView.findViewById(R.id.content_icon_vote_against);
        }

        public void init() {
            title.setText(resolution.name);
            setTargetView(target, resolution.category, resolution.target);

            String proposer = SparkleHelper.getNameFromId(resolution.proposedBy);
            String proposeTemplate = String.format(Locale.US, context.getString(R.string.wa_proposed), resolution.proposedBy);
            SparkleHelper.activityLinkBuilder(context, proposedBy, proposeTemplate, resolution.proposedBy, proposer, ExploreActivity.EXPLORE_NATION);

            if (isActive) {
                voteStart.setText(String.format(Locale.US, context.getString(R.string.wa_voting_time), SparkleHelper.calculateResolutionEnd(context, resolution.voteHistoryFor.size())));
            } else {
                voteStart.setText(String.format(Locale.US, context.getString(R.string.wa_implemented), SparkleHelper.getReadableDateFromUTC(context, resolution.implemented)));
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
        private void setTargetView(TextView t, String category, String target) {
            String template = context.getString(R.string.wa_nominee_template);
            String[] pair = target.split(":");

            if (pair.length <= 1) {
                t.setText(category);
            }
            else {
                switch(pair[0]) {
                    case "N":
                        // If target is a nation, linkify it.
                        String nationTarget = SparkleHelper.getNameFromId(pair[1]);
                        String oldTemplate = String.format(Locale.US, template, category, pair[1]);
                        SparkleHelper.activityLinkBuilder(context, t, oldTemplate, pair[1], nationTarget, ExploreActivity.EXPLORE_NATION);
                        break;
                    case "R":
                        // If target is a nation, linkify it.
                        String regionTarget = SparkleHelper.getNameFromId(pair[1]);
                        String oldRegionTemplate = String.format(Locale.US, template, category, pair[1]);
                        SparkleHelper.activityLinkBuilder(context, t, oldRegionTemplate, pair[1], regionTarget, ExploreActivity.EXPLORE_REGION);
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
            SparkleHelper.setBbCodeFormatting(context, content, resolution.content, resolutionActivity.getSupportFragmentManager());

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
        private PieChart votingBreakdown;
        private TextView nullVote;

        public ResolutionBreakdownCard(View itemView) {
            super(itemView);
            votingBreakdown = (PieChart) itemView.findViewById(R.id.wa_voting_breakdown);
            nullVote = (TextView) itemView.findViewById(R.id.resolution_null_vote);
        }

        public void init() {
            if (!SparkleHelper.getWaVotingChart(context, votingBreakdown, resolution.votesFor, resolution.votesAgainst)) {
                votingBreakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }
    }

    public class ResolutionHistoryCard extends ResolutionCard {
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
            histIconVoteFor.setVisibility(WaVoteStatus.VOTE_FOR.equals(voteStatus) ? View.VISIBLE : View.GONE);
            histIconVoteAgainst.setVisibility(WaVoteStatus.VOTE_AGAINST.equals(voteStatus) ? View.VISIBLE : View.GONE);
        }

        /**
         * Initialize the line graph to show voting history
         * @param votesFor
         * @param votesAgainst
         */
        private void setVotingHistory(List<Integer> votesFor, List<Integer> votesAgainst) {
            final float lineWidth = 2.5f;

            List<Entry> entryFor = new ArrayList<Entry>();
            List<Entry> entryAgainst = new ArrayList<Entry>();

            // Build data
            for (int i=0; i < votesFor.size(); i++) {
                entryFor.add(new Entry(votesFor.get(i), i));
                entryAgainst.add(new Entry(votesAgainst.get(i), i));
            }

            // lots of formatting for the FOR and AGAINST lines
            LineDataSet setFor = new LineDataSet(entryFor, context.getString(R.string.wa_for));
            setFor.setAxisDependency(YAxis.AxisDependency.LEFT);
            setFor.setColors(SparkleHelper.waColourFor, context);
            setFor.setDrawValues(false);
            setFor.setDrawVerticalHighlightIndicator(true);
            setFor.setDrawHorizontalHighlightIndicator(false);
            setFor.setHighLightColor(RaraHelper.getThemeButtonColour(context));
            setFor.setHighlightLineWidth(lineWidth);
            setFor.setDrawCircles(false);
            setFor.setLineWidth(lineWidth);

            LineDataSet setAgainst = new LineDataSet(entryAgainst, context.getString(R.string.wa_against));
            setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
            setAgainst.setColors(SparkleHelper.waColourAgainst, context);
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

            List<String> xLabels = new ArrayList<String>();
            for (int i=0; i < votesFor.size(); i++) {
                // Only add labels for each day
                if (i%24 == 0) {
                    xLabels.add(String.format(Locale.US, context.getString(R.string.wa_x_axis_d), (i/24)+1));
                }
                else {
                    xLabels.add(String.format(Locale.US, context.getString(R.string.wa_x_axis_h), i));
                }
            }
            LineData data = new LineData(xLabels, dataSets);

            // formatting
            votingHistory = SparkleHelper.getFormattedLineChart(context, votingHistory,
                    new VotingHistoryChartListener(voteHistoryFor, voteHistoryAgainst, votesFor, votesAgainst),
                    true, 23, false);

            votingHistory.setData(data);
            votingHistory.invalidate();
        }
    }
}
