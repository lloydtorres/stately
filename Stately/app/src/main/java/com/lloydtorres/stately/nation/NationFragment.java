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

package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.CensusSortDialog;
import com.lloydtorres.stately.census.CensusSubFragment;
import com.lloydtorres.stately.core.DetachFragment;
import com.lloydtorres.stately.core.IToolbarActivity;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.feed.HappeningsSubFragment;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-01-13.
 * The main nation fragment called either by StatelyActivity or ExploreNationActivity.
 * Displays information about a target nation, takes in a nation object.
 */
public class NationFragment extends DetachFragment {
    public static final String NATION_DATA_KEY = "mNationData";

    // Constants used to determine view pager index.
    private static final int OVERVIEW_TAB = 0;
    private static final int PEOPLE_TAB = 1;
    private static final int GOV_TAB = 2;
    private static final int ECONOMY_TAB = 3;
    private static final int RANKINGS_TAB = 4;
    private static final int HAPPEN_TAB = 5;

    private Nation mNation;

    // sub fragments
    private OverviewSubFragment overviewSubFragment;
    private PeopleSubFragment peopleSubFragment;
    private GovernmentSubFragment governmentSubFragment;
    private EconomySubFragment economySubFragment;
    private CensusSubFragment censusSubFragment;
    private HappeningsSubFragment happeningsSubFragment;

    // variables used for nation views
    private TextView nationName;
    private TextView nationPrename;
    private ImageView nationBanner;
    private ImageView nationFlag;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    private Toolbar toolbar;

    public void setNation(Nation n) {
        mNation = n;
    }

    public void updateOverviewData(Nation n) {
        mNation = n;
        if (overviewSubFragment == null) {
            overviewSubFragment = new OverviewSubFragment();
            overviewSubFragment.setNation(mNation);
        } else {
            overviewSubFragment.setNation(mNation);
            overviewSubFragment.forceRefreshData();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nation, container, false);

        // Restore state
        if (savedInstanceState != null && mNation == null) {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        initToolbar(view);
        if (mNation != null) {
            getAllNationViews(view);
        }

        return view;
    }

    /**
     * Initialize the toolbar and pass it back to the containing activity.
     * @param view
     */
    private void initToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_nation);
        toolbar.setTitle("");

        if (getActivity() != null && getActivity() instanceof IToolbarActivity) {
            ((IToolbarActivity) getActivity()).setToolbar(toolbar);
        }

        // Hide the title when the collapsing toolbar is expanded, only show when fully collapsed
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_container);
        collapsingToolbarLayout.setTitle("");

        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.nation_appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset <= 0) {
                    if (mNation != null) {
                        collapsingToolbarLayout.setTitle(mNation.name);
                    }
                    isShow = true;
                }
                else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    /**
     * Initialize the tabs layout and view pager
     * @param view
     */
    private void initTabs(View view) {
        // Initialize the ViewPager and set an adapter
        tabsPager = (ViewPager) view.findViewById(R.id.nation_pager);
        tabsAdapter = new LayoutAdapter(getChildFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) view.findViewById(R.id.nation_tabs);
        tabs.setViewPager(tabsPager);
    }

    /**
     * Get the views for the nation elements within the collapsing toolbar
     * @param view
     */
    private void getAllNationViews(View view) {
        nationName = (TextView) view.findViewById(R.id.nation_name);
        nationPrename = (TextView) view.findViewById(R.id.nation_prename);
        nationBanner = (ImageView) view.findViewById(R.id.nation_banner);
        nationFlag = (ImageView) view.findViewById(R.id.nation_flag);
        initNationData(view);
    }

    /**
     * Load the entire fragment's contents
     * @param view
     */
    public void initNationData(View view) {
        nationName.setText(mNation.name);
        nationPrename.setText(SparkleHelper.getHtmlFormatting(mNation.prename).toString());

        DashHelper dashie = DashHelper.getInstance(getContext());

        if (NightmareHelper.getIsZDayActive(getContext()) && mNation.zombieData != null) {
            dashie.loadImage(NightmareHelper.getZombieBanner(mNation.zombieData.action), nationBanner, false);
        } else {
            dashie.loadImage(RaraHelper.getBannerURL(mNation.bannerKey), nationBanner, false);
        }

        dashie.loadImage(mNation.flagURL, nationFlag, true);

        overviewSubFragment = new OverviewSubFragment();
        overviewSubFragment.setNation(mNation);

        peopleSubFragment = new PeopleSubFragment();
        peopleSubFragment.setNation(mNation);

        governmentSubFragment = new GovernmentSubFragment();
        governmentSubFragment.setNation(mNation);

        economySubFragment = new EconomySubFragment();
        economySubFragment.setNation(mNation);

        censusSubFragment = new CensusSubFragment();
        ArrayList<CensusDetailedRank> censusHolder = new ArrayList<CensusDetailedRank>();
        censusHolder.addAll(mNation.census);
        censusSubFragment.setTarget(SparkleHelper.getIdFromName(mNation.name));
        censusSubFragment.setCensusData(censusHolder);
        censusSubFragment.setMode(CensusSortDialog.CENSUS_MODE_NATION);

        happeningsSubFragment = new HappeningsSubFragment();
        ArrayList<Event> nationHappenings = new ArrayList<Event>(mNation.events);
        happeningsSubFragment.setHappenings(nationHappenings);

        initTabs(view);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null) {
            savedInstanceState.putParcelable(NATION_DATA_KEY, mNation);
        }
    }

    // For formatting the tab slider
    public class LayoutAdapter extends FragmentStatePagerAdapter {

        private final String[] TITLES = getResources().getStringArray(R.array.nation_tabs);

        public LayoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case OVERVIEW_TAB:
                    return overviewSubFragment;
                case PEOPLE_TAB:
                    return peopleSubFragment;
                case GOV_TAB:
                    return governmentSubFragment;
                case ECONOMY_TAB:
                    return economySubFragment;
                case RANKINGS_TAB:
                    return censusSubFragment;
                case HAPPEN_TAB:
                    return happeningsSubFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
