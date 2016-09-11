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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;
import com.lloydtorres.stately.telegrams.TelegramComposeActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simpleframework.xml.core.Persister;

import java.util.HashMap;
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
    // Keys for intent data
    public static final String EXPLORE_ID = "id";
    public static final String EXPLORE_MODE = "mode";
    public static final String EXPLORE_NAME = "name";
    public static final String IS_ME = "isMe";
    public static final String IS_ENDORSABLE = "isEndorsable";
    public static final String IS_ENDORSED = "isEndorsed";
    public static final String IS_MOVEABLE = "isMoveable";
    public static final String IS_PASSWORD = "isPassword";

    public static final String ENDORSE_URL = "https://www.nationstates.net/cgi-bin/endorse.cgi";
    private static final String ENDORSE_REQUEST = "endorse";
    private static final String UNENDORSE_REQUEST = "unendorse";
    private static final String PASSWORD_TAG = "Password";
    private static final Pattern REGION_MOVE_SUCCESS = Pattern.compile("Success! .*? is now located in .*?\\.");
    private static final Pattern REGION_MOVE_WRONG_PASS = Pattern.compile("Moving to .*?: You have not entered the correct password for .*?\\.");

    private String id;
    private String name;
    private int mode;
    private TextView statusMessage;
    private boolean noRefresh;
    private boolean isMe;
    private boolean isEndorsable;
    private boolean isEndorsed;
    private boolean isMoveable;
    private boolean isPassword;
    private boolean isInProgress;

    private NationFragment nFragment;
    private RegionFragment rFragment;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        view = findViewById(R.id.explore_coordinator);
        isInProgress = false;

        if (getIntent() != null)
        {
            // If name passed in as intent
            id = getIntent().getStringExtra(EXPLORE_ID);
            mode = getIntent().getIntExtra(EXPLORE_MODE, SparkleHelper.CLICKY_NATION_MODE);
            if (id == null)
            {
                // If ID passed in through Uri
                // Funny thing here is that in the link source, they have
                // to convert it from a proper name to an ID
                // But we need it as a name so we convert it back
                id = getIntent().getData().getHost();
                id = SparkleHelper.getNameFromId(id);
                mode = Integer.valueOf(getIntent().getData().getLastPathSegment());
            }
        }
        else
        {
            return;
        }

        // Restore state
        if (savedInstanceState != null)
        {
            id = savedInstanceState.getString(EXPLORE_ID);
            mode = savedInstanceState.getInt(EXPLORE_MODE);
            name = savedInstanceState.getString(EXPLORE_NAME);
            isMe = savedInstanceState.getBoolean(IS_ME, false);
            isEndorsable = savedInstanceState.getBoolean(IS_ENDORSABLE, false);
            isEndorsed = savedInstanceState.getBoolean(IS_ENDORSED, false);
            isMoveable = savedInstanceState.getBoolean(IS_MOVEABLE, false);
            isPassword = savedInstanceState.getBoolean(IS_PASSWORD, false);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.explore_toolbar);
        setToolbar(toolbar);
        getSupportActionBar().hide();

        statusMessage = (TextView) findViewById(R.id.explore_status);

        verifyInput(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (mode == SparkleHelper.CLICKY_NATION_MODE)
        {
            if (isEndorsable)
            {
                if (isEndorsed)
                {
                    inflater.inflate(R.menu.activity_explore_nation_endorsed, menu);
                }
                else
                {
                    inflater.inflate(R.menu.activity_explore_nation_endorsable, menu);
                }
            }
            else
            {
                if (!isMe)
                {
                    inflater.inflate(R.menu.activity_explore_nation_not_wa, menu);
                }
                else
                {
                    inflater.inflate(R.menu.activity_explore_default, menu);
                }
            }
        }
        else if (mode == SparkleHelper.CLICKY_REGION_MODE)
        {
            if (isMoveable)
            {
                inflater.inflate(R.menu.activity_explore_region_move, menu);
            }
            else
            {
                inflater.inflate(R.menu.activity_explore_default, menu);
            }
        }
        else
        {
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
     * Used for setting a message while the explore fragment is loading.
     * @param s Message
     */
    private void setExploreStatus(String s)
    {
        statusMessage.setText(s);
    }

    /**
     * Set the name of the target in the explore activity.
     * @param n Name
     */
    private void setName(String n)
    {
        name = n;
    }

    /**
     * Checks if the target ID is valid then routes it to the proper query.
     * @param name Target ID
     */
    private void verifyInput(String name)
    {
        if (SparkleHelper.isValidName(name) && name.length() > 0)
        {
            name = SparkleHelper.getIdFromName(name);
            switch (mode)
            {
                case SparkleHelper.CLICKY_NATION_MODE:
                    queryNation(name);
                    break;
                default:
                    queryRegion(name);
                    break;
            }
        }
        else
        {
            switch (mode)
            {
                case SparkleHelper.CLICKY_NATION_MODE:
                    setExploreStatus(getString(R.string.explore_error_404_nation));
                    break;
                default:
                    setExploreStatus(getString(R.string.region_404));
                    break;
            }
        }
    }

    /**
     * Queries data about a nation from the NS API.
     * @param name Target nation ID
     */
    private void queryNation(String name)
    {
        name = SparkleHelper.getIdFromName(name);
        String targetURL = String.format(Nation.QUERY, name);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Nation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            nationResponse = Nation.parseNationFromXML(getApplicationContext(), serializer, response);

                            setName(nationResponse.name);

                            // determine endorseable state
                            UserLogin u = SparkleHelper.getActiveUser(getApplicationContext());
                            String userId = u.nationId;
                            String userRegionId = SparkleHelper.getRegionSessionData(getApplicationContext());
                            boolean userWaMember = SparkleHelper.getWaSessionData(getApplicationContext());

                            String exploreId = SparkleHelper.getIdFromName(nationResponse.name);
                            String exploreRegionId = SparkleHelper.getIdFromName(nationResponse.region);
                            boolean exploreWaMember = SparkleHelper.isWaMember(getApplicationContext(), nationResponse.waState);

                            // must not be same as session nation, must be in same region, must both be WA members
                            isMe = exploreId.equals(userId);
                            if (!isMe && exploreRegionId.equals(userRegionId) && userWaMember && exploreWaMember)
                            {
                                isEndorsable = true;
                                isEndorsed = nationResponse.endorsements != null && nationResponse.endorsements.contains(userId);
                            }
                            else
                            {
                                isEndorsable = false;
                                isEndorsed = false;
                            }
                            invalidateOptionsMenu();

                            initFragment(nationResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            setExploreStatus(getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    setExploreStatus(getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    setExploreStatus(getString(R.string.explore_error_404_nation));
                }
                else
                {
                    setExploreStatus(getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            setExploreStatus(getString(R.string.rate_limit_error));
        }
    }

    /**
     * Queries a target region from the NS API.
     * @param name Target region ID.
     */
    private void queryRegion(String name)
    {
        String targetURL = String.format(Region.QUERY, name);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Region regionResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            regionResponse = Region.parseRegionXML(getApplicationContext(), serializer, response);

                            setName(regionResponse.name);

                            // determine moveable state
                            String curRegion = SparkleHelper.getRegionSessionData(getApplicationContext());
                            isMoveable = !curRegion.equals(SparkleHelper.getIdFromName(regionResponse.name));
                            isPassword = regionResponse.tags != null && regionResponse.tags.contains(PASSWORD_TAG);
                            invalidateOptionsMenu();

                            initFragment(regionResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            setExploreStatus(getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    setExploreStatus(getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    setExploreStatus(getString(R.string.region_404));
                }
                else
                {
                    setExploreStatus(getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            setExploreStatus(getString(R.string.rate_limit_error));
        }
    }

    private void initFragment(Nation mNation)
    {
        // Initializes and inflates the nation fragment
        nFragment = new NationFragment();
        nFragment.setNation(mNation);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.explore_coordinator, nFragment)
                .commitNow();
    }

    private void initFragment(Region mRegion)
    {
        // Initializes and inflates the region fragment
        rFragment = new RegionFragment();
        rFragment.setRegion(mRegion);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.explore_coordinator, rFragment)
                .commitNow();
    }

    /**
     * Gets the required local ID for a target page.
     * @param url Target page URL
     * @param password Password for region moves (can be null)
     */
    private void getLocalId(final String url, final String password)
    {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }
        isInProgress = true;

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=localid]").first();

                        if (input == null)
                        {
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            isInProgress = false;
                            return;
                        }

                        String localid = input.attr("value");
                        switch (mode)
                        {
                            case SparkleHelper.CLICKY_NATION_MODE:
                                postEndorsement(localid);
                                break;
                            default:
                                postRegionMove(localid, password);
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually does the post to submit an endorsement. This is a bit weird since the API
     * call redirects to a different page, so the "success" case is actually in the error.
     * @param localid Required localId value
     */
    private void postEndorsement(final String localid)
    {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, ENDORSE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // blank since the post gets redirected
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // actual success since post gets redirected
                if (error instanceof ServerError)
                {
                    isInProgress = false;
                    if (isEndorsed)
                    {
                        SparkleHelper.makeSnackbar(view, String.format(getString(R.string.explore_withdraw_endorse_response), name));
                    }
                    else
                    {
                        SparkleHelper.makeSnackbar(view, String.format(getString(R.string.explore_endorsed_response), name));
                    }

                    queryNation(id);
                }
                else
                {
                    SparkleHelper.logError(error.toString());
                    isInProgress = false;
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                    }
                    else
                    {
                        SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                    }
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("nation", SparkleHelper.getIdFromName(id));
        params.put("localid", localid);
        if (isEndorsed) {
            params.put("action", UNENDORSE_REQUEST);
        }
        else {
            params.put("action", ENDORSE_REQUEST);
        }
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Handles which dialog to show for confirmation/password when moving regions.
     */
    public void handleRegionMove()
    {
        if (isInProgress)
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.multiple_request_error));
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, SparkleHelper.getThemeMaterialDialog(this));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.fragment_dialog_move_password, null);
        AppCompatEditText passView = (AppCompatEditText) dialogView.findViewById(R.id.move_password);
        passView.setCustomSelectionActionModeCallback(new NullActionCallback());
        final AppCompatEditText fPassView = passView;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = null;
                if (fPassView.getText().length() >= 0)
                {
                    password = fPassView.getText().toString();
                }
                getLocalId(String.format(Region.QUERY_HTML, SparkleHelper.getIdFromName(id)), password);
                dialog.dismiss();
            }
        };

        if (isPassword)
        {
            dialogBuilder
                    .setTitle(getString(R.string.explore_region_password))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.explore_move_confirm), dialogClickListener)
                    .setNegativeButton(getString(R.string.explore_negative), null);
        }
        else
        {
            dialogBuilder
                    .setTitle(String.format(getString(R.string.explore_region_move), name))
                    .setPositiveButton(getString(R.string.explore_move_confirm), dialogClickListener)
                    .setNegativeButton(getString(R.string.explore_negative), null);
        }

        Dialog d = dialogBuilder.create();
        if (isPassword)
        {
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        d.show();
    }

    /**
     * POSTs to the server to move a nation to a region.
     * @param localid Required localId
     * @param password Password (can be null)
     */
    private void postRegionMove(final String localid, final String password)
    {
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, Region.CHANGE_QUERY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Matcher moveSuccess = REGION_MOVE_SUCCESS.matcher(response);
                        Matcher moveWrongPassword = REGION_MOVE_WRONG_PASS.matcher(response);
                        isInProgress = false;

                        if (moveSuccess.find())
                        {
                            Intent statelyActivityLaunch = new Intent(ExploreActivity.this, StatelyActivity.class);
                            statelyActivityLaunch.putExtra(StatelyActivity.NAV_INIT, StatelyActivity.REGION_FRAGMENT);
                            statelyActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(statelyActivityLaunch);
                        }
                        else if (moveWrongPassword.find())
                        {
                            SparkleHelper.makeSnackbar(view, getString(R.string.explore_move_wrong_password));
                        }
                        else
                        {
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("move_region", "1");
        params.put("region_name", SparkleHelper.getIdFromName(id));
        params.put("localid", localid);
        if (password != null) {
            params.put("password", password);
        }
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            isInProgress = false;
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
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
                getLocalId(String.format(Nation.QUERY_HTML, SparkleHelper.getIdFromName(id)), null);
                return true;
            case R.id.nav_move:
                handleRegionMove();
                return true;
            case R.id.nav_explore:
                // Open an explore dialog to keep going
                FragmentManager fm = getSupportFragmentManager();
                ExploreDialog editNameDialog = new ExploreDialog();
                editNameDialog.show(fm, ExploreDialog.DIALOG_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
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
    }
}
