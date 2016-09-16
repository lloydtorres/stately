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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.EmbassyHolder;
import com.lloydtorres.stately.dto.Officer;
import com.lloydtorres.stately.dto.OfficerHolder;
import com.lloydtorres.stately.dto.Poll;
import com.lloydtorres.stately.dto.PollOption;
import com.lloydtorres.stately.dto.RMBButtonHolder;
import com.lloydtorres.stately.dto.WaVote;
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

    private Context context;
    private FragmentManager fm;
    private List<Parcelable> cards;
    private String regionName;

    public CommunityRecyclerAdapter(Context c, FragmentManager f, List<Parcelable> crds, String n)
    {
        context = c;
        fm = f;
        cards = crds;
        regionName= n;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case BUTTON_CARD:
                View rmbCard = inflater.inflate(R.layout.card_button, parent, false);
                viewHolder = new MessageBoardCard(context, rmbCard);
                break;
            case POLL_CARD:
                View pollCard = inflater.inflate(R.layout.card_poll, parent, false);
                viewHolder = new PollCard(context, pollCard);
                break;
            case WA_CARD:
                View waCard = inflater.inflate(R.layout.card_region_wa, parent, false);
                viewHolder = new RegionWaCard(context, waCard);
                break;
            case OFFICER_CARD:
                View officerCard = inflater.inflate(R.layout.card_officers, parent, false);
                viewHolder = new OfficerCard(context, officerCard);
                break;
            case EMBASSY_CARD:
                View embassyCard = inflater.inflate(R.layout.card_embassies, parent, false);
                viewHolder = new EmbassyCard(context, embassyCard);
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
        if (cards.get(position) instanceof RMBButtonHolder)
        {
            return BUTTON_CARD;
        }
        else if (cards.get(position) instanceof Poll)
        {
            return POLL_CARD;
        }
        else if (cards.get(position) instanceof WaVote)
        {
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

    // Card viewholders

    // Card for the RMB button
    public class MessageBoardCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private TextView unreadCounter;
        private RMBButtonHolder buttonData;

        public MessageBoardCard(Context c, View v) {
            super(v);
            context = c;
            unreadCounter = (TextView) v.findViewById(R.id.card_button_num);
            v.setOnClickListener(this);
        }

        public void init(RMBButtonHolder bh)
        {
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

        private Context context;
        private TextView question;
        private HtmlTextView content;
        private LinearLayout options;
        private PieChart breakdown;
        private TextView nullVote;

        public PollCard(Context c, View v) {
            super(v);
            context = c;
            question = (TextView) v.findViewById(R.id.card_region_poll_question);
            content = (HtmlTextView) v.findViewById(R.id.card_region_poll_content);
            options = (LinearLayout) v.findViewById(R.id.card_region_poll_options);
            breakdown = (PieChart) v.findViewById(R.id.card_region_poll_chart);
            nullVote = (TextView) v.findViewById(R.id.region_poll_null_vote);
        }

        public void init(Poll p)
        {
            SparkleHelper.setHappeningsFormatting(context, question,
                    String.format(Locale.US, context.getString(R.string.card_region_poll_title_author), p.title, p.author));

            if (p.text != null && p.text.length() > 0)
            {
                SparkleHelper.setBbCodeFormatting(context, content, p.text, fm);
            }
            else
            {
                content.setVisibility(View.GONE);
            }

            List<PollOption> results = p.options;
            Collections.sort(results);

            options.removeAllViews();
            int voteTotal = 0;
            List<String> chartLabels = new ArrayList<String>();
            for (int i=0; i<results.size(); i++)
            {
                inflateOption(options, i+1, results.get(i).text, results.get(i).votes, results.get(i).voters);
                voteTotal += results.get(i).votes;
                chartLabels.add(String.format(Locale.US, context.getString(R.string.region_option_index), i+1));
            }

            if (voteTotal > 0)
            {
                breakdown.setVisibility(View.VISIBLE);
                nullVote.setVisibility(View.GONE);

                List<Entry> chartEntries = new ArrayList<Entry>();
                for (int i=0; i<results.size(); i++)
                {
                    chartEntries.add(new Entry((results.get(i).votes * 100f)/voteTotal, i));
                }

                PieDataSet dataSet = new PieDataSet(chartEntries, "");
                dataSet.setDrawValues(false);
                dataSet.setColors(SparkleHelper.chartColours, context);
                PieData dataFull = new PieData(chartLabels, dataSet);

                breakdown = SparkleHelper.getFormattedPieChart(context, breakdown, chartLabels);
                breakdown.setData(dataFull);
                breakdown.invalidate();
            }
            else
            {
                breakdown.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }

        private void inflateOption(LinearLayout optionLayout, int index, String option, int votes, String voters)
        {
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
                        String.format(Locale.US, context.getString(R.string.poll_votes_voter_dialog), index),
                        properVoters, SparkleHelper.CLICKY_NATION_MODE);
                template.setSpan(span, 0, template.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                content.setMovementMethod(LinkMovementMethod.getInstance());
            }
            content.append(template);
            content.append(context.getString(R.string.poll_votes_template_end));

            optionLayout.addView(optionView);
        }
    }

    // Card for the WA poll
    public class RegionWaCard extends RecyclerView.ViewHolder {

        private Context context;
        private TextView title;
        private TextView filler;
        private PieChart chart;
        private LinearLayout resolutionLink;
        private TextView linkContent;
        private TextView nullVote;

        public RegionWaCard(Context c, View v)
        {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.region_wa_title);
            filler = (TextView) v.findViewById(R.id.region_wa_vote_filler);
            chart = (PieChart) v.findViewById(R.id.region_wa_breakdown);
            resolutionLink = (LinearLayout) v.findViewById(R.id.region_wa_link);
            linkContent = (TextView) v.findViewById(R.id.region_wa_link_text);
            nullVote = (TextView) v.findViewById(R.id.region_wa_null_vote);
        }

        public void init(WaVote w)
        {
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
            switch(w.chamber)
            {
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
            if (!SparkleHelper.getWaVotingChart(context, chart, w.voteFor, w.voteAgainst))
            {
                chart.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }
    }

    public class OfficerCard extends RecyclerView.ViewHolder {

        private Context context;
        private LayoutInflater inflater;
        private LinearLayout officersLayout;
        private TextView noOfficers;

        public OfficerCard(Context c, View itemView) {
            super(itemView);
            context = c;
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
                for (int i=0; i<officers.size(); i++)
                {
                    if (officers.get(i).office != null && officers.get(i).name != null)
                    {
                        inflateOfficerEntry(officersLayout, officers.get(i).office, officers.get(i).name);
                    }
                }
            }
        }

        private void inflateOfficerEntry(LinearLayout officersLayout, String position, String nation)
        {
            View delegateView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = (TextView) delegateView.findViewById(R.id.cardentry_label);
            TextView content = (TextView) delegateView.findViewById(R.id.cardentry_content);
            label.setText(SparkleHelper.getHtmlFormatting(position));
            SparkleHelper.activityLinkBuilder(context, content, nation, nation, SparkleHelper.getNameFromId(nation), SparkleHelper.CLICKY_NATION_MODE);
            officersLayout.addView(delegateView);
        }
    }

    public class EmbassyCard extends RecyclerView.ViewHolder {

        private Context context;
        private CardView embassyCard;
        private TextView embassyNum;

        public EmbassyCard(Context c, View itemView) {
            super(itemView);
            context = c;
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
                    nameListDialog.setTarget(SparkleHelper.CLICKY_REGION_MODE);
                    nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                }
            });
        }
    }

}
