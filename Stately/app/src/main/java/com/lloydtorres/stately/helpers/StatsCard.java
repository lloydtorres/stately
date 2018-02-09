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

package com.lloydtorres.stately.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.DataIntPair;

/**
 * Created by Lloyd on 2016-09-13.
 * ViewHolder for the card_wa_members layout, used for showing a DataIntPair.
 */
public class StatsCard extends RecyclerView.ViewHolder {

    private TextView cardLeftValue;
    private TextView cardLeftSub;
    private TextView cardRightValue;
    private TextView cardRightSub;

    public StatsCard(View v) {
        super(v);
        cardLeftValue = v.findViewById(R.id.card_wa_members);
        cardLeftSub = v.findViewById(R.id.card_wa_members_sub);
        cardRightValue = v.findViewById(R.id.card_wa_delegates);
        cardRightSub = v.findViewById(R.id.card_wa_delegates_sub);
    }

    public void init(DataIntPair s, String left, String right)
    {
        cardLeftValue.setText(SparkleHelper.getPrettifiedNumber(s.members));
        cardLeftSub.setText(left);
        cardRightValue.setText(SparkleHelper.getPrettifiedNumber(s.delegates));
        cardRightSub.setText(right);
    }
}
