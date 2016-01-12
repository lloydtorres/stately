package com.lloydtorres.stately;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-10.
 */
public class OverviewFragment extends Fragment {
    private static final String OVERVIEW_KEY = "OVERVIEW_KEY";
    private Nation mNation;

    private TextView govType;
    private TextView region;
    private TextView motto;

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
            govType = (TextView) view.findViewById(R.id.nation_gov_type);
            region = (TextView) view.findViewById(R.id.nation_region);
            motto = (TextView) view.findViewById(R.id.nation_motto);

            govType.setText(mNation.govType);
            region.setText(mNation.region);
            motto.setText(mNation.motto);
        }

        return view;
    }
}
