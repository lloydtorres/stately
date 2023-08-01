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
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private final RegionCommunitySubFragment fragment;
    private final Context context;
    private final FragmentManager fm;
    private List<Parcelable> cards;
    private final String regionName;

    public CommunityRecyclerAdapter(RegionCommunitySubFragment frag, List<Parcelable> crds,
                                    String n) {
        fragment = frag;
        context = fragment.getContext();
        fm = fragment.getParentFragmentManager();
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
        } else if (cards.get(position) instanceof Poll) {
            return POLL_CARD;
        } else if (cards.get(position) instanceof WaVote) {
            return WA_CARD;
        } else if (cards.get(position) instanceof OfficerHolder) {
            return OFFICER_CARD;
        } else if (cards.get(position) instanceof EmbassyHolder) {
            return EMBASSY_CARD;
        }
        return -1;
    }

    /**
     * Updates the poll card in the adapter list if it exists.
     * @param p New poll data.
     */
    public void updatePoll(Poll p) {
        for (int i = 0; i < cards.size(); i++) {
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

        private final TextView unreadCounter;
        private RMBButtonHolder buttonData;

        public MessageBoardCard(View v) {
            super(v);
            unreadCounter = v.findViewById(R.id.card_button_num);
            v.setOnClickListener(this);
        }

        public void init(RMBButtonHolder bh) {
            buttonData = bh;
            if (bh.unreadCount != null && bh.unreadCount.length() > 0) {
                unreadCounter.setVisibility(View.VISIBLE);
                unreadCounter.setText(bh.unreadCount);
            } else {
                unreadCounter.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            unreadCounter.setVisibility(View.INVISIBLE);
            buttonData.unreadCount = null;
            notifyItemChanged(getAdapterPosition());
            SparkleHelper.startRegionRMB(context, buttonData.regionName);
        }
    }

    // Card for the region poll
    public class PollCard extends RecyclerView.ViewHolder {

        private static final String POLL_OPTION_FRAGMENT_START = "%s (";
        private static final String POLL_OPTION_FRAGMENT_VOTES = "%s %s";
        private static final String POLL_OPTION_FRAGMENT_END = ")";
        private final TextView question;
        private final HtmlTextView content;
        private final TextView author;
        private final TextView open;
        private final TextView close;
        private final LinearLayout options;
        private PieChart breakdown;
        private final TextView nullVote;
        private final View divider;
        private final LinearLayout voteButton;
        private final ImageView voteButtonIcon;
        private final ProgressBar voteButtonProgress;
        private final TextView voteButtonContent;

        public PollCard(View v) {
            super(v);
            question = v.findViewById(R.id.card_region_poll_question);
            content = v.findViewById(R.id.card_region_poll_content);
            author = v.findViewById(R.id.card_region_poll_author);
            open = v.findViewById(R.id.card_region_poll_open);
            close = v.findViewById(R.id.card_region_poll_close);
            options = v.findViewById(R.id.card_region_poll_options);
            breakdown = v.findViewById(R.id.card_region_poll_chart);
            nullVote = v.findViewById(R.id.region_poll_null_vote);
            divider = v.findViewById(R.id.view_divider);
            voteButton = v.findViewById(R.id.card_region_poll_vote_button);
            voteButtonIcon = v.findViewById(R.id.card_region_poll_icon);
            voteButtonProgress = v.findViewById(R.id.card_region_poll_progressbar);
            voteButtonContent = v.findViewById(R.id.card_region_poll_vote_button_content);
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
                SparkleHelper.setStyledTextView(context, content, p.text, fm);
            } else {
                content.setVisibility(View.GONE);
            }

            List<PollOption> results = p.options;
            Collections.sort(results);

            // Clear then add vote options
            options.removeAllViews();
            for (int i = 0; i < results.size(); i++) {
                inflateOption(options, i + 1, results.get(i).text, results.get(i).votes,
                        results.get(i).voters, i == p.votedOption);
            }

            int voteTotal = 0;
            for (int i = 0; i < results.size(); i++) {
                voteTotal += results.get(i).votes;
            }
            if (voteTotal > 0) {
                breakdown.setVisibility(View.VISIBLE);
                nullVote.setVisibility(View.GONE);

                List<PieEntry> chartEntries = new ArrayList<PieEntry>();
                for (int i = 0; i < results.size(); i++) {
                    chartEntries.add(new PieEntry((results.get(i).votes * 100f) / voteTotal,
                            String.format(Locale.US,
                                    context.getString(R.string.region_option_index), i + 1)));
                }

                PieDataSet dataSet = new PieDataSet(chartEntries, "");
                dataSet.setDrawValues(false);
                dataSet.setColors(RaraHelper.chartColours, context);
                PieData dataFull = new PieData(dataSet);

                breakdown = RaraHelper.getFormattedPieChart(context, breakdown, true);
                breakdown.setData(dataFull);
                breakdown.invalidate();
            } else {
                breakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }

            if (p.isVotingEnabled) {
                divider.setVisibility(View.VISIBLE);
                voteButton.setVisibility(View.VISIBLE);
                voteButtonContent.setText(context.getString(p.votedOption == Poll.NO_VOTE ?
                        R.string.poll_vote_button_submit : R.string.poll_vote_button_change));
                voteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.showPollVoteDialog(p, PollCard.this);
                    }
                });

                // Set default visibilities
                voteButtonIcon.setVisibility(View.VISIBLE);
                voteButtonProgress.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.GONE);
                voteButton.setVisibility(View.GONE);
                voteButton.setOnClickListener(null);
            }
        }

        public void setIsLoading(boolean isLoading) {
            voteButtonIcon.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            voteButtonProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        private void inflateOption(LinearLayout optionLayout, int index, String option, int votes,
                                   String voters, boolean votedOption) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View optionView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = optionView.findViewById(R.id.cardentry_label);
            TextView content = optionView.findViewById(R.id.cardentry_content);
            label.setText(String.format(Locale.US,
                    context.getString(R.string.region_option_index), index));
            content.setText(String.format(Locale.US, POLL_OPTION_FRAGMENT_START,
                    SparkleHelper.getHtmlFormatting(option)));

            Spannable template = new SpannableString(String.format(Locale.US,
                    POLL_OPTION_FRAGMENT_VOTES, SparkleHelper.getPrettifiedNumber(votes),
                    context.getResources().getQuantityString(R.plurals.vote, votes)));
            if (votes > 0) {
                String[] rawVoters = voters.split(":");
                final ArrayList<String> properVoters = new ArrayList<String>();
                for (String v : rawVoters) {
                    properVoters.add(SparkleHelper.getNameFromId(v));
                }
                NameListSpan span = new NameListSpan(context, fm,
                        String.format(Locale.US,
                                context.getString(R.string.poll_votes_voter_dialog), option),
                        properVoters, ExploreActivity.EXPLORE_NATION);
                template.setSpan(span, 0, template.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                content.setMovementMethod(LinkMovementMethod.getInstance());
                content.setLongClickable(false);
            }
            content.append(template);
            content.append(POLL_OPTION_FRAGMENT_END);

            if (votedOption) {
                content.setTypeface(null, Typeface.BOLD);
            }

            optionLayout.addView(optionView);
        }
    }

    // Card for the WA poll
    public class RegionWaCard extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView votesFor;
        private final TextView votesAgainst;
        private final LinearLayout resolutionLink;
        private final TextView linkContent;

        public RegionWaCard(View v) {
            super(v);
            title = v.findViewById(R.id.region_wa_title);
            votesFor = v.findViewById(R.id.card_region_votes_for);
            votesAgainst = v.findViewById(R.id.card_region_votes_against);
            resolutionLink = v.findViewById(R.id.region_wa_link);
            linkContent = v.findViewById(R.id.region_wa_link_text);
        }

        public void init(final WaVote w) {
            // Setup resolution link
            resolutionLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SparkleHelper.startResolution(context, w.chamber, null);
                }
            });

            String chamberName = "";
            if (w.chamber == Assembly.GENERAL_ASSEMBLY) {
                chamberName = context.getString(R.string.wa_general_assembly);
            } else {
                chamberName = context.getString(R.string.wa_security_council);
            }

            title.setText(String.format(Locale.US,
                    context.getString(R.string.card_region_wa_vote), chamberName));
            linkContent.setText(String.format(Locale.US,
                    context.getString(R.string.card_region_wa_link), chamberName));

            votesFor.setText(SparkleHelper.getPrettifiedNumber(w.voteFor));
            votesAgainst.setText(SparkleHelper.getPrettifiedNumber(w.voteAgainst));
        }
    }

    public class OfficerCard extends RecyclerView.ViewHolder {

        private final LayoutInflater inflater;
        private final LinearLayout officersLayout;
        private final TextView noOfficers;

        public OfficerCard(View itemView) {
            super(itemView);
            inflater = LayoutInflater.from(context);
            officersLayout = itemView.findViewById(R.id.card_region_officers_layout);
            noOfficers = itemView.findViewById(R.id.governance_none);
        }

        public void init(OfficerHolder offhold) {
            List<Officer> officers = offhold.officers;

            officersLayout.removeAllViews();
            if (officers.size() <= 0) {
                noOfficers.setText(String.format(Locale.US,
                        context.getString(R.string.region_filler_no_officers), regionName));
                noOfficers.setVisibility(View.VISIBLE);
                officersLayout.setVisibility(View.GONE);
            } else {
                officersLayout.setVisibility(View.VISIBLE);
                Map<String, String> officerMap = new LinkedHashMap<String, String>();
                for (int i = 0; i < officers.size(); i++) {
                    String nationName = officers.get(i).name;
                    String officeName = officers.get(i).office;
                    if (nationName != null && officeName != null) {
                        if (officerMap.get(nationName) == null) {
                            officerMap.put(nationName, officeName);
                        } else {
                            officerMap.put(nationName,
                                    officerMap.get(nationName) + " / " + officeName);
                        }
                    }
                }

                for (String nationName : officerMap.keySet()) {
                    inflateOfficerEntry(officersLayout, officerMap.get(nationName), nationName);
                }
            }
        }

        private void inflateOfficerEntry(LinearLayout officersLayout, String position,
                                         String nation) {
            View delegateView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = delegateView.findViewById(R.id.cardentry_label);
            TextView content = delegateView.findViewById(R.id.cardentry_content);
            label.setText(SparkleHelper.getHtmlFormatting(position));
            content.setText(SparkleHelper.getNameFromId(nation));
            content.setOnClickListener(SparkleHelper.getExploreOnClickListener(context, nation,
                    ExploreActivity.EXPLORE_NATION));
            content.setTextColor(RaraHelper.getThemeLinkColour(context));
            officersLayout.addView(delegateView);
        }
    }

    public class EmbassyCard extends RecyclerView.ViewHolder {

        private final CardView embassyCard;
        private final TextView embassyNum;

        public EmbassyCard(View itemView) {
            super(itemView);
            embassyCard = itemView.findViewById(R.id.card_region_embassies);
            embassyNum = itemView.findViewById(R.id.card_region_embassies_num);
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
