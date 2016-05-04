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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-04-09.
 * This recycler is used to display census ranking data in an order specified by the user,
 * along with a button at the top to open a dialog for sorting.
 */
public class CensusRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int SORT_MODE_SCORE = 0;
    public static final int SORT_MODE_WORLD_RANK = 1;
    public static final int SORT_MODE_WORLD_PERCENT = 2;
    public static final int SORT_MODE_REGION_RANK = 3;
    public static final int SORT_MODE_REGION_PERCENT = 4;

    private static final int CARD_BUTTON = 0;
    private static final int CARD_CENSUS = 1;

    private String[] WORLD_CENSUS_ITEMS;

    private Context context;
    private CensusSubFragment fragment;
    private ArrayList<CensusDetailedRank> censusData;
    private int sortOrder = SORT_MODE_WORLD_PERCENT;
    private boolean isAscending = true;
    private String target;
    private int mode;

    private int TWO_DP_IN_PIXELS;

    public CensusRecyclerAdapter(CensusSubFragment c, ArrayList<CensusDetailedRank> cen, String t, int m)
    {
        context = c.getContext();
        fragment = c;
        WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);

        Collections.sort(cen, getSort());
        censusData = cen;

        target = t;
        mode = m;

        float dpScale = context.getResources().getDisplayMetrics().density;
        TWO_DP_IN_PIXELS = (int) (2*dpScale + 0.5f);
    }

    private String getSortLabel()
    {
        String censusLabel = context.getString(R.string.census_sort_label);

        String censusType = context.getString(R.string.census_sort_score);
        switch (sortOrder)
        {
            case SORT_MODE_WORLD_RANK:
                censusType = context.getString(R.string.census_sort_world_rank);
                break;
            case SORT_MODE_WORLD_PERCENT:
                censusType = context.getString(R.string.census_sort_world_percent);
                break;
            case SORT_MODE_REGION_RANK:
                censusType = context.getString(R.string.census_sort_region_rank);
                break;
            case SORT_MODE_REGION_PERCENT:
                censusType = context.getString(R.string.census_sort_region_percent);
                break;
        }

        String censusOrder = context.getString(isAscending ? R.string.census_sort_ascending : R.string.census_sort_descending);
        censusOrder = censusOrder.toLowerCase(Locale.US);

        return String.format(censusLabel, censusType, censusOrder);
    }

    private Comparator<CensusDetailedRank> getSort()
    {
        Comparator<CensusDetailedRank> comparator = SORT_SCORE;
        switch (sortOrder)
        {
            case SORT_MODE_WORLD_RANK:
                comparator = SORT_WORLD_RANK;
                break;
            case SORT_MODE_WORLD_PERCENT:
                comparator = SORT_WORLD_PERCENT;
                break;
            case SORT_MODE_REGION_RANK:
                comparator = SORT_REGION_RANK;
                break;
            case SORT_MODE_REGION_PERCENT:
                comparator = SORT_REGION_PERCENT;
                break;
        }

        return isAscending ? comparator : sortDescending(comparator);
    }

    /**
     * Sorts the list of census data based on the passed-in criteria and direction.
     * @param criteria Sort order (as specified by the constants)
     * @param a True if ascending, false if descending
     */
    public void sort(int criteria, boolean a)
    {
        sortOrder = criteria;
        isAscending = a;
        Collections.sort(censusData, getSort());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType)
        {
            case CARD_BUTTON:
                View buttonCard = inflater.inflate(R.layout.card_button, parent, false);
                viewHolder =  new SortButtonCard(buttonCard);
                break;
            default:
                View censusCard = inflater.inflate(R.layout.card_census_delta, parent, false);
                viewHolder =  new CensusCard(censusCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType())
        {
            case CARD_BUTTON:
                SortButtonCard sortButtonCard = (SortButtonCard) holder;
                sortButtonCard.init();
                break;
            default:
                CensusCard censusCard = (CensusCard) holder;
                censusCard.init(censusData.get(position - 1));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return censusData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? CARD_BUTTON : CARD_CENSUS;
    }

    /**
     * Comparators for CensusDetailedRank.
     */

    private Comparator<CensusDetailedRank> sortDescending(final Comparator<CensusDetailedRank> other) {
        return new Comparator<CensusDetailedRank>() {
            public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                return -1 * other.compare(lhs, rhs);
            }
        };
    }

    private final Comparator<CensusDetailedRank> SORT_SCORE = new Comparator<CensusDetailedRank>() {
        @Override
        public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
            if (lhs.score < rhs.score)
            {
                return -1;
            }
            else if (lhs.score > rhs.score)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    };

    private final Comparator<CensusDetailedRank> SORT_WORLD_RANK = new Comparator<CensusDetailedRank>() {
        @Override
        public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
            return lhs.worldRank - rhs.worldRank;
        }
    };

    private final Comparator<CensusDetailedRank> SORT_WORLD_PERCENT = new Comparator<CensusDetailedRank>() {
        @Override
        public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
            if (lhs.worldRankPercent < rhs.worldRankPercent)
            {
                return -1;
            }
            else if (lhs.worldRankPercent > rhs.worldRankPercent)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    };

    private final Comparator<CensusDetailedRank> SORT_REGION_RANK = new Comparator<CensusDetailedRank>() {
        @Override
        public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
            return lhs.regionRank - rhs.regionRank;
        }
    };

    private final Comparator<CensusDetailedRank> SORT_REGION_PERCENT = new Comparator<CensusDetailedRank>() {
        @Override
        public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
            if (lhs.regionRankPercent < rhs.regionRankPercent)
            {
                return -1;
            }
            else if (lhs.regionRankPercent > rhs.regionRankPercent)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    };

    /**
     * View holders.
     */

    public class SortButtonCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView buttonText;

        public SortButtonCard(View v)
        {
            super(v);
            buttonText = (TextView) v.findViewById(R.id.card_button_text);
            v.setOnClickListener(this);
        }

        public void init()
        {
            buttonText.setText(getSortLabel());
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
            CensusSortDialog censusSortDialog = new CensusSortDialog();
            censusSortDialog.setMode(mode);
            censusSortDialog.setSortOrder(sortOrder);
            censusSortDialog.setIsAscending(isAscending);
            censusSortDialog.setAdapter(CensusRecyclerAdapter.this);
            censusSortDialog.show(fm, CensusSortDialog.DIALOG_TAG);
        }
    }

    public class CensusCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CensusDetailedRank censusData;

        private CardView cardHolder;
        private TextView title;
        private TextView unit;
        private TextView superScript;
        private TextView value;

        public CensusCard(View v)
        {
            super(v);
            cardHolder = (CardView) v.findViewById(R.id.card_census_delta_main);
            title = (TextView) v.findViewById(R.id.card_delta_name);
            unit = (TextView) v.findViewById(R.id.card_delta_unit);
            superScript = (TextView) v.findViewById(R.id.card_delta_superscript);
            value = (TextView) v.findViewById(R.id.card_delta_value);
            v.setOnClickListener(this);
        }

        public void init(CensusDetailedRank data)
        {
            censusData = data;

            int censusColorIndex;
            if (sortOrder == SORT_MODE_SCORE || sortOrder == SORT_MODE_WORLD_RANK || sortOrder == SORT_MODE_WORLD_PERCENT)
            {
                censusColorIndex = (int) (data.worldRankPercent / 7);
            }
            else
            {
                censusColorIndex = (int) (data.regionRankPercent / 7);
            }
            censusColorIndex = (SparkleHelper.freedomColours.length - 1) - censusColorIndex;
            // Sanity checks
            censusColorIndex = Math.min(Math.max(censusColorIndex, 0), (SparkleHelper.freedomColours.length - 1));
            cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, SparkleHelper.freedomColours[censusColorIndex]));

            int censusId = censusData.id;
            // if census ID is out of bounds, set it as unknown
            if (censusId >= WORLD_CENSUS_ITEMS.length - 1)
            {
                censusId = WORLD_CENSUS_ITEMS.length - 1;
            }
            String[] censusType = WORLD_CENSUS_ITEMS[censusId].split("##");
            title.setText(censusType[0]);
            unit.setText(censusType[1]);

            switch (sortOrder)
            {
                case SORT_MODE_SCORE:
                    superScript.setVisibility(View.GONE);
                    value.setText(SparkleHelper.getPrettifiedNumber(censusData.score));
                    break;
                case SORT_MODE_WORLD_RANK:
                    if (censusData.worldRank <= 0)
                    {
                        superScript.setVisibility(View.GONE);
                        value.setText(context.getString(R.string.census_blank));
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    }
                    else
                    {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS, 0);
                        superScript.setText(context.getString(R.string.census_hash_symbol));
                        value.setText(SparkleHelper.getPrettifiedNumber(censusData.worldRank));
                    }
                    break;
                case SORT_MODE_WORLD_PERCENT:
                    if (censusData.worldRankPercent <= 0)
                    {
                        superScript.setVisibility(View.GONE);
                        value.setText(context.getString(R.string.census_blank));
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    }
                    else
                    {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS*2, 0);
                        superScript.setText(context.getString(R.string.census_top));
                        value.setText(String.format(context.getString(R.string.census_percent), SparkleHelper.getPrettifiedNumber(censusData.worldRankPercent)));
                    }
                    break;
                case SORT_MODE_REGION_RANK:
                    if (censusData.regionRank <= 0)
                    {
                        superScript.setVisibility(View.GONE);
                        value.setText(context.getString(R.string.census_blank));
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    }
                    else
                    {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS, 0);
                        superScript.setText(context.getString(R.string.census_hash_symbol));
                        value.setText(SparkleHelper.getPrettifiedNumber(censusData.regionRank));
                    }
                    break;
                case SORT_MODE_REGION_PERCENT:
                    if (censusData.regionRankPercent <= 0)
                    {
                        superScript.setVisibility(View.GONE);
                        value.setText(context.getString(R.string.census_blank));
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    }
                    else
                    {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS*2, 0);
                        superScript.setText(context.getString(R.string.census_top));
                        value.setText(String.format(context.getString(R.string.census_percent), SparkleHelper.getPrettifiedNumber(censusData.regionRankPercent)));
                    }
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            int newMode = mode == CensusSortDialog.CENSUS_MODE_NATION ? TrendsActivity.TREND_NATION : TrendsActivity.TREND_REGION;
            SparkleHelper.startTrends(context, target, newMode, censusData.id);
        }
    }
}
