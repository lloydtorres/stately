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

        initToolbar();

        getAllNationViews("greater_tern");
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
                    if (mNation != null)
                    {
                        collapsingToolbarLayout.setTitle(mNation.name);
                    }
                    isShow = true;
                }
                else if(isShow) {
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

    private void getAllNationViews(String name)
    {
        nationName = (TextView)findViewById(R.id.nation_name);
        nationPrename = (TextView)findViewById(R.id.nation_prename);
        nationBanner = (ImageView)findViewById(R.id.nation_banner);
        nationFlag = (RoundedImageView)findViewById(R.id.nation_flag);
        waState = (TextView)findViewById(R.id.nation_wa_status);

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(Nation.QUERY, name);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Nation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            nationResponse = serializer.read(Nation.class, response);
                        }
                        catch (Exception e) {
                            Log.e(APP_TAG, e.toString());
                        }

                        initNationData(nationResponse);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(APP_TAG, error.toString());
                    }
                });

        queue.add(stringRequest);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void initNationData(Nation n) {
        mNation = n;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationName.setText(n.name);
        nationPrename.setText(Html.fromHtml(n.prename).toString());
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, n.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(n.flagURL, nationFlag, imageOptions);

        if (n.waState.equals(getString(R.string.nation_wa_member)))
        {
            waState.setVisibility(View.VISIBLE);
        }

        overviewFragment = new OverviewFragment();
        overviewFragment.setNation(n);

        peopleFragment = new PeopleFragment();
        peopleFragment.setNation(n);

        governmentFragment = new GovernmentFragment();
        governmentFragment.setNation(n);

        economyFragment = new EconomyFragment();
        economyFragment.setNation(n);

        initTabs();
    }

    // For formatting the tab slider
    public class LayoutAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {   getString(R.string.nation_tab_overview),
                                            getString(R.string.nation_tab_people),
                                            getString(R.string.nation_tab_gov),
                                            getString(R.string.nation_tab_economy),
                                            getString(R.string.nation_tab_happen)
                                        };

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
