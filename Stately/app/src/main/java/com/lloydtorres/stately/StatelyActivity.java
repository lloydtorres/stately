package com.lloydtorres.stately;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.common.base.CharMatcher;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.GenericFragment;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.nation.ExploreNationActivity;
import com.lloydtorres.stately.nation.NationFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.simpleframework.xml.core.Persister;

public class StatelyActivity extends PrimeActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String APP_TAG = "com.lloydtorres.stately";
    private static final CharMatcher CHAR_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT
                                                            .or(CharMatcher.WHITESPACE)
                                                            .or(CharMatcher.anyOf("-"))
                                                            .precomputed();
    private final String BANNER_TEMPLATE = "https://www.nationstates.net/images/banners/%s.jpg";
    private final int[] noSelect = {    R.id.nav_explore,
                                        R.id.nav_settings,
                                        R.id.nav_logout
                                    };

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private int currentPosition = R.id.nav_nation;

    private Nation mNation;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;
    private TextView nationNameView;

    private EditText exploreSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stately);

        if (getIntent() != null)
        {
            mNation = getIntent().getParcelableExtra("mNationData");
        }
        if (mNation == null && savedInstanceState != null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_app_bar);
        setToolbar(toolbar);
        getSupportActionBar().hide();
        initNavigationView();
    }

    public void setToolbar(Toolbar t)
    {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, t, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView()
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        initNavBanner();
        NationFragment nf = getNationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.coordinator_app_bar, nf)
                .commit();
    }

    private void initNavBanner()
    {
        View view = navigationView.getHeaderView(0);
        nationBanner = (ImageView) view.findViewById(R.id.nav_banner_back);
        nationFlag = (RoundedImageView) view.findViewById(R.id.nav_flag);
        nationNameView = (TextView) view.findViewById(R.id.nav_nation_name);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationNameView.setText(mNation.name);
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, mNation.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(mNation.flagURL, nationFlag, imageOptions);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable("mNationData", mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        if (id != currentPosition && !isNoSelect(id))
        {
            currentPosition = id;
            android.support.v4.app.Fragment fChoose;

            if (id == R.id.nav_nation) {
                fChoose = getNationFragment();
            }
            else
            {
                fChoose = new GenericFragment();
            }

            fm.beginTransaction()
                    .replace(R.id.coordinator_app_bar, fChoose)
                    .commit();

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_explore)
        {
            explore();
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_logout)
        {
            logout();
            return true;
        }
        else
        {
            return false;
        }
    }

    private NationFragment getNationFragment()
    {
        NationFragment nationFragment = new NationFragment();
        nationFragment.setNation(mNation);

        return nationFragment;
    }

    private boolean isNoSelect(int key)
    {
        for (int i=0; i<noSelect.length; i++)
        {
            if (noSelect[i] == key)
            {
                return true;
            }
        }
        return false;
    }

    private void explore()
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.view_explore_dialog, null);

        exploreSearch = (EditText) dialogView.findViewById(R.id.explore_searchbar);
        final RadioGroup exploreToggleState = (RadioGroup) dialogView.findViewById(R.id.explore_radio_group);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (exploreToggleState.getCheckedRadioButtonId())
                {
                    case R.id.explore_radio_nation:
                        verifyNationInput(findViewById(R.id.drawer_layout));
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.menu_explore)
                .setView(dialogView)
                .setPositiveButton(R.string.explore_positive, dialogListener)
                .setNegativeButton(R.string.explore_negative, null)
                .show();
    }

    private void logout()
    {
        Intent nationActivityLaunch = new Intent(StatelyActivity.this, LoginActivity.class);
        startActivity(nationActivityLaunch);
        finish();
    }

    public void verifyNationInput(View view)
    {
        String name = exploreSearch.getText().toString();
        boolean verify = CHAR_MATCHER.matchesAllOf(name);
        if (verify && name.length() > 0)
        {
            name = name.toLowerCase().replace(" ","_");
            queryNation(view, name);
        }
        else
        {
            makeSnackbar(view, getString(R.string.explore_error_404_nation));
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
                            Log.e(APP_TAG, e.toString());
                            makeSnackbar(fView, getString(R.string.login_error_parsing));
                        }
                        Intent nationActivityLaunch = new Intent(StatelyActivity.this, ExploreNationActivity.class);
                        nationActivityLaunch.putExtra("mNationData", nationResponse);
                        startActivity(nationActivityLaunch);
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
                    makeSnackbar(fView, getString(R.string.explore_error_404_nation));
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
