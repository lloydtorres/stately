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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.orm.SugarApp;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Lloyd on 2016-01-29.
 * This class is used for app-wide changes.
 */
public class StatelyApp extends SugarApp {
    private SharedPreferences storage; // shared preferences

    @Override
    public void onCreate() {
        super.onCreate();

        // analytics
        storage = PreferenceManager.getDefaultSharedPreferences(this);
        if (storage.getBoolean("setting_crashreport", true))
        {
            Fabric.with(this, new Crashlytics());
        }
    }
}
