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

package com.lloydtorres.stately.dto;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for data in chart-based nation cards.
 */
public class NationChartCardData {
    public static final int MODE_PEOPLE = 0;
    public static final int MODE_GOV = 1;
    public static final int MODE_ECON = 2;

    public int mode;
    public LinkedHashMap<String, String> details;
    public List<MortalityCause> mortalityList;
    public String animal;
    public GovBudget govBudget;
    public Sectors sectors;

    public NationChartCardData() { super(); }
}
