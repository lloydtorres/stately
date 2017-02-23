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

package com.lloydtorres.stately.region;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Poll;
import com.lloydtorres.stately.dto.PollOption;

/**
 * Created by Lloyd on 2016-10-02.
 * RecyclerAdapter for PollVoteDialog. Shows a list of poll options and links each one to a call
 * to the fragment that then submits the vote.
 */
public class PollVoteRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RegionCommunitySubFragment fragment;
    private PollVoteDialog dialog;
    private Poll pollData;

    private final PollOption withdrawOption;

    public PollVoteRecyclerAdapter(RegionCommunitySubFragment frag, PollVoteDialog diag, Poll p, String withdrawText) {
        fragment = frag;
        dialog = diag;
        pollData = p;

        withdrawOption = new PollOption();
        withdrawOption.text = withdrawText;
        withdrawOption.id = Poll.NO_VOTE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new PollOptionEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PollOptionEntry pollOptionEntry = (PollOptionEntry) holder;
        pollOptionEntry.init(position < pollData.options.size() ?
                pollData.options.get(position) : withdrawOption);
    }

    @Override
    public int getItemCount() {
        int itemCount = pollData.options.size();

        // Add withdraw option if user already voted
        if (pollData.votedOption != Poll.NO_VOTE) {
            itemCount += 1;
        }

        return itemCount;
    }

    public class PollOptionEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private PollOption pollOption;
        private TextView pollOptionContent;

        public PollOptionEntry(View v) {
            super(v);
            pollOptionContent = (TextView) v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(PollOption op) {
            pollOption = op;
            pollOptionContent.setText(pollOption.text);
            pollOptionContent.setTypeface(null, pollOption.id == pollData.votedOption ? Typeface.BOLD : Typeface.NORMAL);
        }

        @Override
        public void onClick(View v) {
            Poll newPollData = pollData;
            newPollData.votedOption = pollOption.id;
            dialog.dismiss();
            fragment.startSubmitPollVote(newPollData);
        }
    }
}
