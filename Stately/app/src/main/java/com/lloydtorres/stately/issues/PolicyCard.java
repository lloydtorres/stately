/**
 * Copyright 2017 Lloyd Torres
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Policy;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;

/**
 * Created by lloyd on 2017-11-10.
 * View holder used for rendering policies in nations and issue results.
 */
public class PolicyCard extends RecyclerView.ViewHolder {
    private final Context context;
    private final LinearLayout toggleHolder;
    private final TextView toggleText;
    private final ImageView image;
    private final TextView title;
    private final TextView description;

    public PolicyCard(Context c, View itemView) {
        super(itemView);
        context = c;
        toggleHolder = itemView.findViewById(R.id.card_policy_toggle_holder);
        toggleText = itemView.findViewById(R.id.card_policy_toggle_text);
        image = itemView.findViewById(R.id.card_policy_image);
        title = itemView.findViewById(R.id.card_policy_title);
        description = itemView.findViewById(R.id.card_policy_desc);
    }

    public void init(Policy policy) {
        if (policy.renderType == Policy.VIEW_NONE) {
            toggleHolder.setVisibility(View.GONE);
        } else {
            toggleHolder.setVisibility(View.VISIBLE);
            boolean isPolicyEnacted = policy.renderType == Policy.VIEW_ENACTED;
            toggleHolder.setBackgroundColor(ContextCompat.getColor(context, isPolicyEnacted
                    ? R.color.colorChart3 : R.color.colorChart1));
            toggleText.setText(isPolicyEnacted
                    ? R.string.card_policy_enacted : R.string.card_policy_abolished);
        }

        if (policy.renderType != Policy.VIEW_ABOLISHED) {
            image.setVisibility(View.VISIBLE);
            DashHelper.getInstance(context).loadImage(
                    RaraHelper.getBannerURL(policy.imageId),
                    image
            );
        } else {
            image.setVisibility(View.GONE);
        }

        title.setText(policy.name);
        description.setText(policy.description);
    }
}
