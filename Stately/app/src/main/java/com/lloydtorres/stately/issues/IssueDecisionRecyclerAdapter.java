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

package com.lloydtorres.stately.issues;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-29.
 * A RecyclerView used to display the contents of IssueDecisionActivity.
 */
public class IssueDecisionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    private static final int INFO_CARD = 0;
    private static final int OPTION_CARD = 1;
    private static final int DISMISS_CARD = 2;

    private static final int ISSUE_PIRATE_NO = 201;

    private List<Object> cards;
    private Context context;
    private boolean pirateMode;

    public IssueDecisionRecyclerAdapter(Context c, Issue issue) {
        context = c;
        setIssue(issue);
    }

    public void setIssue(Issue issue) {
        cards = new ArrayList<Object>();
        cards.add(issue);
        cards.addAll(issue.options);

        if (issue.id == ISSUE_PIRATE_NO) {
            pirateMode = true;
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case INFO_CARD:
                View infoCard = inflater.inflate(R.layout.card_issue_info, parent, false);
                viewHolder = new IssueInfoCard(infoCard);
                break;
            default:
                View optionCard = inflater.inflate(R.layout.card_issue_option, parent, false);
                viewHolder = new IssueOptionCard(optionCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case INFO_CARD:
                IssueInfoCard infoCard = (IssueInfoCard) holder;
                infoCard.init((Issue) cards.get(position));
                break;
            default:
                IssueOptionCard optionCard = (IssueOptionCard) holder;
                optionCard.init((IssueOption) cards.get(position), holder.getItemViewType());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (cards.get(position) instanceof Issue) {
            return INFO_CARD;
        } else if (cards.get(position) instanceof IssueOption) {
            IssueOption option = (IssueOption) cards.get(position);
            if (option.id == IssueOption.DISMISS_ISSUE_ID) {
                return DISMISS_CARD;
            }
            return OPTION_CARD;
        }
        return -1;
    }

    // Card view holders

    // Card for info on issue
    public class IssueInfoCard extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView issueNo;
        private ImageView image;
        private HtmlTextView content;

        public IssueInfoCard(View v) {
            super(v);
            title = v.findViewById(R.id.card_issue_info_title);
            issueNo = v.findViewById(R.id.card_issue_info_number);
            image = v.findViewById(R.id.card_issue_header_image);
            content = v.findViewById(R.id.card_issue_option_content);
        }

        public void init(Issue issue) {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            title.setText(SparkleHelper.getHtmlFormatting(issue.title).toString());

            if (issue.chain != null) {
                issueNo.setText(String.format(Locale.US,
                        context.getString(R.string.issue_chain_and_number),
                        SparkleHelper.getPrettifiedNumber(issue.id), issue.chain));
            } else {
                issueNo.setText(String.format(Locale.US, context.getString(R.string.issue_number),
                        SparkleHelper.getPrettifiedNumber(issue.id)));
            }

            if (issue.image != null) {
                image.setVisibility(View.VISIBLE);
                DashHelper dashie = DashHelper.getInstance(context);
                dashie.loadImage(RaraHelper.getBannerURL(issue.image), image);
            } else {
                image.setVisibility(View.GONE);
            }

            StringBuilder issueContent = new StringBuilder();

            if (issue.recap != null && !issue.recap.isEmpty()) {
                issueContent.append(issue.recap);
                issueContent.append("<br><br>");
            }
            issueContent.append(issue.content);

            content.setText(SparkleHelper.getHtmlFormatting(issueContent.toString(), false));
        }
    }

    // Card for options
    public class IssueOptionCard extends RecyclerView.ViewHolder {
        private IssueOption option;
        private HtmlTextView content;
        private LinearLayout contentHolder;
        private LinearLayout selectButton;
        private ImageView selectIcon;
        private TextView selectContent;
        private View divider;

        public IssueOptionCard(View v) {
            super(v);
            content = v.findViewById(R.id.card_issue_option_content);
            contentHolder = v.findViewById(R.id.card_issue_option_content_holder);
            selectButton = v.findViewById(R.id.card_issue_option_select);
            selectIcon = v.findViewById(R.id.card_issue_option_select_icon);
            selectContent = v.findViewById(R.id.card_issue_option_select_text);
            divider = v.findViewById(R.id.view_divider);
        }

        public void init(IssueOption op, int mode) {
            option = op;

            // Resets some values to default
            RaraHelper.setViewHolderFullSpan(itemView, false);
            contentHolder.setVisibility(View.VISIBLE);
            selectIcon.setImageResource(R.drawable.ic_check);
            divider.setVisibility(View.VISIBLE);
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IssueDecisionActivity) context).setAdoptPosition(option);
                }
            });

            if (mode != DISMISS_CARD) {
                content.setText(SparkleHelper.getHtmlFormatting(option.content, false));
                selectContent.setText(context.getString(pirateMode ?
                        R.string.issue_select_option_pirate : R.string.issue_select_option));
            } else {
                // Forces card to span across columns
                RaraHelper.setViewHolderFullSpan(itemView);

                contentHolder.setVisibility(View.GONE);
                selectContent.setText(context.getString(pirateMode ?
                        R.string.issue_dismiss_issue_pirate : R.string.issue_dismiss_issue));
                selectIcon.setImageResource(R.drawable.ic_dismiss);
                divider.setVisibility(View.GONE);
            }
        }
    }
}
