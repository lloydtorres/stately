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

package com.lloydtorres.stately.helpers.network;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.BuildConfig;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-08-05.
 * A custom implementation of Volley's StringRequest for interacting with the NationStates servers.
 */
public class NSStringRequest extends StringRequest {

    public static final String PIN_INVALID = "-1";
    public static final String STATELY_USER_AGENT_USER = "Stately/%s (User %s; Droid)";
    public static final String STATELY_USER_AGENT_NOUSER = "Stately/%s (No User; Droid)";

    private static final String HEADER_PIN = "X-Pin";
    private static final String HEADER_PASSWORD = "X-Password";
    private static final String HEADER_AUTOLOGIN = "X-Autologin";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_SET_COOKIE = "Set-Cookie";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_RATE_LIMIT_LIMIT = "RateLimit-Limit";
    private static final String HEADER_RATE_LIMIT_REMAINING = "RateLimit-Remaining";
    private static final String HEADER_RATE_LIMIT_RESET = "RateLimit-Reset";

    private final Context context;
    private UserLogin userDataOverride = null;
    private String password;
    private final int method;
    private Map<String, String> params = new HashMap<String, String>();
    private final Pattern COOKIE_PIN = Pattern.compile("(?:^|\\s+?|;\\s*?)pin=(\\d+?)(?:$|;\\s*?|\\s+?)");

    public NSStringRequest(Context c, int m, String target,
                           boolean appendUserClickTimestamp,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        this(c, m,
                target + (appendUserClickTimestamp ? String.format(Locale.US, "?userclick=%d", System.currentTimeMillis()) : ""),
                listener, errorListener);
    }

    public NSStringRequest(Context c, int m, String target,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        super(m, target, listener, errorListener);
        context = c;
        method = m;
    }

    public void setUserData(UserLogin u) {
        userDataOverride = u;
    }

    public void setPassword(String p) {
        password = p;
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> p) {
        Map<String, String> escapedHashMap = new HashMap<String, String>();
        for (String key : p.keySet()) {
            escapedHashMap.put(SparkleHelper.escapeHtml(key), SparkleHelper.escapeHtml(p.get(key)));
        }
        params = escapedHashMap;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        UserLogin u = userDataOverride == null ? PinkaHelper.getActiveUser(context) :
                userDataOverride;

        // UserLogin will not be null when user is logged in
        if (u != null && u.nationId != null) {
            headers.put(HEADER_USER_AGENT, String.format(Locale.US, STATELY_USER_AGENT_USER,
                    BuildConfig.VERSION_NAME, u.nationId));

            // Case 1: If only autologin cookie is available/pin cookie is invalid
            if ((u.pin == null || PIN_INVALID.equals(u.pin)) && u.autologin != null) {
                headers.put(HEADER_COOKIE, String.format(Locale.US, "autologin=%s",
                        buildCookieAutologinToken(u.nationId, u.autologin)));
                headers.put(HEADER_AUTOLOGIN, buildHeaderAutologinToken(u.nationId, u.autologin));
            }
            // Case 2: If both autologin and pin cookies are available and pin cookie is good
            else if (u.autologin != null && u.pin != null && !PIN_INVALID.equals(u.pin)) {
                headers.put(HEADER_COOKIE, String.format(Locale.US, "autologin=%s; pin=%s",
                        buildCookieAutologinToken(u.nationId, u.autologin), u.pin));
                headers.put(HEADER_AUTOLOGIN, buildHeaderAutologinToken(u.nationId, u.autologin));
                headers.put(HEADER_PIN, u.pin);
            }
            // Case 3: Both are missing, don't do anything
            // ...
        } else {
            headers.put(HEADER_USER_AGENT, String.format(Locale.US, STATELY_USER_AGENT_NOUSER,
                    BuildConfig.VERSION_NAME));
        }

        // If the password is provided, add that to the header
        if (password != null) {
            headers.put(HEADER_PASSWORD, SparkleHelper.escapeHtml(password));
        }

        // Only include x-www-form-urlencoded for POSTs
        if (method == Request.Method.POST) {
            headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
        }
        return headers;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> responseHeaders = response.headers;

        // Update PIN if new one available
        if (responseHeaders.containsKey(HEADER_PIN) && !PIN_INVALID.equals(responseHeaders.get(
                HEADER_PIN))) {
            PinkaHelper.setActivePin(context, responseHeaders.get(HEADER_PIN));
        }

        // Update PIN from cookie if available AND X-Pin not provided
        if (responseHeaders.containsKey(HEADER_SET_COOKIE) &&
                !responseHeaders.containsKey(HEADER_PIN)) {
            Matcher m = COOKIE_PIN.matcher(responseHeaders.get(HEADER_SET_COOKIE));
            if (m.matches() && !PIN_INVALID.equals(m.group(1))) {
                PinkaHelper.setActivePin(context, m.group(1));
            }
        }

        // Update autologin if new one available
        if (responseHeaders.containsKey(HEADER_AUTOLOGIN)) {
            PinkaHelper.setActiveAutologin(context, responseHeaders.get(HEADER_AUTOLOGIN));
        }

        // Sync number of requests seen by server with internal count
        setRateLimitValues(responseHeaders);

        return super.parseNetworkResponse(response);
    }

    private void setRateLimitValues(final Map<String, String> responseHeaders) {
        final DashHelper dashie = DashHelper.getInstance(context);
        if (dashie == null) return;

        if (responseHeaders.containsKey(HEADER_RATE_LIMIT_LIMIT)) {
            try {
                final int serverRateLimit =
                        Integer.parseInt(responseHeaders.get(HEADER_RATE_LIMIT_LIMIT));
                dashie.setRateLimit(serverRateLimit);
            } catch (Exception e) {
                SparkleHelper.logError(e.toString());
            }
        }

        if (responseHeaders.containsKey(HEADER_RATE_LIMIT_REMAINING)) {
            try {
                final int serverRateLimitRemaining =
                        Integer.parseInt(responseHeaders.get(HEADER_RATE_LIMIT_REMAINING));
                dashie.setRemainingCalls(serverRateLimitRemaining);
            } catch (Exception e) {
                SparkleHelper.logError(e.toString());
            }
        }

        if (responseHeaders.containsKey(HEADER_RATE_LIMIT_RESET)) {
            try {
                final int serverNextReset =
                        Integer.parseInt(responseHeaders.get(HEADER_RATE_LIMIT_RESET));
                dashie.setNextReset(serverNextReset);
            } catch (Exception e) {
                SparkleHelper.logError(e.toString());
            }
        }
    }

    /**
     * This function builds the correct autologin cookie expected by the NS servers depending
     * on the version stored locally.
     * If the stored version begins with ".", it's the new format.
     * Otherwise, it's the old legacy format.
     * Regardless of the format, build and return the correct cookie format, which is:
     * [nation name]%3d[token]
     * @param nationId Target nation's ID
     * @param autologin Target nation's stored autologin
     * @return
     */
    private String buildCookieAutologinToken(String nationId, String autologin) {
        if (autologin != null && autologin.length() > 0) {
            String newAutologin;

            // New autologin format
            if (".".equals(autologin.substring(0, 1))) {
                newAutologin = String.format(Locale.US, "%s%%3D%s", nationId, autologin);
            }
            // Old autologin format
            else {
                newAutologin = autologin;
            }
            newAutologin = sketchyUrlDecode(newAutologin);

            return newAutologin;
        }
        return autologin;
    }

    /**
     * This function builds the correct autologin header token expected by the NS servers
     * depending on the version stored locally.
     * If the stored version begins with ".", it's the new format.
     * Otherwise, it's the old legacy format.
     * Regardless of the format, build and return the correct token format, which is the
     * new format.
     * @param nationId Target nation's ID
     * @param autologin Target nation's stored autologin
     * @return
     */
    private String buildHeaderAutologinToken(String nationId, String autologin) {
        String newAutologin = autologin;
        String toRemove = nationId + "%3D";
        newAutologin = newAutologin.replace(toRemove, "");
        newAutologin = sketchyUrlDecode(newAutologin);
        return newAutologin;
    }

    /**
     * Stopgap for fixing malformed autologin tokens. Decodes URL format but adds the +s back in.
     * @param input
     * @return
     */
    private String sketchyUrlDecode(String input) {
        try {
            input = URLDecoder.decode(input, "UTF-8");
            input = input.replace(" ", "+");
            return input;
        } catch (Exception e) {
            SparkleHelper.logError(e.toString());
            return input;
        }
    }
}
