package com.lloydtorres.stately.region;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.PrimeActivity;

/**
 * Created by Lloyd on 2016-01-21.
 * The region fragment called either by StatelyActivity or ExploreRegionActivity.
 * Displays information about a target region, takes in a region name.
 * Does the search and refresh by itself.
 */
public class RegionFragment extends Fragment {
    // Constants used to determine view pager index.
    private final int OVERVIEW_TAB = 0;
    private final int COMMUNITY_TAB = 1;
    private final int GOV_TAB = 2;
    private final int HAPPEN_TAB = 3;

    private String regionName;
    private Region region;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    private Toolbar toolbar;
    private Activity mActivity;

    public void setRegionName(String n)
    {
        regionName = n;
    }

    @Override
    public void onAttach(Context context) {
        // Get activity for manipulation
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_region, container, false);

        // Restore state
        if (savedInstanceState != null)
        {
            regionName = savedInstanceState.getString("mRegionName");
            region = savedInstanceState.getParcelable("mRegionData");
        }

        if (regionName != null)
        {
            initToolbar(view);
        }

        return view;
    }

    /**
     * Initialize the toolbar and pass it back to the containing activity.
     * @param view
     */
    private void initToolbar(View view)
    {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_region);
        toolbar.setTitle("");

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    /**
     * Initialize the tabs layout and view pager
     * @param view
     */
    private void initTabs(View view)
    {
        // Initialize the ViewPager and set an adapter
        tabsPager = (ViewPager) view.findViewById(R.id.region_pager);
        tabsAdapter = new LayoutAdapter(getChildFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) view.findViewById(R.id.region_tabs);
        tabs.setViewPager(tabsPager);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("mRegionName", regionName);
        savedInstanceState.putParcelable("mRegionData", region);
    }

    @Override
    public void onDestroy()
    {
        // Decouple activity on destroy
        super.onDestroy();
        mActivity = null;
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
            switch(position)
            {
                default:
                    return new Fragment();
            }
        }
    }
}
