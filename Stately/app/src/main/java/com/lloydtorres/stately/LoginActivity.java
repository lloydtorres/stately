package com.lloydtorres.stately;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.base.CharMatcher;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-13.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button login;
    private boolean isLoggingIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        username = (EditText) findViewById(R.id.field_username);
        password = (EditText) findViewById(R.id.field_password);
        login = (Button) findViewById(R.id.login_button);
    }

    public void verifyLogin(View view)
    {
        if (!getLoginState())
        {
            setLoginState(true);
            String name = username.getText().toString();
            if (SparkleHelper.isValidNationName(name) && name.length() > 0)
            {
                name = name.toLowerCase().replace(" ","_");
                queryNation(view, name);
            }
            else
            {
                setLoginState(false);
                SparkleHelper.makeSnackbar(view, getString(R.string.login_error_404));
            }
        }
    }

    private void queryNation(View view, String nationName)
    {
        final View fView = view;

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(Nation.QUERY, nationName);

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
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                            setLoginState(false);
                        }
                        Intent nationActivityLaunch = new Intent(LoginActivity.this, StatelyActivity.class);
                        nationActivityLaunch.putExtra("mNationData", nationResponse);
                        startActivity(nationActivityLaunch);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                setLoginState(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_404));
                }
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    private boolean getLoginState()
    {
        return isLoggingIn;
    }

    private void setLoginState(boolean stat)
    {
        if (stat)
        {
            login.setText(getString(R.string.log_in_load));
        }
        else
        {
            login.setText(getString(R.string.log_in));
        }
        isLoggingIn = stat;
    }
}
