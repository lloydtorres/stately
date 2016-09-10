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
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.UnreadData;
import com.lloydtorres.stately.dto.UserData;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.UserNation;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.explore.ExploreDialog;
import com.lloydtorres.stately.feed.ActivityFeedFragment;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.GenericFragment;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.issues.IssuesFragment;
import com.lloydtorres.stately.login.LoginActivity;
import com.lloydtorres.stately.login.SwitchNationDialog;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.telegrams.TelegramsFragment;
import com.lloydtorres.stately.wa.AssemblyMainFragment;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The core Stately activity. This is where the magic happens.
 */
public class StatelyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IToolbarActivity {

    // Keys used for intents
    public static final String NATION_DATA = "mNationData";
    public static final String NAV_INIT = "navInit";

    public static final int NATION_FRAGMENT = 0;
    public static final int ISSUES_FRAGMENT = 1;
    public static final int TELEGRAMS_FRAGMENT = 2;
    public static final int REGION_FRAGMENT = 3;
    public static final int WA_FRAGMENT = 4;
    public static final int ACTIVITY_FEED_FRAGMENT = 5;

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
    private int navInit = NATION_FRAGMENT;

    private UserNation mNation;
    private ImageView nationBanner;
    private ImageView nationFlag;
    private TextView nationNameView;

    private TextView issueCountView;
    private TextView telegramCountView;
    private TextView rmbCountView;
    private TextView waCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch(SettingsActivity.getTheme(this)) {
            case SettingsActivity.THEME_VERT:
                setTheme(R.style.AppTheme_CoreActivity);
                break;
            case SettingsActivity.THEME_ROUGE:
                setTheme(R.style.AppThemeRouge_CoreActivity);
                break;
        }
        setContentView(R.layout.activity_stately);

        // Get nation object from intent or restore state
        if (getIntent() != null)
        {
            mNation = getIntent().getParcelableExtra(NATION_DATA);
            navInit = getIntent().getIntExtra(NAV_INIT, NATION_FRAGMENT);
        }
        if (savedInstanceState != null)
        {
            if (mNation == null)
            {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
            navInit = savedInstanceState.getInt(NAV_INIT, NATION_FRAGMENT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_app_bar);
        setToolbar(toolbar);
        getSupportActionBar().hide();
        getSupportActionBar().setTitle("");

        if (mNation == null)
        {
            UserLogin u = SparkleHelper.getActiveUser(this);
            updateNation(u.name, true);
        }
        else
        {
            initNavigationView(navInit);
            if (mNation.unread != null) {
                processUnreadCounts(mNation.unread);
            }
        }
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
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Initialize the navigation drawer.
     * Set the nation fragment as the current view.
     * @param start Index of the view to start with
     */
    private void initNavigationView(int start)
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(start).setChecked(true);
        initNavBanner();

        issueCountView = (TextView) navigationView.getMenu().findItem(R.id.nav_issues).getActionView();
        telegramCountView = (TextView) navigationView.getMenu().findItem(R.id.nav_telegrams).getActionView();
        rmbCountView = (TextView) navigationView.getMenu().findItem(R.id.nav_region).getActionView();
        waCountView = (TextView) navigationView.getMenu().findItem(R.id.nav_wa).getActionView();

        Fragment f;
        switch (start)
        {
            case ISSUES_FRAGMENT:
                f = getIssuesFragment();
                currentPosition = R.id.nav_issues;
                break;
            case TELEGRAMS_FRAGMENT:
                f = getTelegramsFragment();
                currentPosition = R.id.nav_telegrams;
                break;
            case ACTIVITY_FEED_FRAGMENT:
                f = getActivityFeed();
                currentPosition = R.id.nav_activityfeed;
                break;
            case REGION_FRAGMENT:
                f = getRegionFragment();
                currentPosition = R.id.nav_region;
                break;
            case WA_FRAGMENT:
                f = getWaFragment();
                currentPosition = R.id.nav_wa;
                break;
            default:
                f = getNationFragment();
                currentPosition = R.id.nav_nation;
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coordinator_app_bar, f)
                .commitNow();
    }

    /**
     * Initialize the banner in the navigation drawer with data from Nation.
     */
    private void initNavBanner()
    {
        View view = navigationView.getHeaderView(0);
        nationBanner = (ImageView) view.findViewById(R.id.nav_banner_back);
        nationFlag = (ImageView) view.findViewById(R.id.nav_flag);
        nationNameView = (TextView) view.findViewById(R.id.nav_nation_name);

        nationNameView.setText(mNation.name);

        DashHelper dashie = DashHelper.getInstance(this);
        dashie.loadImage(Nation.getBannerURL(mNation.bannerKey), nationBanner, false);
        dashie.loadImage(mNation.flagURL, nationFlag, true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(NAV_INIT, navInit);
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
        navInit = savedInstanceState.getInt(NAV_INIT, NATION_FRAGMENT);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Redownload nation data on resume
        // isLoaded will only be false on first run, true on all subsequent runs
        // This prevents nation data from being redundantly loaded twice.
        if (isLoaded)
        {
            updateNation(mNation.name, false);
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
        else if (currentPosition != R.id.nav_nation) {
            initNavigationView(NATION_FRAGMENT);
        }
        else
        {
            if (SettingsActivity.getConfirmExitSetting(this)) {
                confirmExit();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void confirmExit()
    {
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle(R.string.exit_confirm)
                .setPositiveButton(R.string.exit, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
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
                    navInit = NATION_FRAGMENT;
                    break;
                case R.id.nav_issues:
                    // Choose Issues
                    fChoose = getIssuesFragment();
                    navInit = ISSUES_FRAGMENT;
                    break;
                case R.id.nav_telegrams:
                    fChoose = getTelegramsFragment();
                    navInit = TELEGRAMS_FRAGMENT;
                    break;
                case R.id.nav_activityfeed:
                    fChoose = getActivityFeed();
                    navInit = ACTIVITY_FEED_FRAGMENT;
                    break;
                case R.id.nav_region:
                    fChoose = getRegionFragment();
                    navInit = REGION_FRAGMENT;
                    break;
                case R.id.nav_wa:
                    // Chose World Assembly
                    fChoose = getWaFragment();
                    navInit = WA_FRAGMENT;
                    break;
                default:
                    // Backup
                    fChoose = new GenericFragment();
                    break;
            }

            // Switch fragments
            fm.beginTransaction()
                    .replace(R.id.coordinator_app_bar, fChoose)
                    .commitNow();
            drawer.closeDrawer(GravityCompat.START);

            // Update the unread counts whenever a new main fragment is selected.
            queryUnreadCounts();

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

    private IssuesFragment getIssuesFragment()
    {
        IssuesFragment issuesFragment = new IssuesFragment();
        issuesFragment.setNationData(mNation);
        return issuesFragment;
    }

    /**
     * Get a new telegrams fragment
     * @return Telegrams fragment
     */
    private TelegramsFragment getTelegramsFragment()
    {
        TelegramsFragment telegramsFragment = new TelegramsFragment();
        return telegramsFragment;
    }

    /**
     * Get a new activity feed fragment
     * @return New activity feed fragment
     */
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
        regionFragment.setRMBUnreadCountText(rmbCountView.getText().toString());

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
     * Starts the process to get unread counts from the NS API.
     */
    private void queryUnreadCounts() {
        if (mNation != null) {
            String target = String.format(Locale.US, UserData.UNREAD_QUERY, SparkleHelper.getIdFromName(mNation.name));
            NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, target,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Persister serializer = new Persister();
                            try {
                                UserData userData = serializer.read(UserData.class, response);
                                processUnreadCounts(userData.unread);
                            }
                            catch (Exception e) {
                                SparkleHelper.logError(e.toString());
                                // Do nothing for dat ~seamless experience~
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SparkleHelper.logError(error.toString());
                }
            });

            DashHelper.getInstance(this).addRequest(stringRequest);
        }
    }

    /**
     * Given the parsed HTML document, grabs the elements containing the unread counts.
     * @param d Unread object from NS API.
     */
    private void processUnreadCounts(UnreadData d) {
        setUnreadCount(issueCountView, d.issues);
        setUnreadCount(telegramCountView, d.telegrams);
        setUnreadCount(rmbCountView, d.rmb);
        setUnreadCount(waCountView, d.wa);
    }

    /**
     * Helper function to sanitize string counts and put and style them in the navigation drawer.
     * @param targetView The TextView where the count will end up in.
     * @param count The number of unreads for the given category.
     */
    private void setUnreadCount(TextView targetView, int count) {
        if (targetView != null) {
            targetView.setVisibility(View.VISIBLE);
            if (count > 100) {
                targetView.setText(getString(R.string.menu_unread_count_over100));
            }
            else if (count > 0) {
                targetView.setText(String.valueOf(count));
            }
            else {
                targetView.setText("");
                targetView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Call used to decrement the telegram unread count by 1 when a telegram is read.
     */
    public void decrementTelegramUnread() {
        if (mNation != null && mNation.unread != null) {
            mNation.unread.telegrams = Math.max(0, mNation.unread.telegrams - 1);
            setUnreadCount(telegramCountView, mNation.unread.telegrams);
        }
    }

    /**
     * Query NationStates for nation data
     * @param name Target nation name
     * @param firstLaunch Indicates if activity is being launched for the first time
     */
    private void updateNation(String name, final boolean firstLaunch)
    {
        final View fView = findViewById(R.id.drawer_layout);
        String targetURL = String.format(UserNation.QUERY, SparkleHelper.getIdFromName(name));

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    UserNation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            nationResponse = UserNation.parseNationFromXML(getApplicationContext(), serializer, response);

                            mNation = nationResponse;
                            SparkleHelper.setSessionData(getApplicationContext(), SparkleHelper.getIdFromName(mNation.region), mNation.waState);

                            if (firstLaunch)
                            {
                                initNavigationView(navInit);
                            }
                            else
                            {
                                initNavBanner();
                                if (mNation.unread != null) {
                                    processUnreadCounts(mNation.unread);
                                }
                            }
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
