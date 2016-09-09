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

package com.lloydtorres.stately.census;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusHistory;
import com.lloydtorres.stately.dto.CensusHistoryPoint;
import com.lloydtorres.stately.dto.CensusHistoryScale;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-09-08.
 * Recycler adapter for TrendsActivity: contains census title/scale, graph and rankings.
 */
public class TrendsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of items
    private final int TITLE_CARD = 0;
    private final int GRAPH_CARD = 1;

    private List<Object> trendItems;
    private String[] WORLD_CENSUS_ITEMS;
    private Context context;

    public TrendsRecyclerAdapter(Context c, Integer id, CensusHistory censusData, String[] censusItems) {
        context = c;
        trendItems = new ArrayList<Object>();
        trendItems.add(id);
        trendItems.add(censusData.scale);
        WORLD_CENSUS_ITEMS = censusItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TITLE_CARD:
                View titleCard = inflater.inflate(R.layout.card_census_trends_title, parent, false);
                viewHolder = new TitleCard(titleCard);
                break;
            case GRAPH_CARD:
                View graphCard = inflater.inflate(R.layout.card_census_trends_graph, parent, false);
                viewHolder = new GraphCard(graphCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TITLE_CARD:
                TitleCard titleCard = (TitleCard) holder;
                titleCard.init((Integer) trendItems.get(position));
                break;
            case GRAPH_CARD:
                GraphCard graphCard = (GraphCard) holder;
                graphCard.init((CensusHistoryScale) trendItems.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return trendItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (trendItems.get(position) instanceof Integer) {
            return TITLE_CARD;
        }
        else if (trendItems.get(position) instanceof CensusHistoryScale) {
            return GRAPH_CARD;
        }
        return -1;
    }

    // Card viewholders

    // Title card
    public class TitleCard extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView unit;

        public TitleCard(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.trends_title);
            unit = (TextView) itemView.findViewById(R.id.trends_unit);
        }

        public void init(Integer id) {
            int censusId = id;
            if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
            {
                censusId = WORLD_CENSUS_ITEMS.length - 1;
            }
            String[] censusType = WORLD_CENSUS_ITEMS[censusId].split("##");
            title.setText(censusType[0]);
            unit.setText(censusType[1]);
        }
    }

    public class GraphCard extends RecyclerView.ViewHolder implements OnChartValueSelectedListener {
        private CensusHistoryScale dataset;
        private TextView date;
        private TextView value;
        private TextView max;
        private RelativeLayout maxHolder;
        private TextView min;
        private RelativeLayout minHolder;
        private TextView avg;
        private RelativeLayout avgHolder;
        private LineChart chart;

        public GraphCard(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.trends_date);
            value = (TextView) itemView.findViewById(R.id.trends_value);
            max = (TextView) itemView.findViewById(R.id.trends_max);
            maxHolder = (RelativeLayout) itemView.findViewById(R.id.trends_max_holder);
            min = (TextView) itemView.findViewById(R.id.trends_min);
            minHolder = (RelativeLayout) itemView.findViewById(R.id.trends_min_holder);
            avg = (TextView) itemView.findViewById(R.id.trends_avg);
            avgHolder = (RelativeLayout) itemView.findViewById(R.id.trends_avg_holder);
            chart = (LineChart) itemView.findViewById(R.id.trends_chart);
        }

        public void init(CensusHistoryScale scale) {
            dataset = scale;
            // Set selected indicator text to latest data
            resetDataSelected();

            // Calculate max, min, average
            List<CensusHistoryPoint> datapoints = dataset.points;

            float maxVal = Float.MIN_VALUE;
            float minVal = Float.MAX_VALUE;
            float total = 0;
            for (int i=0; i < datapoints.size(); i++)
            {
                float value = datapoints.get(i).score;
                if (value > maxVal)
                {
                    maxVal = value;
                }
                if (value < minVal)
                {
                    minVal = value;
                }
                total += value;
            }
            float avgVal = total / datapoints.size();

            max.setText(SparkleHelper.getPrettifiedNumber(maxVal));
            min.setText(SparkleHelper.getPrettifiedNumber(minVal));
            avg.setText(SparkleHelper.getPrettifiedNumber(avgVal));

            // Set up chart
            final float lineWidth = 2.5f;
            List<Entry> historyEntries = new ArrayList<Entry>();
            for (int i=0; i < datapoints.size(); i++)
            {
                historyEntries.add(new Entry(datapoints.get(i).score, i));
            }

            // Formatting
            LineDataSet lineHistoryData = new LineDataSet(historyEntries, "");
            lineHistoryData.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineHistoryData.setColors(SparkleHelper.waColourFor, context);
            lineHistoryData.setDrawValues(false);
            lineHistoryData.setDrawVerticalHighlightIndicator(true);
            lineHistoryData.setDrawHorizontalHighlightIndicator(false);
            lineHistoryData.setHighLightColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            lineHistoryData.setHighlightLineWidth(lineWidth);
            lineHistoryData.setDrawCircles(false);
            lineHistoryData.setLineWidth(lineWidth);

            // Match data with x-axis labels
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(lineHistoryData);
            List<String> xLabels = new ArrayList<String>();
            for (int i=0; i < datapoints.size(); i++)
            {
                xLabels.add(String.format(SparkleHelper.getDateNoYearFromUTC(datapoints.get(i).timestamp), i));
            }
            LineData dataFinal = new LineData(xLabels, dataSets);

            // formatting
            boolean isLargeValue = maxVal >= 1000f;
            chart = SparkleHelper.getFormattedLineChart(chart, this, isLargeValue, 6, false);
            chart.setData(dataFinal);
            chart.invalidate();
        }

        /**
         * This resets the displayed data label to the most current one.
         */
        private void resetDataSelected()
        {
            List<CensusHistoryPoint> datapoints = dataset.points;
            CensusHistoryPoint latest = datapoints.get(datapoints.size() - 1);
            setDataSelected(latest);
        }

        /**
         * Sets the displayed data label based on the passed-in data point.
         * @param point
         */
        private void setDataSelected(CensusHistoryPoint point)
        {
            date.setText(SparkleHelper.getDateNoYearFromUTC(point.timestamp));
            value.setText(SparkleHelper.getPrettifiedNumber(point.score));
        }

        @Override
        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
            CensusHistoryPoint selectedPoint = dataset.points.get(e.getXIndex());
            setDataSelected(selectedPoint);
        }

        @Override
        public void onNothingSelected() {
            resetDataSelected();
        }
    }
}
