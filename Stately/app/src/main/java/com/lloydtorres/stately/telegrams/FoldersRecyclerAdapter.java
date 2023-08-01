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

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.TelegramFolder;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-03-11.
 * A recycler adapter for the folders dialog.
 */
public class FoldersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final TelegramsFragment fragmentInstance;
    private final boolean isMove;
    private int targetMoveId;
    private final FoldersDialog selfDialog;
    private final ArrayList<TelegramFolder> folders;
    private final int selected;

    public FoldersRecyclerAdapter(TelegramsFragment fr, FoldersDialog d,
                                  ArrayList<TelegramFolder> f, int s) {
        fragmentInstance = fr;
        isMove = false;
        selfDialog = d;
        folders = f;
        selected = s;
    }

    public FoldersRecyclerAdapter(TelegramsFragment fr, int targetId, FoldersDialog d,
                                  ArrayList<TelegramFolder> f, int s) {
        fragmentInstance = fr;
        isMove = true;
        targetMoveId = targetId;
        selfDialog = d;
        folders = f;
        selected = s;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new FolderEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FolderEntry folderEntry = (FolderEntry) holder;
        folderEntry.init(folders.get(position));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class FolderEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView folderName;
        private TelegramFolder folder;

        public FolderEntry(View v) {
            super(v);
            folderName = v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(TelegramFolder f) {
            folder = f;
            folderName.setText(folder.name);
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos == selected) {
                folderName.setTypeface(folderName.getTypeface(), Typeface.BOLD);
            } else {
                folderName.setTypeface(folderName.getTypeface(), Typeface.NORMAL);
            }
        }

        @Override
        public void onClick(View v) {
            selfDialog.dismiss();
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                if (!isMove) {
                    fragmentInstance.setSelectedFolder(pos);
                } else {
                    fragmentInstance.startMoveTelegram(targetMoveId, folders.get(pos).value);
                }
            }

        }
    }
}
