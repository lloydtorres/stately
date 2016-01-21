package com.lloydtorres.stately.wa;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyStats;
import com.lloydtorres.stately.dto.HappeningCard;
import com.lloydtorres.stately.dto.HappeningEvent;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-16.
 * RecyclerView used to show different types of cards in the World Assembly fragment.
 */
public class AssemblyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    private final int ACTIVE_CARD = 0;
    private final int INACTIVE_CARD = 1;
    private final int STATS_CARD = 2;
    private final int HAPPENING_CARD = 3;

    // positions of the council cards
    private final int GENERAL_ASSEMBLY_INDEX = 0;
    private final int SECURITY_COUNCIL_INDEX = 1;

    private List<Object> cards;
    private Context context;

    public AssemblyRecyclerAdapter(Context c, Assembly ga, Assembly sc)
    {
        context = c;

        // Setup objects based on RecyclerView content
        cards = new ArrayList<Object>();
        cards.add(ga);
        cards.add(sc);

        AssemblyStats s = new AssemblyStats(sc.numNations, sc.numDelegates);
        cards.add(s);

        List<HappeningEvent> happen = sc.happeningsRoot.events;
        Collections.sort(happen);

        cards.addAll(happen);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ACTIVE_CARD:
                View activeCard = inflater.inflate(R.layout.card_wa_active, parent, false);
                viewHolder = new ActiveCard(context, activeCard);
                break;
            case INACTIVE_CARD:
                View inactiveCard = inflater.inflate(R.layout.card_generic, parent, false);
                viewHolder = new InactiveCard(inactiveCard);
                break;
            case STATS_CARD:
                View statsCard = inflater.inflate(R.layout.card_wa_members, parent, false);
                viewHolder = new StatsCard(statsCard);
                break;
            default:
                View happeningCard = inflater.inflate(R.layout.card_happening, parent, false);
                viewHolder = new HappeningCard(context, happeningCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ACTIVE_CARD:
                ActiveCard activeCard = (ActiveCard) holder;
                activeCard.init((Assembly) cards.get(position), position);
                break;
            case INACTIVE_CARD:
                InactiveCard inactiveCard = (InactiveCard) holder;
                inactiveCard.init((Assembly) cards.get(position), position);
                break;
            case STATS_CARD:
                StatsCard statsCard = (StatsCard) holder;
                statsCard.init((AssemblyStats) cards.get(position));
                break;
            default:
                HappeningCard happeningCard = (HappeningCard) holder;
                happeningCard.init((HappeningEvent) cards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof Assembly)
        {
            Assembly a = (Assembly) cards.get(position);
            return a.resolution.name != null ? ACTIVE_CARD : INACTIVE_CARD;
        }
        else if (cards.get(position) instanceof AssemblyStats)
        {
            return STATS_CARD;
        }
        else if (cards.get(position) instanceof HappeningCard)
        {
            return HAPPENING_CARD;
        }
        return -1;
    }

    // Card viewholders

    // Card for active resolutions
    public class ActiveCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView cardTitle;
        private TextView cardHeader;
        private TextView cardActiveTime;
        private TextView cardFor;
        private TextView cardAgainst;
        private Context context;

        public ActiveCard(Context c, View v) {
            super(v);
            context = c;
            cardTitle = (TextView) v.findViewById(R.id.card_wa_council);
            cardHeader = (TextView) v.findViewById(R.id.card_wa_title);
            cardActiveTime = (TextView) v.findViewById(R.id.card_wa_activetime);
            cardFor = (TextView) v.findViewById(R.id.card_wa_for);
            cardAgainst = (TextView) v.findViewById(R.id.card_wa_against);

            v.setOnClickListener(this);
        }

        public void init(Assembly a, int pos)
        {
            if (pos == GENERAL_ASSEMBLY_INDEX)
            {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_general_assembly));
            }
            else if (pos == SECURITY_COUNCIL_INDEX)
            {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_security_council));
            }

            cardHeader.setText(a.resolution.name);
            cardActiveTime.setText(String.format(context.getString(R.string.wa_voting_time), SparkleHelper.getReadableDateFromUTC(a.resolution.created)));
            cardFor.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesFor));
            cardAgainst.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesAgainst));
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION)
            {
                Intent resolutionActivityLaunch = new Intent(context, ResolutionActivity.class);
                switch (pos)
                {
                    case GENERAL_ASSEMBLY_INDEX:
                        resolutionActivityLaunch.putExtra("councilId", Assembly.GENERAL_ASSEMBLY);
                        resolutionActivityLaunch.putExtra("resolution", ((Assembly)cards.get(GENERAL_ASSEMBLY_INDEX)).resolution);
                        break;
                    case SECURITY_COUNCIL_INDEX:
                        resolutionActivityLaunch.putExtra("councilId", Assembly.SECURITY_COUNCIL);
                        resolutionActivityLaunch.putExtra("resolution", ((Assembly)cards.get(SECURITY_COUNCIL_INDEX)).resolution);
                        break;
                }
                context.startActivity(resolutionActivityLaunch);
            }
        }
    }

    // Card for inactive resolutions
    public class InactiveCard extends RecyclerView.ViewHolder {

        private TextView cardTitle;
        private TextView cardContent;

        public InactiveCard(View v) {
            super(v);
            cardTitle = (TextView) v.findViewById(R.id.card_generic_title);
            cardContent = (TextView) v.findViewById(R.id.card_generic_content);
        }

        public void init(Assembly a, int pos)
        {
            if (pos == GENERAL_ASSEMBLY_INDEX)
            {
                cardTitle.setText(context.getResources().getString(R.string.wa_general_assembly));
            }
            else if (pos == SECURITY_COUNCIL_INDEX)
            {
                cardTitle.setText(context.getResources().getString(R.string.wa_security_council));
            }

            cardContent.setText(SparkleHelper.getHtmlFormatting(a.lastResolution));
        }
    }

    // Card for stats on members and delegates
    public class StatsCard extends RecyclerView.ViewHolder {

        private TextView cardMembers;
        private TextView cardDelegates;

        public StatsCard(View v) {
            super(v);
            cardMembers = (TextView) v.findViewById(R.id.card_wa_members);
            cardDelegates = (TextView) v.findViewById(R.id.card_wa_delegates);
        }

        public void init(AssemblyStats s)
        {
            cardMembers.setText(SparkleHelper.getPrettifiedNumber(s.members));
            cardDelegates.setText(SparkleHelper.getPrettifiedNumber(s.delegates));
        }
    }
}
