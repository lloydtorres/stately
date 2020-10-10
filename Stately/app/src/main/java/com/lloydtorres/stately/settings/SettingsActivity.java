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

package com.lloydtorres.stately.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.SlidrActivity;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.login.LoginActivity;
import com.lloydtorres.stately.push.TrixHelper;
import com.lloydtorres.stately.zombie.NightmareHelper;

/**
 * Created by Lloyd on 2016-01-27.
 * An activity to show app settings.
 */
public class SettingsActivity extends SlidrActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    // Keys for data passed in activity intent
    public static final String INTENT_IS_CHANGE_THEME_TRIGGERED = "isChangeThemeTriggered";

    // Keys for each setting option
    public static final String SETTING_AUTOLOGIN = "setting_autologin";
    public static final String SETTING_ISSUECONFIRM = "setting_issueconfirm";
    public static final String SETTING_EXITCONFIRM = "setting_exitconfirm";

    public static final String SETTING_NOTIFS = "setting_notifs";
    public static final String SETTING_NOTIFS_CHECK = "setting_notifs_check";
    public static final String SETTING_NOTIFS_ISSUES = "setting_notifs_issues";
    public static final String SETTING_NOTIFS_TGS = "setting_notifs_tgs";
    public static final String SETTING_NOTIFS_RMB_MENTION = "setting_notifs_rmb_mention";
    public static final String SETTING_NOTIFS_RMB_QUOTES = "setting_notifs_rmb_quote";
    public static final String SETTING_NOTIFS_RMB_LIKE = "setting_notifs_rmb_like";
    public static final String SETTING_NOTIFS_ENDORSE = "setting_notifs_endorse";

    public static final String SETTING_THEME = "setting_theme";
    public static final String SETTING_GOVERNMENT = "setting_category";

    public static final String SETTING_APP_VERSION = "setting_app_version";
    public static final String SETTING_SEND_TELEGRAM = "setting_send_telegram";

    // Theme keys
    public static final int THEME_VERT = 0;
    public static final int THEME_NOIR = 1;
    public static final int THEME_BLEU = 2;
    public static final int THEME_ROUGE = 3;
    public static final int THEME_VIOLET = 4;

    // Government category keys
    public static final int GOV_CONSERVATIVE = 0;
    public static final int GOV_LIBERAL = 1;
    public static final int GOV_NEUTRAL = 2;

    public static final long NOTIFS_6_HOURS_IN_SEC = 21600L;

    private static AlertDialog.Builder dialogBuilder;

    private SharedPreferences storage;
    private boolean isChangeThemeTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setToolbar(toolbar);

        if (getIntent() != null) {
            isChangeThemeTriggered = getIntent().getBooleanExtra(INTENT_IS_CHANGE_THEME_TRIGGERED, false);
        }

        if (isChangeThemeTriggered) {
            slidrInterface.lock();
        }

        storage = PreferenceManager.getDefaultSharedPreferences(this);
        storage.registerOnSharedPreferenceChangeListener(this);

        dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNewTheme() {
        UserLogin curUser = PinkaHelper.getActiveUser(this);
        Intent loginActivityLaunch = new Intent(SettingsActivity.this, LoginActivity.class);
        loginActivityLaunch.putExtra(LoginActivity.USERDATA_KEY, curUser);
        loginActivityLaunch.putExtra(LoginActivity.NOAUTOLOGIN_KEY, true);
        loginActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivityLaunch);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (isChangeThemeTriggered) {
                    setupNewTheme();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SETTING_NOTIFS:
            case SETTING_NOTIFS_CHECK:
                TrixHelper.stopAlarmForAlphys(this);
                if (getNotificationSetting(this)) {
                    TrixHelper.setAlarmForAlphys(this);
                }
                break;
            case SETTING_THEME:
                // Restart settings activity on theme change
                Intent settingsActivityLaunch = new Intent(this, SettingsActivity.class);
                settingsActivityLaunch.putExtra(INTENT_IS_CHANGE_THEME_TRIGGERED, true);
                startActivity(settingsActivityLaunch);
                finish();
                break;
            case SETTING_GOVERNMENT:
                isChangeThemeTriggered = true;
                break;
        }
    }

    /**
     * GENERAL SETTINGS
     */

    public static boolean getAutologinSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(SettingsActivity.SETTING_AUTOLOGIN, true);
    }

    public static boolean getConfirmIssueDecisionSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(SettingsActivity.SETTING_ISSUECONFIRM, true);
    }

    public static boolean getConfirmExitSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(SettingsActivity.SETTING_EXITCONFIRM, true);
    }

    /**
     * NOTIFICATIONS SETTINGS
     */

    public static boolean getNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(SettingsActivity.SETTING_NOTIFS, false);
    }

    public static long getNotificationIntervalSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        long interval = NOTIFS_6_HOURS_IN_SEC;
        try {
            interval = Long.valueOf(storage.getString(SettingsActivity.SETTING_NOTIFS_CHECK, String.valueOf(NOTIFS_6_HOURS_IN_SEC)));
        }
        catch (Exception e) {
            SparkleHelper.logError(e.toString());
        }
        return interval;
    }

    public static boolean getIssuesNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_ISSUES, true);
    }

    public static boolean getTelegramsNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_TGS, true);
    }

    public static boolean getRmbMentionNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_RMB_MENTION, true);
    }

    public static boolean getRmbQuoteNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_RMB_QUOTES, true);
    }

    public static boolean getRmbLikeNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_RMB_LIKE, true);
    }

    public static boolean getEndorsementNotificationSetting(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return getNotificationSetting(c) &&
                storage.getBoolean(SettingsActivity.SETTING_NOTIFS_ENDORSE, true);
    }

    /**
     * STYLING SETTINGS
     */

    public static int getTheme(Context c) {
        // Safety check
        if (c == null) {
            return THEME_VERT;
        }

        // Force noir theme on Z-Day
        if (NightmareHelper.getIsZDayActive(c)) {
            return THEME_NOIR;
        }

        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        int theme = THEME_VERT;
        try {
            theme = Integer.valueOf(storage.getString(SettingsActivity.SETTING_THEME, String.valueOf(THEME_VERT)));
        }
        catch (Exception e) {
            SparkleHelper.logError(e.toString());
        }
        return theme;
    }

    public static int getGovernmentSetting(Context c) {
        int mode = GOV_NEUTRAL;
        if (c == null) {
            return mode;
        }

        if (RaraHelper.getSpecialDayStatus(c) != RaraHelper.DAY_APRIL_FOOLS) {
            SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
            try {
                mode = Integer.valueOf(storage.getString(SettingsActivity.SETTING_GOVERNMENT, String.valueOf(GOV_NEUTRAL)));
            }
            catch (Exception e) {
                SparkleHelper.logError(e.toString());
            }
            return mode;
        } else {
            return Math.random() >= 0.5d ? GOV_CONSERVATIVE : GOV_LIBERAL;
        }
    }

    @Override
    public void onBackPressed() {
        if (isChangeThemeTriggered) {
            setupNewTheme();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        storage.unregisterOnSharedPreferenceChangeListener(this);
    }
}
