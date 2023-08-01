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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
import com.lloydtorres.stately.dto.CensusNationRank;
import com.lloydtorres.stately.dto.CensusNationRankList;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-08.
 * Recycler adapter for TrendsActivity: contains census title/scale, graph and rankings.
 */
public class TrendsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for the different types of items
    private static final int TITLE_CARD = 0;
    private static final int GRAPH_CARD = 1;
    private static final int DIVIDER_VIEW = 2;
    private static final int RANKING_VIEW = 3;

    private final List<Object> trendItems;
    private final Context context;

    public TrendsRecyclerAdapter(Context c, int mode, int censusId, String title, String unit,
                                 CensusHistory censusData) {
        context = c;

        trendItems = new ArrayList<Object>();
        trendItems.add(new TrendsHeader(title, unit));
        // Don't show graph for Z-Day datasets
        if (censusId < TrendsActivity.CENSUS_ZDAY_SURVIVORS || censusId > TrendsActivity.CENSUS_ZDAY_ZOMBIFICATION) {
            trendItems.add(censusData.scale);
        }
        if (censusData.ranks != null) {
            trendItems.add(new TrendsRankTitle(mode, title));
            trendItems.addAll(censusData.ranks.ranks);
        }
    }

    public void addNewCensusNationRanks(CensusNationRankList rankList) {
        if (rankList.ranks != null && rankList.ranks.size() > 0) {
            int oldSize = trendItems.size();
            trendItems.addAll(rankList.ranks);
            notifyItemRangeInserted(oldSize, rankList.ranks.size());
        }
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
            case DIVIDER_VIEW:
                View rankTitleView = inflater.inflate(R.layout.view_census_trends_ranking_title,
                        parent, false);
                viewHolder = new RankTitleViewHolder(rankTitleView);
                break;
            case RANKING_VIEW:
                View nationRankView = inflater.inflate(R.layout.view_census_trends_ranking_entry,
                        parent, false);
                viewHolder = new NationRankViewHolder(nationRankView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TITLE_CARD:
                TitleCard titleCard = (TitleCard) holder;
                titleCard.init((TrendsHeader) trendItems.get(position));
                break;
            case GRAPH_CARD:
                GraphCard graphCard = (GraphCard) holder;
                graphCard.init((CensusHistoryScale) trendItems.get(position));
                break;
            case DIVIDER_VIEW:
                RankTitleViewHolder rankTitleViewHolder = (RankTitleViewHolder) holder;
                rankTitleViewHolder.init((TrendsRankTitle) trendItems.get(position));
                break;
            case RANKING_VIEW:
                NationRankViewHolder nationRankViewHolder = (NationRankViewHolder) holder;
                nationRankViewHolder.init((CensusNationRank) trendItems.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return trendItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (trendItems.get(position) instanceof TrendsHeader) {
            return TITLE_CARD;
        } else if (trendItems.get(position) instanceof CensusHistoryScale) {
            return GRAPH_CARD;
        } else if (trendItems.get(position) instanceof TrendsRankTitle) {
            return DIVIDER_VIEW;
        } else if (trendItems.get(position) instanceof CensusNationRank) {
            return RANKING_VIEW;
        }
        return -1;
    }

    // Holder for census title and unit header;
    private class TrendsHeader {
        public String title;
        public String unit;

        public TrendsHeader(String t, String u) {
            title = t;
            unit = u;
        }
    }

    // Holder for rankings type and census name
    private class TrendsRankTitle {
        public int mode;
        public String census;

        public TrendsRankTitle(int m, String c) {
            mode = m;
            census = c;
        }
    }

    // Card viewholders

    // Title card
    private class TitleCard extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView unit;

        public TitleCard(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.trends_title);
            unit = itemView.findViewById(R.id.trends_unit);
        }

        public void init(TrendsHeader header) {
            title.setText(header.title);
            unit.setText(header.unit);
        }
    }

    // Graph card
    private class GraphCard extends RecyclerView.ViewHolder implements OnChartValueSelectedListener {
        private CensusHistoryScale dataset;
        private final TextView date;
        private final TextView value;
        private final TextView max;
        private final TextView min;
        private final TextView avg;
        private LineChart chart;

        public GraphCard(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.trends_date);
            value = itemView.findViewById(R.id.trends_value);
            max = itemView.findViewById(R.id.trends_max);
            min = itemView.findViewById(R.id.trends_min);
            avg = itemView.findViewById(R.id.trends_avg);
            chart = itemView.findViewById(R.id.trends_chart);
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
            for (int i = 0; i < datapoints.size(); i++) {
                float value = datapoints.get(i).score;
                if (value > maxVal) {
                    maxVal = value;
                }
                if (value < minVal) {
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
            for (int i = 0; i < datapoints.size(); i++) {
                historyEntries.add(new Entry(i, datapoints.get(i).score));
            }

            // Formatting
            LineDataSet lineHistoryData = new LineDataSet(historyEntries, "");
            lineHistoryData.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineHistoryData.setColors(ContextCompat.getColor(context, R.color.colorChart0));
            lineHistoryData.setDrawValues(false);
            lineHistoryData.setDrawVerticalHighlightIndicator(true);
            lineHistoryData.setDrawHorizontalHighlightIndicator(false);
            lineHistoryData.setHighLightColor(RaraHelper.getThemeButtonColour(context));
            lineHistoryData.setHighlightLineWidth(lineWidth);
            lineHistoryData.setDrawCircles(false);
            lineHistoryData.setLineWidth(lineWidth);

            // Match data with x-axis labels
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(lineHistoryData);
            LineData dataFinal = new LineData(dataSets);
            List<String> xLabels = new ArrayList<String>();
            for (int i = 0; i < datapoints.size(); i++) {
                xLabels.add(String.format(Locale.US,
                        SparkleHelper.getMonthYearDateFromUTC(datapoints.get(i).timestamp), i));
            }

            // formatting
            boolean isLargeValue = maxVal >= 1000f;
            chart = RaraHelper.getFormattedLineChart(context, chart, this, xLabels, isLargeValue,
                    30, false, false);
            chart.setData(dataFinal);
            chart.invalidate();
        }

        /**
         * This resets the displayed data label to the most current one.
         */
        private void resetDataSelected() {
            List<CensusHistoryPoint> datapoints = dataset.points;
            if (datapoints != null && datapoints.size() > 0) {
                CensusHistoryPoint latest = datapoints.get(datapoints.size() - 1);
                setDataSelected(latest);
            }
        }

        /**
         * Sets the displayed data label based on the passed-in data point.
         * @param point
         */
        private void setDataSelected(CensusHistoryPoint point) {
            date.setText(SparkleHelper.getDateFromUTC(point.timestamp));
            value.setText(SparkleHelper.getPrettifiedNumber(point.score));
        }

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            CensusHistoryPoint selectedPoint = dataset.points.get((int) e.getX());
            setDataSelected(selectedPoint);
        }

        @Override
        public void onNothingSelected() {
            resetDataSelected();
        }
    }

    // Title at the beginning of rank list
    private class RankTitleViewHolder extends RecyclerView.ViewHolder {
        private TrendsRankTitle titleData;
        private final TextView type;
        private final TextView census;

        public RankTitleViewHolder(View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.trends_ranking_type);
            census = itemView.findViewById(R.id.trends_ranking_census);
        }

        public void init(TrendsRankTitle title) {
            titleData = title;

            switch (titleData.mode) {
                case TrendsActivity.TREND_REGION:
                    type.setText(context.getString(R.string.trends_regional));
                    break;
                case TrendsActivity.TREND_WORLD:
                    type.setText(context.getString(R.string.trends_world));
                    break;
            }

            census.setText(titleData.census);
        }
    }

    // Rank entry
    private class NationRankViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CensusNationRank rankData;
        private final TextView nation;
        private final TextView score;
        private final TextView rank;

        public NationRankViewHolder(View itemView) {
            super(itemView);
            nation = itemView.findViewById(R.id.trends_ranking_nation);
            score = itemView.findViewById(R.id.trends_ranking_score);
            rank = itemView.findViewById(R.id.trends_ranking_rank);
            itemView.setOnClickListener(this);
        }

        public void init(CensusNationRank r) {
            rankData = r;
            nation.setText(SparkleHelper.getNameFromId(rankData.name));
            score.setText(String.format(Locale.US,
                    context.getString(R.string.trends_score_template),
                    SparkleHelper.getPrettifiedNumber(rankData.score)));
            rank.setText(SparkleHelper.getPrettifiedNumber(rankData.rank));
        }

        @Override
        public void onClick(View view) {
            SparkleHelper.startExploring(context, rankData.name, ExploreActivity.EXPLORE_NATION);
        }
    }
}
