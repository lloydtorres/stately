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
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-16.
 * A ViewHolder used to show a happenings card in a RecyclerView.
 */
public class HappeningCard extends RecyclerView.ViewHolder {

    private Context context;
    private TextView cardTime;
    private TextView cardContent;

    public HappeningCard(Context c, View v) {
        super(v);
        context = c;
        cardTime = (TextView) v.findViewById(R.id.card_happening_time);
        cardContent = (TextView) v.findViewById(R.id.card_happening_content);
    }

    public void init(Event ev)
    {
        if (ev.timestamp != EventRecyclerAdapter.EMPTY_INDICATOR)
        {
            cardTime.setText(SparkleHelper.getReadableDateFromUTC(context, ev.timestamp));
            SparkleHelper.setHappeningsFormatting(context, cardContent, ev.content);
        }
        else
        {
            cardTime.setVisibility(View.GONE);
            cardContent.setText(context.getString(R.string.rmb_no_content));
            cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 0);
            cardContent.setLayoutParams(params);
        }
    }
}
