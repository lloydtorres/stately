package com.lloydtorres.stately.region;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
    public static final int NO_SELECTION = -1;
    private static final int EMPTY_INDICATOR = -1;
    private static final String DELETED_CONTENT = "Message deleted by author";

    private Context context;
    private List<Post> messages;
    private int replyIndex = NO_SELECTION;
    private boolean enableClick = false;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p, boolean ec)
    {
        context = c;
        enableClick = ec;
        setMessages(p);
    }

    public void setMessages(List<Post> p)
    {
        messages = p;

        if (messages.size() <= 0)
        {
            Post np = new Post();
            np.id = EMPTY_INDICATOR;
            messages.add(np);
        }
        notifyDataSetChanged();
    }

    public void setReplyIndex(int i)
    {
        int oldReplyIndex = replyIndex;
        replyIndex = i;

        if (oldReplyIndex != -1)
        {
            notifyItemChanged(oldReplyIndex);
        }
        if (replyIndex != -1)
        {
            notifyItemChanged(replyIndex);
        }

        if (replyIndex == oldReplyIndex)
        {
            replyIndex = NO_SELECTION;
            notifyItemChanged(oldReplyIndex);
        }
    }

    public void addToReplyIndex(int a)
    {
        if (replyIndex != -1)
        {
            setReplyIndex(replyIndex + a);
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
        Post message = messages.get(position);
        postCard.init(message);

        if (position == replyIndex)
        {
            postCard.select();
        }
        else
        {
            postCard.deselect();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class PostCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private Post post;
        private CardView cardContainer;
        private TextView cardAuthor;
        private TextView cardTime;
        private HtmlTextView cardContent;

        public PostCard(Context c, View v) {
            super(v);
            context = c;
            cardContainer = (CardView) v.findViewById(R.id.card_post_container);
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);

            if (enableClick)
            {
                v.setOnClickListener(this);
            }
        }

        public void init(Post p)
        {
            post = p;
            if (post.id != EMPTY_INDICATOR)
            {
                SparkleHelper.activityLinkBuilder(context, cardAuthor, post.name, post.name, SparkleHelper.getNameFromId(post.name), SparkleHelper.CLICKY_NATION_MODE);
                cardTime.setText(SparkleHelper.getReadableDateFromUTC(post.timestamp));
                SparkleHelper.setBbCodeFormatting(context, cardContent, post.message);
            }
            else
            {
                cardTime.setVisibility(View.GONE);
                cardAuthor.setVisibility(View.GONE);
                cardContent.setText(context.getString(R.string.rmb_no_content));
                cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            }
        }

        public void select()
        {
            cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.highlightColor));
        }

        public void deselect()
        {
            cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && post.message != null && !post.message.equals(DELETED_CONTENT))
            {
                if (replyIndex == pos)
                {
                    ((MessageBoardActivity) context).setReplyMessage(null);
                }
                else
                {
                    ((MessageBoardActivity) context).setReplyMessage(post);
                    setReplyIndex(pos);
                }
            }
        }
    }

}
