package com.lloydtorres.stately;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.atteo.evo.inflector.English;
import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class OverviewFragment extends Fragment {
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
                                            R.color.colorFreedom10,
                                            R.color.colorFreedom11,
                                            R.color.colorFreedom12,
                                            R.color.colorFreedom13,
                                            R.color.colorFreedom14
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
        String suffix = getString(R.string.million);
        double popHolder = mNation.popBase;
        if (mNation.popBase >= 1000D && mNation.popBase < 10000D)
        {
            suffix = getString(R.string.billion);
            popHolder /= 1000D;
        }
        else if (mNation.popBase >= 10000D)
        {
            suffix = getString(R.string.trillion);
            popHolder /= 1000000D;
        }

        population.setText(String.format(getString(R.string.val_currency), NumberFormat.getInstance(Locale.US).format(popHolder).toString(), suffix));

        motto = (TextView) view.findViewById(R.id.nation_motto);
        motto.setText(Html.fromHtml(mNation.motto).toString());
    }

    private void initFreedomCards(View view)
    {
        civilRightsCard = (CardView) view.findViewById(R.id.card_overview_civrights);
        civilRightsDesc = (TextView) view.findViewById(R.id.overview_civrights);
        civilRightsPts = (TextView) view.findViewById(R.id.overview_civrights_pts);

        civilRightsDesc.setText(mNation.freedomDesc.civilRightsDesc);
        civilRightsPts.setText(String.valueOf(mNation.freedomPts.civilRightsPts));
        int civColInd = mNation.freedomPts.civilRightsPts / 7;
        civilRightsCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[civColInd]));

        economyCard = (CardView) view.findViewById(R.id.card_overview_economy);
        economyDesc = (TextView) view.findViewById(R.id.overview_economy);
        economyPts = (TextView) view.findViewById(R.id.overview_economy_pts);

        economyDesc.setText(mNation.freedomDesc.economyDesc);
        economyPts.setText(String.valueOf(mNation.freedomPts.economyPts));
        int econColInd = mNation.freedomPts.economyPts / 7;
        economyCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[econColInd]));

        politicalCard = (CardView) view.findViewById(R.id.card_overview_polifree);
        politicalDesc = (TextView) view.findViewById(R.id.overview_polifree);
        politicalPts = (TextView) view.findViewById(R.id.overview_polifree_pts);

        politicalDesc.setText(mNation.freedomDesc.politicalDesc);
        politicalPts.setText(String.valueOf(mNation.freedomPts.politicalPts));
        int polColInd = mNation.freedomPts.politicalPts / 7;
        politicalCard.setCardBackgroundColor(ContextCompat.getColor(getContext(), freedomColours[polColInd]));
    }

    private void initGovernmentCard(View view)
    {
        if (mNation.leader != null)
        {
            leader = (TextView) view.findViewById(R.id.nation_leader);
            leader.setText(Html.fromHtml(mNation.leader).toString());
        }
        else
        {
            leaderLayout = (LinearLayout) view.findViewById(R.id.card_overview_gov_leader);
            leaderLayout.setVisibility(View.GONE);
        }

        if (mNation.capital != null)
        {
            capital = (TextView) view.findViewById(R.id.nation_capital);
            capital.setText(Html.fromHtml(mNation.capital).toString());
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

        String suffix = getString(R.string.thousand);
        long gdpHolder = mNation.gdp;
        if (gdpHolder >= 1000000L && gdpHolder < 1000000000L)
        {
            suffix = getString(R.string.million);
            gdpHolder /= 1000000L;
        }
        else if (gdpHolder >= 1000000000L && gdpHolder < 1000000000000L)
        {
            suffix = getString(R.string.billion);
            gdpHolder /= 1000000000L;
        }
        else if (gdpHolder >= 1000000000000L)
        {
            suffix = getString(R.string.trillion);
            gdpHolder /= 1000000000000L;
        }

        gdp.setText(String.format(getString(R.string.val_suffix_currency), NumberFormat.getInstance(Locale.US).format(gdpHolder).toString(), suffix, English.plural(mNation.currency)));

        industry = (TextView) view.findViewById(R.id.nation_industry);
        industry.setText(mNation.industry);

        income = (TextView) view.findViewById(R.id.nation_income);
        income.setText(String.format(getString(R.string.val_currency), NumberFormat.getInstance(Locale.US).format(mNation.income).toString(), English.plural(mNation.currency)));
    }

    private void initOtherCard(View view)
    {
        demonym = (TextView) view.findViewById(R.id.nation_demonym);
        if (mNation.demAdjective.equals(mNation.demNoun))
        {
            demonym.setText(Html.fromHtml(String.format(getString(R.string.card_overview_other_demonym_txt2), mNation.demNoun, mNation.demPlural)).toString());
        }
        else
        {
            demonym.setText(Html.fromHtml(String.format(getString(R.string.card_overview_other_demonym_txt1), mNation.demNoun, mNation.demPlural, mNation.demAdjective)).toString());
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
