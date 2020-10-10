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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private static final int POST_EXPANDED = 0;
    private static final int POST_COLLAPSED = 1;

    private Context context;
    private FragmentManager fm;
    private List<Post> messages;
    private int modifierIndex = NO_SELECTION;
    private boolean isPostable = false;
    private boolean isSuppressable = false;
    private int messageMode = MessageBoardActivity.MODE_NORMAL;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p, boolean isP, boolean isS,
                                       FragmentManager f) {
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
            np.status = Post.POST_EMPTY;
            messages.add(np);
        }
        notifyDataSetChanged();
    }

    /**
     * Set which message to reply to
     * @param i Index
     */
    public void setModifierIndex(int i, int mode) {
        int oldModifierIndex = modifierIndex;
        modifierIndex = i;
        int oldMessageMode = messageMode;
        messageMode = mode;

        if (oldModifierIndex != NO_SELECTION) {
            notifyItemChanged(oldModifierIndex);
        }
        if (modifierIndex != NO_SELECTION) {
            notifyItemChanged(modifierIndex);
        }

        if (modifierIndex == oldModifierIndex && messageMode == oldMessageMode) {
            modifierIndex = NO_SELECTION;
            messageMode = MessageBoardActivity.MODE_NORMAL;
            notifyItemChanged(oldModifierIndex);
        }
    }

    /**
     * Add an offset to the reply index
     * @param a Offset
     */
    public void addToModifierIndex(int a) {
        if (modifierIndex != -1) {
            setModifierIndex(modifierIndex + a, messageMode);
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

    public void updatePostContent(int id, String message) {
        for (int i = 0; i < messages.size(); i++) {
            Post p = messages.get(i);
            if (p.id == id) {
                p.messageRaw = message;
                p.message = SparkleHelper.transformBBCodeToHtml(context, message,
                        SparkleHelper.BBCODE_PERMISSIONS_RMB);
                p.editedTimestamp = System.currentTimeMillis() / 1000L;
                notifyItemChanged(i);
                break;
            }
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

                if (position == modifierIndex) {
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
        if (targetPost.status == Post.POST_REGULAR ||
                (targetPost.status == Post.POST_SUPPRESSED && targetPost.isExpanded)) {
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
        private ImageView cardSuppressedIcon;
        private TextView cardSuppressedContent;
        private HtmlTextView cardContent;
        private RelativeLayout actionsHolder;
        private ImageView likeButton;
        private TextView likeCount;
        private ImageView suppressButton;
        private ImageView editButton;
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
                        boolean curLikeStatus =
                                post.likedBy != null && post.likedBy.contains(userId);
                        ((MessageBoardActivity) context).setLikeStatus(pos, post.id,
                                !curLikeStatus);
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
                    if (modifierIndex == pos && messageMode == MessageBoardActivity.MODE_REPLY) {
                        ((MessageBoardActivity) context).setModifierMessage(null,
                                MessageBoardActivity.MODE_NORMAL);
                    } else {
                        ((MessageBoardActivity) context).setModifierMessage(post,
                                MessageBoardActivity.MODE_REPLY, pos);
                        setModifierIndex(pos, MessageBoardActivity.MODE_REPLY);
                    }
                }
            }
        };

        private View.OnClickListener editClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && post.message != null) {
                    if (modifierIndex == pos && messageMode == MessageBoardActivity.MODE_EDIT) {
                        ((MessageBoardActivity) context).setModifierMessage(null,
                                MessageBoardActivity.MODE_NORMAL);
                    } else {
                        ((MessageBoardActivity) context).setModifierMessage(post,
                                MessageBoardActivity.MODE_EDIT, pos);
                        setModifierIndex(pos, MessageBoardActivity.MODE_EDIT);
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
                    SparkleHelper.startReport(context, ReportActivity.REPORT_TYPE_RMB, post.id,
                            post.name);
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
            cardContainer = v.findViewById(R.id.card_post_container);
            cardAuthor = v.findViewById(R.id.card_post_name);
            cardTime = v.findViewById(R.id.card_post_time);
            cardSuppressedHolder = v.findViewById(R.id.card_post_suppressed_holder);
            cardSuppressedIcon = v.findViewById(R.id.card_post_suppressed_icon);
            cardSuppressedContent = v.findViewById(R.id.card_post_suppressed_content);
            cardContent = v.findViewById(R.id.card_post_content);
            actionsHolder = v.findViewById(R.id.card_post_actions_holder);
            likeButton = v.findViewById(R.id.card_post_like);
            likeCount = v.findViewById(R.id.card_post_like_count);
            suppressButton = v.findViewById(R.id.card_post_suppress);
            editButton = v.findViewById(R.id.card_post_edit);
            deleteButton = v.findViewById(R.id.card_post_delete);
            reportButton = v.findViewById(R.id.card_post_report);
            replyButton = v.findViewById(R.id.card_post_reply);
        }

        public void init(Post p) {
            post = p;

            if (!Post.POST_NS_MODERATORS.equals(post.name)) {
                cardAuthor.setText(SparkleHelper.getNameFromId(post.name));
                cardAuthor.setOnClickListener(SparkleHelper.getExploreOnClickListener(context,
                        post.name, ExploreActivity.EXPLORE_NATION));
            } else {
                cardAuthor.setText(post.name);
                cardAuthor.setOnClickListener(null);
            }

            cardTime.setText(SparkleHelper.getReadableDateFromUTC(context, post.timestamp));
            if (post.editedTimestamp != 0) {
                cardTime.append(" ");
                cardTime.append(context.getString(R.string.rmb_edit_indicator));
            }

            // Show suppresssed holder if post is suppressed
            if (post.status == Post.POST_SUPPRESSED && post.suppressor != null) {
                cardSuppressedHolder.setVisibility(View.VISIBLE);
                cardSuppressedIcon.setImageResource(R.drawable.ic_unsuppress_post);
                String suppressedText = String.format(Locale.US,
                        context.getString(R.string.rmb_suppressed_main), post.suppressor);
                SparkleHelper.setHappeningsFormatting(context, cardSuppressedContent,
                        suppressedText);
                cardSuppressedContent.setTextColor(ContextCompat.getColor(context,
                        R.color.colorChart1));
            } else if (Post.POST_NS_MODERATORS.equals(post.name)) {
                cardSuppressedHolder.setVisibility(View.VISIBLE);
                cardSuppressedIcon.setImageResource(R.drawable.ic_alert_moderator);
                cardSuppressedContent.setText(context.getString(R.string.rmb_moderation));
                cardSuppressedContent.setTextColor(ContextCompat.getColor(context,
                        R.color.colorChart0));
            } else {
                cardSuppressedHolder.setVisibility(View.GONE);
            }

            SparkleHelper.setStyledTextView(context, cardContent, post.message, fm);

            actionsHolder.setVisibility(View.VISIBLE);

            // Disable buttons for all cases first
            replyButton.setVisibility(View.GONE);
            replyButton.setOnClickListener(null);
            editButton.setVisibility(View.GONE);
            editButton.setOnClickListener(null);
            deleteButton.setVisibility(View.GONE);
            deleteButton.setOnClickListener(null);
            reportButton.setVisibility(View.GONE);
            reportButton.setOnClickListener(null);
            suppressButton.setVisibility(View.GONE);
            suppressButton.setOnClickListener(null);

            // Setup reply, delete, report, suppress buttons based on user status

            // Only user's own posts can be deleted and edited
            if (isSelfPost()) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(deleteClickListener);
                // Only show edit buttons if user can post in board
                if (isPostable) {
                    editButton.setVisibility(View.VISIBLE);
                    editButton.setOnClickListener(editClickListener);
                }
            } else {
                reportButton.setVisibility(View.VISIBLE);
                reportButton.setOnClickListener(reportClickListener);
            }

            // Only show reply button if postable; setup alignment of other buttons depending on
            // case
            if (isPostable) {
                // All posts can be replied to
                replyButton.setVisibility(View.VISIBLE);
                replyButton.setOnClickListener(replyClickListener);
                setAlignParentRight(isSelfPost() ? deleteButton : reportButton, false);
            } else {
                setAlignParentRight(isSelfPost() ? deleteButton : reportButton, true);
            }

            // Setup suppression button -- only visible if user has suppression rights and for
            // non-self posts
            if (isSuppressable && !isSelfPost()) {
                suppressButton.setVisibility(View.VISIBLE);
                suppressButton.setOnClickListener(suppressClickListener);
                suppressButton.setImageResource(post.status == Post.POST_SUPPRESSED ?
                        R.drawable.ic_unsuppress_post : R.drawable.ic_suppress_post);
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
                        FragmentManager fm =
                                ((MessageBoardActivity) context).getSupportFragmentManager();
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

        /**
         * Checks if the current post is the user's own.
         * @return
         */
        private boolean isSelfPost() {
            return context != null && PinkaHelper.getActiveUser(context).nationId.equals(post.name);
        }

        /**
         * Given an image view, sets its alignment to parent right to true or false.
         * @param target
         * @param shouldAlignRight
         */
        private void setAlignParentRight(ImageView target, boolean shouldAlignRight) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) target.getLayoutParams();
            if (shouldAlignRight) {

                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else {
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            }
            target.setLayoutParams(params);
        }

        public void select() {
            if (SettingsActivity.getTheme(context) != SettingsActivity.THEME_NOIR) {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context,
                        R.color.highlightColor));
            } else {
                cardContainer.setCardBackgroundColor(ContextCompat.getColor(context,
                        R.color.colorAccentNoir));
            }

            editButton.setImageResource(R.drawable.ic_edit_post);
            replyButton.setImageResource(R.drawable.ic_reply);
            switch (messageMode) {
                case MessageBoardActivity.MODE_REPLY:
                    replyButton.setImageResource(R.drawable.ic_clear);
                    break;
                case MessageBoardActivity.MODE_EDIT:
                    editButton.setImageResource(R.drawable.ic_clear);
                    break;
            }
        }

        public void deselect() {
            cardContainer.setCardBackgroundColor(RaraHelper.getThemeCardColour(context));
            editButton.setImageResource(R.drawable.ic_edit_post);
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

            cardContent = v.findViewById(R.id.card_post_content);
            cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 0);
            cardContent.setLayoutParams(params);

            TextView cardAuthor = v.findViewById(R.id.card_post_name);
            TextView cardTime = v.findViewById(R.id.card_post_time);
            RelativeLayout actionsHolder = v.findViewById(R.id.card_post_actions_holder);
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
                    messageContent = String.format(Locale.US,
                            context.getString(R.string.rmb_suppressed), post.name, post.suppressor);
                    view.setOnClickListener(this);
                    break;
                case Post.POST_DELETED:
                    messageContent = String.format(Locale.US,
                            context.getString(R.string.rmb_deleted), post.name);
                    break;
                case Post.POST_BANHAMMERED:
                    messageContent = String.format(Locale.US,
                            context.getString(R.string.rmb_banhammered), post.name);
                    break;
                case Post.POST_EMPTY:
                    messageContent = context.getString(R.string.rmb_no_content);
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
