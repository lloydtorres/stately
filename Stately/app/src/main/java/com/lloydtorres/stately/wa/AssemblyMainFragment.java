package com.lloydtorres.stately.wa;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.helpers.PrimeActivity;

import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-16.
 */
public class AssemblyMainFragment extends Fragment {
    private final String APP_TAG = "com.lloydtorres.stately";
    private final int GENERAL_ASSEMBLY = 1;
    private final int SECURITY_COUNCIL = 2;

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private Assembly genAssembly;
    private Assembly secCouncil;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_generic, container, false);
        toolbar = (Toolbar) mView.findViewById(R.id.refreshview_toolbar);

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        queryWorldAssembly(mView);
        return mView;
    }

    private void queryWorldAssembly(View view)
    {
        queryWorldAssemblyHeavy(mView, GENERAL_ASSEMBLY);
    }

    private void queryWorldAssemblyHeavy(View view, int chamberId)
    {
        final View fView = view;
        final int chamberMode = chamberId;

        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String targetURL = String.format(Assembly.QUERY, chamberId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    Assembly waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            waResponse = serializer.read(Assembly.class, response);
                        }
                        catch (Exception e) {
                            Log.e(APP_TAG, e.toString());
                            makeSnackbar(fView, getString(R.string.login_error_parsing));
                        }

                        if (chamberMode == GENERAL_ASSEMBLY)
                        {
                            setGeneralAssembly(waResponse);
                            queryWorldAssemblyHeavy(mView, SECURITY_COUNCIL);
                        }
                        else if (chamberMode == SECURITY_COUNCIL)
                        {
                            setSecurityCouncil(secCouncil);
                            refreshRecycler();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(APP_TAG, error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    private void makeSnackbar(View view, String str)
    {
        Snackbar.make(view, str, Snackbar.LENGTH_LONG).show();
    }

    private void setGeneralAssembly(Assembly g)
    {
        genAssembly = g;
    }

    private void setSecurityCouncil(Assembly s)
    {
        secCouncil = s;
    }

    private void refreshRecycler()
    {
        mRecyclerAdapter = new AssemblyRecyclerAdapter(getContext(), genAssembly, secCouncil);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }
}
