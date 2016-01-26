package com.lloydtorres.stately.region;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.lloydtorres.stately.dto.Poll;
import com.lloydtorres.stately.dto.PollOption;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.WaVote;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.atteo.evo.inflector.English;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * This is the RecyclerView adapter for the Region community subfragment.
 */
public class CommunityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    public final int BUTTON_CARD = 0;
    public final int POLL_CARD = 1;
    public final int WA_CARD = 2;

    private List<Object> cards;
    private Context context;

    public CommunityRecyclerAdapter(Context c, Region mRegion)
    {
        cards = new ArrayList<Object>();

        context = c;
        // This adds a button to the RMB
        cards.add(mRegion.name);

        if (mRegion.poll != null)
        {
            cards.add(mRegion.poll);
        }

        if (mRegion.gaVote != null && (mRegion.gaVote.voteFor + mRegion.gaVote.voteAgainst) > 0)
        {
            mRegion.gaVote.chamber = Assembly.GENERAL_ASSEMBLY;
            cards.add(mRegion.gaVote);
        }

        if (mRegion.scVote != null && (mRegion.scVote.voteFor + mRegion.scVote.voteAgainst) > 0)
        {
            mRegion.scVote.chamber = Assembly.SECURITY_COUNCIL;
            cards.add(mRegion.scVote);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
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
            default:
                View waCard = inflater.inflate(R.layout.card_region_wa, parent, false);
                viewHolder = new RegionWaCard(context, waCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case BUTTON_CARD:
                MessageBoardCard rmbCard = (MessageBoardCard) holder;
                rmbCard.init((String) cards.get(position));
                break;
            case POLL_CARD:
                PollCard pollCard = (PollCard) holder;
                pollCard.init((Poll) cards.get(position));
                break;
            default:
                RegionWaCard waVoteCard = (RegionWaCard) holder;
                waVoteCard.init((WaVote) cards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof String)
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
        return -1;
    }

    // Card viewholders

    // Card for the RMB button
    public class MessageBoardCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private CardView buttonCard;
        private String regionName;

        public MessageBoardCard(Context c, View v) {
            super(v);
            context = c;
            buttonCard = (CardView) v.findViewById(R.id.card_button_main);
            v.setOnClickListener(this);
        }

        public void init(String n)
        {
            regionName = n;
        }

        @Override
        public void onClick(View v) {
            Intent messageBoardActivity = new Intent(context, MessageBoardActivity.class);
            messageBoardActivity.putExtra("regionName", regionName);
            context.startActivity(messageBoardActivity);
        }
    }

    // Card for the region poll
    public class PollCard extends RecyclerView.ViewHolder {

        private Context context;
        private TextView question;
        private TextView content;
        private LinearLayout options;
        private PieChart breakdown;
        private TextView nullVote;

        public PollCard(Context c, View v) {
            super(v);
            context = c;
            question = (TextView) v.findViewById(R.id.card_region_poll_question);
            content = (TextView) v.findViewById(R.id.card_region_poll_content);
            options = (LinearLayout) v.findViewById(R.id.card_region_poll_options);
            breakdown = (PieChart) v.findViewById(R.id.card_region_poll_chart);
            nullVote = (TextView) v.findViewById(R.id.region_poll_null_vote);
        }

        public void init(Poll p)
        {
            question.setText(p.title);

            if (p.text != null && p.text.length() > 0)
            {
                content.setText(p.text);
            }
            else
            {
                content.setVisibility(View.GONE);
            }

            List<PollOption> results = p.options;
            Collections.sort(results);

            float voteTotal = 0;
            List<String> chartLabels = new ArrayList<String>();
            for (int i=0; i<results.size(); i++)
            {
                inflateOption(options, i+1, results.get(i).text, results.get(i).votes);
                voteTotal += (float) results.get(i).votes;
                chartLabels.add(String.format(context.getString(R.string.region_option_index), i+1));
            }

            if (voteTotal > 0)
            {
                List<Entry> chartEntries = new ArrayList<Entry>();
                for (int i=0; i<results.size(); i++)
                {
                    chartEntries.add(new Entry(((float) results.get(i).votes * 100f)/voteTotal, i));
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

        private void inflateOption(LinearLayout optionLayout, int index, String option, int votes)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View optionView = inflater.inflate(R.layout.view_cardentry, null);
            TextView label = (TextView) optionView.findViewById(R.id.cardentry_label);
            TextView content = (TextView) optionView.findViewById(R.id.cardentry_content);
            label.setText(String.format(context.getString(R.string.region_option_index), index));
            content.setText(String.format(context.getString(R.string.poll_votes_template), option, votes, English.plural(context.getString(R.string.region_filler_vote), votes)));
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
            resolutionActivityLaunch.putExtra("councilId", w.chamber);
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

            title.setText(String.format(context.getString(R.string.card_region_wa_vote), chamberName));
            linkContent.setText(String.format(context.getString(R.string.card_region_wa_link), chamberName));

            filler.setText(String.format(context.getString(R.string.region_wa_filler), w.voteFor, w.voteAgainst));
            if (!SparkleHelper.setWaVotingBreakdown(context, chart, w.voteFor, w.voteAgainst))
            {
                chart.setVisibility(View.GONE);
                nullVote.setVisibility(View.VISIBLE);
            }
        }
    }

}
