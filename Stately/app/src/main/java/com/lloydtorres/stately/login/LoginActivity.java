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

package com.lloydtorres.stately.login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.BroadcastableActivity;
import com.lloydtorres.stately.core.StatelyActivity;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.dto.UserNation;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.push.TrixHelper;
import com.lloydtorres.stately.region.MessageBoardActivity;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.telegrams.TelegramHistoryActivity;
import com.lloydtorres.stately.zombie.NightmareHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simpleframework.xml.core.Persister;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-13.
 * The launcher activity for Stately!
 * Takes in user logins and verifies them against NationStates.
 */
public class LoginActivity extends BroadcastableActivity {
    // Intent keys
    public static final String USERDATA_KEY = "userdata";
    public static final String NOAUTOLOGIN_KEY = "noAutologin";
    public static final String ROUTE_BUNDLE_KEY = "routeBundleKey";
    public static final String ROUTE_PATH_KEY = "routePathKey";

    // Routes for intents from notifications
    public static final int ROUTE_ISSUES = 0;
    public static final int ROUTE_TG = 1;
    public static final int ROUTE_RMB = 2;
    public static final int ROUTE_EXPLORE = 3;

    // Other constants
    public static final int INVALID_NATION_ERROR_GUIDANCE_LIMIT = 3;

    private View view;
    private ImageView headerImage;
    private TextView subtitle;
    private AppCompatEditText username;
    private AppCompatEditText password;
    private TextInputLayout userHolder;
    private TextInputLayout passHolder;
    private Button login;
    private Button createNation;
    private boolean isLocked;

    private Bundle routeBundle;

    private int invalidAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch(SettingsActivity.getTheme(this)) {
            case SettingsActivity.THEME_VERT:
                setTheme(R.style.AppTheme);
                break;
            case SettingsActivity.THEME_NOIR:
                setTheme(R.style.AppThemeNoir);
                break;
            case SettingsActivity.THEME_BLEU:
                setTheme(R.style.AppThemeBleu);
                break;
            case SettingsActivity.THEME_ROUGE:
                setTheme(R.style.AppThemeRouge);
                break;
            case SettingsActivity.THEME_VIOLET:
                setTheme(R.style.AppThemeViolet);
                break;
        }
        setContentView(R.layout.activity_login);

        view = findViewById(R.id.activity_login_main);
        headerImage = findViewById(R.id.login_header);
        subtitle = findViewById(R.id.login_subtitle);
        username = findViewById(R.id.field_username);
        password = findViewById(R.id.field_password);
        userHolder = findViewById(R.id.holder_user);
        passHolder = findViewById(R.id.holder_password);
        login = findViewById(R.id.login_button);
        createNation = findViewById(R.id.register_button);

        int statelyLogo = RaraHelper.getSpecialDayStatus(this) != RaraHelper.DAY_Z_DAY ? R.drawable.stately : R.drawable.stately_zday;
        DashHelper.getInstance(this).loadImageWithoutPlaceHolder(statelyLogo, headerImage);

        if (RaraHelper.getSpecialDayStatus(this) == RaraHelper.DAY_NORMAL) {
            switch (SettingsActivity.getGovernmentSetting(this)) {
                case SettingsActivity.GOV_CONSERVATIVE:
                    subtitle.setText(getString(R.string.login_subtitle_conservative));
                    break;
                case SettingsActivity.GOV_LIBERAL:
                    subtitle.setText(getString(R.string.login_subtitle_liberal));
                    break;
                default:
                    subtitle.setText(getString(R.string.login_subtitle_neutral));
                    break;
            }
        } else {
            switch (RaraHelper.getSpecialDayStatus(this)) {
                case RaraHelper.DAY_Z_DAY:
                    subtitle.setText(getString(R.string.login_subtitle_zday));
                    break;
                case RaraHelper.DAY_NEW_YEAR:
                    subtitle.setText(String.format(Locale.US, getString(R.string.login_subtitle_new_year), SparkleHelper.getUtc5Calendar().get(Calendar.YEAR)));
                    break;
                case RaraHelper.DAY_STATELY_BIRTHDAY:
                    subtitle.setText(getString(R.string.login_subtitle_stately_bday));
                    break;
                case RaraHelper.DAY_APRIL_FOOLS:
                    subtitle.setText(getString(R.string.login_subtitle_april_fools));
                    break;
                case RaraHelper.DAY_CANADA_DAY:
                    subtitle.setText(getString(R.string.login_subtitle_canada_day));
                    break;
                case RaraHelper.DAY_HALLOWEEN:
                    subtitle.setText(getString(R.string.login_subtitle_halloween));
                    break;
                case RaraHelper.DAY_NS_BIRTHDAY:
                    subtitle.setText(String.format(Locale.US, getString(R.string.login_subtitle_ns_bday), (SparkleHelper.getUtc5Calendar().get(Calendar.YEAR) - RaraHelper.NS_FOUNDATION_YEAR)));
                    break;
            }
        }

        // If activity was launched by an intent, handle that first
        if (getIntent() != null) {
            UserLogin userdata = getIntent().getParcelableExtra(USERDATA_KEY);
            boolean noAutologin = getIntent().getBooleanExtra(NOAUTOLOGIN_KEY, false);
            routeBundle = getIntent().getBundleExtra(ROUTE_BUNDLE_KEY);

            if (userdata != null || routeBundle != null) {
                verifyAccount(userdata);
                return;
            }

            // If the launching intent doesn't want to autologin, skip that
            if (noAutologin) {
                return;
            }
        }

        // If settings allows it and user login exists, try logging in first
        if (SettingsActivity.getAutologinSetting(this)) {
            UserLogin u = PinkaHelper.getActiveUser(this);
            checkZDayActive(u);
        } else {
            PinkaHelper.removeActiveUser(this);
            checkZDayActive(null);
        }
    }

    /**
     * Callback for login button.
     * Verifies if the input is valid. If yes, verify password next.
     * @param view
     */
    public void verifyUsername(View view) {
        if (!getLockedState()) {
            setLockedState(true);
            String name = username.getText().toString();
            if (SparkleHelper.isValidName(name) && name.length() > 0) {
                String pass = password.getText().toString();
                verifyAccount(name, pass);
            } else {
                setLockedState(false);
                SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
                invalidAttempts++;
                showInvalidLoginGuidance();
            }
        }
    }

    /**
     * Builds an auth-required NS API call that downloads nation data and starts the app on success.
     * Shows appropriate errors otherwise.
     * @param nationId Target nation's ID.
     * @param u UserLogin data (if available)
     * @return No frills NSStringRequest
     */
    private NSStringRequest buildUserAuthRequest(final String nationId, final UserLogin u) {
        final String targetURL = String.format(Locale.US, UserNation.QUERY, SparkleHelper.getIdFromName(nationId));
        final String oldActivePin = PinkaHelper.getActivePin(this);
        NSStringRequest stringRequest = new NSStringRequest(this, Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    UserNation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        // Don't bother if user already closed app
                        if (isFinishing()) {
                            return;
                        }

                        Persister serializer = new Persister();
                        try {
                            nationResponse = UserNation.parseNationFromXML(LoginActivity.this, serializer, response);

                            if (u != null) {
                                PinkaHelper.setActiveAutologin(LoginActivity.this, u.autologin);

                                // Only override pin if it hasn't been changed by the server
                                String newActivePin = PinkaHelper.getActivePin(LoginActivity.this);
                                if (newActivePin != null && oldActivePin != null && newActivePin.equals(oldActivePin)) {
                                    PinkaHelper.setActivePin(LoginActivity.this, u.pin);
                                }
                            }
                            PinkaHelper.setActiveUser(LoginActivity.this, nationResponse.name);
                            PinkaHelper.setSessionData(LoginActivity.this, SparkleHelper.getIdFromName(nationResponse.region), nationResponse.waState);

                            if (routeBundle == null) {
                                Intent nationActivityLaunch = new Intent(LoginActivity.this, StatelyActivity.class);
                                nationActivityLaunch.putExtra(StatelyActivity.NATION_DATA, nationResponse);
                                nationActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(nationActivityLaunch);
                            } else {
                                Intent nextActivity = new Intent();
                                int routePath = routeBundle.getInt(ROUTE_PATH_KEY, ROUTE_ISSUES);
                                switch (routePath) {
                                    case ROUTE_ISSUES:
                                        nextActivity = new Intent(LoginActivity.this, StatelyActivity.class);
                                        break;
                                    case ROUTE_TG:
                                        nextActivity = new Intent(LoginActivity.this, TelegramHistoryActivity.class);
                                        break;
                                    case ROUTE_RMB:
                                        nextActivity = new Intent(LoginActivity.this, MessageBoardActivity.class);
                                        break;
                                    case ROUTE_EXPLORE:
                                        nextActivity = new Intent(LoginActivity.this, ExploreActivity.class);
                                        break;
                                }
                                for (String bundleKey : routeBundle.keySet()) {
                                    Object value = routeBundle.get(bundleKey);
                                    if (value instanceof String) {
                                        nextActivity.putExtra(bundleKey, (String) routeBundle.get(bundleKey));
                                    } else if (value instanceof Integer) {
                                        nextActivity.putExtra(bundleKey, (Integer) routeBundle.get(bundleKey));
                                    }
                                }
                                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(nextActivity);
                            }
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            setLockedState(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());

                if (isFinishing()) {
                    return;
                }

                setLockedState(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError || error instanceof AuthFailureError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
                    invalidAttempts++;
                    showInvalidLoginGuidance();
                }
                else {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        });
        return stringRequest;
    }

    /**
     * Builds an authenticated user request and executes it.
     * This version relies on the user/pass combo typed in by the user.
     * (For normal logins.)
     * @param user Username
     * @param pass Password
     */
    private void verifyAccount(String user, String pass) {
        NSStringRequest stringRequest = buildUserAuthRequest(SparkleHelper.getIdFromName(user), null);
        stringRequest.setPassword(pass);
        // Set dummy UserLogin data
        stringRequest.setUserData(new UserLogin());
        executeRequest(stringRequest);
    }

    /**
     * Builds an authenticated user request and executes it.
     * This version relies on a UserLogin object passed to it, either from autologin or
     * by the nation switcher.
     * @param u
     */
    private void verifyAccount(UserLogin u) {
        setLockedState(true);
        NSStringRequest stringRequest = buildUserAuthRequest(u.nationId, u);
        stringRequest.setUserData(u);
        executeRequest(stringRequest);
    }

    /**
     * Executes the provided string request, if the app won't go overboard the limit.
     * @param stringRequest
     */
    private void executeRequest(NSStringRequest stringRequest) {
        executeRequest(stringRequest, getString(R.string.log_in_load));
    }

    private void executeRequest(NSStringRequest stringRequest, String lockedMessage) {
        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
            setLockedState(false, lockedMessage);
        }
    }

    /**
     * Callback for the 'Create New Nation' button.
     * Opens a dialog to confirm the start of nation creation.
     * @param v
     */
    public void startCreateNation(View v) {
        if (isFinishing()) {
            return;
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent createNationIntent = new Intent(LoginActivity.this, WebRegisterActivity.class);
                startActivityForResult(createNationIntent, WebRegisterActivity.REGISTER_RESULT);
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
        dialogBuilder.setTitle(R.string.create_nation)
                .setMessage(R.string.create_nation_redirect)
                .setPositiveButton(R.string.create_continue, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
    }

    /**
     * First checks if Z-Day is active in NS and sets the appropriate variables. If a UserLogin
     * was passed in, it continues with autlogin.
     * @param userLogin
     */
    public void checkZDayActive(final UserLogin userLogin) {
        // Skip check if it's not around Halloween
        Calendar cal = SparkleHelper.getUtc5Calendar();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (!((month == Calendar.OCTOBER && day >= 25) ||
                (month == Calendar.NOVEMBER && day <= 2))) {
            NightmareHelper.setIsZDayActive(this, false);
            continueToVerifyAccount(userLogin);
            return;
        }

        setLockedState(true, getString(R.string.calibration_load));
        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, NightmareHelper.ZDAY_REFERENCE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        boolean isZDayActive = d.select(NightmareHelper.ZDAY_REFERENCE_DIV).first() != null;
                        NightmareHelper.setIsZDayActive(LoginActivity.this, isZDayActive);
                        continueToVerifyAccount(userLogin);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NightmareHelper.setIsZDayActive(LoginActivity.this, false);
                continueToVerifyAccount(userLogin);
            }
        });
        // This forces the request to check without logging in
        UserLogin nullUser = new UserLogin();
        nullUser.nationId = null;
        stringRequest.setUserData(nullUser);
        executeRequest(stringRequest, getString(R.string.calibration_load));
    }

    /**
     * Helper function used to continue the account verification process.
     * @param u
     */
    private void continueToVerifyAccount(UserLogin u) {
        if (u != null) {
            verifyAccount(u);
        } else {
            setLockedState(false);
        }
    }

    /**
     * Shows a dialog providing a suggestion to copy-paste nation name into field (has helped some users)
     * when too many login errors occur.
     */
    private void showInvalidLoginGuidance() {
        if (invalidAttempts == INVALID_NATION_ERROR_GUIDANCE_LIMIT && !isFinishing()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
            dialogBuilder.setTitle(R.string.guidance_invalid_login_title)
                    .setMessage(R.string.guidance_invalid_login_content)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isFinishing()) {
            return;
        }

        if (requestCode == WebRegisterActivity.REGISTER_RESULT && resultCode == Activity.RESULT_OK) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
            dialogBuilder.setTitle(R.string.create_nation)
                    .setMessage(R.string.create_finish)
                    .setPositiveButton(R.string.got_it, null)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // notification polling
        TrixHelper.updateLastActiveTime(this);
        TrixHelper.stopAlarmForAlphys(this);
        if (SettingsActivity.getNotificationSetting(this)) {
            TrixHelper.setAlarmForAlphys(this);
        }
    }

    /**
     * Get the locked state (e.g. if the login process is currently being done).
     * @return The locked state (true or not).
     */
    private boolean getLockedState() {
        return isLocked;
    }

    /**
     * Set the locked state.
     * @param stat The current locked state. True if performing some network call, false otherwise.
     */
    private void setLockedState(boolean stat) {
        setLockedState(stat, getString(R.string.log_in_load));
    }

    private void setLockedState(boolean stat, String message) {
        if (stat) {
            userHolder.setVisibility(View.GONE);
            passHolder.setVisibility(View.GONE);
            login.setText(message);
            createNation.setVisibility(View.GONE);
        } else {
            userHolder.setVisibility(View.VISIBLE);
            passHolder.setVisibility(View.VISIBLE);
            login.setText(getString(R.string.log_in));
            createNation.setVisibility(View.VISIBLE);
        }
        isLocked = stat;
    }
}
