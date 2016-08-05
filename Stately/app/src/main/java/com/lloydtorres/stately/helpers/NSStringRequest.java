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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-08-05.
 * A custom implementation of Volley's StringRequest for interacting with the NationStates servers.
 */
public class NSStringRequest extends StringRequest {

    public static final String PIN_INVALID = "-1";

    private Context context;
    private int method;
    private Map<String, String> params = new HashMap<String, String>();
    private boolean noPin = false;
    private String autologinOverride;

    public NSStringRequest(Context c, int m, String target,
                           Response.Listener<String> listener,
                           Response.ErrorListener errorListener) {
        super(m, target, listener, errorListener);
        context = c;
        method = m;
    }

    public void disablePin(boolean b) {
        noPin = b;
    }

    public void setAutologinOverride(String a) { autologinOverride = a; }

    public void setParams(Map<String, String> p) { params = p; }

    @Override
    protected Map<String,String> getParams(){
        return params;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String,String> params = new HashMap<String, String>();
        UserLogin u = SparkleHelper.getActiveUser(context);

        // UserLogin will not be null when user is logged in
        if (u != null && u.nationId != null) {
            params.put("User-Agent", String.format(Locale.US, context.getString(R.string.app_header), u.nationId));

            String autoHeader = autologinOverride == null ? u.autologin : autologinOverride;
            // Case 1: If only autologin cookie is available/pin cookie is invalid
            if ((noPin || u.pin == null || PIN_INVALID.equals(u.pin)) && autoHeader != null) {
                params.put("Cookie", String.format(Locale.US, "autologin=%s", autoHeader));
            }
            // Case 2: If both autologin and pin cookies are available and pin cookie is good
            else if (u.autologin != null && u.pin != null && !PIN_INVALID.equals(u.pin)) {
                params.put("Cookie", String.format(Locale.US, "autologin=%s; pin=%s", autoHeader, u.pin));
            }
            // Case 3: Both are missing, don't do anything
        }
        else {
            params.put("User-Agent", context.getString(R.string.app_header_nouser));
        }

        // Only include x-www-form-urlencoded for POSTs
        if (method == Request.Method.POST) {
            params.put("Content-Type", "application/x-www-form-urlencoded");
        }
        return params;
    }
}
