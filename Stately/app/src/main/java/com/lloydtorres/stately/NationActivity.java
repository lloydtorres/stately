package com.lloydtorres.stately;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.astuetz.PagerSlidingTabStrip;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.simpleframework.xml.core.Persister;

public class NationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String APP_TAG = "com.lloydtorres.stately";
    private final String BANNER_TEMPLATE = "http://www.nationstates.net/images/banners/%s.jpg";
    private final int OVERVIEW_TAB = 0;
    private final int PEOPLE_TAB = 1;
    private final int GOV_TAB = 2;
    private final int ECONOMY_TAB = 3;
    private final int HAPPEN_TAB = 4;

    private Nation mNation;
    private OverviewFragment overviewFragment;
    private PeopleFragment peopleFragment;
    private GovernmentFragment governmentFragment;
    private EconomyFragment economyFragment;

    // variables used for nation views
    private TextView nationName;
    private TextView nationPrename;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;
    private TextView waState;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nation);

        if (getIntent() != null)
        {
            mNation = (Nation) getIntent().getParcelableExtra("mNationData");
        }
        if (mNation == null && savedInstanceState != null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }


        initToolbar();
        getAllNationViews();
        initNationData();
    }

    private void initToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        collapsingToolbarLayout.setTitle("");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.nation_appbar);
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
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initTabs()
    {
        // Initialize the ViewPager and set an adapter
        tabsPager = (ViewPager) findViewById(R.id.nation_pager);
        tabsAdapter = new LayoutAdapter(getSupportFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) findViewById(R.id.nation_tabs);
        tabs.setViewPager(tabsPager);
    }

    private void getAllNationViews()
    {
        nationName = (TextView)findViewById(R.id.nation_name);
        nationPrename = (TextView)findViewById(R.id.nation_prename);
        nationBanner = (ImageView)findViewById(R.id.nation_banner);
        nationFlag = (RoundedImageView)findViewById(R.id.nation_flag);
        waState = (TextView)findViewById(R.id.nation_wa_status);
    }

    public void initNationData() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationName.setText(mNation.name);
        nationPrename.setText(Html.fromHtml(mNation.prename).toString());
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, mNation.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(mNation.flagURL, nationFlag, imageOptions);

        if (mNation.waState.equals(getString(R.string.nation_wa_member)))
        {
            waState.setVisibility(View.VISIBLE);
        }

        overviewFragment = new OverviewFragment();
        overviewFragment.setNation(mNation);

        peopleFragment = new PeopleFragment();
        peopleFragment.setNation(mNation);

        governmentFragment = new GovernmentFragment();
        governmentFragment.setNation(mNation);

        economyFragment = new EconomyFragment();
        economyFragment.setNation(mNation);

        initTabs();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable("mNationData", mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                    return overviewFragment;
                case PEOPLE_TAB:
                    return peopleFragment;
                case GOV_TAB:
                    return governmentFragment;
                case ECONOMY_TAB:
                    return economyFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
