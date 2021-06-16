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

package com.lloydtorres.stately.issues;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueFullHolder;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.zombie.NightmareHelper;
import com.lloydtorres.stately.zombie.ZombieControlActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-28.
 * An adapter for the IssuesFragment recycler.
 */
public class IssuesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ISSUE_CARD = 0;
    private static final int NEXT_CARD = 1;
    private static final int ZOMBIE_CARD = 2;

    private Context context;
    private List<Object> issuesCards;
    private Nation mNation;

    public IssuesRecyclerAdapter(Context c, List<Parcelable> i, long nextIssueTime, Nation n) {
        context = c;
        mNation = n;
        setIssueCards(i, nextIssueTime);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ISSUE_CARD:
                View issueCard = inflater.inflate(R.layout.card_issue_main, parent, false);
                viewHolder = new IssueCard(issueCard);
                break;
            case ZOMBIE_CARD:
                View zombieCard = inflater.inflate(R.layout.card_issue_zombie_control, parent,
                        false);
                viewHolder = new ZombieIssueCard(zombieCard);
                break;
            default:
                View nextCard = inflater.inflate(R.layout.card_generic, parent, false);
                viewHolder = new NextCard(nextCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ISSUE_CARD:
                IssueCard issueCard = (IssueCard) holder;
                issueCard.init((Issue) issuesCards.get(position));
                break;
            case ZOMBIE_CARD:
                ZombieIssueCard zombieCard = (ZombieIssueCard) holder;
                zombieCard.init((Zombie) issuesCards.get(position));
                break;
            default:
                NextCard nextCard = (NextCard) holder;
                nextCard.init((Long) issuesCards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return issuesCards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (issuesCards.get(position) instanceof Issue) {
            return ISSUE_CARD;
        } else if (issuesCards.get(position) instanceof Zombie) {
            return ZOMBIE_CARD;
        } else if (issuesCards.get(position) instanceof Long) {
            return NEXT_CARD;
        }
        return -1;
    }

    /**
     * Sets the list of objects as the cards for the recycler adapter.
     * @param cards
     */
    public void setIssueCards(List<Parcelable> cards, long nextIssueTime) {
        issuesCards = new ArrayList<Object>(cards);
        final int maxIssues = !NightmareHelper.getIsZDayActive(context)
                ? IssueFullHolder.MAX_ISSUE_COUNT_REGULAR : IssueFullHolder.MAX_ISSUE_COUNT_ZOMBIE;
        if (issuesCards.size() < maxIssues && nextIssueTime != IssueFullHolder.UNKNOWN_NEXT_ISSUE_TIME) {
            issuesCards.add(nextIssueTime);
        }
        notifyDataSetChanged();
    }

    // Card viewholders
    public class IssueCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView id;
        private ImageView image;
        private Issue issue;

        public IssueCard(View v) {
            super(v);
            title = v.findViewById(R.id.card_issue_main_title);
            id = v.findViewById(R.id.card_issue_main_number);
            image = v.findViewById(R.id.card_issue_image);
            v.setOnClickListener(this);
        }

        public void init(Issue i) {
            issue = i;
            title.setText(issue.title);

            if (issue.chain != null) {
                id.setText(String.format(Locale.US,
                        context.getString(R.string.issue_chain_and_number),
                        SparkleHelper.getPrettifiedNumber(issue.id), issue.chain));
            } else {
                id.setText(String.format(Locale.US, context.getString(R.string.issue_number),
                        SparkleHelper.getPrettifiedNumber(issue.id)));
            }

            if (issue.image != null) {
                image.setVisibility(View.VISIBLE);
                DashHelper dashie = DashHelper.getInstance(context);
                dashie.loadImage(RaraHelper.getBannerURL(issue.image), image, false);
            } else {
                image.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (issue != null) {
                Intent decisionActivityLaunch = new Intent(context, IssueDecisionActivity.class);
                decisionActivityLaunch.putExtra(IssueDecisionActivity.ISSUE_DATA, issue);
                decisionActivityLaunch.putExtra(IssueDecisionActivity.NATION_DATA, mNation);
                context.startActivity(decisionActivityLaunch);
            }
        }
    }

    public class NextCard extends RecyclerView.ViewHolder {

        private TextView nextUpdate;

        public NextCard(View v) {
            super(v);
            v.findViewById(R.id.card_generic_title).setVisibility(View.GONE);
            nextUpdate = v.findViewById(R.id.card_generic_content);
        }

        // Heh
        public void init(long time) {
            RaraHelper.setViewHolderFullSpan(itemView);

            nextUpdate.setText(String.format(Locale.US, context.getString(R.string.next_issue),
                    SparkleHelper.getReadableDateFromUTC(context, time)));
            nextUpdate.setTypeface(nextUpdate.getTypeface(), Typeface.ITALIC);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 0);
            nextUpdate.setLayoutParams(params);
        }
    }

    public class ZombieIssueCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView background;
        private TextView survivalRate;

        public ZombieIssueCard(View v) {
            super(v);
            background = v.findViewById(R.id.card_issue_zombie_control_background);
            survivalRate = v.findViewById(R.id.card_issue_zombie_control_survival_rate);
            v.setOnClickListener(this);
        }

        public void init(Zombie zombieData) {
            DashHelper dashie = DashHelper.getInstance(context);
            dashie.loadImage(NightmareHelper.getZombieBanner(mNation.zombieData.action),
                    background, false);
            float total = zombieData.survivors + zombieData.dead + zombieData.zombies;
            float survivalRatePercent = (zombieData.survivors / total) * 100f;
            survivalRate.setText(String.format(Locale.US,
                    context.getString(R.string.zombie_control_survival),
                    SparkleHelper.getPrettifiedNumber(survivalRatePercent)));
        }

        @Override
        public void onClick(View view) {
            Intent zombieControlLaunch = new Intent(context, ZombieControlActivity.class);
            context.startActivity(zombieControlLaunch);
        }
    }
}
