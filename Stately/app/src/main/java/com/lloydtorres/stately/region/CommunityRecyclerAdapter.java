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

package com.lloydtorres.stately.region;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.EmbassyHolder;
import com.lloydtorres.stately.dto.Officer;
import com.lloydtorres.stately.dto.OfficerHolder;
import com.lloydtorres.stately.dto.Poll;
import com.lloydtorres.stately.dto.PollOption;
import com.lloydtorres.stately.dto.RMBButtonHolder;
import com.lloydtorres.stately.dto.WaVote;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.helpers.links.NameListSpan;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.atteo.evo.inflector.English;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-24.
 * This is the RecyclerView adapter for the Region community subfragment.
 */
public class CommunityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    public static final int BUTTON_CARD = 0;
    public static final int POLL_CARD = 1;
    public static final int WA_CARD = 2;
    public static final int OFFICER_CARD = 3;
    public static final int EMBASSY_CARD = 4;

    private RegionCommunitySubFragment fragment;
    private Context context;
    private FragmentManager fm;
    private List<Parcelable> cards;
    private String regionName;

    public CommunityRecyclerAdapter(RegionCommunitySubFragment frag, List<Parcelable> crds, String n) {
        fragment = frag;
        context = fragment.getContext();
        fm = fragment.getFragmentManager();
        regionName = n;

        setCards(crds);
    }

    public void setCards(List<Parcelable> crds) {
        cards = crds;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case BUTTON_CARD:
                View rmbCard = inflater.inflate(R.layout.card_button, parent, false);
                viewHolder = new MessageBoardCard(rmbCard);
                break;
            case POLL_CARD:
                View pollCard = inflater.inflate(R.layout.card_poll, parent, false);
                viewHolder = new PollCard(pollCard);
                break;
            case WA_CARD:
                View waCard = inflater.inflate(R.layout.card_region_wa, parent, false);
                viewHolder = new RegionWaCard(waCard);
                break;
            case OFFICER_CARD:
                View officerCard = inflater.inflate(R.layout.card_officers, parent, false);
                viewHolder = new OfficerCard(officerCard);
                break;
            case EMBASSY_CARD:
                View embassyCard = inflater.inflate(R.layout.card_embassies, parent, false);
                viewHolder = new EmbassyCard(embassyCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case BUTTON_CARD:
                MessageBoardCard rmbCard = (MessageBoardCard) holder;
                rmbCard.init((RMBButtonHolder) cards.get(position));
                break;
            case POLL_CARD:
                PollCard pollCard = (PollCard) holder;
                pollCard.init((Poll) cards.get(position));
                break;
            case WA_CARD:
                RegionWaCard waVoteCard = (RegionWaCard) holder;
                waVoteCard.init((WaVote) cards.get(position));
                break;
            case OFFICER_CARD:
                OfficerCard officerCard = (OfficerCard) holder;
                officerCard.init((OfficerHolder) cards.get(position));
                break;
            case EMBASSY_CARD:
                EmbassyCard embassyCard = (EmbassyCard) holder;
                embassyCard.init((EmbassyHolder) cards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof RMBButtonHolder) {
            return BUTTON_CARD;
        }
        else if (cards.get(position) instanceof Poll) {
            return POLL_CARD;
        }
        else if (cards.get(position) instanceof WaVote) {
            return WA_CARD;
        }
        else if (cards.get(position) instanceof OfficerHolder) {
            return OFFICER_CARD;
        }
        else if (cards.get(position) instanceof EmbassyHolder) {
            return EMBASSY_CARD;
        }
        return -1;
    }

    /**
     * Updates the poll card in the adapter list if it exists.
     * @param p New poll data.
     */
    public void updatePoll(Poll p) {
        for (int i=0; i<cards.size(); i++) {
            if (cards.get(i) instanceof Poll) {
                cards.set(i, p);
                notifyItemChanged(i);
                break;
            }
        }
    }

    // Card viewholders

    // Card for the RMB button
    public class MessageBoardCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView unreadCounter;
        private RMBButtonHolder buttonData;

        public MessageBoardCard(View v) {
            super(v);
            unreadCounter = (TextView) v.findViewById(R.id.card_button_num);
            v.setOnClickListener(this);
        }

        public void init(RMBButtonHolder bh) {
            buttonData = bh;
            if (bh.unreadCount != null && bh.unreadCount.length() > 0) {
                unreadCounter.setVisibility(View.VISIBLE);
                unreadCounter.setText(bh.unreadCount);
            }
            else {
                unreadCounter.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            unreadCounter.setVisibility(View.INVISIBLE);
            buttonData.unreadCount = null;
            notifyItemChanged(getAdapterPosition());

            Intent messageBoardActivity = new Intent(context, MessageBoardActivity.class);
            messageBoardActivity.putExtra(MessageBoardActivity.BOARD_REGION_NAME, buttonData.regionName);
            context.startActivity(messageBoardActivity);
        }
    }

    // Card for the region poll
    public class PollCard extends RecyclerView.ViewHolder {

        private TextView question;
        private HtmlTextView content;
        private TextView author;
        private TextView open;
        private TextView close;
        private LinearLayout options;
        private PieChart breakdown;
        private TextView nullVote;
        private View divider;
        private LinearLayout voteButton;
        private TextView voteButtonContent;

        public PollCard(View v) {
            super(v);
            question = (TextView) v.findViewById(R.id.card_region_poll_question);
            content = (HtmlTextView) v.findViewById(R.id.card_region_poll_content);
            author = (TextView) v.findViewById(R.id.card_region_poll_author);
            open = (TextView) v.findViewById(R.id.card_region_poll_open);
            close = (TextView) v.findViewById(R.id.card_region_poll_close);
            options = (LinearLayout) v.findViewById(R.id.card_region_poll_options);
            breakdown = (PieChart) v.findViewById(R.id.card_region_poll_chart);
            nullVote = (TextView) v.findViewById(R.id.region_poll_null_vote);
            divider = v.findViewById(R.id.view_divider);
            voteButton = (LinearLayout) v.findViewById(R.id.card_region_poll_vote_button);
            voteButtonContent = (TextView) v.findViewById(R.id.card_region_poll_vote_button_content);
        }

        public void init(final Poll p) {
            question.setText(SparkleHelper.getHtmlFormatting(p.title));
            SparkleHelper.setHappeningsFormatting(context, author,
                    String.format(Locale.US, context.getString(R.string.poll_author), p.author));
            open.setText(String.format(Locale.US, context.getString(R.string.poll_open),
                    SparkleHelper.getReadableDateFromUTC(context, p.startTime)));
            close.setText(String.format(Locale.US, context.getString(R.string.poll_close),
                    SparkleHelper.getReadableDateFromUTC(context, p.stopTime)));

            if (p.text != null && p.text.length() > 0) {
                SparkleHelper.setBbCodeFormatting(context, content, p.text, fm);
            }
            else {
                content.setVisibility(View.GONE);
            }

            List<PollOption> results = p.options;
            Collections.sort(results);

            options.removeAllViews();
            int voteTotal = 0;
            for (int i=0; i<results.size(); i++) {
                voteTotal += results.get(i).votes;
            }

            if (voteTotal > 0) {
                breakdown.setVisibility(View.VISIBLE);
                nullVote.setVisibility(View.GONE);

                List<PieEntry> chartEntries = new ArrayList<PieEntry>();
                for (int i=0; i<results.size(); i++) {
                    inflateOption(options, i+1, results.get(i).text, results.get(i).votes, results.get(i).voters, i==p.votedOption);
                    chartEntries.add(new PieEntry((results.get(i).votes * 100f)/voteTotal, String.format(Locale.US, context.getString(R.string.region_option_index), i+1)));
                }

                PieDataSet dataSet = new PieDataSet(chartEntries, "");
                dataSet.setDrawValues(false);
                dataSet.setColors(RaraHelper.chartColours, context);
                PieData dataFull = new PieData(dataSet);

                breakdown = RaraHelper.getFormattedPieChart(context, breakdown);
                breakdown.setData(dataFull);
                breakdown.invalidate();
            } else {
                breakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }

            if (p.isVotingEnabled) {
                divider.setVisibility(View.VISIBLE);
                voteButton.setVisibility(View.VISIBLE);
                voteButtonContent.setText(context.getString(p.votedOption == Poll.NO_VOTE ? R.string.poll_vote_button_submit : R.string.poll_vote_button_change));
                voteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.showPollVoteDialog(p);
                    }
                });
            } else {
                divider.setVisibility(View.GONE);
                voteButton.setVisibility(View.GONE);
                voteButton.setOnClickListener(null);
            }
        }

        private void inflateOption(LinearLayout optionLayout, int index, String option, int votes, String voters, boolean votedOption) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View optionView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = (TextView) optionView.findViewById(R.id.cardentry_label);
            TextView content = (TextView) optionView.findViewById(R.id.cardentry_content);
            label.setText(String.format(Locale.US, context.getString(R.string.region_option_index), index));
            content.setText(String.format(Locale.US, context.getString(R.string.poll_votes_template_start), SparkleHelper.getHtmlFormatting(option)));

            Spannable template = new SpannableString(String.format(Locale.US, context.getString(R.string.poll_votes_template_votes), votes, English.plural(context.getString(R.string.region_filler_vote), votes)));
            if (votes > 0) {
                String[] rawVoters = voters.split(":");
                final ArrayList<String> properVoters = new ArrayList<String>();
                for (String v : rawVoters) {
                    properVoters.add(SparkleHelper.getNameFromId(v));
                }
                NameListSpan span = new NameListSpan(context, fm,
                        String.format(Locale.US, context.getString(R.string.poll_votes_voter_dialog), option),
                        properVoters, ExploreActivity.EXPLORE_NATION);
                template.setSpan(span, 0, template.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                content.setMovementMethod(LinkMovementMethod.getInstance());
            }
            content.append(template);
            content.append(context.getString(R.string.poll_votes_template_end));

            if (votedOption) {
                content.setTypeface(null, Typeface.BOLD);
            }

            optionLayout.addView(optionView);
        }
    }

    // Card for the WA poll
    public class RegionWaCard extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView filler;
        private PieChart chart;
        private LinearLayout resolutionLink;
        private TextView linkContent;
        private TextView nullVote;

        public RegionWaCard(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.region_wa_title);
            filler = (TextView) v.findViewById(R.id.region_wa_vote_filler);
            chart = (PieChart) v.findViewById(R.id.region_wa_breakdown);
            resolutionLink = (LinearLayout) v.findViewById(R.id.region_wa_link);
            linkContent = (TextView) v.findViewById(R.id.region_wa_link_text);
            nullVote = (TextView) v.findViewById(R.id.region_wa_null_vote);
        }

        public void init(WaVote w) {
            // Setup resolution link
            Intent resolutionActivityLaunch = new Intent(context, ResolutionActivity.class);
            resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, w.chamber);
            final Intent fResolution = resolutionActivityLaunch;
            resolutionLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(fResolution);
                }
            });

            String chamberName = "";
            switch(w.chamber) {
                case Assembly.GENERAL_ASSEMBLY:
                    chamberName = context.getString(R.string.wa_general_assembly);
                    break;
                default:
                    chamberName = context.getString(R.string.wa_security_council);
                    break;
            }

            title.setText(String.format(Locale.US, context.getString(R.string.card_region_wa_vote), chamberName));
            linkContent.setText(String.format(Locale.US, context.getString(R.string.card_region_wa_link), chamberName));

            filler.setText(String.format(Locale.US, context.getString(R.string.region_wa_filler), w.voteFor, w.voteAgainst));
            if (!RaraHelper.getWaVotingChart(context, chart, w.voteFor, w.voteAgainst)) {
                chart.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }
    }

    public class OfficerCard extends RecyclerView.ViewHolder {

        private LayoutInflater inflater;
        private LinearLayout officersLayout;
        private TextView noOfficers;

        public OfficerCard(View itemView) {
            super(itemView);
            inflater = LayoutInflater.from(context);
            officersLayout = (LinearLayout) itemView.findViewById(R.id.card_region_officers_layout);
            noOfficers = (TextView) itemView.findViewById(R.id.governance_none);
        }

        public void init(OfficerHolder offhold) {
            List<Officer> officers = offhold.officers;

            officersLayout.removeAllViews();
            if (officers.size() <= 0) {
                noOfficers.setText(String.format(Locale.US, context.getString(R.string.region_filler_no_officers), regionName));
                noOfficers.setVisibility(View.VISIBLE);
                officersLayout.setVisibility(View.GONE);
            }
            else {
                officersLayout.setVisibility(View.VISIBLE);
                for (int i=0; i<officers.size(); i++) {
                    if (officers.get(i).office != null && officers.get(i).name != null) {
                        inflateOfficerEntry(officersLayout, officers.get(i).office, officers.get(i).name);
                    }
                }
            }
        }

        private void inflateOfficerEntry(LinearLayout officersLayout, String position, String nation) {
            View delegateView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = (TextView) delegateView.findViewById(R.id.cardentry_label);
            TextView content = (TextView) delegateView.findViewById(R.id.cardentry_content);
            label.setText(SparkleHelper.getHtmlFormatting(position));
            SparkleHelper.activityLinkBuilder(context, content, nation, nation, SparkleHelper.getNameFromId(nation), ExploreActivity.EXPLORE_NATION);
            officersLayout.addView(delegateView);
        }
    }

    public class EmbassyCard extends RecyclerView.ViewHolder {

        private CardView embassyCard;
        private TextView embassyNum;

        public EmbassyCard(View itemView) {
            super(itemView);
            embassyCard = (CardView) itemView.findViewById(R.id.card_region_embassies);
            embassyNum = (TextView) itemView.findViewById(R.id.card_region_embassies_num);
        }

        public void init(EmbassyHolder embhold) {
            ArrayList<String> embassyList = embhold.embassies;

            embassyNum.setText(String.valueOf(embassyList.size()));
            final ArrayList<String> embassies = embassyList;
            embassyCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NameListDialog nameListDialog = new NameListDialog();
                    nameListDialog.setTitle(context.getString(R.string.card_region_embassies));
                    nameListDialog.setNames(embassies);
                    nameListDialog.setTarget(ExploreActivity.EXPLORE_REGION);
                    nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                }
            });
        }
    }
}
