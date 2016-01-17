package com.lloydtorres.stately.dto;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-01-16.
 */
public class HappeningCard extends RecyclerView.ViewHolder {

    private TextView cardTime;
    private TextView cardContent;

    public HappeningCard(View v) {
        super(v);
        cardTime = (TextView) v.findViewById(R.id.card_happening_time);
        cardContent = (TextView) v.findViewById(R.id.card_happening_content);
    }

    public void init(HappeningEvent ev)
    {
        cardTime.setText("Whatever");
        cardContent.setText(Html.fromHtml(ev.content).toString());
    }
}
