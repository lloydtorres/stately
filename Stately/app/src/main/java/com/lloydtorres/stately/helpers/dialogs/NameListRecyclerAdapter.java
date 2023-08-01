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

package com.lloydtorres.stately.helpers.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.DelegateVote;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-19.
 * An adapter used for the endorsement dialog's recycler.
 */
public class NameListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final NameListDialog selfDialog;
    private List<String> names;
    private List<DelegateVote> delegateVotes;
    private final int target;

    public NameListRecyclerAdapter(Context c, NameListDialog d, List<String> n, int t) {
        context = c;
        selfDialog = d;
        names = n;
        target = t;
    }

    public NameListRecyclerAdapter(Context c, NameListDialog d, List<DelegateVote> dvs) {
        context = c;
        selfDialog = d;
        delegateVotes = dvs;
        target = ExploreActivity.EXPLORE_NATION;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new NameEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NameEntry nameEntry = (NameEntry) holder;
        if (delegateVotes != null) {
            DelegateVote dv = delegateVotes.get(position);
            String displayFormat = String.format(Locale.US,
                    context.getString(R.string.wa_delegate_votes_dialog_format),
                    SparkleHelper.getNameFromId(dv.delegate),
                    SparkleHelper.getPrettifiedNumber(dv.votes),
                    context.getResources().getQuantityString(R.plurals.vote, dv.votes));
            nameEntry.init(displayFormat);
        } else {
            nameEntry.init(names.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (delegateVotes != null) {
            return delegateVotes.size();
        } else {
            return names.size();
        }
    }

    public class NameEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView nationName;

        public NameEntry(View v) {
            super(v);
            nationName = v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(String n) {
            nationName.setText(n);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                selfDialog.dismiss();
                if (delegateVotes != null) {
                    SparkleHelper.startExploring(context, delegateVotes.get(pos).delegate, target);
                } else {
                    SparkleHelper.startExploring(context, names.get(pos), target);
                }
            }
        }
    }
}