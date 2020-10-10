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

package com.lloydtorres.stately.feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Event;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 * An adapter used to show happenings in its RecyclerView.
 */
public class EventRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int EMPTY_INDICATOR = -1;

    private Context context;
    private List<Event> events;

    public EventRecyclerAdapter(Context c, List<Event> ev) {
        context = c;
        setEvents(ev);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View happeningCard = inflater.inflate(R.layout.card_happening, parent, false);
        RecyclerView.ViewHolder viewHolder = new HappeningCard(context, happeningCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HappeningCard happeningCard = (HappeningCard) holder;
        happeningCard.init(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> evs) {
        events = evs;
        if (events.size() <= 0) {
            Event ne = new Event();
            ne.timestamp = EMPTY_INDICATOR;
            events.add(ne);
        }
        notifyDataSetChanged();
    }
}
