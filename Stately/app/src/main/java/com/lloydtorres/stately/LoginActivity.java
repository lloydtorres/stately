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

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-13.
 */
public class LoginActivity extends AppCompatActivity {
    private final String APP_TAG = "com.lloydtorres.stately";
    private static final CharMatcher CHAR_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT
                                                                .or(CharMatcher.WHITESPACE)
                                                                .or(CharMatcher.anyOf("-"))
                                                                .precomputed();

    private EditText username;
    private EditText password;
    private Button login;

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
        String name = username.getText().toString();
        boolean verify = CHAR_MATCHER.matchesAllOf(name);
        if (verify && name.length() > 0)
        {
            name = name.toLowerCase().replace(" ","_");
            queryNS(view, name);
        }
        else
        {
            makeSnackbar(view, getString(R.string.login_error_404));
        }
    }

    private void queryNS(View view, String nationName)
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
                        }
                        catch (Exception e) {
                            Log.e(APP_TAG, e.toString());
                            makeSnackbar(fView, getString(R.string.login_error_parsing));
                        }
                        Intent nationActivityLaunch = new Intent(LoginActivity.this, NationActivity.class);
                        nationActivityLaunch.putExtra("mNationData", nationResponse);
                        startActivity(nationActivityLaunch);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(APP_TAG, error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    makeSnackbar(fView, getString(R.string.login_error_404));
                }
                else
                {
                    makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    public void makeSnackbar(View view, String str)
    {
        Snackbar.make(view, str, Snackbar.LENGTH_LONG).show();
    }
}
