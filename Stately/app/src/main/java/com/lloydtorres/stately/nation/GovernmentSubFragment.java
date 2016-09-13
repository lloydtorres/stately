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

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment within the Nation fragment that displays government data.
 * Takes in a Nation object.
 */
public class GovernmentSubFragment extends NationSubFragment {

    @Override
    protected void initData() {
        NationGenericCardData ngcSummary = new NationGenericCardData();
        ngcSummary.title = getString(R.string.card_main_title_summary);
        String descContent = mNation.govtDesc;
        descContent = descContent.replace(". ", ".<br /><br />");
        ngcSummary.mainContent = descContent;
        ngcSummary.nationCensusTarget = mNation.name;
        ngcSummary.idCensusTarget = TrendsActivity.CENSUS_GOVERNMENT_SIZE;
        cards.add(ngcSummary);

        NationChartCardData nccExpenditures = new NationChartCardData();
        long budgetHolder = (long) (mNation.gdp * (mNation.sectors.government/100d));
        String budgetText = String.format(Locale.US, getString(R.string.card_government_expenditures_budget_flavour), SparkleHelper.getMoneyFormatted(getContext(), budgetHolder, mNation.currency), mNation.sectors.government);
        nccExpenditures.details.add(new DataPair(getString(R.string.card_government_expenditures_budget), budgetText));
        nccExpenditures.mode = NationChartCardData.MODE_GOV;
        nccExpenditures.govBudget = mNation.govBudget;
        cards.add(nccExpenditures);
    }
}
