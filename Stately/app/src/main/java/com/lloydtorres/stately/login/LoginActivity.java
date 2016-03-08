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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.StatelyActivity;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-13.
 * The launcher activity for Stately!
 * Takes in user logins and verifies them against NationStates.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String USERNAME_KEY = "username";
    public static final String AUTOLOGIN_KEY = "autologin";
    public static final String NOAUTOLOGIN_KEY = "disableAutoLogin";

    // Cookie shenanigans
    private static final String LOGIN_TARGET = "https://www.nationstates.net/";
    private static final URI LOGIN_URI = URI.create(LOGIN_TARGET);
    private static final String LOGIN_DOMAIN = "nationstates.net";
    private static final long LOGIN_EXPIRY = 12960000; // about 5 months in seconds
    private CookieManager cookies;
    private SharedPreferences storage;

    private EditText username;
    private EditText password;
    private Button login;
    private boolean isLoggingIn;
    private String autologin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        storage = PreferenceManager.getDefaultSharedPreferences(this);

        username = (EditText) findViewById(R.id.field_username);
        username.setCustomSelectionActionModeCallback(new NullActionCallback());
        password = (EditText) findViewById(R.id.field_password);
        password.setCustomSelectionActionModeCallback(new NullActionCallback());
        login = (Button) findViewById(R.id.login_button);

        // Set cookie handler
        cookies = new CookieManager();
        cookies.getCookieStore().removeAll();
        cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookies);

        // If activity was launched by an intent, handle that first
        if (getIntent() != null)
        {
            String username = getIntent().getStringExtra(USERNAME_KEY);
            String autologin = getIntent().getStringExtra(AUTOLOGIN_KEY);
            boolean noAutoLogin = getIntent().getBooleanExtra(NOAUTOLOGIN_KEY, false);

            if (username != null && autologin != null)
            {
                verifyAutologin(username, autologin);
            }
            // Prevent autologin
            if (noAutoLogin)
            {
                return;
            }
        }

        // If settings allows it and user login exists, try logging in first
        if (storage.getBoolean("setting_autologin", true))
        {
            UserLogin u = SparkleHelper.getActiveUser(this);
            if (u != null)
            {
                verifyAutologin(u.name, u.autologin);
            }
        }
        else
        {
            SparkleHelper.removeActiveUser(this);
        }
    }

    /**
     * Verify that the stored autologin cookie is correct.
     * Proceeds to load nation data if correct, resets otherwise.
     * @param name Name of the nation
     */
    private void verifyAutologin(final String name, final String autologin)
    {
        setLoginState(true);

        HttpCookie cookie = new HttpCookie("autologin", autologin);
        cookie.setPath("/");
        cookie.setDomain(LOGIN_DOMAIN);
        cookie.setMaxAge(LOGIN_EXPIRY);
        cookies.getCookieStore().add(LOGIN_URI, cookie);

        final View view = findViewById(R.id.activity_login_main);
        String targetURL = LOGIN_TARGET;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (handleCookieResponse())
                        {
                            queryNation(view, name);
                        }
                        else
                        {
                            // Reset if not successful
                            setLoginState(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_autologin));
                            SparkleHelper.removeActiveUser(getApplicationContext());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                setLoginState(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("User-Agent", getString(R.string.app_header_nouser));
                params.put("Cookie", String.format("autologin=%s", autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
            setLoginState(false);
        }
    }

    /**
     * Callback for login button.
     * Verifies if the input is valid. If yes, verify password next.
     * @param view
     */
    public void verifyUsername(View view)
    {
        if (!getLoginState())
        {
            setLoginState(true);
            String name = username.getText().toString();
            if (SparkleHelper.isValidName(name) && name.length() > 0)
            {
                verifyPassword(view, name);
            }
            else
            {
                setLoginState(false);
                SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
            }
        }
    }

    /**
     * This verifies the password entered by the user. If so, download the actual nation data.
     * @param view View
     * @param name Nation name
     */
    private void verifyPassword(final View view, final String name)
    {
        final String pass = password.getText().toString();
        cookies.getCookieStore().removeAll();

        String targetURL = LOGIN_TARGET;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (handleCookieResponse())
                        {
                            queryNation(view, name);
                        }
                        else
                        {
                            setLoginState(false);
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                setLoginState(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("logging_in", "1");
                params.put("nation", name);
                params.put("password", pass);
                params.put("submit", "Login");
                params.put("autologin", "yes");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("User-Agent", getString(R.string.app_header_nouser));
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * This verifies if the login was successful, by checking if the autologin cookie was created.
     * Also performs operations to note the username and password.
     * @return If login was successful or not
     */
    private boolean handleCookieResponse()
    {
        boolean autologinFlag = false;
        boolean pinFlag = false;

        List<HttpCookie> cookieResponse = cookies.getCookieStore().getCookies();
        for (HttpCookie c : cookieResponse)
        {
            if (c.getName().equals("autologin"))
            {
                autologin = c.getValue();
                autologinFlag =  true;
            }
            if (c.getName().equals("pin"))
            {
                pinFlag =  true;
            }
        }
        return autologinFlag && pinFlag;
    }

    /**
     * Downloads nation data for specified nation.
     * If successful, start the main StatelyActivity.
     * @param view
     * @param nationName
     */
    private void queryNation(final View view, final String nationName)
    {
        String targetURL = String.format(Nation.QUERY, SparkleHelper.getIdFromName(nationName));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Nation nationResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            nationResponse = serializer.read(Nation.class, response);

                            // Switch flag URL to https
                            nationResponse.flagURL = nationResponse.flagURL.replace("http://","https://");

                            // Map out government priorities
                            switch (nationResponse.govtPriority)
                            {
                                case "Defence":
                                    nationResponse.govtPriority = getString(R.string.defense);
                                    break;
                                case "Commerce":
                                    nationResponse.govtPriority = getString(R.string.industry);
                                    break;
                                case "Social Equality":
                                    nationResponse.govtPriority = getString(R.string.social_policy);
                                    break;
                            }

                            SparkleHelper.setActiveUser(getApplicationContext(), nationName, autologin);
                            SparkleHelper.setSessionData(getApplicationContext(), SparkleHelper.getIdFromName(nationResponse.region), nationResponse.waState);

                            Intent nationActivityLaunch = new Intent(LoginActivity.this, StatelyActivity.class);
                            nationActivityLaunch.putExtra(StatelyActivity.NATION_DATA, nationResponse);
                            nationActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(nationActivityLaunch);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(view, getString(R.string.login_error_parsing));
                            setLoginState(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                setLoginState(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                params.put("User-Agent", getString(R.string.app_header_nouser));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Get the login state (i.e. if the login process is currently being done).
     * @return The login state (true or not).
     */
    private boolean getLoginState()
    {
        return isLoggingIn;
    }

    /**
     * Set the login state.
     * @param stat The current login state. True if logging in, false otherwise.
     */
    private void setLoginState(boolean stat)
    {
        if (stat)
        {
            username.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            login.setText(getString(R.string.log_in_load));
        }
        else
        {
            username.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            login.setText(getString(R.string.log_in));
        }
        isLoggingIn = stat;
    }
}
