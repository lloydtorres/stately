package com.lloydtorres.stately.issues;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-29.
 * A RecyclerView used to display the contents of IssueDecisionActivity.
 */
public class IssueDecisionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of cards
    private final int INFO_CARD = 0;
    private final int OPTION_CARD = 1;
    private final int DISMISS_CARD = 2;

    private List<Object> cards;
    private Context context;

    public IssueDecisionRecyclerAdapter(Context c, Issue issue)
    {
        context = c;
        cards = new ArrayList<Object>();

        cards.add(issue);
        cards.addAll(issue.options);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case INFO_CARD:
                View infoCard = inflater.inflate(R.layout.card_issue_info, parent, false);
                viewHolder = new IssueInfoCard(context, infoCard);
                break;
            default:
                View optionCard = inflater.inflate(R.layout.card_issue_option, parent, false);
                viewHolder = new IssueOptionCard(context, optionCard);
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
        if (cards.get(position) instanceof Issue)
        {
            return INFO_CARD;
        }
        else if (cards.get(position) instanceof IssueOption)
        {
            IssueOption option = (IssueOption) cards.get(position);
            if (option.index == -1)
            {
                return DISMISS_CARD;
            }
            return OPTION_CARD;
        }
        return -1;
    }

    // Card view holders

    // Card for info on issue
    public class IssueInfoCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView title;
        private TextView issueNo;
        private TextView content;

        public IssueInfoCard(Context c, View v)
        {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_issue_info_title);
            issueNo = (TextView) v.findViewById(R.id.card_issue_info_number);
            content = (TextView) v.findViewById(R.id.card_issue_option_content);
        }

        public void init(Issue issue)
        {
            title.setText(SparkleHelper.getHtmlFormatting(issue.title).toString());
            issueNo.setText(String.format(context.getString(R.string.issue_number), issue.id));
            content.setText(SparkleHelper.getHtmlFormatting(issue.content).toString());
        }
    }

    // Card for options
    public class IssueOptionCard extends RecyclerView.ViewHolder {
        private Context context;
        private IssueOption option;
        private TextView content;
        private LinearLayout contentHolder;
        private LinearLayout selectButton;
        private ImageView selectIcon;
        private TextView selectContent;

        public IssueOptionCard(Context c, View v)
        {
            super(v);

            context = c;
            content = (TextView) v.findViewById(R.id.card_issue_option_content);
            contentHolder = (LinearLayout) v.findViewById(R.id.card_issue_option_content_holder);
            selectButton = (LinearLayout) v.findViewById(R.id.card_issue_option_select);
            selectIcon = (ImageView) v.findViewById(R.id.card_issue_option_select_icon);
            selectContent = (TextView) v.findViewById(R.id.card_issue_option_select_text);
        }

        public void init(IssueOption op, int mode)
        {
            option = op;
            content.setText(SparkleHelper.getHtmlFormatting(option.content).toString());
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IssueDecisionActivity) context).sendAdoptPosition(option.index);
                }
            });

            if (mode == DISMISS_CARD)
            {
                contentHolder.setVisibility(View.GONE);
                selectContent.setText(context.getString(R.string.issue_dismiss_issue));
                selectIcon.setImageResource(R.drawable.ic_dismiss_green);
            }

            if (op.selected)
            {
                selectButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                selectContent.setTextColor(ContextCompat.getColor(context, R.color.white));

                switch (mode)
                {
                    case OPTION_CARD:
                        selectContent.setText(context.getText(R.string.issue_option_selected));
                        selectIcon.setImageResource(R.drawable.ic_check_white);
                        break;
                    case DISMISS_CARD:
                        selectContent.setText(context.getText(R.string.issue_issue_dismissed));
                        selectIcon.setImageResource(R.drawable.ic_dismiss_white);
                        break;
                }
            }
        }
    }
}
