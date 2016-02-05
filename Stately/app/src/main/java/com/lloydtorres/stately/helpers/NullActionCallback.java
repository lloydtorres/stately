package com.lloydtorres.stately.helpers;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Lloyd on 2016-02-04.
 * This callback helper disables the context menu that pops up on long pressing an EditText.
 */
public class NullActionCallback implements ActionMode.Callback {
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }
}
