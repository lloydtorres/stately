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

package com.lloydtorres.stately.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Lloyd on 2016-10-15.
 * Recycler view card for combining a bunch of happenings together.
 */
public class BreakingNewsCard extends RecyclerView.ViewHolder {
    @BindView(R.id.card_world_breaking_news_holder)
    LinearLayout newsHolder;

    private LayoutInflater inflater;

    public BreakingNewsCard(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void init(Context context, List<Event> newsItems) {
        inflater = LayoutInflater.from(context);
        newsHolder.removeAllViews();
        int index = 0;
        for (Event e : newsItems) {
            View newsItemView = inflater.inflate(R.layout.view_world_breaking_news_entry, null);
            HtmlTextView newsContent = (HtmlTextView) newsItemView.findViewById(R.id.card_world_breaking_news_content);
            SparkleHelper.setHappeningsFormatting(context, newsContent, e.content);

            if (++index >= newsItems.size()) {
                newsItemView.findViewById(R.id.view_divider).setVisibility(View.GONE);
            }

            newsHolder.addView(newsItemView);
        }
    }
}