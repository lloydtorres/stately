package com.lloydtorres.stately.helpers;

import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;

/**
 * Created by Lloyd on 2016-01-29.
 */
public class GenericAdListener extends AdListener {

    public AdView ad;

    public GenericAdListener(AdView view)
    {
        ad = view;
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        ad.setVisibility(View.GONE);
    }
}
