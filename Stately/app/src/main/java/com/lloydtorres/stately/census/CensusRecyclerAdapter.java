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

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private static final String TOP_PERCENT_TEMPLATE = "%s%%";
    private static final String CENSUS_BLANK = "—";
    private static final int CARD_BUTTON = 0;
    private static final int CARD_CENSUS = 1;
    private static final Comparator<CensusDetailedRank> SORT_SCORE =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    if (lhs.score < rhs.score) {
                        return -1;
                    } else if (lhs.score > rhs.score) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
    private static final Comparator<CensusDetailedRank> SORT_WORLD_RANK =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    return lhs.worldRank - rhs.worldRank;
                }
            };
    private static final Comparator<CensusDetailedRank> SORT_WORLD_PERCENT =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    if (lhs.worldRankPercent < rhs.worldRankPercent) {
                        return -1;
                    } else if (lhs.worldRankPercent > rhs.worldRankPercent) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
    private static final Comparator<CensusDetailedRank> SORT_REGION_RANK =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    return lhs.regionRank - rhs.regionRank;
                }
            };
    private static final Comparator<CensusDetailedRank> SORT_REGION_PERCENT =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    if (lhs.regionRankPercent < rhs.regionRankPercent) {
                        return -1;
                    } else if (lhs.regionRankPercent > rhs.regionRankPercent) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
    private static final Comparator<CensusDetailedRank> SORT_ALPHABETICAL =
            new Comparator<CensusDetailedRank>() {
                @Override
                public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                    return lhs.name.compareTo(rhs.name);
                }
            };
    private LinkedHashMap<Integer, CensusScale> censusScales;
    private final Context context;
    private final CensusSubFragment fragment;
    private ArrayList<CensusDetailedRank> censusData;
    private int sortOrder = SORT_MODE_WORLD_PERCENT;
    private boolean isAlphabetical = false;
    private boolean isAscending = true;
    private final String target;
    private final int mode;
    private final int TWO_DP_IN_PIXELS;

    public CensusRecyclerAdapter(CensusSubFragment c, ArrayList<CensusDetailedRank> cen,
                                 String t, int m) {
        context = c.getContext();
        fragment = c;
        String[] WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
        censusScales = SparkleHelper.getCensusScales(WORLD_CENSUS_ITEMS);

        target = t;
        mode = m;

        if (!(NightmareHelper.getIsZDayActive(context)
                && mode == CensusSortDialog.CENSUS_MODE_REGION)) {
            censusScales = NightmareHelper.trimZDayCensusDatasets(censusScales);
        }

        float dpScale = context.getResources().getDisplayMetrics().density;
        TWO_DP_IN_PIXELS = (int) (2 * dpScale + 0.5f);

        setCensusData(cen);
    }

    public void setCensusData(ArrayList<CensusDetailedRank> cen) {
        Collections.sort(cen, getSort());
        censusData = cen;
        notifyDataSetChanged();
    }

    private String getSortLabel() {
        String censusLabel = context.getString(isAlphabetical ?
                R.string.census_sort_label_alphabetical : R.string.census_sort_label);

        String censusType = context.getString(R.string.census_sort_score);
        switch (sortOrder) {
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

        String censusOrder = context.getString(isAscending ? R.string.census_sort_ascending :
                R.string.census_sort_descending);
        censusOrder = censusOrder.toLowerCase(Locale.US);

        if (isAlphabetical) {
            censusType = censusType.toLowerCase(Locale.US);
            return String.format(Locale.US, censusLabel, censusOrder, censusType);
        }

        return String.format(Locale.US, censusLabel, censusType, censusOrder);
    }

    private Comparator<CensusDetailedRank> getSort() {
        if (isAlphabetical) {
            return isAscending ? SORT_ALPHABETICAL : sortDescending(SORT_ALPHABETICAL);
        }

        Comparator<CensusDetailedRank> comparator = SORT_SCORE;
        switch (sortOrder) {
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
     * @param isAlph True if alphabetical order should be forced, false otherwise
     * @param a True if ascending, false if descending
     */
    public void sort(int criteria, boolean isAlph, boolean a) {
        sortOrder = criteria;
        isAlphabetical = isAlph;
        isAscending = a;
        Collections.sort(censusData, getSort());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == CARD_BUTTON) {
            View buttonCard = inflater.inflate(R.layout.card_button, parent, false);
            viewHolder = new SortButtonCard(buttonCard);
        } else {
            View censusCard = inflater.inflate(R.layout.card_census_delta, parent, false);
            viewHolder = new CensusCard(censusCard);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == CARD_BUTTON) {
            SortButtonCard sortButtonCard = (SortButtonCard) holder;
            sortButtonCard.init();
        } else {
            CensusCard censusCard = (CensusCard) holder;
            censusCard.init(censusData.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return censusData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? CARD_BUTTON : CARD_CENSUS;
    }

    /**
     * Comparators for CensusDetailedRank.
     */

    private Comparator<CensusDetailedRank> sortDescending(
            final Comparator<CensusDetailedRank> other) {
        return new Comparator<CensusDetailedRank>() {
            public int compare(CensusDetailedRank lhs, CensusDetailedRank rhs) {
                return -1 * other.compare(lhs, rhs);
            }
        };
    }

    /**
     * View holders.
     */

    public class SortButtonCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView buttonText;

        public SortButtonCard(View v) {
            super(v);
            buttonText = v.findViewById(R.id.card_button_text);
            v.setOnClickListener(this);
        }

        public void init() {
            // Forces card to span across columns
            RaraHelper.setViewHolderFullSpan(itemView);

            buttonText.setText(getSortLabel());
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = fragment.getParentFragmentManager();
            CensusSortDialog censusSortDialog = new CensusSortDialog();
            censusSortDialog.setMode(mode);
            censusSortDialog.setSortOrder(sortOrder);
            censusSortDialog.setIsAlphabetical(isAlphabetical);
            censusSortDialog.setIsAscending(isAscending);
            censusSortDialog.setAdapter(CensusRecyclerAdapter.this);
            censusSortDialog.show(fm, CensusSortDialog.DIALOG_TAG);
        }
    }

    public class CensusCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CensusDetailedRank censusData;

        private final CardView cardHolder;
        private final TextView title;
        private final TextView unit;
        private final TextView superScript;
        private final TextView value;

        public CensusCard(View v) {
            super(v);
            cardHolder = v.findViewById(R.id.card_census_delta_main);
            title = v.findViewById(R.id.card_delta_name);
            unit = v.findViewById(R.id.card_delta_unit);
            superScript = v.findViewById(R.id.card_delta_superscript);
            value = v.findViewById(R.id.card_delta_value);
            v.setOnClickListener(this);
        }

        public void init(CensusDetailedRank data) {
            censusData = data;

            int censusColorIndex;
            if (sortOrder == SORT_MODE_SCORE ||
                    sortOrder == SORT_MODE_WORLD_RANK || sortOrder == SORT_MODE_WORLD_PERCENT) {
                censusColorIndex = (int) (data.worldRankPercent / 7);
            } else {
                censusColorIndex = (int) (data.regionRankPercent / 7);
            }
            censusColorIndex = (RaraHelper.freedomColours.length - 1) - censusColorIndex;
            // Sanity checks
            censusColorIndex = Math.min(Math.max(censusColorIndex, 0),
                    (RaraHelper.freedomColours.length - 1));
            cardHolder.setCardBackgroundColor(ContextCompat.getColor(context,
                    RaraHelper.freedomColours[censusColorIndex]));

            CensusScale censusType = SparkleHelper.getCensusScale(censusScales, censusData.id);
            title.setText(censusType.name);
            unit.setText(censusType.unit);

            switch (sortOrder) {
                case SORT_MODE_SCORE:
                    superScript.setVisibility(View.GONE);
                    value.setText(SparkleHelper.getPrettifiedShortSuffixedNumber(context,
                            censusData.score));
                    break;
                case SORT_MODE_WORLD_RANK:
                    if (censusData.worldRank <= 0) {
                        superScript.setVisibility(View.GONE);
                        value.setText(CENSUS_BLANK);
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context,
                                R.color.colorSecondaryText));
                    } else {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS, 0);
                        superScript.setText(context.getString(R.string.census_hash_symbol));
                        value.setText(SparkleHelper.getPrettifiedNumber(censusData.worldRank));
                    }
                    break;
                case SORT_MODE_WORLD_PERCENT:
                    if (censusData.worldRankPercent <= 0) {
                        superScript.setVisibility(View.GONE);
                        value.setText(CENSUS_BLANK);
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context,
                                R.color.colorSecondaryText));
                    } else {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS * 2, 0);
                        superScript.setText(context.getString(R.string.census_top));
                        value.setText(String.format(Locale.US, TOP_PERCENT_TEMPLATE,
                                SparkleHelper.getPrettifiedNumber(censusData.worldRankPercent, 3)));
                    }
                    break;
                case SORT_MODE_REGION_RANK:
                    if (censusData.regionRank <= 0) {
                        superScript.setVisibility(View.GONE);
                        value.setText(CENSUS_BLANK);
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context,
                                R.color.colorSecondaryText));
                    } else {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS, 0);
                        superScript.setText(context.getString(R.string.census_hash_symbol));
                        value.setText(SparkleHelper.getPrettifiedNumber(censusData.regionRank));
                    }
                    break;
                case SORT_MODE_REGION_PERCENT:
                    if (censusData.regionRankPercent <= 0) {
                        superScript.setVisibility(View.GONE);
                        value.setText(CENSUS_BLANK);
                        cardHolder.setCardBackgroundColor(ContextCompat.getColor(context,
                                R.color.colorSecondaryText));
                    } else {
                        superScript.setVisibility(View.VISIBLE);
                        superScript.setPadding(0, 0, TWO_DP_IN_PIXELS * 2, 0);
                        superScript.setText(context.getString(R.string.census_top));
                        value.setText(String.format(Locale.US, TOP_PERCENT_TEMPLATE,
                                SparkleHelper.getPrettifiedNumber(censusData.regionRankPercent,
                                        3)));
                    }
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            int newMode = mode == CensusSortDialog.CENSUS_MODE_NATION ?
                    TrendsActivity.TREND_NATION : TrendsActivity.TREND_REGION;
            SparkleHelper.startTrends(context, target, newMode, censusData.id);
        }
    }
}
