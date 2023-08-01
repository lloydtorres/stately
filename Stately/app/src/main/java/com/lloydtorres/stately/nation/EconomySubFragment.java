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
 * A sub-fragment within the Nation fragment showing economic data.
 * Takes in nation object.
 */
public class EconomySubFragment extends NationSubFragment {

    @Override
    protected void initData() {
        super.initData();

        NationGenericCardData ngcSummary = new NationGenericCardData();
        ngcSummary.title = getString(R.string.card_main_title_summary);
        String descContent = mNation.industryDesc;
        descContent = descContent.replace(". ", ".<br /><br />");
        ngcSummary.mainContent = descContent;
        ngcSummary.nationCensusTarget = mNation.name;
        ngcSummary.idCensusTarget = TrendsActivity.CENSUS_AVERAGE_INCOME;
        cards.add(ngcSummary);

        NationChartCardData nccExpenditures = new NationChartCardData();
        nccExpenditures.details.add(new DataPair(getString(R.string.card_economy_analysis_gdp),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency)));
        String perCapitaText = String.format(Locale.US,
                getString(R.string.avg_val_currency),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency)) + "<br>" + String.format(Locale.US,
                getString(R.string.poor_val_currency),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.poorest, mNation.currency)) +
                "<br>" + String.format(Locale.US,
                getString(R.string.rich_val_currency),
                SparkleHelper.getMoneyFormatted(getContext(), mNation.richest, mNation.currency));
        nccExpenditures.details.add(new DataPair(getString(R.string.card_economy_analysis_per_capita), perCapitaText));
        nccExpenditures.mode = NationChartCardData.MODE_ECON;
        nccExpenditures.sectors = mNation.sectors;
        cards.add(nccExpenditures);
    }
}
