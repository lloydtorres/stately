package com.lloydtorres.stately.region;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * An adapter for the recyclerview in MessageBoardActivity.
 */
public class MessageBoardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Post> messages;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p)
    {
        context = c;
        messages = p;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View postCard = inflater.inflate(R.layout.card_post, parent, false);
        RecyclerView.ViewHolder viewHolder = new PostCard(context, postCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostCard postCard = (PostCard) holder;
        postCard.init(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class PostCard extends RecyclerView.ViewHolder {

        private Context context;
        private TextView cardAuthor;
        private TextView cardTime;
        private TextView cardContent;

        public PostCard(Context c, View v) {
            super(v);
            context = c;
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardContent = (TextView) v.findViewById(R.id.card_post_content);
        }

        public void init(Post p)
        {
            SparkleHelper.activityLinkBuilder(context, cardAuthor, p.name, p.name, SparkleHelper.getNameFromId(p.name), SparkleHelper.CLICKY_NATION_MODE);
            cardTime.setText(SparkleHelper.getReadableDateFromUTC(p.timestamp));
            SparkleHelper.setBbCodeFormatting(context, cardContent, p.message);
        }
    }

}
