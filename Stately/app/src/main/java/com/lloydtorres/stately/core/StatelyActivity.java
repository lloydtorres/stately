package com.lloydtorres.stately.core;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.explore.ExploreDialog;
import com.lloydtorres.stately.feed.ActivityFeedFragment;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.GenericFragment;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.issues.IssuesFragment;
import com.lloydtorres.stately.login.LoginActivity;
import com.lloydtorres.stately.login.SwitchNationDialog;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.wa.AssemblyMainFragment;
import com.squareup.picasso.Picasso;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * The core Stately activity. This is where the magic happens.
 */
public class StatelyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PrimeActivity {

    // Keys used for intents
    public static final String NATION_DATA = "mNationData";

    // A list of navdrawer options that shouldn't switch the nav position on select.
    private final int[] noSelect = {    R.id.nav_explore,
                                        R.id.nav_switch,
                                        R.id.nav_settings,
                                        R.id.nav_logout
                                    };

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private int currentPosition = R.id.nav_nation;
    private boolean isLoaded = false;

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
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (mNation == null && savedInstanceState != null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA);
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

        nationNameView.setText(mNation.name);

        Picasso.with(this).load(SparkleHelper.getBannerURL(mNation.bannerKey)).into(nationBanner);
        Picasso.with(this).load(mNation.flagURL).into(nationFlag);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }
    }

    @Override
    public void onResume()
    {
        // Redownload nation data on resume
        super.onResume();
        if (isLoaded)
        {
            updateNation();
        }
        else
        {
            isLoaded = true;
        }
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
                case R.id.nav_issues:
                    // Choose Issues
                    fChoose = new IssuesFragment();
                    break;
                case R.id.nav_activityfeed:
                    fChoose = getActivityFeed();
                    break;
                case R.id.nav_region:
                    fChoose = getRegionFragment();
                    break;
                case R.id.nav_wa:
                    // Chose World Assembly
                    fChoose = getWaFragment();
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
                case R.id.nav_switch:
                    switchNation();
                    break;
                case R.id.nav_settings:
                    startSettings();
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

    private ActivityFeedFragment getActivityFeed()
    {
        ActivityFeedFragment activityFeedFragment = new ActivityFeedFragment();
        activityFeedFragment.setNationName(mNation.name);
        activityFeedFragment.setRegionName(mNation.region);
        return activityFeedFragment;
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
     * Get a new WA fragment
     * @return A new WA fragment
     */
    private AssemblyMainFragment getWaFragment()
    {
        AssemblyMainFragment waFragment = new AssemblyMainFragment();
        WaVoteStatus voteStatus = new WaVoteStatus();
        voteStatus.waState = mNation.waState;
        voteStatus.gaVote = mNation.gaVote;
        voteStatus.scVote = mNation.scVote;
        waFragment.setVoteStatus(voteStatus);

        return waFragment;
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
        ExploreDialog exploreDialog = new ExploreDialog();
        exploreDialog.show(fm, ExploreDialog.DIALOG_TAG);
    }

    /**
     * Start switch nation dialog.
     */
    private void switchNation()
    {
        List<UserLogin> logins = UserLogin.listAll(UserLogin.class);
        // If no other nations besides current one, show warning dialog
        // with link to login activity
        if (logins.size() <= 1)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SparkleHelper.startAddNation(StatelyActivity.this);
                    dialog.dismiss();
                }
            };
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
            dialogBuilder
                    .setTitle(getString(R.string.menu_switch))
                    .setMessage(getString(R.string.switch_single_warn))
                    .setPositiveButton(getString(R.string.log_in), dialogClickListener)
                    .setNegativeButton(getString(R.string.explore_negative), null).show();
        }
        // If other nations exist, show switch dialog
        else
        {
            FragmentManager fm = getSupportFragmentManager();
            SwitchNationDialog switchDialog = new SwitchNationDialog();
            switchDialog.setLogins(new ArrayList<UserLogin>(logins));
            switchDialog.show(fm, SwitchNationDialog.DIALOG_TAG);
        }
    }

    /**
     * Start settings activity.
     */
    private void startSettings()
    {
        Intent settingsActivityLaunch = new Intent(StatelyActivity.this, SettingsActivity.class);
        startActivity(settingsActivityLaunch);
    }

    /**
     * Start logout process
     */
    private void logout()
    {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SparkleHelper.removeActiveUser(getApplicationContext());
                Intent nationActivityLaunch = new Intent(StatelyActivity.this, LoginActivity.class);
                startActivity(nationActivityLaunch);
                finish();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
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
        String targetURL = String.format(Nation.QUERY, SparkleHelper.getIdFromName(mNation.name));

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
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            SparkleHelper.makeSnackbar(fView, getString(R.string.rate_limit_error));
        }
    }
}
