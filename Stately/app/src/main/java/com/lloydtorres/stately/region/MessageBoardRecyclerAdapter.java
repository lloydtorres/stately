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
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;
import com.lloydtorres.stately.report.ReportActivity;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-24.
 * An adapter for the recyclerview in MessageBoardActivity.
 */
public class MessageBoardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int NO_SELECTION = -1;
    private static final String DELETED_CONTENT = "Message deleted by author";
    private static final int EMPTY_INDICATOR = -1;

    private static final int POST_EXPANDED = 0;
    private static final int POST_COLLAPSED = 1;

    private Context context;
    private FragmentManager fm;
    private List<Post> messages;
    private int replyIndex = NO_SELECTION;
    private boolean isPostable = false;
    private boolean isSuppressable = false;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p, boolean isP, boolean isS, FragmentManager f) {
        context = c;
        fm = f;
        isPostable = isP;
        isSuppressable = isS;
        setMessages(p);
    }

    /**
     * Set new messages
     * @param p List of posts
     */
    public void setMessages(List<Post> p) {
        messages = p;

        if (messages.size() <= 0) {
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
    public void setReplyIndex(int i) {
        int oldReplyIndex = replyIndex;
        replyIndex = i;

        if (oldReplyIndex != -1) {
            notifyItemChanged(oldReplyIndex);
        }
        if (replyIndex != -1) {
            notifyItemChanged(replyIndex);
        }

        if (replyIndex == oldReplyIndex) {
            replyIndex = NO_SELECTION;
            notifyItemChanged(oldReplyIndex);
        }
    }

    /**
     * Add an offset to the reply index
     * @param a Offset
     */
    public void addToReplyIndex(int a) {
        if (replyIndex != -1) {
            setReplyIndex(replyIndex + a);
        }
    }

    /**
     * Mark a message as having been deleted
     * @param i
     */
    public void setAsDeleted(int i) {
        messages.get(i).message = DELETED_CONTENT;
        messages.get(i).status = Post.POST_DELETED;
        notifyItemChanged(i);
    }

    public void toggleSuppressedStatus(int i) {
        if (context != null) {
            if (messages.get(i).status == Post.POST_SUPPRESSED) {
                messages.get(i).status = Post.POST_REGULAR;
                messages.get(i).suppressor = null;
            } else {
                String userNationId = PinkaHelper.getActiveUser(context).nationId;
                messages.get(i).status = Post.POST_SUPPRESSED;
                messages.get(i).suppressor = userNationId;
                messages.get(i).isExpanded = true;
            }
            notifyItemChanged(i);
        }
    }

    /**
     * Mark a message as either liked or unliked
     * @param pos Position of message in adapter
     * @param like True to like, false to unlike
     */
    public void setLikeStatus(int pos, boolean like) {
        if (context == null) {
            return;
        }

        Post targetPost = messages.get(pos);
        String userId = PinkaHelper.getActiveUser(context).nationId;
        if (like) {
            // Either set the user as the only liker, or append their id to the string
            if (targetPost.likedBy == null || targetPost.likedBy.length() <= 0) {
                targetPost.likedBy = userId;
            } else {
                targetPost.likedBy = targetPost.likedBy + ":" + userId;
            }
            targetPost.likes++;
        } else {
            // If string contains user ID, remove it
            if (targetPost.likedBy != null && targetPost.likedBy.contains(userId)) {
                String[] likes = targetPost.likedBy.split(":");
                ArrayList<String> properLikes = new ArrayList<String>();
                for (String li : likes) {
                    if (!li.equals(userId)) {
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
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case POST_COLLAPSED:
                viewHolder = new CollapsedPostCard(postCard);
                break;
            default:
                viewHolder = new PostCard(postCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case POST_EXPANDED:
                PostCard postCard = (PostCard) holder;
                Post message = messages.get(position);
                postCard.init(message);

                if (position == replyIndex) {
                    postCard.select();
                } else {
                    postCard.deselect();
                }

                if (message.likes > 0 && message.likedBy != null && message.likedBy.length() > 0) {
                    // If current user is in the like list, highlight the like buttons
                    if (context != null && message.likedBy.contains(PinkaHelper.getActiveUser(context).nationId)) {
                        postCard.like();
                    } else {
                        postCard.unlike();
                    }
                } else {
                    postCard.unlike();
                }
                break;
            case POST_COLLAPSED:
                CollapsedPostCard collapsedPostCard = (CollapsedPostCard) holder;
                collapsedPostCard.init(messages.get(position));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post targetPost = messages.get(position);
        if (targetPost.id != EMPTY_INDICATOR &&
                (targetPost.status == Post.POST_REGULAR ||
                        (targetPost.status == Post.POST_SUPPRESSED && targetPost.isExpanded))) {
            return POST_EXPANDED;
        } else {
            return POST_COLLAPSED;
        }
    }

    public class PostCard extends RecyclerView.ViewHolder {
        private Post post;
        private CardView cardContainer;
        private TextView cardAuthor;
        private TextView cardTime;
        private RelativeLayout cardSuppressedHolder;
        private TextView cardSuppressedContent;
        private HtmlTextView cardContent;
        private RelativeLayout actionsHolder;
        private ImageView likeButton;
        private TextView likeCount;
        private ImageView suppressButton;
        private ImageView deleteButton;
        private ImageView reportButton;
        private ImageView replyButton;

        private View.OnClickListener likeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    String userId = PinkaHelper.getActiveUser(context).nationId;
                    // Users can't like their own posts
                    if (!SparkleHelper.getIdFromName(post.name).equals(userId)) {
                        boolean curLikeStatus = post.likedBy != null && post.likedBy.contains(userId);
                        ((MessageBoardActivity) context).setLikeStatus(pos, post.id, !curLikeStatus);
                    } else {
                        ((MessageBoardActivity) context).selfLikeStatus();
                    }
                }
            }
        };

        private View.OnClickListener replyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && post.message != null) {
                    if (replyIndex == pos) {
                        ((MessageBoardActivity) context).setReplyMessage(null);
                    } else {
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
                if (pos != RecyclerView.NO_POSITION) {
                    ((MessageBoardActivity) context).confirmDelete(pos, post.id);
                }
            }
        };

        private View.OnClickListener reportClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    SparkleHelper.startReport(context, ReportActivity.REPORT_TYPE_RMB, post.id, post.name);
                }
            }
        };

        private View.OnClickListener suppressClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    ((MessageBoardActivity) context).confirmSuppress(pos, post.id, post.status);
                }
            }
        };

        public PostCard(View v) {
            super(v);
            cardContainer = (CardView) v.findViewById(R.id.card_post_container);
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardSuppressedHolder = (RelativeLayout) v.findViewById(R.id.card_post_suppressed_holder);
            cardSuppressedContent = (TextView) v.findViewById(R.id.card_post_suppressed_content);
            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
            actionsHolder = (RelativeLayout) v.findViewById(R.id.card_post_actions_holder);
            likeButton = (ImageView) v.findViewById(R.id.card_post_like);
            likeCount = (TextView) v.findViewById(R.id.card_post_like_count);
            suppressButton = (ImageView) v.findViewById(R.id.card_post_suppress);
            deleteButton = (ImageView) v.findViewById(R.id.card_post_delete);
            reportButton = (ImageView) v.findViewById(R.id.card_post_report);
            replyButton = (ImageView) v.findViewById(R.id.card_post_reply);
        }

        public void init(Post p) {
            post = p;

            cardAuthor.setText(SparkleHelper.getNameFromId(post.name));
            cardAuthor.setOnClickListener(SparkleHelper.getExploreOnClickListener(context, post.name, ExploreActivity.EXPLORE_NATION));
            cardTime.setText(SparkleHelper.getReadableDateFromUTC(context, post.timestamp));

            // Show suppresssed holder if post is suppressed
            if (post.status == Post.POST_SUPPRESSED && post.suppressor != null) {
                cardSuppressedHolder.setVisibility(View.VISIBLE);
                String suppressedText = String.format(Locale.US, context.getString(R.string.rmb_suppressed_main), post.suppressor);
                SparkleHelper.setHappeningsFormatting(context, cardSuppressedContent, suppressedText);
            } else {
                cardSuppressedHolder.setVisibility(View.GONE);
            }

            SparkleHelper.setStyledTextView(context, cardContent, post.message, fm);

            actionsHolder.setVisibility(View.VISIBLE);

            // Setup reply, delete, report, suppress buttons based on user status
            if (isPostable) {
                // All posts can be replied to
                replyButton.setOnClickListener(replyClickListener);
                // Only user's own posts can be deleted
                if (context != null && PinkaHelper.getActiveUser(context).nationId.equals(post.name)) {
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(deleteClickListener);
                    reportButton.setVisibility(View.GONE);
                    reportButton.setOnClickListener(null);
                } else {
                    deleteButton.setVisibility(View.GONE);
                    deleteButton.setOnClickListener(null);
                    reportButton.setVisibility(View.VISIBLE);
                    reportButton.setOnClickListener(reportClickListener);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) reportButton.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    reportButton.setLayoutParams(params);

                    // Setup suppression button -- only visible if user has suppression rights and for non-self posts
                    if (isSuppressable) {
                        suppressButton.setVisibility(View.VISIBLE);
                        suppressButton.setOnClickListener(suppressClickListener);
                        suppressButton.setImageResource(post.status == Post.POST_SUPPRESSED ? R.drawable.ic_unsuppress_post : R.drawable.ic_suppress_post);
                    } else {
                        suppressButton.setVisibility(View.GONE);
                        suppressButton.setOnClickListener(null);
                    }
                }
            } else {
                replyButton.setVisibility(View.GONE);
                replyButton.setOnClickListener(null);
                deleteButton.setVisibility(View.GONE);
                deleteButton.setOnClickListener(null);
                suppressButton.setVisibility(View.GONE);
                suppressButton.setOnClickListener(null);

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
            if (post.likes > 0 && post.likedBy != null && post.likedBy.length() > 0) {
                String[] likes = post.likedBy.split(":");
                ArrayList<String> properLikes = new ArrayList<String>();
                for (String li : likes) {
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
                        nameListDialog.setTarget(ExploreActivity.EXPLORE_NATION);
                        nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                    }
                });
            } else {
                likeCount.setOnClickListener(null);
            }
        }

        public void select() {
            if (SettingsActivity.getTheme(context) != SettingsActivity.THEME_NOIR) {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.highlightColor));
            } else {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentNoir));
            }
            replyButton.setImageResource(R.drawable.ic_clear);
        }

        public void deselect() {
            cardContainer.setCardBackgroundColor(RaraHelper.getThemeCardColour(context));
            replyButton.setImageResource(R.drawable.ic_reply);
        }

        public void like() {
            likeButton.setImageResource(R.drawable.ic_liked);
            likeCount.setTextColor(ContextCompat.getColor(context, R.color.colorChart1));
        }

        public void unlike() {
            likeButton.setImageResource(R.drawable.ic_like);
            likeCount.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
        }
    }

    public class CollapsedPostCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Post post;
        private View view;
        private HtmlTextView cardContent;

        public CollapsedPostCard(View v) {
            super(v);
            view = v;

            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
            cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 0);
            cardContent.setLayoutParams(params);

            TextView cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            TextView cardTime = (TextView) v.findViewById(R.id.card_post_time);
            RelativeLayout actionsHolder = (RelativeLayout) v.findViewById(R.id.card_post_actions_holder);
            cardAuthor.setVisibility(View.GONE);
            cardTime.setVisibility(View.GONE);
            actionsHolder.setVisibility(View.GONE);
        }

        public void init(Post p) {
            post = p;

            String messageContent = "";
            view.setOnClickListener(null);
            switch (post.status) {
                case Post.POST_SUPPRESSED:
                    messageContent = String.format(Locale.US, context.getString(R.string.rmb_suppressed), post.name, post.suppressor);
                    view.setOnClickListener(this);
                    break;
                case Post.POST_DELETED:
                    messageContent = String.format(Locale.US, context.getString(R.string.rmb_deleted), post.name);
                    break;
                case Post.POST_BANHAMMERED:
                    messageContent = String.format(Locale.US, context.getString(R.string.rmb_banhammered), post.name);
                    break;
            }

            SparkleHelper.setHappeningsFormatting(context, cardContent, messageContent);
        }

        @Override
        public void onClick(View v) {
            post.isExpanded = true;
            notifyItemChanged(getAdapterPosition());
        }
    }
}
