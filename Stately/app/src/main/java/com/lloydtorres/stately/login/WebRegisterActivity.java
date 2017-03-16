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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.WebViewActivity;
import com.lloydtorres.stately.helpers.network.ProgressBarWebViewClient;

/**
 * Created by Lloyd on 2016-08-06.
 * An activity containing a WebView for creating new nations.
 */
public class WebRegisterActivity extends WebViewActivity {

    public static final String REGISTER_URL = "https://m.nationstates.net/page=create_nation";
    public static final int REGISTER_RESULT = 54321;

    public static final String FINISHED_URL_PART = "https://m.nationstates.net/nation=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.create_nation));

        // Checks if the user is in the nation page on load
        mWebView.setWebViewClient(new ProgressBarWebViewClient(progressBar) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url != null && url.contains(FINISHED_URL_PART)) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        mWebView.loadUrl(REGISTER_URL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
