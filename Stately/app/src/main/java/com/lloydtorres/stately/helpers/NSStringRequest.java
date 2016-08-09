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

package com.lloydtorres.stately.helpers;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;

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

    private Context context;
    private UserLogin userDataOverride = null;
    private String password;
    private int method;
    private Map<String, String> params = new HashMap<String, String>();

    public NSStringRequest(Context c, int m, String target,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        super(m, target, listener, errorListener);
        context = c;
        method = m;
    }

    public void setUserData(UserLogin u) { userDataOverride = u; }

    public void setPassword(String p) { password = p; }

    public void setParams(Map<String, String> p) { params = p; }

    @Override
    protected Map<String,String> getParams(){
        return params;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String,String> params = new HashMap<String, String>();
        UserLogin u = userDataOverride == null ? SparkleHelper.getActiveUser(context) : userDataOverride;

        // UserLogin will not be null when user is logged in
        if (u != null && u.nationId != null) {
            params.put("User-Agent", String.format(Locale.US, context.getString(R.string.app_header), u.nationId));

            // Case 1: If only autologin cookie is available/pin cookie is invalid
            if ((u.pin == null || PIN_INVALID.equals(u.pin)) && u.autologin != null) {
                params.put("Cookie", String.format(Locale.US, "autologin=%s", buildCookieAutologinToken(u.nationId, u.autologin)));
                params.put("Autologin", buildHeaderAutologinToken(u.nationId, u.autologin));
            }
            // Case 2: If both autologin and pin cookies are available and pin cookie is good
            else if (u.autologin != null && u.pin != null && !PIN_INVALID.equals(u.pin)) {
                params.put("Cookie", String.format(Locale.US, "autologin=%s; pin=%s", buildCookieAutologinToken(u.nationId, u.autologin), u.pin));
                params.put("Autologin", buildHeaderAutologinToken(u.nationId, u.autologin));
                params.put("Pin", u.pin);
            }
            // Case 3: Both are missing, don't do anything
            // ...
        }
        else {
            params.put("User-Agent", context.getString(R.string.app_header_nouser));
        }

        // If the password is provided, add that to the header
        if (password != null) {
            params.put("Password", password);
        }

        // Only include x-www-form-urlencoded for POSTs
        if (method == Request.Method.POST) {
            params.put("Content-Type", "application/x-www-form-urlencoded");
        }
        return params;
    }

    private Pattern COOKIE_PIN = Pattern.compile("(?:^|\\s+|;\\s*)pin=(\\d+)(?:$|;\\s*|\\s+)");

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Map<String, String> responseHeaders = response.headers;

        // Update PIN if new one available
        if (responseHeaders.containsKey("X-Pin") && !PIN_INVALID.equals(responseHeaders.get("X-Pin"))) {
            SparkleHelper.setActivePin(context, responseHeaders.get("X-Pin"));
        }

        // Update PIN from cookie if available AND X-Pin not provided
        if (responseHeaders.containsKey("Set-Cookie") && !responseHeaders.containsKey("X-Pin")) {
            Matcher m = COOKIE_PIN.matcher(responseHeaders.get("Set-Cookie"));
            if (m.matches() && !PIN_INVALID.equals(m.group(1))) {
                SparkleHelper.setActivePin(context, m.group(1));
            }
        }

        // Update autologin if new one available
        if (responseHeaders.containsKey("X-Autologin")) {
            SparkleHelper.setActiveAutologin(context, responseHeaders.get("X-Autologin"));
        }

        // Sync number of requests seen by server with internal count
        if (responseHeaders.containsKey("X-Ratelimit-Requests-Seen")) {
            try {
                int serverCount = Integer.parseInt(responseHeaders.get("X-Ratelimit-Requests-Seen"));
                DashHelper dashie = DashHelper.getInstance(context);
                dashie.setNumCalls(serverCount);
            }
            catch (Exception e) {
                SparkleHelper.logError(e.toString());
            }
        }

        return super.parseNetworkResponse(response);
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
        }
        catch (Exception e) {
            SparkleHelper.logError(e.toString());
            return input;
        }
    }
}
