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

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.report.ReportActivity;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * An adapter for the recyclerview in MessageBoardActivity.
 */
public class MessageBoardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int NO_SELECTION = -1;
    private static final String DELETED_CONTENT = "Message deleted by author";
    private static final int EMPTY_INDICATOR = -1;

    private Context context;
    private FragmentManager fm;
    private List<Post> messages;
    private int replyIndex = NO_SELECTION;
    private boolean isPostable = false;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p, boolean ec, FragmentManager f)
    {
        context = c;
        fm = f;
        isPostable = ec;
        setMessages(p);
    }

    /**
     * Set new messages
     * @param p List of posts
     */
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

    /**
     * Set which message to reply to
     * @param i Index
     */
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

    /**
     * Add an offset to the reply index
     * @param a Offset
     */
    public void addToReplyIndex(int a)
    {
        if (replyIndex != -1)
        {
            setReplyIndex(replyIndex + a);
        }
    }

    /**
     * Mark a message as having been deleted
     * @param i
     */
    public void setAsDeleted(int i)
    {
        messages.get(i).message = DELETED_CONTENT;
        messages.get(i).status = Post.POST_DELETED;
        notifyItemChanged(i);
    }

    /**
     * Mark a message as either liked or unliked
     * @param pos Position of message in adapter
     * @param like True to like, false to unlike
     */
    public void setLikeStatus(int pos, boolean like)
    {
        if (context == null)
        {
            return;
        }

        Post targetPost = messages.get(pos);
        String userId = SparkleHelper.getActiveUser(context).nationId;
        if (like)
        {
            // Either set the user as the only liker, or append their id to the string
            if (targetPost.likedBy == null || targetPost.likedBy.length() <= 0)
            {
                targetPost.likedBy = userId;
            }
            else
            {
                targetPost.likedBy = targetPost.likedBy + ":" + userId;
            }
            targetPost.likes++;
        }
        else
        {
            // If string contains user ID, remove it
            if (targetPost.likedBy != null && targetPost.likedBy.contains(userId))
            {
                String[] likes = targetPost.likedBy.split(":");
                ArrayList<String> properLikes = new ArrayList<String>();
                for (String li : likes)
                {
                    if (!li.equals(userId))
                    {
                        properLikes.add(li);
                    }
                }
                targetPost.likedBy = SparkleHelper.joinStringList(properLikes, ":");
                targetPost.likes--;
            }
        }
        messages.set(pos, targetPost);
        notifyItemChanged(pos);
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

        if (message.likes > 0 && message.likedBy != null && message.likedBy.length() > 0)
        {
            // If current user is in the like list, highlight the like buttons
            if (context != null && message.likedBy.contains(SparkleHelper.getActiveUser(context).nationId))
            {
                postCard.like();
            }
            else
            {
                postCard.unlike();
            }
        }
        else
        {
            postCard.unlike();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class PostCard extends RecyclerView.ViewHolder {

        private Context context;
        private Post post;
        private CardView cardContainer;
        private TextView cardAuthor;
        private TextView cardTime;
        private HtmlTextView cardContent;
        private RelativeLayout actionsHolder;
        private ImageView likeButton;
        private TextView likeCount;
        private ImageView deleteButton;
        private ImageView reportButton;
        private ImageView replyButton;

        private View.OnClickListener likeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    String userId = SparkleHelper.getActiveUser(context).nationId;
                    // Users can't like their own posts
                    if (!SparkleHelper.getIdFromName(post.name).equals(userId))
                    {
                        boolean curLikeStatus = post.likedBy != null && post.likedBy.contains(userId);
                        ((MessageBoardActivity) context).setLikeStatus(pos, post.id, !curLikeStatus);
                    }
                    else
                    {
                        ((MessageBoardActivity) context).selfLikeStatus();
                    }
                }
            }
        };

        private View.OnClickListener replyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && post.message != null)
                {
                    if (replyIndex == pos)
                    {
                        ((MessageBoardActivity) context).setReplyMessage(null);
                    }
                    else
                    {
                        ((MessageBoardActivity) context).setReplyMessage(post, pos);
                        setReplyIndex(pos);
                    }
                }
            }
        };

        private View.OnClickListener deleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    ((MessageBoardActivity) context).confirmDelete(pos, post.id);
                }
            }
        };

        private View.OnClickListener reportClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    SparkleHelper.startReport(context, ReportActivity.REPORT_TYPE_RMB, post.id, post.name);
                }
            }
        };

        public PostCard(Context c, View v) {
            super(v);
            context = c;
            cardContainer = (CardView) v.findViewById(R.id.card_post_container);
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
            actionsHolder = (RelativeLayout) v.findViewById(R.id.card_post_actions_holder);
            likeButton = (ImageView) v.findViewById(R.id.card_post_like);
            likeCount = (TextView) v.findViewById(R.id.card_post_like_count);
            deleteButton = (ImageView) v.findViewById(R.id.card_post_delete);
            reportButton = (ImageView) v.findViewById(R.id.card_post_report);
            replyButton = (ImageView) v.findViewById(R.id.card_post_reply);
        }

        public void init(Post p)
        {
            post = p;
            if (post.id != EMPTY_INDICATOR)
            {
                SparkleHelper.activityLinkBuilder(context, cardAuthor, post.name, post.name, SparkleHelper.getNameFromId(post.name), SparkleHelper.CLICKY_NATION_MODE);
                cardTime.setText(SparkleHelper.getReadableDateFromUTC(context, post.timestamp));
                String postContent = post.message;
                if (post.status == Post.POST_SUPPRESSED && post.suppressor != null)
                {
                    postContent = String.format(context.getString(R.string.rmb_suppressed), post.suppressor, postContent);
                }
                if (post.status == Post.POST_DELETED || post.status == Post.POST_BANHAMMERED)
                {
                    postContent = "[i]" + postContent + "[/i]";
                }
                SparkleHelper.setBbCodeFormatting(context, cardContent, postContent, fm);

                // Setup actions holder
                if (post.status == Post.POST_REGULAR || post.status == Post.POST_SUPPRESSED)
                {
                    actionsHolder.setVisibility(View.VISIBLE);

                    if (isPostable)
                    {
                        // All posts can be replied to
                        replyButton.setOnClickListener(replyClickListener);
                        // Only user's own posts can be deleted
                        if (context != null && SparkleHelper.getActiveUser(context).nationId.equals(post.name))
                        {
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setOnClickListener(deleteClickListener);
                            reportButton.setVisibility(View.GONE);
                            reportButton.setOnClickListener(null);
                        }
                        else
                        {
                            deleteButton.setVisibility(View.GONE);
                            deleteButton.setOnClickListener(null);
                            reportButton.setVisibility(View.VISIBLE);
                            reportButton.setOnClickListener(reportClickListener);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) reportButton.getLayoutParams();
                            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                            reportButton.setLayoutParams(params);
                        }
                    }
                    else
                    {
                        replyButton.setVisibility(View.GONE);
                        replyButton.setOnClickListener(null);
                        deleteButton.setVisibility(View.GONE);
                        deleteButton.setOnClickListener(null);

                        reportButton.setVisibility(View.VISIBLE);
                        reportButton.setOnClickListener(reportClickListener);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) reportButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        reportButton.setLayoutParams(params);
                    }

                    // like button and count are visible to all
                    likeButton.setOnClickListener(likeClickListener);
                    likeCount.setText(SparkleHelper.getPrettifiedNumber(post.likes));
                    // Only build liked list if there are likes
                    if (post.likes > 0 && post.likedBy != null && post.likedBy.length() > 0)
                    {
                        String[] likes = post.likedBy.split(":");
                        ArrayList<String> properLikes = new ArrayList<String>();
                        for (String li : likes)
                        {
                            properLikes.add(SparkleHelper.getNameFromId(li));
                        }
                        final ArrayList<String> fLikes = properLikes;
                        likeCount.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentManager fm = ((MessageBoardActivity) context).getSupportFragmentManager();
                                NameListDialog nameListDialog = new NameListDialog();
                                nameListDialog.setTitle(context.getString(R.string.rmb_likes));
                                nameListDialog.setNames(fLikes);
                                nameListDialog.setTarget(SparkleHelper.CLICKY_NATION_MODE);
                                nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                            }
                        });
                    }
                    else
                    {
                        likeCount.setOnClickListener(null);
                    }
                }
                else
                {
                    actionsHolder.setVisibility(View.GONE);
                }
            }
            else
            {
                cardTime.setVisibility(View.GONE);
                cardAuthor.setVisibility(View.GONE);
                cardContent.setText(context.getString(R.string.rmb_no_content));
                cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 0);
                cardContent.setLayoutParams(params);
                actionsHolder.setVisibility(View.GONE);
            }
        }

        public void select()
        {
            if (SettingsActivity.getTheme(context) != SettingsActivity.THEME_NOIR) {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.highlightColor));
            }
            else {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentNoir));
            }
            replyButton.setImageResource(R.drawable.ic_clear);
        }

        public void deselect()
        {
            cardContainer.setCardBackgroundColor(SparkleHelper.getThemeCardColour(context));
            replyButton.setImageResource(R.drawable.ic_reply);
        }

        public void like()
        {
            likeButton.setImageResource(R.drawable.ic_liked);
            likeCount.setTextColor(ContextCompat.getColor(context, R.color.colorChart1));
        }

        public void unlike()
        {
            likeButton.setImageResource(R.drawable.ic_like);
            likeCount.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
        }
    }

}
