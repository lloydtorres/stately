package com.lloydtorres.stately.region;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * An adapter for the recyclerview in MessageBoardActivity.
 */
public class MessageBoardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_INDICATOR = -1;

    private Context context;
    private List<Post> messages;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p)
    {
        context = c;
        messages = p;

        if (messages.size() <= 0)
        {
            Post np = new Post();
            np.id = EMPTY_INDICATOR;
            messages.add(np);
        }
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
        private HtmlTextView cardContent;

        public PostCard(Context c, View v) {
            super(v);
            context = c;
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
        }

        public void init(Post p)
        {
            if (p.id != EMPTY_INDICATOR)
            {
                SparkleHelper.activityLinkBuilder(context, cardAuthor, p.name, p.name, SparkleHelper.getNameFromId(p.name), SparkleHelper.CLICKY_NATION_MODE);
                cardTime.setText(SparkleHelper.getReadableDateFromUTC(p.timestamp));
                SparkleHelper.setBbCodeFormatting(context, cardContent, p.message);
            }
            else
            {
                cardTime.setVisibility(View.GONE);
                cardAuthor.setVisibility(View.GONE);
                cardContent.setText(context.getString(R.string.rmb_no_content));
                cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            }
        }
    }

}
