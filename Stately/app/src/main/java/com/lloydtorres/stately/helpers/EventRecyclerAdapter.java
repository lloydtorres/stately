package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.HappeningCard;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 * An adapter used to show happenings in its RecyclerView.
 */
public class EventRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int EMPTY_INDICATOR = -1;

    private Context context;
    private List<Event> events;

    public EventRecyclerAdapter(Context c, List<Event> ev)
    {
        context = c;
        events = ev;

        if (events.size() <= 0)
        {
            Event ne = new Event();
            ne.timestamp = EMPTY_INDICATOR;
            events.add(ne);
        }
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
}
