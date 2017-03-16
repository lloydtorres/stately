/**
 * Copyright 2017 Lloyd Torres
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
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.WebViewActivity;

/**
 * Created by lloyd on 2017-03-16.
 * Shows Stately's privacy policy.
 */
public class PrivacyPolicyActivity extends WebViewActivity {
    private static final String PRIVACY_POLICY_URL = "https://www.iubenda.com/privacy-policy/7793041";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.setting_privacy_policy));

        mWebView.loadUrl(PRIVACY_POLICY_URL);
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
}
