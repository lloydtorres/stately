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
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-10-15.
 * A recycler view card showing stats about a zombie infection, along with some
 * buttons if enabled.
 */

public class ZombieChartCard extends RecyclerView.ViewHolder {
    // Different card modes, changes visibility of buttons
    public static final int MODE_NATION_ZCONTROL = 0;
    public static final int MODE_NATION_DEFAULT = 1;
    public static final int MODE_NATION_SUPERWEAPON = 2;
    public static final int MODE_REGION_ZCONTROL = 3;
    public static final int MODE_REGION_DEFAULT = 4;

    private Context context;
    private ExploreActivity exploreActivity;

    private TextView title;
    private TextView action;

    private TextView nullData;
    private PieChart chart;

    private View divider;

    private LinearLayout genericButton;
    private ImageView genericButtonIcon;
    private TextView genericButtonText;

    private LinearLayout missileButton;
    private ImageView missileIcon;
    private ProgressBar missileProgressBar;

    public ZombieChartCard(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.card_zombie_chart_title);
        action = itemView.findViewById(R.id.card_zombie_chart_action);

        nullData = itemView.findViewById(R.id.card_zombie_chart_null);
        chart = itemView.findViewById(R.id.card_zombie_chart);

        divider = itemView.findViewById(R.id.view_divider);

        genericButton = itemView.findViewById(R.id.card_zombie_chart_button_generic);
        genericButtonIcon = itemView.findViewById(R.id.card_zombie_chart_button_generic_icon);
        genericButtonText = itemView.findViewById(R.id.card_zombie_chart_button_generic_text);

        missileButton = itemView.findViewById(R.id.card_zombie_chart_button_missile);
        missileIcon = itemView.findViewById(R.id.card_zombie_chart_missile_icon);
        missileProgressBar = itemView.findViewById(R.id.card_zombie_chart_missile_progressbar);
    }

    public void initExplore(ExploreActivity act, final Zombie zombieData, final int mode, final String target) {
        exploreActivity = act;
        init(act, zombieData, mode, target);

        if (mode == MODE_NATION_SUPERWEAPON) {
            missileButton.setVisibility(View.VISIBLE);

            // Set default visibilities
            missileIcon.setVisibility(View.VISIBLE);
            missileProgressBar.setVisibility(View.GONE);

            missileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exploreActivity != null) {
                        exploreActivity.showSuperweaponDialog(ZombieChartCard.this);
                    }
                }
            });
        }
    }

    public void setIsLoading(boolean isLoading) {
        missileIcon.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        missileProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    public void init(final Context c, final Zombie zombieData, final int mode, final String target) {
        context = c;

        // Set title
        if (mode == MODE_NATION_ZCONTROL || mode == MODE_REGION_ZCONTROL) {
            title.setText(String.format(Locale.US,
                    context.getString(R.string.zombie_report_template),
                    target));
        } else {
            title.setText(context.getString(R.string.zombie_report_title));
        }

        if (mode == MODE_NATION_DEFAULT || mode == MODE_NATION_SUPERWEAPON) {
            action.setVisibility(View.VISIBLE);
            action.setText(zombieData.getActionDescription(context, target));
        } else {
            action.setVisibility(View.GONE);
        }

        // Init the chart
        initZombieChart(context, zombieData);

        // Changes which buttons are visible
        // Set everything to the default first
        divider.setVisibility(View.VISIBLE);
        genericButtonIcon.setImageResource(R.drawable.ic_zombie_control);
        genericButtonText.setText(context.getString(R.string.zombie_control));
        genericButton.setVisibility(View.VISIBLE);
        genericButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent zombieControlLaunch = new Intent(context, ZombieControlActivity.class);
                context.startActivity(zombieControlLaunch);
            }
        });

        missileButton.setVisibility(View.GONE);
        missileButton.setOnClickListener(null);

        switch (mode) {
            case MODE_NATION_ZCONTROL:
                divider.setVisibility(View.GONE);
                genericButton.setVisibility(View.GONE);
                genericButton.setOnClickListener(null);
                break;
            case MODE_REGION_ZCONTROL:
                genericButtonIcon.setImageResource(R.drawable.ic_region_white);
                genericButtonText.setText(context.getString(R.string.card_region_rmb));
                genericButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SparkleHelper.startRegionRMB(context, target);
                    }
                });
                break;
        }
    }

    private void initZombieChart(Context c, Zombie zombieData) {
        float popTotal = zombieData.survivors + zombieData.zombies + zombieData.dead;

        if (popTotal > 0) {
            nullData.setVisibility(View.GONE);
            chart.setVisibility(View.VISIBLE);

            List<PieEntry> chartEntries = new ArrayList<PieEntry>();
            List<Integer> chartColours = new ArrayList<Integer>();

            // Set data
            if (zombieData.survivors > 0) {
                float popSurvivors = (zombieData.survivors * 100f)/popTotal;
                chartEntries.add(new PieEntry(popSurvivors, c.getString(R.string.zombie_survivors)));
                chartColours.add(ContextCompat.getColor(context, R.color.colorChart3));
            }

            if (zombieData.zombies > 0) {
                float popZombies = (zombieData.zombies * 100f)/popTotal;
                chartEntries.add(new PieEntry(popZombies, c.getString(R.string.zombie_infected)));
                chartColours.add(ContextCompat.getColor(context, R.color.colorChart1));
            }

            if (zombieData.dead > 0) {
                float popDead = (zombieData.dead * 100f)/popTotal;
                chartEntries.add(new PieEntry(popDead, c.getString(R.string.zombie_dead)));
                chartColours.add(ContextCompat.getColor(context, R.color.colorChart20));
            }

            // Set colour and disable chart labels
            PieDataSet dataSet = new PieDataSet(chartEntries, "");
            dataSet.setDrawValues(false);
            dataSet.setColors(chartColours);
            PieData dataFull = new PieData(dataSet);

            // formatting
            chart = RaraHelper.getFormattedPieChart(c, chart, true);
            chart.setData(dataFull);
            chart.invalidate();
        } else {
            nullData.setVisibility(View.VISIBLE);
            chart.setVisibility(View.GONE);
        }
    }
}
