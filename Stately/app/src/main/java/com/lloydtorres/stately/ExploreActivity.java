package com.lloydtorres.stately;

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
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.nation.NationFragment;
import com.lloydtorres.stately.region.RegionFragment;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-15.
 * This activity can be invoked to load and open a nation/region page, either as an Intent
 * or through this Uri: com.lloydtorres.stately.explore://<name>/<mode>
 * Requires a name to be passed in; does error checking as well.
 */
public class ExploreActivity extends AppCompatActivity implements PrimeActivity {
    private String id;
    private int mode;
    private TextView statusMessage;

    private NationFragment nFragment;
    private RegionFragment rFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        if (getIntent() != null)
        {
            // If name passed in as intent
            id = getIntent().getStringExtra("id");
            mode = getIntent().getIntExtra("mode", SparkleHelper.CLICKY_NATION_MODE);
            if (id == null)
            {
                // If ID passed in through Uri
                // Funny thing here is that in the link source, they have
                // to convert it from a proper name to an ID
                // But we need it as a name so we convert it back
                id = getIntent().getData().getHost();
                id = SparkleHelper.getNameFromId(id);
                mode = Integer.valueOf(getIntent().getData().getLastPathSegment());
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

        verifyInput(id);
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
        // We need a back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setExploreStatus(String s)
    {
        statusMessage.setText(s);
    }

    private void verifyInput(String name)
    {
        if (SparkleHelper.isValidName(name) && name.length() > 0)
        {
            name = name.toLowerCase().replace(" ","_");
            switch (mode)
            {
                case SparkleHelper.CLICKY_NATION_MODE:
                    queryNation(name);
                    break;
                default:
                    queryRegion(name);
                    break;
            }
        }
        else
        {
            switch (mode)
            {
                case SparkleHelper.CLICKY_NATION_MODE:
                    setExploreStatus(getString(R.string.explore_error_404_nation));
                    break;
                default:
                    setExploreStatus(getString(R.string.region_404));
                    break;
            }
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

    private void queryRegion(String name)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(Region.QUERY, name);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Region regionResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            regionResponse = serializer.read(Region.class, response);

                            // Switch flag URL to https
                            if (regionResponse.flagURL != null)
                            {
                                regionResponse.flagURL = regionResponse.flagURL.replace("http://","https://");
                            }

                            initFragment(regionResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
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
                    setExploreStatus(getString(R.string.region_404));
                }
                else
                {
                    setExploreStatus(getString(R.string.error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    private void initFragment(Nation mNation)
    {
        // Initializes and inflates the nation fragment
        nFragment = new NationFragment();
        nFragment.setNation(mNation);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.explore_coordinator, nFragment)
                .commit();
    }

    private void initFragment(Region mRegion)
    {
        // Initializes and inflates the region fragment
        rFragment = new RegionFragment();
        rFragment.setRegion(mRegion);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.explore_coordinator, rFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_explore:
                // Open an explore dialog to keep going
                FragmentManager fm = getSupportFragmentManager();
                ExploreDialog editNameDialog = new ExploreDialog();
                editNameDialog.show(fm, ExploreDialog.DIALOG_TAG);
        }
        return super.onOptionsItemSelected(item);
    }
}
