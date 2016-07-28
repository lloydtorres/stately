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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.r0adkll.slidr.Slidr;

/**
 * Created by Lloyd on 2016-01-27.
 * An activity to show app settings.
 */
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String SETTING_AUTOLOGIN = "setting_autologin";
    public static final String SETTING_ISSUECONFIRM = "setting_issueconfirm";
    public static final String SETTING_EXITCONFIRM = "setting_exitconfirm";
    public static final String SETTING_CRASHREPORT = "setting_crashreport";

    private SharedPreferences storage;
    private static AlertDialog.Builder dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Slidr.attach(this, SparkleHelper.slidrConfig);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setToolbar(toolbar);

        storage = PreferenceManager.getDefaultSharedPreferences(this);
        storage.registerOnSharedPreferenceChangeListener(this);

        dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
    }

    private void setToolbar(Toolbar toolbar)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        storage.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SETTING_CRASHREPORT)) {
            dialogBuilder.setMessage(getString(R.string.warn_crashreport)).setPositiveButton(getString(R.string.got_it), null).show();
        }
    }
}
