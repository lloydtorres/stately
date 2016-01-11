package com.lloydtorres.stately;

import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class NationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NationAsyncResponse {

    private final String BANNER_TEMPLATE = "http://www.nationstates.net/images/banners/%s.jpg";

    // variables used for nation views
    private TextView nationName;
    private TextView nationPrename;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        // Initialize the ViewPager and set an adapter
        tabsPager = (ViewPager) findViewById(R.id.nation_pager);
        tabsAdapter = new LayoutAdapter(getSupportFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) findViewById(R.id.nation_tabs);
        tabs.setViewPager(tabsPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getAllNationViews("greater_tern");
    }

    private void getAllNationViews(String name)
    {
        NationAsyncTask nAsyncTask = new NationAsyncTask(this, name);
        nAsyncTask.execute();

        nationName = (TextView)findViewById(R.id.nation_name);
        nationPrename = (TextView)findViewById(R.id.nation_prename);
        nationBanner = (ImageView)findViewById(R.id.nation_banner);
        nationFlag = (RoundedImageView)findViewById(R.id.nation_flag);
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

    @Override
    public void nationAsyncResult(Nation n) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        nationName.setText(n.name);
        nationPrename.setText(n.prename);
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, n.bannerKey), nationBanner);
        imageLoader.displayImage(n.flagURL,nationFlag);
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
            return new Fragment();
        }
    }
}
