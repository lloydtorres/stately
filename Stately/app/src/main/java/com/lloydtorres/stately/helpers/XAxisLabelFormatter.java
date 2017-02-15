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

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

/**
 * Created by Lloyd on 2016-10-22.
 * Provides labels to show in a chart's XAxis.
 */
public class XAxisLabelFormatter implements IAxisValueFormatter {

    private List<String> labels;

    public XAxisLabelFormatter(List<String> l) {
        labels = l;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (labels == null || value >= labels.size()) {
            return "";
        } else {
            return labels.get((int) value);
        }
    }
}
