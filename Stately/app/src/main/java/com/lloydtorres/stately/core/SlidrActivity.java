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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.r0adkll.slidr.Slidr;

/**
 * Created by Lloyd on 2016-09-10.
 * Base activity for all activities using the 'AppTheme.SlidrActivity' theme.
 */
public abstract class SlidrActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int primaryColor = 0;
        int secondaryColor = 0;

        switch(SettingsActivity.getTheme(this)) {
            case SettingsActivity.THEME_VERT:
                setTheme(R.style.AppTheme_SlidrActivity);
                primaryColor = R.color.colorPrimary;
                secondaryColor = R.color.colorPrimaryDark;
                break;
            case SettingsActivity.THEME_ROUGE:
                setTheme(R.style.AppThemeRouge_SlidrActivity);
                primaryColor = R.color.colorPrimaryRouge;
                secondaryColor = R.color.colorPrimaryDarkRouge;
                break;
        }

        Slidr.attach(this, SparkleHelper.slidrConfig);
        super.onCreate(savedInstanceState);
    }
}
