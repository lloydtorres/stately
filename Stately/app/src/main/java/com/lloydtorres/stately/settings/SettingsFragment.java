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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lloydtorres.stately.BuildConfig;
import com.lloydtorres.stately.R;
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
    private static final String DEVELOPER_TARGET = "Greater Tern";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        Preference appVersionSetting = findPreference(SettingsActivity.SETTING_APP_VERSION);
        appVersionSetting.setTitle(String.format(Locale.US, getString(R.string.app_version),
                BuildConfig.VERSION_NAME));

        // Set on click listener to send telegram
        Preference sendTelegramSetting = findPreference(SettingsActivity.SETTING_SEND_TELEGRAM);
        sendTelegramSetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SparkleHelper.startTelegramCompose(getContext(), DEVELOPER_TARGET,
                        TelegramComposeActivity.NO_REPLY_ID, true);
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
}
