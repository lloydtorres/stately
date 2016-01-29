package com.lloydtorres.stately.issues;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-28.
 * An adapter for the IssuesFragment recycler.
 */
public class IssuesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Issue> issues;

    public IssuesRecyclerAdapter(Context c, List<Issue> i)
    {
        context = c;
        issues = i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View issueCard = inflater.inflate(R.layout.card_issue_main, parent, false);
        RecyclerView.ViewHolder viewHolder = new IssueCard(context, issueCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        IssueCard issueCard = (IssueCard) holder;
        issueCard.init(issues.get(position));
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    public class IssueCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private TextView title;
        private TextView state;
        private ImageView stateIcon;
        private Issue issue;

        public IssueCard(Context c, View v) {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_issue_main_title);
            state = (TextView) v.findViewById(R.id.card_issue_main_status);
            stateIcon = (ImageView) v.findViewById(R.id.card_issue_main_status_icon);

            v.setOnClickListener(this);
        }

        public void init(Issue i)
        {
            issue = i;

            title.setText(issue.title);
            switch(issue.status)
            {
                case Issue.STATUS_PENDING:
                    state.setText(context.getString(R.string.issue_pending));
                    stateIcon.setImageResource(R.drawable.ic_check_grey);
                    break;
                case Issue.STATUS_DISMISSED:
                    state.setText(context.getString(R.string.issue_dismissed));
                    stateIcon.setImageResource(R.drawable.ic_dismiss_grey);
                    break;
                default:
                    state.setText(context.getString(R.string.issue_unaddressed));
                    state.setTextColor(ContextCompat.getColor(context, R.color.issueUnaddressed));
                    stateIcon.setImageResource(R.drawable.ic_unaddressed);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            if (issue != null)
            {
                Intent decisionActivityLaunch = new Intent(context, IssueDecisionActivity.class);
                decisionActivityLaunch.putExtra(IssueDecisionActivity.ISSUE_DATA, issue);
                context.startActivity(decisionActivityLaunch);
            }
        }
    }
}
