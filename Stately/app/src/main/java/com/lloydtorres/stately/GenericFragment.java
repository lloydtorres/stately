package com.lloydtorres.stately;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lloyd on 2016-01-14.
 */
public class GenericFragment extends Fragment {

    private Activity mActivity;
    private Toolbar toolbar;

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
        View view = inflater.inflate(R.layout.fragment_generic, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_generic);

        if (mActivity instanceof StatelyActivity)
        {
            ((StatelyActivity) mActivity).setToolbar(toolbar);
        }

        return view;
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mActivity = null;
    }

}
