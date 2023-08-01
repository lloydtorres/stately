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

package com.lloydtorres.stately.zombie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.census.TrendsOnClickListener;
import com.lloydtorres.stately.dto.ZSuperweaponProgress;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.dto.ZombieControlData;
import com.lloydtorres.stately.dto.ZombieRegion;
import com.lloydtorres.stately.feed.BreakingNewsCard;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;

import java.util.Locale;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by Lloyd on 2016-10-15.
 * RecyclerView adapter for zombie control.
 */
public class ZombieControlRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for different cards
    public static final int CARD_ACTION = 0;
    public static final int CARD_CHART_NATION = 1;
    public static final int CARD_CHART_REGION = 2;
    public static final int CARD_HAPPENINGS = 3;

    private static final int NUMBER_OF_CARDS = 4;

    private ZombieControlActivity activity;
    private Context context;
    private FragmentManager fm;
    private ZombieControlData userData;
    private ZombieRegion regionData;
    private ZSuperweaponProgress progress;

    public ZombieControlRecyclerAdapter(ZombieControlActivity act, FragmentManager f,
                                        ZombieControlData zcd, ZombieRegion zr,
                                        ZSuperweaponProgress p) {
        activity = act;
        context = activity;
        fm = f;
        setContent(zcd, zr, p);
    }

    public void setContent(ZombieControlData zcd, ZombieRegion zr, ZSuperweaponProgress p) {
        userData = zcd;
        regionData = zr;
        progress = p;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case CARD_ACTION:
                View actionCard = inflater.inflate(R.layout.card_zombie_action, parent, false);
                viewHolder = new ZombieActionCard(actionCard);
                break;
            case CARD_CHART_NATION:
            case CARD_CHART_REGION:
                View chartCard = inflater.inflate(R.layout.card_zombie_chart, parent, false);
                viewHolder = new ZombieChartCard(chartCard);
                break;
            case CARD_HAPPENINGS:
                View happenCard = inflater.inflate(R.layout.card_world_breaking_news, parent,
                        false);
                viewHolder = new BreakingNewsCard(happenCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CARD_ACTION:
                ZombieActionCard actionCard = (ZombieActionCard) holder;
                actionCard.init(userData, progress);
                break;
            case CARD_CHART_NATION:
            case CARD_CHART_REGION:
                ZombieChartCard chartCard = (ZombieChartCard) holder;
                Zombie zombieData = position == CARD_CHART_NATION ? userData.zombieData :
                        regionData.zombieData;
                int mode = position == CARD_CHART_NATION ? ZombieChartCard.MODE_NATION_ZCONTROL :
                        ZombieChartCard.MODE_REGION_ZCONTROL;
                String target = position == CARD_CHART_NATION ? userData.name : regionData.name;
                chartCard.init(context, zombieData, mode, target);
                break;
            case CARD_HAPPENINGS:
                BreakingNewsCard breakingNewsCard = (BreakingNewsCard) holder;
                breakingNewsCard.init(context, context.getString(R.string.zombie_reports),
                        userData.events);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return NUMBER_OF_CARDS;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // View holder for action card
    public class ZombieActionCard extends RecyclerView.ViewHolder {
        public static final int PULSE_DURATION_ACTION = 4000;
        public static final int PULSE_DURATION_NOACTION = PULSE_DURATION_ACTION * 3;

        private ImageView headerBackground;
        private ImageView flag;
        private PulsatorLayout pulsator;

        private TextView action;
        private LinearLayout superweaponContent;

        private View divider;
        private LinearLayout button;
        private TextView buttonText;
        private LinearLayout trendsButton;
        private TextView trendsText;

        public ZombieActionCard(View itemView) {
            super(itemView);
            headerBackground = itemView.findViewById(R.id.card_zombie_action_header_background);
            flag = itemView.findViewById(R.id.card_zombie_action_header_flag);
            pulsator = itemView.findViewById(R.id.card_zombie_action_header_pulse);

            action = itemView.findViewById(R.id.card_zombie_action_content);
            superweaponContent = itemView.findViewById(R.id.card_zombie_action_superweapon_holder);

            divider = itemView.findViewById(R.id.view_divider);
            button = itemView.findViewById(R.id.card_zombie_action_button);
            buttonText = itemView.findViewById(R.id.card_zombie_action_button_text);
            trendsButton = itemView.findViewById(R.id.card_zombie_explore_button);
            trendsText = itemView.findViewById(R.id.card_zombie_trends_list);
        }

        public void init(final ZombieControlData data, final ZSuperweaponProgress progress) {
            LayoutInflater inflater = LayoutInflater.from(context);

            DashHelper dashie = DashHelper.getInstance(context);
            dashie.loadImage(NightmareHelper.getZombieBanner(data.zombieData.action),
                    headerBackground);
            dashie.loadImage(data.flagURL, flag);

            if (data.zombieData.action == null || (data.zombieData.survivors <= 0 && data.zombieData.zombies > 0
                    && !Zombie.ZACTION_ZOMBIE.equals(data.zombieData.action))) {
                pulsator.setDuration(PULSE_DURATION_NOACTION);
                pulsator.setColor(ContextCompat.getColor(context, R.color.colorChart1));
            } else {
                pulsator.setDuration(PULSE_DURATION_ACTION);
                switch (data.zombieData.action) {
                    case Zombie.ZACTION_MILITARY:
                        pulsator.setColor(ContextCompat.getColor(context, R.color.colorChart3));
                        break;
                    case Zombie.ZACTION_CURE:
                        pulsator.setColor(ContextCompat.getColor(context, R.color.colorChart0));
                        break;
                    case Zombie.ZACTION_ZOMBIE:
                        pulsator.setColor(ContextCompat.getColor(context, R.color.colorChart1));
                        break;
                }
            }
            // Only start if there are survivors/zombies left
            if (data.zombieData.survivors > 0 || data.zombieData.zombies > 0) {
                pulsator.start();
            }

            action.setText(data.zombieData.getActionDescription(context, data.name));

            superweaponContent.removeAllViews();

            if (progress != null) {
                if (progress.isAnySuperweaponVisible()) {
                    inflateEntry(inflater, superweaponContent,
                            context.getString(R.string.zombie_superweapon),
                            context.getString(R.string.zombie_superweapon_desc));
                }

                // Check for TZES availability
                if (progress.isTzesVisible()) {
                    StringBuilder sb = new StringBuilder();
                    if (progress.isTzesReady()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_current),
                                progress.tzesCurrentLevel));
                    }
                    if (progress.isTzesNextVisible()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_next),
                                progress.tzesNextLevel, progress.tzesNextProgress));
                    }
                    sb.append(context.getString(R.string.zombie_superweapon_tze_content));
                    inflateEntry(inflater, superweaponContent,
                            context.getString(R.string.zombie_superweapon_tze_title),
                            sb.toString());
                }

                // Check for cure availability
                if (progress.isCureVisible()) {
                    StringBuilder sb = new StringBuilder();
                    if (progress.isCureReady()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_current),
                                progress.cureCurrentLevel));
                    }
                    if (progress.isCureNextVisible()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_next),
                                progress.cureNextLevel, progress.cureNextProgress));
                    }
                    sb.append(context.getString(R.string.zombie_superweapon_cure_content));
                    inflateEntry(inflater, superweaponContent,
                            context.getString(R.string.zombie_superweapon_cure_title),
                            sb.toString());
                }

                // Check for horde availability
                if (progress.isHordeVisible()) {
                    StringBuilder sb = new StringBuilder();
                    if (progress.isHordeReady()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_horde_current),
                                progress.hordeCurrentLevel));
                    }
                    if (progress.isHordeNextVisible()) {
                        sb.append(String.format(Locale.US,
                                context.getString(R.string.zombie_superweapon_next),
                                progress.hordeNextLevel, progress.hordeNextProgress));
                    }
                    sb.append(context.getString(R.string.zombie_superweapon_horde_content));
                    inflateEntry(inflater, superweaponContent,
                            context.getString(R.string.zombie_superweapon_horde_title),
                            sb.toString());
                }
            }

            boolean isActionButtonVisible = true;
            boolean isExploreButtonVisible = progress != null && progress.isAnySuperweaponReady();

            // Setup button
            // Only show if there are still survivors or zombies
            if (data.zombieData.survivors > 0 || data.zombieData.zombies > 0) {
                button.setVisibility(View.VISIBLE);
                if (data.zombieData.action == null) {
                    buttonText.setText(context.getString(R.string.zombie_button_noaction));
                } else {
                    buttonText.setText(context.getString(R.string.zombie_button_change));
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.showDecisionDialog();
                    }
                });
            } else {
                button.setVisibility(View.GONE);
                button.setOnClickListener(null);
                isActionButtonVisible = false;
            }

            if (progress != null && progress.isAnySuperweaponReady()) {
                int censusTarget = TrendsActivity.CENSUS_ZDAY_ZOMBIES;
                switch (data.zombieData.action) {
                    case Zombie.ZACTION_MILITARY:
                    case Zombie.ZACTION_CURE:
                        censusTarget = TrendsActivity.CENSUS_ZDAY_ZOMBIES;
                        trendsText.setText(context.getString(R.string.zombie_button_zombie_list));
                        break;
                    case Zombie.ZACTION_ZOMBIE:
                        censusTarget = TrendsActivity.CENSUS_ZDAY_SURVIVORS;
                        trendsText.setText(context.getString(R.string.zombie_button_survivor_list));
                        break;
                }
                trendsButton.setOnClickListener(new TrendsOnClickListener(context,
                        SparkleHelper.getIdFromName(regionData.name), censusTarget,
                        TrendsActivity.TREND_REGION));
                trendsButton.setVisibility(View.VISIBLE);
            } else {
                trendsButton.setOnClickListener(null);
                trendsButton.setVisibility(View.GONE);
            }

            divider.setVisibility((isActionButtonVisible || isExploreButtonVisible) ?
                    View.VISIBLE : View.GONE);
        }

        /**
         * Inflates an entry for the superweapons section.
         * @param inflater
         * @param targetLayout
         * @param title
         * @param content
         */
        private void inflateEntry(LayoutInflater inflater, LinearLayout targetLayout,
                                  String title, String content) {
            View entryView = inflater.inflate(R.layout.view_cardentry, null);
            TextView titleView = entryView.findViewById(R.id.cardentry_label);
            TextView contentView = entryView.findViewById(R.id.cardentry_content);
            titleView.setText(title);
            contentView.setText(content);
            targetLayout.addView(entryView);
        }
    }
}
