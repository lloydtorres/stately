package com.lloydtorres.stately.nation;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-13.
 * The main nation fragment called either by StatelyActivity or ExploreNationActivity.
 * Displays information about a target nation, takes in a nation object.
 */
public class NationFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNationData";

    // Constants used to determine view pager index.
    private final int OVERVIEW_TAB = 0;
    private final int PEOPLE_TAB = 1;
    private final int GOV_TAB = 2;
    private final int ECONOMY_TAB = 3;
    private final int HAPPEN_TAB = 4;

    private Nation mNation;

    // sub fragments
    private OverviewSubFragment overviewSubFragment;
    private PeopleSubFragment peopleSubFragment;
    private GovernmentSubFragment governmentSubFragment;
    private EconomySubFragment economySubFragment;
    private HappeningSubFragment happeningSubFragment;

    // variables used for nation views
    private TextView nationName;
    private TextView nationPrename;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    private Toolbar toolbar;
    private Activity mActivity;

    public void setNation(Nation n)
    {
        mNation = n;
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
        View view = inflater.inflate(R.layout.fragment_nation, container, false);

        SparkleHelper.initAd(view, R.id.ad_nation_fragment);

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        initToolbar(view);
        if (mNation != null)
        {
            getAllNationViews(view);
        }

        return view;
    }

    /**
     * Initialize the toolbar and pass it back to the containing activity.
     * @param view
     */
    private void initToolbar(View view)
    {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_nation);
        toolbar.setTitle("");

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
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
    private void initTabs(View view)
    {
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
    private void getAllNationViews(View view)
    {
        nationName = (TextView) view.findViewById(R.id.nation_name);
        nationPrename = (TextView) view.findViewById(R.id.nation_prename);
        nationBanner = (ImageView) view.findViewById(R.id.nation_banner);
        nationFlag = (RoundedImageView) view.findViewById(R.id.nation_flag);

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
        dashie.loadImage(SparkleHelper.getBannerURL(mNation.bannerKey), nationBanner, false);
        dashie.loadImage(mNation.flagURL, nationFlag, true);

        overviewSubFragment = new OverviewSubFragment();
        overviewSubFragment.setNation(mNation);

        peopleSubFragment = new PeopleSubFragment();
        peopleSubFragment.setNation(mNation);

        governmentSubFragment = new GovernmentSubFragment();
        governmentSubFragment.setNation(mNation);

        economySubFragment = new EconomySubFragment();
        economySubFragment.setNation(mNation);

        happeningSubFragment = new HappeningSubFragment();
        happeningSubFragment.setNation(mNation);

        initTabs(view);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable(NATION_DATA_KEY, mNation);
        }
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
            switch(position)
            {
                case OVERVIEW_TAB:
                    return overviewSubFragment;
                case PEOPLE_TAB:
                    return peopleSubFragment;
                case GOV_TAB:
                    return governmentSubFragment;
                case ECONOMY_TAB:
                    return economySubFragment;
                case HAPPEN_TAB:
                    return happeningSubFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
