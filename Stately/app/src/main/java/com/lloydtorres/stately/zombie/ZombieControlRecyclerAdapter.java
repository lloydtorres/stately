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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.dto.ZombieControlData;
import com.lloydtorres.stately.dto.ZombieRegion;
import com.lloydtorres.stately.feed.BreakingNewsCard;
import com.lloydtorres.stately.helpers.network.DashHelper;

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

    public ZombieControlRecyclerAdapter(ZombieControlActivity act, FragmentManager f, ZombieControlData zcd, ZombieRegion zr) {
        activity = act;
        context = activity;
        fm = f;
        setContent(zcd, zr);
    }

    public void setContent(ZombieControlData zcd, ZombieRegion zr) {
        userData = zcd;
        regionData = zr;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch(viewType) {
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
                View happenCard = inflater.inflate(R.layout.card_world_breaking_news, parent, false);
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
                actionCard.init(userData);
                break;
            case CARD_CHART_NATION:
            case CARD_CHART_REGION:
                ZombieChartCard chartCard = (ZombieChartCard) holder;
                Zombie zombieData = position == CARD_CHART_NATION ? userData.zombieData : regionData.zombieData;
                int mode = position == CARD_CHART_NATION ? ZombieChartCard.MODE_NATION_ZCONTROL : ZombieChartCard.MODE_REGION_ZCONTROL;
                String target = position == CARD_CHART_NATION ? userData.name : regionData.name;
                chartCard.init(context, zombieData, mode, target);
                break;
            case CARD_HAPPENINGS:
                BreakingNewsCard breakingNewsCard = (BreakingNewsCard) holder;
                breakingNewsCard.init(context, userData.events);
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
        private LinearLayout button;
        private TextView buttonText;
        private LinearLayout exploreButton;

        public ZombieActionCard(View itemView) {
            super(itemView);
            headerBackground = (ImageView) itemView.findViewById(R.id.card_zombie_action_header_background);
            flag = (ImageView) itemView.findViewById(R.id.card_zombie_action_header_flag);
            pulsator = (PulsatorLayout) itemView.findViewById(R.id.card_zombie_action_header_pulse);

            action = (TextView) itemView.findViewById(R.id.card_zombie_action_content);
            superweaponContent = (LinearLayout) itemView.findViewById(R.id.card_zombie_action_superweapon_holder);

            button = (LinearLayout) itemView.findViewById(R.id.card_zombie_action_button);
            buttonText = (TextView) itemView.findViewById(R.id.card_zombie_action_button_text);
            exploreButton = (LinearLayout) itemView.findViewById(R.id.card_zombie_explore_button);
        }

        public void init(final ZombieControlData data) {
            LayoutInflater inflater = LayoutInflater.from(context);

            DashHelper dashie = DashHelper.getInstance(context);
            dashie.loadImage(NightmareHelper.getZombieBanner(data.zombieData.action), headerBackground, false);
            dashie.loadImage(data.flagURL, flag, true);

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

            // @TODO: Add superweapon descriptions depending on what's available
            superweaponContent.removeAllViews();
            String superweaponDesc = context.getString(R.string.zombie_superweapon_desc) + context.getString(R.string.zombie_superweapon_none);
            inflateEntry(inflater, superweaponContent, context.getString(R.string.zombie_superweapon), superweaponDesc);

            // Setup button
            if (data.zombieData.action == null) {
                buttonText.setText(context.getString(R.string.zombie_button_noaction));
            } else {
                buttonText.setText(context.getString(R.string.zombie_button_change));
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // @TODO Callback
                }
            });

            // @TODO: Setup explore button if cure is available

            //
            /**
             exploreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExploreDialog exploreDialog = new ExploreDialog();
                    exploreDialog.show(fm, ExploreDialog.DIALOG_TAG);
                }
            });**/
        }

        /**
         * Inflates an entry for the superweapons section.
         * @param inflater
         * @param targetLayout
         * @param title
         * @param content
         */
        private void inflateEntry(LayoutInflater inflater, LinearLayout targetLayout, String title, String content) {
            View entryView = inflater.inflate(R.layout.view_cardentry, null);
            TextView titleView = (TextView) entryView.findViewById(R.id.cardentry_label);
            TextView contentView = (TextView) entryView.findViewById(R.id.cardentry_content);
            titleView.setText(title);
            contentView.setText(content);
            targetLayout.addView(entryView);
        }
    }
}
