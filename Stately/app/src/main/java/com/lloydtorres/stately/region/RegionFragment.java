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

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.CensusSortDialog;
import com.lloydtorres.stately.census.CensusSubFragment;
import com.lloydtorres.stately.core.IToolbarActivity;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.feed.HappeningsSubFragment;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import org.atteo.evo.inflector.English;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Lloyd on 2016-01-21.
 * The region fragment called either by StatelyActivity or ExploreRegionActivity.
 * Displays information about a target region, takes in a region name or object.
 * Can do the search and refresh by itself.
 */
public class RegionFragment extends Fragment {
    public static final String REGION_NAME_KEY = "mRegionName";
    public static final String REGION_DATA_KEY = "mRegionData";
    public static final String REGION_RMB_UNREAD_KEY = "rmbUnreadCountData";

    // Constants used to determine view pager index.
    private static final int OVERVIEW_TAB = 0;
    private static final int COMMUNITY_TAB = 1;
    private static final int CENSUS_TAB = 2;
    private static final int HAPPEN_TAB = 3;

    private String mRegionName;
    private Region mRegion;
    private String rmbUnreadCountText;

    // sub fragments
    private RegionOverviewSubFragment regionOverviewSubFragment;
    private RegionCommunitySubFragment regionCommunitySubFragment;
    private CensusSubFragment censusSubFragment;
    private HappeningsSubFragment regionHappeningsSubFragment;

    // variables used for mRegion views

    @BindView(R.id.region_name)
    TextView regionName;
    @BindView(R.id.region_pop)
    TextView regionPop;
    @BindView(R.id.region_flag)
    ImageView regionFlag;

    // variables used for tabs
    @BindView(R.id.region_tabs)
    PagerSlidingTabStrip tabs;
    @BindView(R.id.region_pager)
    ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    @BindView(R.id.toolbar_region)
    Toolbar toolbar;
    @BindView(R.id.region_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.collapsing_container_region)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.region_appbar)
    AppBarLayout appBarLayout;

    private Unbinder unbinder;

    public void setRegionName(String n) { mRegionName = n; }
    public void setRegion(Region r) { mRegion = r; }
    public void setRMBUnreadCountText(String countText) { rmbUnreadCountText = countText; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_region, container, false);
        unbinder = ButterKnife.bind(this, view);

        initToolbar(view);

        // Restore state
        if (savedInstanceState != null) {
            mRegionName = savedInstanceState.getString(REGION_NAME_KEY);
            mRegion = savedInstanceState.getParcelable(REGION_DATA_KEY);
            rmbUnreadCountText = savedInstanceState.getString(REGION_RMB_UNREAD_KEY);
        }

        if (mRegion != null) {
            mRegionName = mRegion.name;
            initRegionData(view);
        }
        else {
            updateRegion(view);
        }

        return view;
    }

    /**
     * Initialize the toolbar and pass it back to the containing activity.
     * @param view
     */
    private void initToolbar(View view) {
        toolbar.setTitle("");

        if (getActivity() != null && getActivity() instanceof IToolbarActivity) {
            ((IToolbarActivity) getActivity()).setToolbar(toolbar);
        }

        // Hide the title when the collapsing toolbar is expanded, only show when fully collapsed
        collapsingToolbarLayout.setTitle("");

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1 && mRegion != null) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset <= 0) {
                    if (mRegion != null) {
                        collapsingToolbarLayout.setTitle(mRegion.name);
                    }
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    public Toolbar getToolbar() { return toolbar; }

    /**
     * Initialize the tabs layout and view pager
     * @param view
     */
    private void initTabs(View view) {
        // Initialize the ViewPager and set an adapter
        tabsAdapter = new LayoutAdapter(getChildFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs.setViewPager(tabsPager);
    }

    /**
     * Load the entire fragment's contents
     * @param view
     */
    private void initRegionData(View view) {
        if (mRegion.flagURL != null) {
            regionFlag.setVisibility(View.VISIBLE);
            DashHelper.getInstance(getContext()).loadImage(mRegion.flagURL, regionFlag, true);
        }

        regionName.setText(mRegion.name);
        regionPop.setText(String.format(Locale.US, getString(R.string.val_currency), SparkleHelper.getPrettifiedNumber(mRegion.numNations), English.plural(getString(R.string.region_pop), mRegion.numNations)));

        regionOverviewSubFragment = new RegionOverviewSubFragment();
        regionOverviewSubFragment.setRegion(mRegion);

        regionCommunitySubFragment = new RegionCommunitySubFragment();
        regionCommunitySubFragment.setRegion(mRegion);
        regionCommunitySubFragment.setRMBUnreadCountText(rmbUnreadCountText);
        regionCommunitySubFragment.setMainFragmentView(view);

        censusSubFragment = new CensusSubFragment();
        ArrayList<CensusDetailedRank> censusHolder = new ArrayList<CensusDetailedRank>();
        censusHolder.addAll(mRegion.census);
        censusSubFragment.setTarget(SparkleHelper.getIdFromName(mRegion.name));
        censusSubFragment.setCensusData(censusHolder);
        censusSubFragment.setMode(CensusSortDialog.CENSUS_MODE_REGION);

        regionHappeningsSubFragment = new HappeningsSubFragment();
        ArrayList<Event> events = new ArrayList<Event>(mRegion.happenings);
        events.addAll(mRegion.history);
        regionHappeningsSubFragment.setHappenings(events);

        initTabs(view);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(REGION_NAME_KEY, mRegionName);
        savedInstanceState.putParcelable(REGION_DATA_KEY, mRegion);
        savedInstanceState.putString(REGION_RMB_UNREAD_KEY, rmbUnreadCountText);
    }

    private void updateRegion(final View view) {
        progressBar.setVisibility(View.VISIBLE);

        String targetURL = String.format(Locale.US, Region.QUERY, SparkleHelper.getIdFromName(mRegionName));

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Region regionResponse = null;
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        Persister serializer = new Persister();
                        try {
                            regionResponse = Region.parseRegionXML(serializer, response);

                            mRegion = regionResponse;
                            initRegionData(view);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                SparkleHelper.logError(error.toString());
                progressBar.setVisibility(View.GONE);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.region_404));
                }
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // For formatting the tab slider
    public class LayoutAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = getResources().getStringArray(R.array.region_tabs);

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
                    return regionOverviewSubFragment;
                case COMMUNITY_TAB:
                    return regionCommunitySubFragment;
                case CENSUS_TAB:
                    return censusSubFragment;
                case HAPPEN_TAB:
                    return regionHappeningsSubFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
