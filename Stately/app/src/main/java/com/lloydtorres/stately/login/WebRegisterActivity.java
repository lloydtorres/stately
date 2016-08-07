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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-08-06.
 * An activity containing a WebView for creating new nations.
 */
public class WebRegisterActivity extends AppCompatActivity {

    public static final String REGISTER_URL = "https://m.nationstates.net/page=create_nation";
    public static final int REGISTER_RESULT = 54321;

    public static final String FINISHED_URL_PART = "m.nationstates3.net/nation=";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setToolbar(toolbar);

        // Initialize WebView
        mWebView = (WebView) findViewById(R.id.register_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setUserAgentString(getString(R.string.app_header_nouser));

        // Checks if the user is in the nation page on loa
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url != null && url.contains(FINISHED_URL_PART)) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        mWebView.loadUrl(REGISTER_URL);
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(getString(R.string.create_nation));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
