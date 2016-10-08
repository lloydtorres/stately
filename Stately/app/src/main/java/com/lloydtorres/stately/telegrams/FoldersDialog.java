/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.telegrams;

import android.os.Bundle;
import android.view.View;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerDialogFragment;
import com.lloydtorres.stately.dto.TelegramFolder;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-03-11.
 * A dialog showing the available telegram folders.
 */
public class FoldersDialog extends RecyclerDialogFragment {
    public static final String DIALOG_TAG = "fragment_folder_dialog";
    public static final String FOLDERS_KEY = "folders";
    public static final String SELECTED_KEY = "selected";

    public static final int NO_SELECTION = -1;

    private ArrayList<TelegramFolder> folders;
    private int selected;
    private TelegramsFragment telegramsFragment;
    private boolean isMove;
    private int moveTelegramId;

    public void setFolders(ArrayList<TelegramFolder> f)
    {
        folders = f;
    }

    public void setSelected(int s)
    {
        selected = s;
    }

    public void setFragment(TelegramsFragment tf) {
        telegramsFragment = tf;
    }

    public void setMoveMode(boolean mode) {
        isMove = mode;
    }

    public void setMoveTelegramId(int id) {
        moveTelegramId = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore saved state
        if (savedInstanceState != null) {
            folders = savedInstanceState.getParcelableArrayList(FOLDERS_KEY);
            selected = savedInstanceState.getInt(SELECTED_KEY);
        }
    }

    @Override
    protected void initRecycler(View view) {
        super.initRecycler(view);
        setDialogTitle(getString(!isMove ? R.string.telegrams_folders : R.string.telegrams_move));

        if (!isMove) {
            mRecyclerAdapter = new FoldersRecyclerAdapter(telegramsFragment, this, folders, selected);
        }
        else {
            mRecyclerAdapter = new FoldersRecyclerAdapter(telegramsFragment, moveTelegramId, this, folders, selected);
        }
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(FOLDERS_KEY, folders);
        outState.putInt(SELECTED_KEY, selected);
    }
}
