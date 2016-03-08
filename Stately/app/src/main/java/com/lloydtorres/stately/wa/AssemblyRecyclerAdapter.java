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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyStats;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.helpers.HappeningCard;
import com.lloydtorres.stately.dto.WaVoteStatus;
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
    private WaVoteStatus voteStatus;

    public AssemblyRecyclerAdapter(Context c, Assembly ga, Assembly sc, WaVoteStatus vs)
    {
        context = c;
        voteStatus = vs;

        // Setup objects based on RecyclerView content
        cards = new ArrayList<Object>();
        cards.add(ga);
        cards.add(sc);

        AssemblyStats s = new AssemblyStats(sc.numNations, sc.numDelegates);
        cards.add(s);

        List<Event> happen = sc.happeningsRoot.events;
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
                happeningCard.init((Event) cards.get(position));
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

        private Context context;
        private TextView cardTitle;
        private TextView cardHeader;
        private TextView cardActiveTime;
        private TextView cardFor;
        private TextView cardAgainst;
        private ImageView iconVoteFor;
        private ImageView iconVoteAgainst;

        public ActiveCard(Context c, View v) {
            super(v);
            context = c;
            cardTitle = (TextView) v.findViewById(R.id.card_wa_council);
            cardHeader = (TextView) v.findViewById(R.id.card_wa_title);
            cardActiveTime = (TextView) v.findViewById(R.id.card_wa_activetime);
            cardFor = (TextView) v.findViewById(R.id.card_wa_for);
            cardAgainst = (TextView) v.findViewById(R.id.card_wa_against);
            iconVoteFor = (ImageView) v.findViewById(R.id.main_icon_vote_for);
            iconVoteAgainst = (ImageView) v.findViewById(R.id.main_icon_vote_against);

            v.setOnClickListener(this);
        }

        public void init(Assembly a, int pos)
        {
            String voteStats = "";
            if (pos == GENERAL_ASSEMBLY_INDEX)
            {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_general_assembly));
                voteStats = voteStatus.gaVote;
            }
            else if (pos == SECURITY_COUNCIL_INDEX)
            {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_security_council));
                voteStats = voteStatus.scVote;
            }

            cardHeader.setText(a.resolution.name);
            cardActiveTime.setText(String.format(context.getString(R.string.wa_voting_time), SparkleHelper.calculateResolutionEnd(a.resolution.voteHistoryFor.size()+1)));
            cardFor.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesFor));
            cardAgainst.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesAgainst));

            if (SparkleHelper.isWaMember(context, voteStatus.waState))
            {
                // If voting FOR the resolution
                if (context.getString(R.string.wa_vote_state_for).equals(voteStats))
                {
                    iconVoteFor.setVisibility(View.VISIBLE);
                }
                // If voting AGAINST the resolution
                else if (context.getString(R.string.wa_vote_state_against).equals(voteStats))
                {
                    iconVoteAgainst.setVisibility(View.VISIBLE);
                }
            }
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
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, Assembly.GENERAL_ASSEMBLY);
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_RESOLUTION, ((Assembly)cards.get(GENERAL_ASSEMBLY_INDEX)).resolution);
                        break;
                    case SECURITY_COUNCIL_INDEX:
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, Assembly.SECURITY_COUNCIL);
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_RESOLUTION, ((Assembly)cards.get(SECURITY_COUNCIL_INDEX)).resolution);
                        break;
                }
                resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_VOTE_STATUS, voteStatus);
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
