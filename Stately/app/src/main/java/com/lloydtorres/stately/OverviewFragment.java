package com.lloydtorres.stately;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class OverviewFragment extends Fragment {
    private static final String OVERVIEW_KEY = "OVERVIEW_KEY";
    private final int[] freedomColours = {  R.color.colorFreedom0,
                                            R.color.colorFreedom1,
                                            R.color.colorFreedom2,
                                            R.color.colorFreedom3,
                                            R.color.colorFreedom4,
                                            R.color.colorFreedom5,
                                            R.color.colorFreedom6,
                                            R.color.colorFreedom7,
                                            R.color.colorFreedom8,
                                            R.color.colorFreedom9,
                                            R.color.colorFreedom10
                                         };
    private Nation mNation;

    // main card
    private TextView govType;
    private TextView region;
    private TextView population;
    private TextView motto;

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
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

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

    private void initMainCard(View view)
    {
        govType = (TextView) view.findViewById(R.id.nation_gov_type);
        govType.setText(mNation.govType);

        region = (TextView) view.findViewById(R.id.nation_region);
        region.setText(mNation.region);

        population = (TextView) view.findViewById(R.id.nation_population);
        String suffix = "million";
        double popHolder = mNation.popBase;
        if (mNation.popBase >= 1000D && mNation.popBase < 10000D)
        {
            suffix = "billion";
            popHolder /= 1000D;
        }
        else if (mNation.popBase >= 10000D)
        {
            suffix = "trillion";
            popHolder /= 1000000D;
        }

        population.setText(String.format("%s %s", NumberFormat.getInstance(Locale.US).format(popHolder).toString(), suffix));

        motto = (TextView) view.findViewById(R.id.nation_motto);
        motto.setText(mNation.motto);
    }

    private void initFreedomCards(View view)
    {
        civilRightsCard = (CardView) view.findViewById(R.id.card_overview_civrights);
        civilRightsDesc = (TextView) view.findViewById(R.id.overview_civrights);
        civilRightsPts = (TextView) view.findViewById(R.id.overview_civrights_pts);

        civilRightsDesc.setText(mNation.freedomDesc.civilRightsDesc);
        civilRightsPts.setText(String.valueOf(mNation.freedomPts.civilRightsPts));
        int civColInd = mNation.freedomPts.civilRightsPts / 10;
        civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[civColInd]));

        economyCard = (CardView) view.findViewById(R.id.card_overview_economy);
        economyDesc = (TextView) view.findViewById(R.id.overview_economy);
        economyPts = (TextView) view.findViewById(R.id.overview_economy_pts);

        economyDesc.setText(mNation.freedomDesc.economyDesc);
        economyPts.setText(String.valueOf(mNation.freedomPts.economyPts));
        int econColInd = mNation.freedomPts.economyPts / 10;
        economyCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[econColInd]));

        politicalCard = (CardView) view.findViewById(R.id.card_overview_polifree);
        politicalDesc = (TextView) view.findViewById(R.id.overview_polifree);
        politicalPts = (TextView) view.findViewById(R.id.overview_polifree_pts);

        politicalDesc.setText(mNation.freedomDesc.politicalDesc);
        politicalPts.setText(String.valueOf(mNation.freedomPts.politicalPts));
        int polColInd = mNation.freedomPts.politicalPts / 10;
        politicalCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[polColInd]));
    }

    private void initGovernmentCard(View view)
    {
        if (mNation.leader != null)
        {
            leader = (TextView) view.findViewById(R.id.nation_leader);
            leader.setText(mNation.leader);
        }
        else
        {
            leaderLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_leader);
            leaderLayout.setVisibility(View.GONE);
        }

        if (mNation.capital != null)
        {
            capital = (TextView) view.findViewById(R.id.nation_capital);
            capital.setText(mNation.capital);
        }
        else
        {
            capitalLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_capital);
            capitalLayout.setVisibility(View.GONE);
        }

        priority = (TextView) view.findViewById(R.id.nation_priority);
        priority.setText(mNation.govtPriority);

        tax = (TextView) view.findViewById(R.id.nation_tax);
        tax.setText(String.format("%.1f%%", mNation.tax));
    }

    private void initEconomyCard(View view)
    {
        currency = (TextView) view.findViewById(R.id.nation_currency);
        currency.setText(mNation.currency);

        gdp = (TextView) view.findViewById(R.id.nation_gdp);

        String suffix = "thousand";
        long gdpHolder = mNation.gdp;
        if (gdpHolder >= 1000000L && gdpHolder < 1000000000L)
        {
            suffix = "million";
            gdpHolder /= 1000000L;
        }
        else if (gdpHolder >= 1000000000L && gdpHolder < 1000000000000L)
        {
            suffix = "billion";
            gdpHolder /= 1000000000L;
        }
        else if (gdpHolder >= 1000000000000L)
        {
            suffix = "trillion";
            gdpHolder /= 1000000000000L;
        }

        gdp.setText(String.format("%s %s %ss", NumberFormat.getInstance(Locale.US).format(gdpHolder).toString(), suffix, mNation.currency));

        industry = (TextView) view.findViewById(R.id.nation_industry);
        industry.setText(mNation.industry);

        income = (TextView) view.findViewById(R.id.nation_income);
        income.setText(String.format("%s %ss", NumberFormat.getInstance(Locale.US).format(mNation.income).toString(), mNation.currency));
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
