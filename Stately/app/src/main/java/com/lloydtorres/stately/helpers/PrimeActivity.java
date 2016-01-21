package com.lloydtorres.stately.helpers;

import android.support.v7.widget.Toolbar;

/**
 * Created by Lloyd on 2016-01-17.
 * A contract for activities that use toolbar-setting fragments, such as the NationFragment.
 */
public interface PrimeActivity {
    void setToolbar(Toolbar t);
}
