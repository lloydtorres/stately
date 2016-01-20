package com.lloydtorres.stately.nation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-19.
 */
public class EndorsementRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> nations;

    public EndorsementRecyclerAdapter(Context c, List<String> n)
    {
        context = c;
        nations = n;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new EndorsementEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EndorsementEntry happeningCard = (EndorsementEntry) holder;
        happeningCard.init(nations.get(position));
    }

    @Override
    public int getItemCount() {
        return nations.size();
    }

    public class EndorsementEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nationName;

        public EndorsementEntry(View v) {
            super(v);
            nationName = (TextView) v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(String n)
        {
            nationName.setText(n);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION)
            {
                SparkleHelper.startExploring(context, nations.get(pos));
            }
        }
    }
}