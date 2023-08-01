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

package com.lloydtorres.stately.census;

import android.content.Context;
import android.view.View;

import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-04-10.
 * Convenience class for creating an OnClickListener to a user's trend given a census ID.
 */
public class TrendsOnClickListener implements View.OnClickListener {

    private final Context context;
    private final String nationId;
    private final int id;
    private int mode;

    public TrendsOnClickListener(Context c, String n, int i) {
        context = c;
        nationId = n;
        id = i;
        mode = TrendsActivity.TREND_NATION;
    }

    public TrendsOnClickListener(Context c, String n, int i, int m) {
        this(c, n, i);
        mode = m;
    }

    @Override
    public void onClick(View v) {
        SparkleHelper.startTrends(context, nationId, mode, id);
    }
}
