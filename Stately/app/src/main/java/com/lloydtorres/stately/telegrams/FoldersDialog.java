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

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.TelegramFolder;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-03-11.
 * A dialog showing the available telegram folders.
 */
public class FoldersDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_folder_dialog";
    public static final String FOLDERS_KEY = "folders";
    public static final String SELECTED_KEY = "selected";

    public static final int NO_SELECTION = -1;

    // RecyclerView variables
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<TelegramFolder> folders;
    private int selected;
    private TelegramsFragment telegramsFragment;
    private TelegramReadActivity telegramReadActivity;

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

    public void setActivity(TelegramReadActivity tra) {
        telegramReadActivity = tra;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            dialog = new AppCompatDialog(getActivity(), R.style.AlertDialogCustom);
        }
        else
        {
            dialog = new AppCompatDialog(getActivity(), R.style.MaterialDialog);
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_recycler, container, false);

        // Restore saved state
        if (savedInstanceState != null)
        {
            folders = savedInstanceState.getParcelableArrayList(FOLDERS_KEY);
            selected = savedInstanceState.getInt(SELECTED_KEY);
        }

        if (telegramsFragment != null) {
            getDialog().setTitle(getString(R.string.telegrams_folders));
        }
        else {
            getDialog().setTitle(getString(R.string.telegrams_move));
        }

        initRecycler(view);

        return view;
    }

    private void initRecycler(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (telegramsFragment != null) {
            mRecyclerAdapter = new FoldersRecyclerAdapter(telegramsFragment, this, folders, selected);
        }
        else {
            mRecyclerAdapter = new FoldersRecyclerAdapter(telegramReadActivity, this, folders, selected);
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
