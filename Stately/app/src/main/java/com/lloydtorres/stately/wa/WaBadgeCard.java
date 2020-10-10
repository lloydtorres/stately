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

package com.lloydtorres.stately.wa;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.WaBadge;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.Locale;

/**
 * Created by lloyd on 2017-03-07.
 * Card viewholder that shows a nation/region's commendation/condemnation/liberation,
 * and has a link to the SC resolution causing it.
 */
public class WaBadgeCard extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;
    private CardView container;
    private TextView badgeDescription;
    private WaBadge badgeData;

    public WaBadgeCard(Context c, View v) {
        super(v);
        context = c;
        container = v.findViewById(R.id.wa_badge_root);
        badgeDescription = v.findViewById(R.id.wa_resolution_vote_content);
        v.setOnClickListener(this);
    }

    public void init(WaBadge badge) {
        RaraHelper.setViewHolderFullSpan(itemView);

        badgeData = badge;

        int containerColour = ContextCompat.getColor(context, R.color.colorChart12);
        int descriptionText = R.string.wa_badge_commend;

        switch (badgeData.type) {
            case WaBadge.TYPE_COMMEND:
                containerColour = ContextCompat.getColor(context, R.color.colorChart3);
                descriptionText = R.string.wa_badge_commend;
                break;
            case WaBadge.TYPE_CONDEMN:
                containerColour = ContextCompat.getColor(context, R.color.colorChart1);
                descriptionText = R.string.wa_badge_condemn;
                break;
            case WaBadge.TYPE_LIBERATE:
                containerColour = ContextCompat.getColor(context, R.color.colorChart0);
                descriptionText = R.string.wa_badge_liberated;
                break;
        }

        container.setCardBackgroundColor(containerColour);
        badgeDescription.setText(String.format(Locale.US, context.getString(descriptionText), badgeData.scResolution));
    }

    @Override
    public void onClick(View v) {
        if (badgeData != null) {
            SparkleHelper.startResolution(context, Assembly.SECURITY_COUNCIL, badgeData.scResolution);
        }
    }
}
