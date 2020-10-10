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

package com.lloydtorres.stately.world;

import android.content.Context;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.BaseRegion;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.DataIntPair;
import com.lloydtorres.stately.dto.EventsHolder;
import com.lloydtorres.stately.dto.World;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.feed.BreakingNewsCard;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.StatsCard;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.region.RegionOverviewRecyclerAdapter;
import com.lloydtorres.stately.zombie.NightmareHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-12.
 * A recycler adapter for the World fragment.
 */
public class WorldRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // consts for card types
    private static final int WORLD_COUNT = 0;
    private static final int WORLD_FEATURED_REGION = 1;
    private static final int WORLD_BREAKING_NEWS = 2;
    private static final int WORLD_FEATURED_CENSUS = 3;

    private List<Object> cards;
    private Context context;
    private FragmentManager fragmentManager;
    private LinkedHashMap<Integer, CensusScale> censusScale;

    public WorldRecyclerAdapter(Context c, FragmentManager fm, World w, BaseRegion fr) {
        context = c;
        fragmentManager = fm;
        String[] WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
        censusScale = SparkleHelper.getCensusScales(WORLD_CENSUS_ITEMS);

        if (!NightmareHelper.getIsZDayActive(context)) {
            censusScale = NightmareHelper.trimZDayCensusDatasets(censusScale);
        }

        setContent(w, fr);
    }

    public void setContent(World w, BaseRegion fr) {
        cards = new ArrayList<Object>();

        // Add the number of nations and regions
        cards.add(new DataIntPair(w.numNations, w.numRegions));

        // Get featured census
        if (w.census.size() > 0) {
            CensusDetailedRank featuredCensus = w.census.get(0);
            featuredCensus.isFeatured = true;
            cards.add(featuredCensus);
        }

        // Add featured region if available
        if (fr != null) {
            cards.add(fr);
        }

        // Add world happenings as one data structure
        EventsHolder events = new EventsHolder(w.happenings);
        cards.add(events);

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case WORLD_COUNT:
                View statsCard = inflater.inflate(R.layout.card_stats, parent, false);
                viewHolder = new StatsCard(statsCard);
                break;
            case WORLD_FEATURED_REGION:
                View featuredRegionCard = inflater.inflate(R.layout.card_world_featured_region, parent, false);
                viewHolder = new FeaturedRegionCard(featuredRegionCard);
                break;
            case WORLD_BREAKING_NEWS:
                View breakingNewsCard = inflater.inflate(R.layout.card_world_breaking_news, parent, false);
                viewHolder = new BreakingNewsCard(breakingNewsCard);
                break;
            case WORLD_FEATURED_CENSUS:
                View featuredCensusCard = inflater.inflate(R.layout.card_world_featured_census, parent, false);
                viewHolder = new FeaturedCensusCard(featuredCensusCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case WORLD_COUNT:
                StatsCard statsCard = (StatsCard) holder;
                statsCard.init((DataIntPair) cards.get(position),
                        context.getString(R.string.world_nations),
                        context.getString(R.string.world_regions));
                break;
            case WORLD_FEATURED_REGION:
                FeaturedRegionCard featuredRegionCard = (FeaturedRegionCard) holder;
                featuredRegionCard.init((BaseRegion) cards.get(position));
                break;
            case WORLD_BREAKING_NEWS:
                BreakingNewsCard breakingNewsCard = (BreakingNewsCard) holder;
                breakingNewsCard.init(context, context.getString(R.string.issue_breaking), ((EventsHolder) cards.get(position)).events);
                break;
            case WORLD_FEATURED_CENSUS:
                FeaturedCensusCard featuredCensusCard = (FeaturedCensusCard) holder;
                featuredCensusCard.init((CensusDetailedRank) cards.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof DataIntPair) {
            return WORLD_COUNT;
        }
        else if (cards.get(position) instanceof BaseRegion) {
            return WORLD_FEATURED_REGION;
        }
        else if (cards.get(position) instanceof EventsHolder) {
            return WORLD_BREAKING_NEWS;
        }
        else if (cards.get(position) instanceof CensusDetailedRank) {
            return WORLD_FEATURED_CENSUS;
        }
        return -1;
    }

    // Card viewholders
    // Featured region
    public class FeaturedRegionCard extends RecyclerView.ViewHolder {

        private BaseRegion regionData;

        private RelativeLayout header;
        private TextView regionName;
        private TextView nationCount;
        private ImageView flag;

        private TextView waDelegate;
        private TextView founder;

        private LinearLayout factbookHolder;
        private HtmlTextView factbook;

        private TextView tags;

        private LinearLayout visitButton;
        private TextView visitText;

        private View.OnClickListener regionOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (regionData != null) {
                    SparkleHelper.startExploring(context,
                            SparkleHelper.getIdFromName(regionData.name),
                            ExploreActivity.EXPLORE_REGION);
                }
            }
        };

        public FeaturedRegionCard(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.card_world_featured_header_container);
            regionName = itemView.findViewById(R.id.card_world_featured_region_name);
            nationCount = itemView.findViewById(R.id.card_world_featured_nation_count);
            flag = itemView.findViewById(R.id.card_world_featured_flag);

            waDelegate = itemView.findViewById(R.id.card_world_featured_wa);
            founder = itemView.findViewById(R.id.card_world_featured_founder);

            factbookHolder = itemView.findViewById(R.id.card_world_featured_factbook_holder);
            factbook = itemView.findViewById(R.id.card_world_featured_factbook);

            tags = itemView.findViewById(R.id.card_world_featured_tags);

            visitButton = itemView.findViewById(R.id.card_world_featured_region_button_holder);
            visitText = itemView.findViewById(R.id.card_world_featured_region_button_name);
        }

        public void init(BaseRegion r) {
            regionData = r;
            header.setOnClickListener(regionOnClick);
            regionName.setText(regionData.name);
            nationCount.setText(String.format(Locale.US,
                    SparkleHelper.CURRENCY_NOSUFFIX_TEMPLATE,
                    SparkleHelper.getPrettifiedNumber(regionData.numNations),
                    context.getResources().getQuantityString(R.plurals.nation_prop, regionData.numNations)));
            if (regionData.flagURL != null) {
                flag.setVisibility(View.VISIBLE);
                DashHelper.getInstance(context).loadImage(regionData.flagURL, flag, false);
            } else {
                flag.setVisibility(View.GONE);
            }

            RegionOverviewRecyclerAdapter.initWaDelegate(context, waDelegate, regionData.delegate, regionData.delegateVotes, regionData.lastUpdate);
            RegionOverviewRecyclerAdapter.initFounder(context, founder, regionData.founder, regionData.founded);

            if (regionData.factbook != null) {
                SparkleHelper.setStyledTextView(context, factbook, regionData.factbook, fragmentManager);
                factbookHolder.setVisibility(View.VISIBLE);
            }
            else {
                factbookHolder.setVisibility(View.GONE);
            }

            String tagCombine = SparkleHelper.joinStringList(regionData.tags, ", ");
            tags.setText(tagCombine);

            visitButton.setOnClickListener(regionOnClick);
            visitText.setText(String.format(Locale.US, context.getString(R.string.telegrams_region_explore), regionData.name));
        }
    }

    // Featured census
    public class FeaturedCensusCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CensusDetailedRank featuredCensus;

        private TextView censusTitle;
        private TextView censusUnit;
        private TextView censusScore;
        private ImageView censusBg;

        public FeaturedCensusCard(View itemView) {
            super(itemView);
            censusTitle = itemView.findViewById(R.id.card_world_featured_census);
            censusUnit = itemView.findViewById(R.id.card_world_featured_census_unit);
            censusScore = itemView.findViewById(R.id.card_world_featured_census_score);
            censusBg = itemView.findViewById(R.id.card_world_census_background);
            itemView.setOnClickListener(this);
        }

        public void init(CensusDetailedRank census) {
            featuredCensus = census;

            CensusScale censusType = SparkleHelper.getCensusScale(censusScale, featuredCensus.id);

            censusTitle.setText(censusType.name);
            censusUnit.setText(censusType.unit);
            censusScore.setText(String.format(Locale.US,
                    context.getString(R.string.world_census_score),
                    SparkleHelper.getPrettifiedShortSuffixedNumber(context, featuredCensus.score)));

            String bgUrl = RaraHelper.getBannerURL(censusType.banner);
            DashHelper.getInstance(context).loadImage(bgUrl, censusBg, false);
        }

        @Override
        public void onClick(View view) {
            SparkleHelper.startTrends(context, null, TrendsActivity.TREND_WORLD, featuredCensus.id);
        }
    }
}
