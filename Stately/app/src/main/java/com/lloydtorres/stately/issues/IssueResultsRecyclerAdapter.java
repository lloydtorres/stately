package com.lloydtorres.stately.issues;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusDelta;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResultHeadline;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.List;

/**
 * Created by Lloyd on 2016-02-29.
 * An adapter for showing the results of an issue resolution.
 */
public class IssueResultsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] WORLD_CENSUS_ITEMS;

    private final int NEWS_CARD = 0;
    private final int POSITION_CARD = 1;
    private final int HEADLINE_CARD = 2;
    private final int POSTCARD_CARD = 3;
    private final int CENSUSDELTA_CARD = 4;

    private Context context;
    private List<Object> content;

    public IssueResultsRecyclerAdapter(Context c, List<Object> con)
    {
        context = c;
        content = con;
        WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View genericCard = inflater.inflate(R.layout.card_generic, parent, false);

        switch (viewType)
        {
            case NEWS_CARD:
                viewHolder = new NewsCard(context, genericCard);
                break;
            case POSITION_CARD:
                viewHolder = new PositionCard(context, genericCard);
                break;
            case POSTCARD_CARD:
                View postcardCard = inflater.inflate(R.layout.card_postcard, parent, false);
                viewHolder = new PostcardCard(context, postcardCard);
                break;
            case CENSUSDELTA_CARD:
                View censusDeltaCard = inflater.inflate(R.layout.card_census_delta, parent, false);
                viewHolder = new CensusDeltaCard(context, censusDeltaCard);
                break;
            default:
                View headlineCard = inflater.inflate(R.layout.card_headline, parent, false);
                viewHolder = new HeadlineCard(context, headlineCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case NEWS_CARD:
                NewsCard newsCard = (NewsCard) holder;
                newsCard.init((String) content.get(position));
                break;
            case POSITION_CARD:
                PositionCard positionCard = (PositionCard) holder;
                positionCard.init((IssueOption) content.get(position));
                break;
            case POSTCARD_CARD:
                PostcardCard postcardCard = (PostcardCard) holder;
                postcardCard.init((IssuePostcard) content.get(position));
                break;
            case CENSUSDELTA_CARD:
                CensusDeltaCard censusDeltaCard = (CensusDeltaCard) holder;
                censusDeltaCard.init((CensusDelta) content.get(position));
                break;
            default:
                HeadlineCard headlineCard = (HeadlineCard) holder;
                headlineCard.init((IssueResultHeadline) content.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (content.get(position) instanceof String)
        {
            return NEWS_CARD;
        }
        else if (content.get(position) instanceof IssueOption)
        {
            return POSITION_CARD;
        }
        else if (content.get(position) instanceof IssueResultHeadline)
        {
            return HEADLINE_CARD;
        }
        else if (content.get(position) instanceof IssuePostcard)
        {
            return POSTCARD_CARD;
        }
        else if (content.get(position) instanceof CensusDelta)
        {
            return CENSUSDELTA_CARD;
        }
        return -1;
    }

    public class NewsCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView title;
        private TextView content;

        public NewsCard(Context c, View v) {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_generic_title);
            content = (TextView) v.findViewById(R.id.card_generic_content);
        }

        public void init(String n)
        {
            title.setText(context.getString(R.string.issue_breaking));
            content.setText(n);
        }
    }

    public class PositionCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView title;
        private TextView content;

        public PositionCard(Context c, View v) {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_generic_title);
            content = (TextView) v.findViewById(R.id.card_generic_content);
        }

        public void init(IssueOption op)
        {
            title.setText(context.getString(R.string.issue_position));
            content.setText(op.content);
        }
    }

    public class HeadlineCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView title;
        private ImageView img;

        public HeadlineCard(Context c, View v) {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_issue_headline);
            img = (ImageView) v.findViewById(R.id.card_issue_headline_img);
        }

        public void init(IssueResultHeadline headline)
        {
            headline.headline = headline.headline.trim();
            if (context != null)
            {
                headline.headline = headline.headline.replace("@@NAME@@", SparkleHelper.getActiveUser(context).name);
            }
            title.setText(headline.headline);
            DashHelper.getInstance(context).loadImage(headline.imgUrl, img, false);
        }
    }

    public class PostcardCard extends RecyclerView.ViewHolder {
        private Context context;
        private TextView title;
        private ImageView img;

        public PostcardCard(Context c, View v) {
            super(v);
            context = c;
            title = (TextView) v.findViewById(R.id.card_postcard_title);
            img = (ImageView) v.findViewById(R.id.card_postcard_img);
        }

        public void init(IssuePostcard card)
        {
            title.setText(String.format(context.getString(R.string.issue_postcard), card.title.trim()));
            DashHelper.getInstance(context).loadImage(card.imgUrl, img, false);
        }
    }

    public class CensusDeltaCard extends RecyclerView.ViewHolder {
        private Context context;
        private CardView cardHolder;
        private TextView title;
        private TextView unit;
        private TextView value;

        public CensusDeltaCard(Context c, View v)
        {
            super(v);
            context = c;
            cardHolder = (CardView) v.findViewById(R.id.card_census_delta_main);
            title = (TextView) v.findViewById(R.id.card_delta_name);
            unit = (TextView) v.findViewById(R.id.card_delta_unit);
            value = (TextView) v.findViewById(R.id.card_delta_value);
        }

        public void init(CensusDelta delta)
        {
            cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, delta.isPositive ? R.color.colorFreedom14 : R.color.colorFreedom0));

            int censusId = delta.censusId;
            // if census ID is out of bounds, set it as unknown
            if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
            {
                censusId = WORLD_CENSUS_ITEMS.length - 1;
            }
            String[] censusType = WORLD_CENSUS_ITEMS[censusId].split("##");
            title.setText(censusType[0]);
            unit.setText(censusType[1]);
            String valueHolder = delta.isPositive ? "+"+delta.delta : "-"+delta.delta;
            value.setText(valueHolder);
        }
    }
}
