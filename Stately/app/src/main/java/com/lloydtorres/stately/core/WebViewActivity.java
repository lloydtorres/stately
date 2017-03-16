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

package com.lloydtorres.stately.core;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.lloydtorres.stately.BuildConfig;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.network.NSStringRequest;

import java.util.Locale;

/**
 * Created by lloyd on 2017-03-16.
 * Activity used for rendering web pages in-app.
 */
public abstract class WebViewActivity extends SlidrActivity {
    protected WebView mWebView;
    protected ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setToolbar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.register_progress_bar);

        // Disable cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        // Initialize WebView
        mWebView = (WebView) findViewById(R.id.register_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setUserAgentString(String.format(Locale.US, NSStringRequest.STATELY_USER_AGENT_NOUSER, BuildConfig.VERSION_NAME));
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    protected void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
