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

package com.lloydtorres.stately.nation;

import android.os.Bundle;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment of the Nation fragment showing data on people.
 * Takes in a Nation object.
 */
public class PeopleSubFragment extends NationSubFragment {
    private final HashMap<String, String> waCategoryDescriptors = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] govTypes = getResources().getStringArray(R.array.gov_types);
        String[] govDescriptors = getResources().getStringArray(R.array.gov_descriptors);

        for (int i = 0; i < govTypes.length; i++) {
            waCategoryDescriptors.put(govTypes[i], govDescriptors[i]);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        NationGenericCardData ngcSummary = new NationGenericCardData();
        ngcSummary.title = getString(R.string.card_main_title_summary);
        StringBuilder summaryContent = new StringBuilder(String.format(Locale.US, getString(R.string.card_people_summarydesc_flavour),
                mNation.prename,
                mNation.name,
                mNation.notable,
                mNation.sensible,
                SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase),
                mNation.demPlural));

        String waCategory = mNation.govType.toLowerCase(Locale.US).replace(" ", "_").replace("-", "_");
        if (waCategoryDescriptors.containsKey(waCategory))
        {
            summaryContent.append("<br /><br />").append(String.format(Locale.US, waCategoryDescriptors.get(waCategory), mNation.demPlural));
        }
        summaryContent.append("<br /><br />").append(mNation.crime);
        ngcSummary.mainContent = summaryContent.toString();
        ngcSummary.nationCensusTarget = mNation.name;
        ngcSummary.idCensusTarget = TrendsActivity.CENSUS_CRIME;
        cards.add(ngcSummary);

        NationChartCardData nccMortality = new NationChartCardData();
        nccMortality.mode = NationChartCardData.MODE_PEOPLE;
        nccMortality.mortalityList = mNation.causes;
        nccMortality.animal = mNation.animal;
        cards.add(nccMortality);
    }
}
