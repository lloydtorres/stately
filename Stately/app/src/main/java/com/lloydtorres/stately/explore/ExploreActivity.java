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

package com.lloydtorres.stately.explore;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.IToolbarActivity;
import com.lloydtorres.stately.core.SlidrActivity;
import com.lloydtorres.stately.core.StatelyActivity;
import com.lloydtorres.stately.dto.Dossier;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.UserExploreData;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.ZSuperweaponStatus;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.ProgressDialog;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;
import com.lloydtorres.stately.telegrams.TelegramComposeActivity;
import com.lloydtorres.stately.zombie.NightmareHelper;
import com.lloydtorres.stately.zombie.SuperweaponDialog;
import com.lloydtorres.stately.zombie.ZombieChartCard;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-15.
 * This activity can be invoked to load and open a nation/region page, either as an Intent
 * or through this Uri: com.lloydtorres.stately.explore://<name>/<mode>
 * Requires a name to be passed in; does error checking as well.
 */
public class ExploreActivity extends SlidrActivity implements IToolbarActivity {

    // Explore modes
    public static final int EXPLORE_NATION = 1;
    public static final int EXPLORE_REGION = 2;

    // Uri to invoke the ExploreActivity
    public static final String EXPLORE_PROTOCOL = "com.lloydtorres.stately.explore";
    public static final String EXPLORE_TARGET = EXPLORE_PROTOCOL + "://";

    // Keys for intent data
    public static final String EXPLORE_ID = "id";
    public static final String EXPLORE_MODE = "mode";
    public static final String EXPLORE_NAME = "name";
    public static final String IS_ME = "isMe";
    public static final String IS_ENDORSABLE = "isEndorsable";
    public static final String IS_ENDORSED = "isEndorsed";
    public static final String IS_MOVEABLE = "isMoveable";
    public static final String IS_PASSWORD = "isPassword";
    public static final String IS_IN_DOSSIER = "isInDossier";
    public static final String SUPERWEAPON_STATUS = "superweaponStatus";

    public static final String ENDORSE_URL = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/endorse" +
            ".cgi";
    private static final String ENDORSE_REQUEST = "endorse";
    private static final String UNENDORSE_REQUEST = "unendorse";
    private static final String PASSWORD_TAG = "Password";
    private static final Pattern REGION_MOVE_SUCCESS =
            Pattern.compile("Success! " + SparkleHelper.VALID_NAME_BASE + "+? is now located in ");
    private static final Pattern REGION_MOVE_WRONG_PASS =
            Pattern.compile("Moving to " + SparkleHelper.VALID_NAME_BASE + "+?: You have not " +
                    "entered the correct password");
    private static final String REGION_MOVE_BLOCKED_PASS = "temporarily blocked from moving into password-protected regions";
    private static final String DOSSIER_ADD_CONFIRM = "has been added to your Dossier.";
    private static final String DOSSIER_REMOVE_CONFIRM = "Removed 1";
    private static final String SUPERWEAPON_BUTTON_TEMPLATE = "button[name=%s]";
    private static final String ZSW_TZES_RESPONSE = "ZOMBIE HUNTERS ARE GO";
    private static final String ZSW_CURE_RESPONSE = "CURE MISSILE FIRED";
    private static final String ZSW_HORDE_RESPONSE = "HORDE UNLEASHED";
    private static final String ZSW_MISFIRE = "Superweapon not ready! Misfire!";
    private static final String ZSW_HIT = "Cooldown was reset";
    private static final String ZSW_NO_ZOMBIES = "is free of infection";
    private static final String ZSW_NO_SURVIVORS = "is free of survivors";
    private String id;
    private String name;
    private int mode;
    private String userId;
    private TextView statusMessage;
    private ImageView exploreButton;
    private boolean noRefresh;
    private boolean isMe;
    private boolean isEndorsable;
    private boolean isEndorsed;
    private boolean isMoveable;
    private boolean isPassword;
    private boolean isInDossier;
    private boolean isInProgress;
    private ZSuperweaponStatus superweaponStatus;
    private Fragment mFragment;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        view = findViewById(R.id.explore_coordinator);
        isInProgress = false;

        if (getIntent() != null) {
            // If name passed in as intent
            name = getIntent().getStringExtra(EXPLORE_ID);
            id = SparkleHelper.getIdFromName(name);
            mode = getIntent().getIntExtra(EXPLORE_MODE, EXPLORE_NATION);
            if (id == null) {
                // If ID passed in through Uri
                // Funny thing here is that in the link source, they have
                // to convert it from a proper name to an ID
                // But we need it as a name so we convert it back
                id = getIntent().getData().getHost();
                id = SparkleHelper.getIdFromName(id);
                name = SparkleHelper.getNameFromId(id);
                mode = Integer.valueOf(getIntent().getData().getLastPathSegment());
            }
        } else {
            return;
        }

        // Restore state
        if (savedInstanceState != null) {
            id = savedInstanceState.getString(EXPLORE_ID);
            mode = savedInstanceState.getInt(EXPLORE_MODE);
            name = savedInstanceState.getString(EXPLORE_NAME);
            isMe = savedInstanceState.getBoolean(IS_ME, false);
            isEndorsable = savedInstanceState.getBoolean(IS_ENDORSABLE, false);
            isEndorsed = savedInstanceState.getBoolean(IS_ENDORSED, false);
            isMoveable = savedInstanceState.getBoolean(IS_MOVEABLE, false);
            isPassword = savedInstanceState.getBoolean(IS_PASSWORD, false);
            isInDossier = savedInstanceState.getBoolean(IS_IN_DOSSIER, false);
            superweaponStatus = savedInstanceState.getParcelable(SUPERWEAPON_STATUS);
        }

        Toolbar toolbar = findViewById(R.id.explore_toolbar);
        setToolbar(toolbar);
        getSupportActionBar().hide();

        statusMessage = findViewById(R.id.explore_status);
        exploreButton = findViewById(R.id.explore_button);

        verifyInput(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        switch (mode) {
            case EXPLORE_NATION:
                if (isEndorsable) {
                    if (isEndorsed) {
                        inflater.inflate(isInDossier ?
                                R.menu.activity_explore_nation_endorsed_dossier :
                                R.menu.activity_explore_nation_endorsed_nodossier, menu);
                    } else {
                        inflater.inflate(isInDossier ?
                                R.menu.activity_explore_nation_endorsable_dossier :
                                R.menu.activity_explore_nation_endorsable_nodossier, menu);
                    }
                } else {
                    if (!isMe) {
                        inflater.inflate(isInDossier ?
                                R.menu.activity_explore_nation_not_wa_dossier :
                                R.menu.activity_explore_nation_not_wa_nodossier, menu);
                    } else {
                        inflater.inflate(R.menu.activity_explore_default, menu);
                    }
                }
                break;
            case EXPLORE_REGION:
                if (isMoveable) {
                    inflater.inflate(isInDossier ? R.menu.activity_explore_region_move_dossier :
                            R.menu.activity_explore_region_move_nodossier, menu);
                } else {
                    inflater.inflate(R.menu.activity_explore_default, menu);
                }
                break;
            default:
                inflater.inflate(R.menu.activity_explore_default, menu);
        }

        return true;
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        // We need a back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Sets a message on the activity and makes the explore button visible.
     * @param s
     */
    private void setExploreStatusError(String s) {
        statusMessage.setText(s);
        exploreButton.setVisibility(View.VISIBLE);
    }

    /**
     * Set the name of the target in the explore activity.
     * @param n Name
     */
    private void setName(String n) {
        name = n;
    }

    /**
     * Checks if the target ID is valid then routes it to the proper query.
     * @param name Target ID
     */
    private void verifyInput(String name) {
        if (SparkleHelper.isValidName(name) && name.length() > 0) {
            name = SparkleHelper.getIdFromName(name);
            queryAndCheckUserExploreData(name);
        } else {
            switch (mode) {
                case EXPLORE_NATION:
                    setExploreStatusError(getString(R.string.explore_error_404_nation));
                    break;
                case EXPLORE_REGION:
                    setExploreStatusError(getString(R.string.region_404));
                    break;
            }
        }
    }

    /**
     * Checks if the nation/region being explored is in the current user's dossier.
     * Also checks the current user's zombie stats as appropriate.
     * Calls on the appropriate query function afterwards.
     * @param name Nation/region to check
     */
    private void queryAndCheckUserExploreData(final String name) {
        userId = PinkaHelper.getActiveUser(this).nationId;
        String targetURL = String.format(Locale.US, UserExploreData.QUERY,
                SparkleHelper.getIdFromName(userId));

        NSStringRequest stringRequest = new NSStringRequest(this, Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        UserExploreData userDataResponse;
                        Persister serializer = new Persister();
                        try {
                            userDataResponse = serializer.read(UserExploreData.class, response);
                            String targetId = SparkleHelper.getIdFromName(name);
                            if (mode == EXPLORE_NATION && userDataResponse.nations != null) {
                                isInDossier = userDataResponse.nations.contains(targetId);
                            }
                            if (mode == EXPLORE_REGION && userDataResponse.regions != null) {
                                isInDossier = userDataResponse.regions.contains(targetId);
                            }
                        } catch (Exception e) {
                            // Keep going even if there's an error
                            SparkleHelper.logError(e.toString());
                        }
                        queryHelper(name);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Keep going even if there's an error
                SparkleHelper.logError(error.toString());

                if (isFinishing()) {
                    return;
                }

                queryHelper(name);
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            setExploreStatusError(getString(R.string.rate_limit_error));
        }
    }

    /**
     * Helper function for querying either a nation or a region.
     * @param name Target name
     */
    private void queryHelper(String name) {
        switch (mode) {
            case EXPLORE_NATION:
                queryNation(name);
                break;
            case EXPLORE_REGION:
                queryRegion(name);
                break;
        }
    }

    /**
     * Queries data about a nation from the NS API.
     * @param name Target nation ID
     */
    private void queryNation(String name) {
        name = SparkleHelper.getIdFromName(name);
        String targetURL = String.format(Locale.US, Nation.QUERY, name);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Nation nationResponse = null;

                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        Persister serializer = new Persister();
                        try {
                            nationResponse = Nation.parseNationFromXML(getApplicationContext(),
                                    serializer, response);

                            setName(nationResponse.name);

                            // determine endorseable state
                            UserLogin u = PinkaHelper.getActiveUser(getApplicationContext());
                            String userRegionId =
                                    PinkaHelper.getRegionSessionData(getApplicationContext());
                            boolean userWaMember =
                                    PinkaHelper.getWaSessionData(getApplicationContext());

                            String exploreId = SparkleHelper.getIdFromName(nationResponse.name);
                            String exploreRegionId =
                                    SparkleHelper.getIdFromName(nationResponse.region);
                            boolean exploreWaMember =
                                    SparkleHelper.isWaMember(nationResponse.waState);

                            // must not be same as session nation, must be in same region, must
                            // both be WA members
                            isMe = exploreId.equals(userId);
                            if (!isMe && exploreRegionId.equals(userRegionId) && userWaMember && exploreWaMember) {
                                isEndorsable = true;
                                isEndorsed =
                                        nationResponse.endorsements != null && nationResponse.endorsements.contains(userId);
                            } else {
                                isEndorsable = false;
                                isEndorsed = false;
                            }
                            invalidateOptionsMenu();

                            if (NightmareHelper.getIsZDayActive(ExploreActivity.this)) {
                                checkZDaySuperweapons(nationResponse);
                            } else {
                                initFragment(nationResponse);
                            }
                        } catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            setExploreStatusError(getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());

                if (isFinishing()) {
                    return;
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    setExploreStatusError(getString(R.string.login_error_no_internet));
                } else if (error instanceof ServerError) {
                    setExploreStatusError(getString(R.string.explore_error_404_nation));
                } else {
                    setExploreStatusError(getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            setExploreStatusError(getString(R.string.rate_limit_error));
        }
    }

    /**
     * Queries a target region from the NS API.
     * @param name Target region ID.
     */
    private void queryRegion(String name) {
        String targetURL = String.format(Locale.US, Region.QUERY, name);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Region regionResponse = null;

                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        Persister serializer = new Persister();
                        try {
                            regionResponse = Region.parseRegionXML(ExploreActivity.this,
                                    serializer, response);

                            setName(regionResponse.name);

                            // determine moveable state
                            String curRegion =
                                    PinkaHelper.getRegionSessionData(getApplicationContext());
                            isMoveable =
                                    !curRegion.equals(SparkleHelper.getIdFromName(regionResponse.name))
                                        && !regionResponse.isNationBannedFromRegion(userId);
                            isPassword =
                                    regionResponse.tags != null && regionResponse.tags.contains(PASSWORD_TAG);
                            invalidateOptionsMenu();

                            initFragment(regionResponse);
                        } catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            setExploreStatusError(getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());

                if (isFinishing()) {
                    return;
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    setExploreStatusError(getString(R.string.login_error_no_internet));
                } else if (error instanceof ServerError) {
                    setExploreStatusError(getString(R.string.region_404));
                } else {
                    setExploreStatusError(getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            setExploreStatusError(getString(R.string.rate_limit_error));
        }
    }

    private void initFragment(Nation mNation) {
        if (isFinishing()) {
            return;
        }

        // Initializes and inflates the nation fragment
        if (mFragment == null || !(mFragment instanceof NationFragment)) {
            mFragment = new NationFragment();
            ((NationFragment) mFragment).setNation(mNation);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.explore_coordinator, mFragment)
                    .commitAllowingStateLoss();
        } else {
            ((NationFragment) mFragment).updateOverviewData(mNation);
        }
    }

    private void initFragment(Region mRegion) {
        // Initializes and inflates the region fragment
        if (!isFinishing()) {
            mFragment = new RegionFragment();
            ((RegionFragment) mFragment).setRegion(mRegion);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.explore_coordinator, mFragment)
                    .commitAllowingStateLoss();
        }
    }

    /**
     * Gets the required local ID for a target page.
     * @param url Target page URL
     * @param password Password for region moves (can be null)
     */
    private void getLocalId(final String url, final String password) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        String progressMessage = "";
        switch (mode) {
            case EXPLORE_NATION:
                if (isEndorsed) {
                    progressMessage = String.format(Locale.US,
                            getString(R.string.explore_withdraw_endorse_progress), name);
                } else {
                    progressMessage = String.format(Locale.US,
                            getString(R.string.explore_endorse_progress), name);
                }
                break;
            case EXPLORE_REGION:
                progressMessage = String.format(Locale.US,
                        getString(R.string.explore_move_progress), name);
                break;
        }
        final ProgressDialog pd = new ProgressDialog();
        pd.setContent(progressMessage);
        pd.show(getSupportFragmentManager(), ProgressDialog.DIALOG_TAG);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=localid]").first();

                        if (input == null) {
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.login_error_parsing));
                            isInProgress = false;
                            return;
                        }

                        String localid = input.attr("value");
                        switch (mode) {
                            case EXPLORE_NATION:
                                postEndorsement(localid, pd);
                                break;
                            case EXPLORE_REGION:
                                postRegionMove(localid, password, pd);
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * First part of the dossier process. Gets the chk value from NationStates.
     */
    private void getCheckValueForDossier() {
        final String urlTemplate = mode == EXPLORE_NATION ? Nation.QUERY_HTML : Region.QUERY_HTML;
        final String url = !isInDossier ?
                String.format(Locale.US, urlTemplate, SparkleHelper.getIdFromName(id)) :
                Dossier.POST_QUERY_NO_TEMPLATE;
        final NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=chk]").first();

                        if (input == null) {
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.login_error_parsing));
                            isInProgress = false;
                            return;
                        }

                        String chkValue = input.attr("value");
                        postDossier(chkValue);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Shows a dialog with the appropriate text to confirm an endorsement/withdrawal request.
     */
    private void handleEndorsement() {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }

        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,
                RaraHelper.getThemeMaterialDialog(this));
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getLocalId(String.format(Locale.US, Nation.QUERY_HTML,
                                SparkleHelper.getIdFromName(id)), null);
                    }
                };

        dialogBuilder
                .setTitle(String.format(Locale.US, isEndorsed ?
                        getString(R.string.explore_withdraw_endorse_confirm) :
                        getString(R.string.explore_endorse_confirm), name))
                .setPositiveButton(isEndorsed ?
                        getString(R.string.explore_withdraw_endorse_button) :
                        getString(R.string.explore_endorse_button), dialogClickListener)
                .setNegativeButton(getString(R.string.explore_negative), null)
                .show();
    }

    /**
     * Actually does the post to submit an endorsement.
     * @param localid Required localId value
     * @param pd ProgressDialog previously shown
     */
    private void postEndorsement(final String localid, final ProgressDialog pd) {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.POST, ENDORSE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        isInProgress = false;
                        isEndorsed = !isEndorsed;
                        if (!isEndorsed) {
                            SparkleHelper.makeSnackbar(view, String.format(Locale.US,
                                    getString(R.string.explore_withdraw_endorse_response), name));
                        } else {
                            SparkleHelper.makeSnackbar(view, String.format(Locale.US,
                                    getString(R.string.explore_endorsed_response), name));
                        }
                        if (pd != null) {
                            pd.dismiss();
                        }
                        queryNation(id);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                pd.dismiss();
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String, String> params = new HashMap<String, String>();
        params.put("nation", SparkleHelper.getIdFromName(id));
        params.put("localid", localid);
        if (isEndorsed) {
            params.put("action", UNENDORSE_REQUEST);
        } else {
            params.put("action", ENDORSE_REQUEST);
        }
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            pd.dismiss();
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Shows a dialog to confirm adding/removing a nation/region from the user's dossier.
     */
    private void handleDossier() {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }

        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,
                RaraHelper.getThemeMaterialDialog(this));
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getCheckValueForDossier();
                    }
                };

        dialogBuilder
                .setTitle(String.format(Locale.US, isInDossier ?
                        getString(R.string.explore_dossier_rem_confirm) :
                        getString(R.string.explore_dossier_add_confirm), name))
                .setPositiveButton(isInDossier ? getString(R.string.explore_dossier_rem_button) :
                        getString(R.string.explore_dossier_add_button), dialogClickListener)
                .setNegativeButton(getString(R.string.explore_negative), null)
                .show();
    }

    /**
     * Actually sends a POST to NS to add the nation/region to the current user's dossier.
     */
    private void postDossier(final String checkValue) {
        final String progressMessage = String.format(Locale.US, getString(isInDossier ?
                        R.string.explore_dossier_rem_progress :
                        R.string.explore_dossier_add_progress),
                name);
        final ProgressDialog dossierProgress = new ProgressDialog();
        dossierProgress.setContent(progressMessage);
        dossierProgress.show(getSupportFragmentManager(), ProgressDialog.DIALOG_TAG);

        String targetUrl = Dossier.POST_QUERY_NO_TEMPLATE;
        if (!isInDossier && mode == EXPLORE_REGION) {
            targetUrl = String.format(Locale.US, Dossier.POST_REGION_QUERY_NO_TEMPLATE, SparkleHelper.getIdFromName(id));
        }

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.POST, targetUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }
                        isInProgress = false;
                        dossierProgress.dismiss();

                        final String sanitizedResponse = SparkleHelper.getStrippedHtml(response);
                        final boolean isAddSuccesful = !isInDossier &&
                                sanitizedResponse.contains(DOSSIER_ADD_CONFIRM);
                        final boolean isRemoveSuccessful = isInDossier &&
                                sanitizedResponse.contains(DOSSIER_REMOVE_CONFIRM);
                        if (!isAddSuccesful && !isRemoveSuccessful) {
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                            return;
                        }

                        isInDossier = !isInDossier;
                        if (isInDossier) {
                            SparkleHelper.makeSnackbar(view, String.format(Locale.US,
                                    getString(R.string.explore_dossier_added), name));
                        } else {
                            SparkleHelper.makeSnackbar(view, String.format(Locale.US,
                                    getString(R.string.explore_dossier_removed), name));
                        }
                        invalidateOptionsMenu();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                dossierProgress.dismiss();
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String, String> params = new HashMap<String, String>();
        // If not currently in dossier and trying to add
        if (!isInDossier) {
            switch (mode) {
                case EXPLORE_NATION:
                    params.put("nation", id);
                    params.put("action", "add");
                    break;
                case EXPLORE_REGION:
                    params.put("add_to_dossier", "1");
                    break;
            }
        } else {
            switch (mode) {
                case EXPLORE_NATION:
                    params.put(String.format(Locale.US, Dossier.PARAM_REMOVE_TEMPLATE,
                            Dossier.PARAM_REMOVE_NATION, id), "on");
                    params.put("remove_from_dossier", "1");
                    break;
                case EXPLORE_REGION:
                    params.put(String.format(Locale.US, Dossier.PARAM_REMOVE_TEMPLATE,
                            Dossier.PARAM_REMOVE_REGION, id), "on");
                    params.put("remove_from_region_dossier", "1");
                    break;
            }
        }
        params.put("chk", checkValue);

        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            dossierProgress.dismiss();
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Handles which dialog to show for confirmation/password when moving regions.
     */
    public void handleRegionMove() {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }

        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,
                RaraHelper.getThemeMaterialDialog(this));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_dialog_move_password, null);
        final AppCompatEditText passView = dialogView.findViewById(R.id.move_password);
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = null;
                        if (passView.getText().length() >= 0) {
                            password = passView.getText().toString();
                        }
                        dialog.dismiss();
                        getLocalId(String.format(Locale.US, Region.QUERY_HTML,
                                SparkleHelper.getIdFromName(id)), password);
                    }
                };

        if (isPassword) {
            dialogBuilder
                    .setTitle(String.format(Locale.US,
                            getString(R.string.explore_region_password), name))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.explore_move_confirm),
                            dialogClickListener)
                    .setNegativeButton(getString(R.string.explore_negative), null);
        } else {
            dialogBuilder
                    .setTitle(String.format(Locale.US, getString(R.string.explore_region_move),
                            name))
                    .setPositiveButton(getString(R.string.explore_move_confirm),
                            dialogClickListener)
                    .setNegativeButton(getString(R.string.explore_negative), null);
        }

        Dialog d = dialogBuilder.create();
        if (isPassword) {
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        d.show();
    }

    /**
     * POSTs to the server to move a nation to a region.
     * @param localid Required localId
     * @param password Password (can be null)
     * @param pd ProgressDialog previously shown
     */
    private void postRegionMove(final String localid, final String password,
                                final ProgressDialog pd) {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.POST, Region.CHANGE_QUERY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }
                        final String sanitizedResponse = SparkleHelper.getStrippedHtml(response);

                        Matcher moveSuccess = REGION_MOVE_SUCCESS.matcher(sanitizedResponse);
                        Matcher moveWrongPassword = REGION_MOVE_WRONG_PASS.matcher(sanitizedResponse);
                        isInProgress = false;
                        pd.dismiss();
                        if (moveSuccess.find()) {
                            Intent statelyActivityLaunch = new Intent(ExploreActivity.this,
                                    StatelyActivity.class);
                            statelyActivityLaunch.putExtra(StatelyActivity.NAV_INIT,
                                    StatelyActivity.REGION_FRAGMENT);
                            statelyActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(statelyActivityLaunch);
                        } else if (moveWrongPassword.find()) {
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.explore_move_wrong_password));
                        } else if (sanitizedResponse.contains(REGION_MOVE_BLOCKED_PASS)) {
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.explore_move_blocked));
                        } else {
                            SparkleHelper.makeSnackbar(view,
                                    getString(R.string.login_error_generic));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                pd.dismiss();
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String, String> params = new HashMap<String, String>();
        params.put("move_region", "1");
        params.put("region_name", SparkleHelper.getIdFromName(id));
        params.put("localid", localid);
        if (password != null) {
            params.put("password", password);
        }
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            pd.dismiss();
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Opens the explore dialog.
     */
    private void openExploreDialog(boolean closeOnFinish) {
        if (isFinishing()) {
            return;
        }

        ExploreDialog exploreDialog = new ExploreDialog();

        if (closeOnFinish) {
            exploreDialog.setActivityCloseOnFinish(this);
        }

        if (exploreButton.getVisibility() == View.VISIBLE) {
            exploreDialog.setSearchOverride(name, mode);
        }

        exploreDialog.show(getSupportFragmentManager(), ExploreDialog.DIALOG_TAG);
    }

    /**
     * Callback from layout.
     * @param v
     */
    public void openExploreDialog(View v) {
        openExploreDialog(true);
    }

    /**
     * Checks superweapon availability for the given nation, then continues on to initialize the
     * nation fragment.
     * Used for Z-Day.
     * @param nationResponse
     */
    private void checkZDaySuperweapons(final Nation nationResponse) {
        String targetURL = String.format(Locale.US, Nation.QUERY_HTML,
                SparkleHelper.getIdFromName(nationResponse.name));
        NSStringRequest stringRequest = new NSStringRequest(this, Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        superweaponStatus = new ZSuperweaponStatus();
                        superweaponStatus.isTZES = d.select(String.format(Locale.US,
                                SUPERWEAPON_BUTTON_TEMPLATE,
                                ZSuperweaponStatus.ZSUPER_TZES)).first() != null;
                        superweaponStatus.isCure = d.select(String.format(Locale.US,
                                SUPERWEAPON_BUTTON_TEMPLATE,
                                ZSuperweaponStatus.ZSUPER_CURE)).first() != null;
                        superweaponStatus.isHorde = d.select(String.format(Locale.US,
                                SUPERWEAPON_BUTTON_TEMPLATE,
                                ZSuperweaponStatus.ZSUPER_HORDE)).first() != null;
                        initFragment(nationResponse);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Keep going even if there's an error
                SparkleHelper.logError(error.toString());

                if (isFinishing()) {
                    return;
                }

                initFragment(nationResponse);
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            // Keep going even if there's an error
            initFragment(nationResponse);
        }
    }

    /**
     * Call to show the superweapon dialog.
     * @param zombieCard The card viewholder calling the action
     */
    public void showSuperweaponDialog(ZombieChartCard zombieCard) {
        if (isFinishing()) {
            return;
        }

        // Only show dialog if at least one superweapon is available
        if (superweaponStatus != null &&
                (superweaponStatus.isTZES || superweaponStatus.isCure || superweaponStatus.isHorde)) {
            SuperweaponDialog superweaponDialog = new SuperweaponDialog();
            superweaponDialog.setSuperweaponStatus(superweaponStatus);
            superweaponDialog.setZombieCard(zombieCard);
            superweaponDialog.show(getSupportFragmentManager(), SuperweaponDialog.DIALOG_TAG);
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,
                    RaraHelper.getThemeMaterialDialog(this));
            dialogBuilder.setTitle(R.string.zombie_button_missile)
                    .setMessage(R.string.superweapon_none)
                    .setPositiveButton(R.string.got_it, null);
            dialogBuilder.show();
        }
    }

    /**
     * Sends a POST to NS with the user's selected superweapon. Reacts appropriately based on
     * response.
     * Used for Z-Day.
     * @param superweapon Header for the selected superweapon
     * @param zombieCard The card viewholder invoking the action
     */
    public void deployZSuperweapon(final String superweapon, final ZombieChartCard zombieCard) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;
        zombieCard.setIsLoading(true);

        String targetURL = String.format(Locale.US, Nation.QUERY_HTML,
                SparkleHelper.getIdFromName(id));
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(),
                Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isFinishing()) {
                            return;
                        }

                        if (zombieCard != null) {
                            zombieCard.setIsLoading(false);
                        }
                        processZSuperweaponResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;

                if (isFinishing()) {
                    return;
                }

                if (zombieCard != null) {
                    zombieCard.setIsLoading(false);
                }
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String, String> params = new HashMap<String, String>();
        params.put(superweapon, "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            isInProgress = false;
            zombieCard.setIsLoading(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Processes the response from NS after a superweapon was fired.
     * @param response HTML response from NS
     */
    private void processZSuperweaponResponse(final String response) {
        if (response != null) {
            if (response.contains(ZSW_TZES_RESPONSE)) {
                SparkleHelper.makeSnackbar(view,
                        String.format(Locale.US, getString(R.string.superweapon_tzes_response),
                                name));
                queryNation(name);
            } else if (response.contains(ZSW_CURE_RESPONSE)) {
                SparkleHelper.makeSnackbar(view,
                        String.format(Locale.US, getString(R.string.superweapon_cure_response),
                                name));
                queryNation(name);
            } else if (response.contains(ZSW_HORDE_RESPONSE)) {
                SparkleHelper.makeSnackbar(view,
                        String.format(Locale.US, getString(R.string.superweapon_horde_response),
                                name));
                queryNation(name);
            } else if (response.contains(ZSW_MISFIRE)) {
                SparkleHelper.makeSnackbar(view, getString(R.string.superweapon_misfire));
            } else if (response.contains(ZSW_HIT)) {
                SparkleHelper.makeSnackbar(view, getString(R.string.superweapon_hit));
            } else if (response.contains(ZSW_NO_ZOMBIES)) {
                SparkleHelper.makeSnackbar(view,
                        String.format(Locale.US, getString(R.string.superweapon_no_zombies), name));
            } else if (response.contains(ZSW_NO_SURVIVORS)) {
                SparkleHelper.makeSnackbar(view,
                        String.format(Locale.US, getString(R.string.superweapon_no_survivors),
                                name));
            } else {
                SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                queryNation(name);
            }
        } else {
            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
        }
        isInProgress = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_send_telegram:
                SparkleHelper.startTelegramCompose(this, name, TelegramComposeActivity.NO_REPLY_ID);
                return true;
            case R.id.nav_endorse:
                handleEndorsement();
                return true;
            case R.id.nav_dossier:
                handleDossier();
                return true;
            case R.id.nav_move:
                handleRegionMove();
                return true;
            case R.id.nav_explore:
                // Open an explore dialog to keep going
                openExploreDialog(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(EXPLORE_ID, id);
        savedInstanceState.putInt(EXPLORE_MODE, mode);
        savedInstanceState.putString(EXPLORE_NAME, name);
        savedInstanceState.putBoolean(IS_ME, isMe);
        savedInstanceState.putBoolean(IS_ENDORSABLE, isEndorsable);
        savedInstanceState.putBoolean(IS_ENDORSED, isEndorsed);
        savedInstanceState.putBoolean(IS_MOVEABLE, isMoveable);
        savedInstanceState.putBoolean(IS_PASSWORD, isPassword);
        savedInstanceState.putBoolean(IS_IN_DOSSIER, isInDossier);
        savedInstanceState.putParcelable(SUPERWEAPON_STATUS, superweaponStatus);
    }
}
