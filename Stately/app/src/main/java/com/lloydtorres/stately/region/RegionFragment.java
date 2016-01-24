package com.lloydtorres.stately.region;

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
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.astuetz.PagerSlidingTabStrip;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.atteo.evo.inflector.English;
import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-21.
 * The region fragment called either by StatelyActivity or ExploreRegionActivity.
 * Displays information about a target region, takes in a region name or object.
 * Can do the search and refresh by itself.
 */
public class RegionFragment extends Fragment {
    // Constants used to determine view pager index.
    private final int OVERVIEW_TAB = 0;
    private final int COMMUNITY_TAB = 1;
    private final int GOV_TAB = 2;
    private final int HAPPEN_TAB = 3;

    private String mRegionName;
    private Region mRegion;
    private boolean noRefresh;

    // sub fragments
    private RegionOverviewSubFragment regionOverviewSubFragment;
    private RegionGovernanceSubFragment regionGovernanceSubFragment;
    private RegionHappeningSubFragment regionHappeningSubFragment;

    // variables used for mRegion views
    private TextView regionName;
    private TextView regionPop;
    private RoundedImageView regionFlag;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    private Toolbar toolbar;
    private Activity mActivity;

    public void setRegionName(String n)
    {
        mRegionName = n;
    }
    public void setRegion(Region r)
    {
        mRegion = r;
    }
    public void setRefreshState(boolean b)
    {
        noRefresh = b;
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

        initToolbar(view);

        // Restore state
        if (savedInstanceState != null)
        {
            mRegionName = savedInstanceState.getString("mRegionName");
            mRegion = savedInstanceState.getParcelable("mRegionData");
        }

        if (mRegion != null)
        {
            mRegionName = mRegion.name;
            getAllRegionViews(view);
        }
        else
        {
            updateRegion(view);
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

        // Hide the title when the collapsing toolbar is expanded, only show when fully collapsed
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_container_region);
        collapsingToolbarLayout.setTitle("");

        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.region_appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
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

    /**
     * Get the views for the region elements at the top of the fragment
     * @param view
     */
    private void getAllRegionViews(View view)
    {
        regionName = (TextView) view.findViewById(R.id.region_name);
        regionPop = (TextView) view.findViewById(R.id.region_pop);
        regionFlag = (RoundedImageView) view.findViewById(R.id.region_flag);

        initRegionData(view);
    }

    /**
     * Load the entire fragment's contents
     * @param view
     */
    private void initRegionData(View view)
    {
        if (mRegion.flagURL != null)
        {
            regionFlag.setVisibility(View.VISIBLE);

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).build();
            ImageLoader.getInstance().init(config);
            ImageLoader imageLoader = ImageLoader.getInstance();

            // Fade image in on finish load
            DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

            imageLoader.displayImage(mRegion.flagURL, regionFlag, imageOptions);
        }

        regionName.setText(mRegion.name);
        regionPop.setText(String.format(getString(R.string.val_currency), SparkleHelper.getPrettifiedNumber(mRegion.numNations), English.plural(getString(R.string.region_pop), mRegion.numNations)));

        regionOverviewSubFragment = new RegionOverviewSubFragment();
        regionOverviewSubFragment.setRegion(mRegion);

        regionGovernanceSubFragment = new RegionGovernanceSubFragment();
        regionGovernanceSubFragment.setRegion(mRegion);

        regionHappeningSubFragment = new RegionHappeningSubFragment();
        regionHappeningSubFragment.setRegion(mRegion);

        initTabs(view);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("mRegionName", mRegionName);
        savedInstanceState.putParcelable("mRegionData", mRegion);
    }

    @Override
    public void onResume()
    {
        // Redownload region data on resume
        super.onResume();
        if (!noRefresh)
        {
            updateRegion(getView());
        }
    }

    @Override
    public void onDestroy()
    {
        // Decouple activity on destroy
        super.onDestroy();
        mActivity = null;
    }

    private void updateRegion(View view)
    {
        final View fView = view;

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String targetURL = String.format(Region.QUERY, mRegionName.toLowerCase().replace(" ", "_"));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Region regionResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            regionResponse = serializer.read(Region.class, response);

                            // Switch flag URL to https
                            if (regionResponse.flagURL != null)
                            {
                                regionResponse.flagURL = regionResponse.flagURL.replace("http://","https://");
                            }

                            mRegion = regionResponse;
                            getAllRegionViews(fView);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.region_404));
                }
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.error_generic));
                }
            }
        });

        queue.add(stringRequest);
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
                case OVERVIEW_TAB:
                    return regionOverviewSubFragment;
                case GOV_TAB:
                    return regionGovernanceSubFragment;
                case HAPPEN_TAB:
                    return regionHappeningSubFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
