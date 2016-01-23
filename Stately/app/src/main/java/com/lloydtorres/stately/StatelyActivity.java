package com.lloydtorres.stately;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.GenericFragment;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;
import com.lloydtorres.stately.wa.AssemblyMainFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.simpleframework.xml.core.Persister;

/**
 * The core Stately activity. This is where the magic happens.
 */
public class StatelyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PrimeActivity {

    // A list of navdrawer options that shouldn't switch the nav position on select.
    private final int[] noSelect = {    R.id.nav_explore,
                                        R.id.nav_settings,
                                        R.id.nav_logout
                                    };

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private int currentPosition = R.id.nav_nation;

    private Nation mNation;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;
    private TextView nationNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stately);

        // Get nation object from intent or restore state
        if (getIntent() != null)
        {
            mNation = getIntent().getParcelableExtra("mNationData");
        }
        if (mNation == null && savedInstanceState != null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_app_bar);
        setToolbar(toolbar);
        getSupportActionBar().hide();
        getSupportActionBar().setTitle("");
        initNavigationView();
    }

    /**
     * Method used by associated fragments to set their own toolbars.
     * @param t
     */
    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, t, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Initialize the navigation drawer.
     * Set the nation fragment as the current view.
     */
    private void initNavigationView()
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        initNavBanner();
        NationFragment nf = getNationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coordinator_app_bar, nf)
                .commit();
    }

    /**
     * Initialize the banner in the navigation drawer with data from Nation.
     */
    private void initNavBanner()
    {
        View view = navigationView.getHeaderView(0);
        nationBanner = (ImageView) view.findViewById(R.id.nav_banner_back);
        nationFlag = (RoundedImageView) view.findViewById(R.id.nav_flag);
        nationNameView = (TextView) view.findViewById(R.id.nav_nation_name);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationNameView.setText(mNation.name);
        imageLoader.displayImage(SparkleHelper.getBannerURL(mNation.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(mNation.flagURL, nationFlag, imageOptions);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable("mNationData", mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }
    }

    @Override
    public void onResume()
    {
        // Redownload nation data on resume
        super.onResume();
        updateNation();
    }

    @Override
    public void onBackPressed() {
        // Handle back presses
        // Close drawer if open, call super function otherwise

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentManager fm = getSupportFragmentManager();

        // Main selections
        if (id != currentPosition && !isNoSelect(id))
        {
            currentPosition = id;
            Fragment fChoose;

            switch (id)
            {
                case R.id.nav_nation:
                    // Choose Nation
                    fChoose = getNationFragment();
                    break;
                case R.id.nav_region:
                    fChoose = getRegionFragment();
                    break;
                case R.id.nav_wa:
                    // Chose World Assembly
                    fChoose = new AssemblyMainFragment();
                    break;
                default:
                    // Backup
                    fChoose = new GenericFragment();
                    break;
            }

            // Switch fragments
            fm.beginTransaction()
                    .replace(R.id.coordinator_app_bar, fChoose)
                    .commit();

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        // Other selections
        else if (isNoSelect(id))
        {
            switch (id)
            {
                case R.id.nav_explore:
                    // Open explore dialog
                    explore();
                    break;
                case R.id.nav_logout:
                    // Start logout process
                    logout();
                    break;
                default:
                    break;
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Get a new nation fragment
     * @return A new nation fragment
     */
    private NationFragment getNationFragment()
    {
        NationFragment nationFragment = new NationFragment();
        nationFragment.setNation(mNation);

        return nationFragment;
    }

    /**
     * Get a new region fragment
     * @return A new region fragment
     */
    private RegionFragment getRegionFragment()
    {
        RegionFragment regionFragment = new RegionFragment();
        regionFragment.setRegionName(mNation.region);

        return regionFragment;
    }

    /**
     * Determine if a nav key is part of the unselectable IDs
     */
    private boolean isNoSelect(int key)
    {
        for (int i=0; i<noSelect.length; i++)
        {
            if (noSelect[i] == key)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Start exploration dialog
     */
    private void explore()
    {
        FragmentManager fm = getSupportFragmentManager();
        ExploreDialog editNameDialog = new ExploreDialog();
        editNameDialog.show(fm, ExploreDialog.DIALOG_TAG);
    }

    /**
     * Start logout process
     */
    private void logout()
    {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent nationActivityLaunch = new Intent(StatelyActivity.this, LoginActivity.class);
                startActivity(nationActivityLaunch);
                finish();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.logout_confirm)
                .setPositiveButton(R.string.menu_logout, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();

    }

    /**
     * Query NationStates for nation data
     */
    private void updateNation()
    {
        final View fView = findViewById(R.id.drawer_layout);

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(Nation.QUERY, mNation.name.toLowerCase().replace(" ", "_"));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Nation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            nationResponse = serializer.read(Nation.class, response);

                            // Switch flag URL to https
                            nationResponse.flagURL = nationResponse.flagURL.replace("http://","https://");

                            // Map out government priorities
                            switch (nationResponse.govtPriority)
                            {
                                case "Defence":
                                    nationResponse.govtPriority = getString(R.string.defense);
                                    break;
                                case "Commerce":
                                    nationResponse.govtPriority = getString(R.string.industry);
                                    break;
                                case "Social Equality":
                                    nationResponse.govtPriority = getString(R.string.social_policy);
                                    break;
                            }
                            mNation = nationResponse;
                            initNavBanner();
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
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }
}
