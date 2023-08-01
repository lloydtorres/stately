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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.DataIntPair;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.EventsHolder;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.feed.BreakingNewsCard;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.StatsCard;
import com.lloydtorres.stately.helpers.network.DashHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-16.
 * RecyclerView used to show different types of cards in the World Assembly fragment.
 */
public class AssemblyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    private static final int ACTIVE_CARD = 0;
    private static final int INACTIVE_CARD = 1;
    private static final int STATS_CARD = 2;
    private static final int HAPPENING_CARD = 3;

    // positions of the council cards
    private static final int GENERAL_ASSEMBLY_INDEX = 0;
    private static final int SECURITY_COUNCIL_INDEX = 1;

    private static final String WA_BANNER_URL = SparkleHelper.BASE_URI + "images/banners/wa1.jpg";
    private static final int WA_FOUNDATION_ID = 654;
    private static final Pattern LASTRESOLUTION_LINK = Pattern.compile("(?i)(?s)" +
            "\\/page=WA_past_resolution\\/id=([0-9]+?)\\/council=(1|2)");
    private List<Object> cards;
    private final Context context;
    private WaVoteStatus voteStatus;

    public AssemblyRecyclerAdapter(Context c, Assembly ga, Assembly sc, WaVoteStatus vs) {
        context = c;
        setData(ga, sc, vs);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ACTIVE_CARD:
                View activeCard = inflater.inflate(R.layout.card_wa_active, parent, false);
                viewHolder = new ActiveCard(activeCard);
                break;
            case INACTIVE_CARD:
                View inactiveCard = inflater.inflate(R.layout.card_wa_inactive, parent, false);
                viewHolder = new InactiveCard(inactiveCard);
                break;
            case STATS_CARD:
                View statsCard = inflater.inflate(R.layout.card_wa_members, parent, false);
                viewHolder = new WaHeaderCard(statsCard);
                break;
            default:
                View happeningCard = inflater.inflate(R.layout.card_world_breaking_news, parent,
                        false);
                viewHolder = new BreakingNewsCard(happeningCard);
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
                WaHeaderCard statsCard = (WaHeaderCard) holder;
                statsCard.init((DataIntPair) cards.get(position));
                break;
            default:
                BreakingNewsCard happeningCard = (BreakingNewsCard) holder;
                happeningCard.init(context, context.getString(R.string.wa_happenings_title),
                        ((EventsHolder) cards.get(position)).events);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof Assembly) {
            Assembly a = (Assembly) cards.get(position);
            return a.resolution.name != null ? ACTIVE_CARD : INACTIVE_CARD;
        } else if (cards.get(position) instanceof DataIntPair) {
            return STATS_CARD;
        } else if (cards.get(position) instanceof EventsHolder) {
            return HAPPENING_CARD;
        }
        return -1;
    }

    // Card viewholders

    public void setData(Assembly ga, Assembly sc, WaVoteStatus vs) {
        voteStatus = vs;

        // Setup objects based on RecyclerView content
        cards = new ArrayList<Object>();

        cards.add(ga);
        cards.add(sc);

        DataIntPair s = new DataIntPair(sc.numNations, sc.numDelegates);
        cards.add(s);

        List<Event> happen = sc.events;
        Collections.sort(happen);
        EventsHolder events = new EventsHolder(happen);

        cards.add(events);
        notifyDataSetChanged();
    }

    // Card for active resolutions
    public class ActiveCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView cardTitle;
        private final TextView cardHeader;
        private final TextView cardActiveTime;
        private final TextView cardFor;
        private final TextView cardAgainst;
        private final ImageView iconVoteFor;
        private final ImageView iconVoteAgainst;

        public ActiveCard(View v) {
            super(v);
            cardTitle = v.findViewById(R.id.card_wa_council);
            cardHeader = v.findViewById(R.id.card_wa_title);
            cardActiveTime = v.findViewById(R.id.card_wa_activetime);
            cardFor = v.findViewById(R.id.card_wa_for);
            cardAgainst = v.findViewById(R.id.card_wa_against);
            iconVoteFor = v.findViewById(R.id.main_icon_vote_for);
            iconVoteAgainst = v.findViewById(R.id.main_icon_vote_against);

            v.setOnClickListener(this);
        }

        public void init(Assembly a, int pos) {
            String voteStats = "";
            if (pos == GENERAL_ASSEMBLY_INDEX) {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_general_assembly));
                voteStats = voteStatus.gaVote;
            } else if (pos == SECURITY_COUNCIL_INDEX) {
                cardTitle.setText(AssemblyRecyclerAdapter.this.context.getResources().getString(R.string.wa_security_council));
                voteStats = voteStatus.scVote;
            }

            cardHeader.setText(a.resolution.name);
            cardActiveTime.setText(String.format(Locale.US,
                    context.getString(R.string.wa_voting_time),
                    SparkleHelper.calculateResolutionEnd(context,
                            a.resolution.voteHistoryFor.size())));
            cardFor.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesFor));
            cardAgainst.setText(SparkleHelper.getPrettifiedNumber(a.resolution.votesAgainst));

            iconVoteFor.setVisibility(View.GONE);
            iconVoteAgainst.setVisibility(View.GONE);
            if (SparkleHelper.isWaMember(voteStatus.waState)) {
                // If voting FOR the resolution
                if (WaVoteStatus.VOTE_FOR.equals(voteStats)) {
                    iconVoteFor.setVisibility(View.VISIBLE);
                }
                // If voting AGAINST the resolution
                else if (WaVoteStatus.VOTE_AGAINST.equals(voteStats)) {
                    iconVoteAgainst.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                Intent resolutionActivityLaunch = new Intent(context, ResolutionActivity.class);
                switch (pos) {
                    case GENERAL_ASSEMBLY_INDEX:
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID,
                                Assembly.GENERAL_ASSEMBLY);
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_RESOLUTION,
                                ((Assembly) cards.get(GENERAL_ASSEMBLY_INDEX)).resolution);
                        break;
                    case SECURITY_COUNCIL_INDEX:
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_COUNCIL_ID,
                                Assembly.SECURITY_COUNCIL);
                        resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_RESOLUTION,
                                ((Assembly) cards.get(SECURITY_COUNCIL_INDEX)).resolution);
                        break;
                }
                resolutionActivityLaunch.putExtra(ResolutionActivity.TARGET_VOTE_STATUS,
                        voteStatus);
                context.startActivity(resolutionActivityLaunch);
            }
        }
    }

    // Card for inactive resolutions
    public class InactiveCard extends RecyclerView.ViewHolder {

        private final TextView cardTitle;
        private final TextView cardContent;
        private final View buttonDivider;
        private final LinearLayout buttonHolder;

        public InactiveCard(View v) {
            super(v);
            cardTitle = v.findViewById(R.id.wa_inactive_title);
            cardContent = v.findViewById(R.id.wa_inactive_content);
            buttonDivider = v.findViewById(R.id.view_divider);
            buttonHolder = v.findViewById(R.id.wa_inactive_read_button);
        }

        public void init(Assembly a, int pos) {
            if (pos == GENERAL_ASSEMBLY_INDEX) {
                cardTitle.setText(context.getResources().getString(R.string.wa_general_assembly));
            } else if (pos == SECURITY_COUNCIL_INDEX) {
                cardTitle.setText(context.getResources().getString(R.string.wa_security_council));
            }

            cardContent.setText(SparkleHelper.getHtmlFormatting(a.lastResolution));
            Matcher m = LASTRESOLUTION_LINK.matcher(a.lastResolution);
            if (m.find()) {
                buttonDivider.setVisibility(View.VISIBLE);
                buttonHolder.setVisibility(View.VISIBLE);
                final int councilId = Integer.valueOf(m.group(2));
                final int resId = Integer.valueOf(m.group(1));
                buttonHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SparkleHelper.startResolution(context, councilId, resId);
                    }
                });

            } else {
                buttonDivider.setVisibility(View.GONE);
                buttonHolder.setVisibility(View.GONE);
                buttonHolder.setOnClickListener(null);
            }
        }
    }

    public class WaHeaderCard extends StatsCard {
        private final ImageView banner;
        private final LinearLayout foundationButton;

        public WaHeaderCard(View v) {
            super(v);
            banner = v.findViewById(R.id.card_wa_header);
            foundationButton = v.findViewById(R.id.card_wa_header_foundation_button);

            DashHelper.getInstance(context).loadImage(WA_BANNER_URL, banner);
        }

        public void init(DataIntPair s) {
            super.init(s, context.getString(R.string.wa_members),
                    context.getString(R.string.wa_delegates));

            foundationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SparkleHelper.startResolution(context, Assembly.GENERAL_ASSEMBLY,
                            WA_FOUNDATION_ID);
                }
            });
        }
    }
}
