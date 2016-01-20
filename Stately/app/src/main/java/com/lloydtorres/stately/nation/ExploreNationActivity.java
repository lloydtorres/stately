package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

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
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-15.
 */
public class ExploreNationActivity extends AppCompatActivity implements PrimeActivity {
    private String nationId;
    private NationFragment nFragment;
    private TextView statusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        if (getIntent() != null)
        {
            nationId = getIntent().getStringExtra("nationId");
            if (nationId == null)
            {
                nationId = getIntent().getData().getHost();
            }
        }
        else
        {
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.explore_toolbar);
        setToolbar(toolbar);
        getSupportActionBar().hide();

        statusMessage = (TextView) findViewById(R.id.explore_status);

        verifyNationInput(nationId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_explore, menu);
        return true;
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setExploreStatus(String s)
    {
        statusMessage.setText(s);
    }

    private void verifyNationInput(String name)
    {
        if (SparkleHelper.isValidNationName(name) && name.length() > 0)
        {
            name = name.toLowerCase().replace(" ","_");
            queryNation(name);
        }
        else
        {
            setExploreStatus(getString(R.string.explore_error_404_nation));
        }
    }

    private void queryNation(String name)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(Nation.QUERY, name);

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

                            initFragment(nationResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(toString());
                            setExploreStatus(getString(R.string.login_error_parsing));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    setExploreStatus(getString(R.string.login_error_no_internet));
                }
                else if (error instanceof ServerError)
                {
                    setExploreStatus(getString(R.string.explore_error_404_nation));
                }
                else
                {
                    setExploreStatus(getString(R.string.login_error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    private void initFragment(Nation mNation)
    {
        nFragment = new NationFragment();
        nFragment.setNation(mNation);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.explore_coordinator, nFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.nav_explore:
                FragmentManager fm = getSupportFragmentManager();
                ExploreNationDialog editNameDialog = new ExploreNationDialog();
                editNameDialog.show(fm, ExploreNationDialog.DIALOG_TAG);
        }
        return super.onOptionsItemSelected(item);
    }
}
