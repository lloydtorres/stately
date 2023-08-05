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

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lloydtorres.stately.BuildConfig;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.telegrams.TelegramComposeActivity;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-27.
 * The fragment within the Settings activity.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int NOTIFICATIONS_PERMISSION_REQUEST_CODE = 1;
    private static final String DEVELOPER_TARGET = "Greater Tern";
    private static final String NS_RIGHT_TO_ERASURE_POLICY_LINK =
            "https://m.nationstates.net/page=privacy#Erase";

    private Preference mNotificationsSetting;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        final Preference appVersionSetting = findPreference(SettingsActivity.SETTING_APP_VERSION);
        appVersionSetting.setTitle(String.format(Locale.US, getString(R.string.app_version),
                BuildConfig.VERSION_NAME));

        // Set on click listener for notifications
        mNotificationsSetting = findPreference(SettingsActivity.SETTING_NOTIFS);
        mNotificationsSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                handleNotificationsPermission();
                return true;
            }
        });

        // Set on click listener to send telegram
        final Preference sendTelegramSetting = findPreference(SettingsActivity.SETTING_SEND_TELEGRAM);
        sendTelegramSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SparkleHelper.startTelegramCompose(getContext(), DEVELOPER_TARGET,
                        TelegramComposeActivity.NO_REPLY_ID, true);
                return true;
            }
        });

        // Set on click listener for NationStates erasure policy
        final Preference deleteDataSetting = findPreference(SettingsActivity.SETTING_DELETE_DATA);
        deleteDataSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDeleteDataConfirmationDialog();
                return true;
            }
        });

        // Disable theme options and show warning on Z-Day
        if (getContext() != null) {
            if (NightmareHelper.getIsZDayActive(getContext())) {
                Preference themeSetting = findPreference(SettingsActivity.SETTING_THEME);
                themeSetting.setEnabled(false);
                themeSetting.setSummary(getString(R.string.setting_desc_zombie));
            }
            if (RaraHelper.getSpecialDayStatus(getContext()) == RaraHelper.DAY_APRIL_FOOLS) {
                Preference governmentSetting = findPreference(SettingsActivity.SETTING_GOVERNMENT);
                governmentSetting.setEnabled(false);
                governmentSetting.setSummary(getString(R.string.setting_category_april));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void handleNotificationsPermission() {
        final boolean isNotificationsEnabled = SettingsActivity.getNotificationSetting(getContext());
        if (!isNotificationsEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;

        final int notificationPermissionsGrantedState =
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS);
        if (notificationPermissionsGrantedState == PackageManager.PERMISSION_GRANTED) return;
        requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATIONS_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        SparkleHelper.makeSnackbar(getView(), getString(R.string.setting_notifs_permission_denied_error));
        mNotificationsSetting.setEnabled(false);
        SettingsActivity.setNotificationSetting(getContext(), false);

    }

    private void showDeleteDataConfirmationDialog() {
        final Activity activity = getActivity();

        final DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PinkaHelper.removeActiveUser(activity);
                final Intent openPolicyInBrowserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(NS_RIGHT_TO_ERASURE_POLICY_LINK));
                startActivity(openPolicyInBrowserIntent);
                activity.finishAffinity();
            }
        };

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity,
                RaraHelper.getThemeMaterialDialog(activity));
        dialogBuilder.setTitle(R.string.setting_delete_data)
                .setMessage(R.string.setting_delete_data_dialog_content)
                .setPositiveButton(R.string.create_continue, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
    }
}
