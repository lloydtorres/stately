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
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.EmptyParcelable;
import com.lloydtorres.stately.dto.RegionFactbookCardData;
import com.lloydtorres.stately.dto.RegionQuickFactsCardData;
import com.lloydtorres.stately.dto.RegionTagsCardData;
import com.lloydtorres.stately.dto.WaBadge;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.wa.WaBadgeCard;
import com.lloydtorres.stately.zombie.ZombieChartCard;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-14.
 * Recycler adapter for Region > Overview section.
 */
public class RegionOverviewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // consts for card types
    private static final int REGION_QUICK_FACTS = 0;
    private static final int REGION_FACTBOOK = 1;
    private static final int REGION_TAGS = 2;
    private static final int REGION_ZOMBIE = 3;
    private static final int REGION_WA_BADGE = 4;
    private static final int REGION_BANNED_BADGE = 5;

    private List<Parcelable> cards;
    private final String regionName;
    private final Context context;
    private final FragmentManager fragmentManager;

    public RegionOverviewRecyclerAdapter(Context c, String r, FragmentManager fm,
                                         List<Parcelable> crds) {
        regionName = r;
        context = c;
        fragmentManager = fm;
        setCards(crds);
    }

    public static void initWaDelegate(Context c, TextView tv, String delegateId,
                                      int delegateVotes, long lastUpdate) {
        if (!"0".equals(delegateId)) {
            String delegateProper = SparkleHelper.getNameFromId(delegateId);
            String delegateTemplate = String.format(Locale.US,
                    c.getString(R.string.region_delegate_votes),
                    delegateId, SparkleHelper.getPrettifiedNumber(delegateVotes),
                    c.getResources().getQuantityString(R.plurals.vote, delegateVotes),
                    SparkleHelper.getReadableDateFromUTC(c, lastUpdate));
            delegateTemplate = SparkleHelper.addExploreActivityLink(delegateTemplate, delegateId,
                    delegateProper, ExploreActivity.EXPLORE_NATION);
            SparkleHelper.setStyledTextView(c, tv, delegateTemplate);
        } else {
            tv.setText(c.getString(R.string.region_filler_none));
        }
    }

    public static void initFounder(Context c, TextView tv, String founder, long founded) {
        if (!"0".equals(founder)) {
            String founderTemplate = SparkleHelper.addExploreActivityLink(founder, founder,
                    SparkleHelper.getNameFromId(founder), ExploreActivity.EXPLORE_NATION);
            SparkleHelper.setStyledTextView(c, tv, founderTemplate);
        } else {
            tv.setText(c.getString(R.string.region_filler_none));
        }

        if (founded != 0) {
            tv.append(" " + String.format(Locale.US, c.getString(R.string.region_founded_append),
                    SparkleHelper.getReadableDateFromUTC(c, founded)));
        }
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
            case REGION_QUICK_FACTS:
                View quickFactsCard = inflater.inflate(R.layout.card_region_quick_facts, parent,
                        false);
                viewHolder = new RegionQuickFactsCard(quickFactsCard);
                break;
            case REGION_FACTBOOK:
                View factbookCard = inflater.inflate(R.layout.card_region_generic, parent, false);
                viewHolder = new RegionFactbookCard(factbookCard);
                break;
            case REGION_TAGS:
                View tagsCard = inflater.inflate(R.layout.card_region_generic, parent, false);
                viewHolder = new RegionTagsCard(tagsCard);
                break;
            case REGION_ZOMBIE:
                View zombieCard = inflater.inflate(R.layout.card_zombie_chart, parent, false);
                viewHolder = new ZombieChartCard(zombieCard);
                break;
            case REGION_WA_BADGE:
                View waBadgeCard = inflater.inflate(R.layout.card_wa_badge, parent, false);
                viewHolder = new WaBadgeCard(context, waBadgeCard);
                break;
            case REGION_BANNED_BADGE:
                View regionBannedCard = inflater.inflate(R.layout.card_region_banned, parent, false);
                viewHolder = new RegionBannedCard(context, regionBannedCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case REGION_QUICK_FACTS:
                RegionQuickFactsCard quickFactsCard = (RegionQuickFactsCard) holder;
                quickFactsCard.init((RegionQuickFactsCardData) cards.get(position));
                break;
            case REGION_FACTBOOK:
                RegionFactbookCard factbookCard = (RegionFactbookCard) holder;
                factbookCard.init((RegionFactbookCardData) cards.get(position));
                break;
            case REGION_TAGS:
                RegionTagsCard tagsCard = (RegionTagsCard) holder;
                tagsCard.init((RegionTagsCardData) cards.get(position));
                break;
            case REGION_ZOMBIE:
                ZombieChartCard zombieChartCard = (ZombieChartCard) holder;
                zombieChartCard.init(context, (Zombie) cards.get(position),
                        ZombieChartCard.MODE_REGION_DEFAULT, regionName);
                break;
            case REGION_WA_BADGE:
                WaBadgeCard bc = (WaBadgeCard) holder;
                bc.init((WaBadge) cards.get(position));
                break;
            case REGION_BANNED_BADGE:
                RegionBannedCard regionBannedCard = (RegionBannedCard) holder;
                regionBannedCard.init();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof RegionQuickFactsCardData) {
            return REGION_QUICK_FACTS;
        } else if (cards.get(position) instanceof RegionFactbookCardData) {
            return REGION_FACTBOOK;
        } else if (cards.get(position) instanceof RegionTagsCardData) {
            return REGION_TAGS;
        } else if (cards.get(position) instanceof Zombie) {
            return REGION_ZOMBIE;
        } else if (cards.get(position) instanceof WaBadge) {
            return REGION_WA_BADGE;
        } else if (cards.get(position) instanceof EmptyParcelable) {
            return REGION_BANNED_BADGE;
        }
        return -1;
    }

    // Card viewholders
    public class RegionQuickFactsCard extends RecyclerView.ViewHolder {
        private RegionQuickFactsCardData data;

        private final TextView delegate;
        private final TextView founder;
        private final TextView power;

        public RegionQuickFactsCard(View itemView) {
            super(itemView);
            delegate = itemView.findViewById(R.id.region_delegate);
            founder = itemView.findViewById(R.id.region_founder);
            power = itemView.findViewById(R.id.region_power);
        }

        public void init(RegionQuickFactsCardData d) {
            data = d;
            initWaDelegate(context, delegate, data.waDelegate, data.delegateVotes, data.lastUpdate);
            initFounder(context, founder, data.founder, data.founded);
            power.setText(data.power);
        }
    }

    public abstract class RegionGenericCard extends RecyclerView.ViewHolder {
        protected TextView cardTitle;
        protected HtmlTextView cardContent;

        public RegionGenericCard(View itemView) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.card_region_factbook_title);
            cardContent = itemView.findViewById(R.id.region_factbook);
        }
    }

    public class RegionFactbookCard extends RegionGenericCard {
        private RegionFactbookCardData data;

        public RegionFactbookCard(View itemView) {
            super(itemView);
            cardTitle.setText(context.getString(R.string.card_region_factbook_title));
        }

        public void init(RegionFactbookCardData d) {
            data = d;
            SparkleHelper.setStyledTextView(context, cardContent, data.factbook, fragmentManager);
        }
    }

    public class RegionTagsCard extends RegionGenericCard {
        private RegionTagsCardData data;

        public RegionTagsCard(View itemView) {
            super(itemView);
            cardTitle.setText(context.getString(R.string.card_region_tags_title));
        }

        public void init(RegionTagsCardData d) {
            data = d;
            String tagCombine = SparkleHelper.joinStringList(data.tags, ", ");
            cardContent.setText(tagCombine);
        }
    }

    public class RegionBannedCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView badgeTextView;

        public RegionBannedCard(final Context c, final View itemView) {
            super(itemView);
            context = c;
            badgeTextView = itemView.findViewById(R.id.region_banned_description);
        }

        public void init() {
            final String selfNationName = PinkaHelper.getActiveUser(context).name;
            badgeTextView.setText(String.format(Locale.US,
                    context.getString(R.string.region_ban_badge_description), selfNationName));
        }
    }
}
