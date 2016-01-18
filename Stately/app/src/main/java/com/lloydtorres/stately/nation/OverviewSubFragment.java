package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class OverviewSubFragment extends Fragment {
    private Nation mNation;

    // main card
    private TextView govType;
    private TextView region;
    private TextView population;
    private TextView motto;
    private TextView time;

    // freedom cards
    private CardView civilRightsCard;
    private TextView civilRightsDesc;
    private TextView civilRightsPts;

    private CardView economyCard;
    private TextView economyDesc;
    private TextView economyPts;

    private CardView politicalCard;
    private TextView politicalDesc;
    private TextView politicalPts;

    // government cards
    private LinearLayout leaderLayout;
    private TextView leader;
    private LinearLayout capitalLayout;
    private TextView capital;
    private TextView priority;
    private TextView tax;

    // economy cards
    private TextView currency;
    private TextView gdp;
    private TextView industry;
    private TextView income;

    // other cards
    private TextView demonym;
    private LinearLayout religionLayout;
    private TextView religion;
    private TextView animal;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_overview, container, false);

        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNation");
        }

        if (mNation != null)
        {
            initMainCard(view);
            initFreedomCards(view);
            initGovernmentCard(view);
            initEconomyCard(view);
            initOtherCard(view);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable("mNation", mNation);
        }
    }

    private void initMainCard(View view)
    {
        govType = (TextView) view.findViewById(R.id.nation_gov_type);
        govType.setText(mNation.govType);

        region = (TextView) view.findViewById(R.id.nation_region);
        region.setText(mNation.region);

        population = (TextView) view.findViewById(R.id.nation_population);
        population.setText(SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase));

        motto = (TextView) view.findViewById(R.id.nation_motto);
        motto.setText(SparkleHelper.getHtmlFormatting(mNation.motto));

        time = (TextView) view.findViewById(R.id.nation_time);
        time.setText(String.format(getString(R.string.nation_time_founded), mNation.foundedAgo, mNation.lastActivityAgo));
    }

    private void initFreedomCards(View view)
    {
        civilRightsCard = (CardView) view.findViewById(R.id.card_overview_civrights);
        civilRightsDesc = (TextView) view.findViewById(R.id.overview_civrights);
        civilRightsPts = (TextView) view.findViewById(R.id.overview_civrights_pts);

        civilRightsDesc.setText(mNation.freedomDesc.civilRightsDesc);
        civilRightsPts.setText(String.valueOf(mNation.freedomPts.civilRightsPts));
        int civColInd = mNation.freedomPts.civilRightsPts / 7;
        civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[civColInd]));

        economyCard = (CardView) view.findViewById(R.id.card_overview_economy);
        economyDesc = (TextView) view.findViewById(R.id.overview_economy);
        economyPts = (TextView) view.findViewById(R.id.overview_economy_pts);

        economyDesc.setText(mNation.freedomDesc.economyDesc);
        economyPts.setText(String.valueOf(mNation.freedomPts.economyPts));
        int econColInd = mNation.freedomPts.economyPts / 7;
        economyCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[econColInd]));

        politicalCard = (CardView) view.findViewById(R.id.card_overview_polifree);
        politicalDesc = (TextView) view.findViewById(R.id.overview_polifree);
        politicalPts = (TextView) view.findViewById(R.id.overview_polifree_pts);

        politicalDesc.setText(mNation.freedomDesc.politicalDesc);
        politicalPts.setText(String.valueOf(mNation.freedomPts.politicalPts));
        int polColInd = mNation.freedomPts.politicalPts / 7;
        politicalCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), SparkleHelper.freedomColours[polColInd]));
    }

    private void initGovernmentCard(View view)
    {
        if (mNation.leader != null)
        {
            leader = (TextView) view.findViewById(R.id.nation_leader);
            leader.setText(SparkleHelper.getHtmlFormatting(mNation.leader));
        }
        else
        {
            leaderLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_leader);
            leaderLayout.setVisibility(View.GONE);
        }

        if (mNation.capital != null)
        {
            capital = (TextView) view.findViewById(R.id.nation_capital);
            capital.setText(SparkleHelper.getHtmlFormatting(mNation.capital));
        }
        else
        {
            capitalLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_capital);
            capitalLayout.setVisibility(View.GONE);
        }

        priority = (TextView) view.findViewById(R.id.nation_priority);
        priority.setText(mNation.govtPriority);

        tax = (TextView) view.findViewById(R.id.nation_tax);
        tax.setText(String.format(getString(R.string.percent), mNation.tax));
    }

    private void initEconomyCard(View view)
    {
        currency = (TextView) view.findViewById(R.id.nation_currency);
        currency.setText(mNation.currency);

        gdp = (TextView) view.findViewById(R.id.nation_gdp);
        gdp.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.gdp, mNation.currency));

        industry = (TextView) view.findViewById(R.id.nation_industry);
        industry.setText(mNation.industry);

        income = (TextView) view.findViewById(R.id.nation_income);
        income.setText(SparkleHelper.getMoneyFormatted(getContext(), mNation.income, mNation.currency));
    }

    private void initOtherCard(View view)
    {
        demonym = (TextView) view.findViewById(R.id.nation_demonym);
        if (mNation.demAdjective.equals(mNation.demNoun))
        {
            demonym.setText(String.format(getString(R.string.card_overview_other_demonym_txt2), mNation.demNoun, mNation.demPlural));
        }
        else
        {
            demonym.setText(String.format(getString(R.string.card_overview_other_demonym_txt1), mNation.demNoun, mNation.demPlural, mNation.demAdjective));
        }

        if (mNation.religion != null)
        {
            religion = (TextView) view.findViewById(R.id.nation_religion);
            religion.setText(mNation.religion);
        }
        else
        {
            religionLayout = (LinearLayout) view.findViewById(R.id.card_overview_other_religion);
            religionLayout.setVisibility(View.GONE);
        }

        animal = (TextView) view.findViewById(R.id.nation_animal);
        animal.setText(mNation.animal);
    }
}
