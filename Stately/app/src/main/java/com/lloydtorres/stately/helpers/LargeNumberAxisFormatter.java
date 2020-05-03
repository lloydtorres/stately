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

import android.content.Context;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * Created by Lloyd on 2016-10-31.
 * Axis formatter for charts that deals with large numbers.
 * Implemented because MPAndroidChart's own implementation breaks for REALLY large numbers.
 */
public class LargeNumberAxisFormatter extends ValueFormatter {

    private Context context;

    public LargeNumberAxisFormatter(Context c) {
        context = c;
    }

    @Override
    public String getFormattedValue(float value) {
        return SparkleHelper.getPrettifiedShortSuffixedNumber(context, value);
    }
}
