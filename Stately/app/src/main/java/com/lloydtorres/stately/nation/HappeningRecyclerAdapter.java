package com.lloydtorres.stately.nation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.HappeningCard;
import com.lloydtorres.stately.dto.HappeningEvent;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 */
public class HappeningRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HappeningEvent> events;

    public HappeningRecyclerAdapter(List<HappeningEvent> ev)
    {
        events = ev;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View happeningCard = inflater.inflate(R.layout.card_happening, parent, false);
        RecyclerView.ViewHolder viewHolder = new HappeningCard(happeningCard);
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
