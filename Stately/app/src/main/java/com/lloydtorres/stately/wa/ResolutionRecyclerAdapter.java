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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    private int councilId;
    private int prefixId;
    private boolean isActive;

    public ResolutionRecyclerAdapter(ResolutionActivity activity, Resolution res, String vs, int cId) {
        resolutionActivity = activity;
        context = resolutionActivity;
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
        @BindView(R.id.wa_resolution_title)
        TextView title;
        @BindView(R.id.wa_nominee)
        TextView target;
        @BindView(R.id.wa_proposed_by)
        TextView proposedBy;
        @BindView(R.id.wa_activetime)
        TextView voteStart;
        @BindView(R.id.wa_repealed)
        TextView repealed;
        @BindView(R.id.wa_resolution_for)
        TextView votesFor;
        @BindView(R.id.wa_resolution_against)
        TextView votesAgainst;
        @BindView(R.id.content_icon_vote_for)
        ImageView iconVoteFor;
        @BindView(R.id.content_icon_vote_against)
        ImageView iconVoteAgainst;

        public ResolutionHeaderCard(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void init() {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            title.setText(resolution.name);
            setTargetView(target, resolution.category, resolution.target, resolution.repealTarget);

            String proposer = SparkleHelper.getNameFromId(resolution.proposedBy);
            String proposeTemplate = String.format(Locale.US, context.getString(R.string.wa_proposed), resolution.proposedBy);
            SparkleHelper.activityLinkBuilder(context, proposedBy, proposeTemplate, resolution.proposedBy, proposer, ExploreActivity.EXPLORE_NATION);

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
                }
            }

            votesFor.setText(SparkleHelper.getPrettifiedNumber(resolution.votesFor));
            votesAgainst.setText(SparkleHelper.getPrettifiedNumber(resolution.votesAgainst));

            iconVoteFor.setVisibility(WaVoteStatus.VOTE_FOR.equals(voteStatus) ? View.VISIBLE : View.GONE);
            iconVoteAgainst.setVisibility(WaVoteStatus.VOTE_AGAINST.equals(voteStatus) ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.wa_repealed)
        public void onClickRepealed() {
            Intent resolutionActivityIntent = new Intent(context, ResolutionActivity.class);
            resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, councilId);
            resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_OVERRIDE_RES_ID, resolution.repealed);
            context.startActivity(resolutionActivityIntent);
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
        private int voteChoice;

        @BindView(R.id.wa_resolution_content)
        HtmlTextView content;
        @BindView(R.id.wa_resolution_button_icon)
        ImageView voteButtonIcon;
        @BindView(R.id.view_divider)
        View voteButtonDivider;
        @BindView(R.id.wa_resolution_vote)
        LinearLayout voteButton;
        @BindView(R.id.wa_resolution_vote_content)
        TextView voteButtonContent;

        public ResolutionContentCard(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void init() {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            SparkleHelper.setBbCodeFormatting(context, content, resolution.content, resolutionActivity.getSupportFragmentManager());

            if (isActive && PinkaHelper.getWaSessionData(context)) {
                voteButton.setVisibility(View.VISIBLE);
                voteButtonDivider.setVisibility(View.VISIBLE);

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
            }
            else {
                voteButtonDivider.setVisibility(View.GONE);
                voteButton.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.wa_resolution_vote)
        public void onClickVoteButton() {
            resolutionActivity.showVoteDialog(voteChoice);
        }
    }

    public class ResolutionBreakdownCard extends ResolutionCard {
        @BindView(R.id.wa_voting_breakdown)
        PieChart votingBreakdown;
        @BindView(R.id.resolution_null_vote)
        TextView nullVote;

        public ResolutionBreakdownCard(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void init() {
            if (!RaraHelper.getWaVotingChart(context, votingBreakdown, resolution.votesFor, resolution.votesAgainst)) {
                votingBreakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }
    }

    public class ResolutionHistoryCard extends ResolutionCard implements OnChartValueSelectedListener {
        private List<Integer> votesFor;
        private List<Integer> votesAgainst;

        @BindView(R.id.wa_voting_history)
        LineChart votingHistory;
        @BindView(R.id.wa_vote_history_for)
        TextView voteHistoryFor;
        @BindView(R.id.wa_vote_history_against)
        TextView voteHistoryAgainst;
        @BindView(R.id.history_icon_vote_for)
        ImageView histIconVoteFor;
        @BindView(R.id.history_icon_vote_against)
        ImageView histIconVoteAgainst;

        public ResolutionHistoryCard(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
            setFor.setColors(RaraHelper.waColourFor, context);
            setFor.setDrawValues(false);
            setFor.setDrawVerticalHighlightIndicator(true);
            setFor.setDrawHorizontalHighlightIndicator(false);
            setFor.setHighLightColor(RaraHelper.getThemeButtonColour(context));
            setFor.setHighlightLineWidth(lineWidth);
            setFor.setDrawCircles(false);
            setFor.setLineWidth(lineWidth);

            LineDataSet setAgainst = new LineDataSet(entryAgainst, context.getString(R.string.wa_against));
            setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
            setAgainst.setColors(RaraHelper.waColourAgainst, context);
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
