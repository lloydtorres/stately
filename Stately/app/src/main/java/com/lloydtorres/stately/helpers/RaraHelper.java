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

package com.lloydtorres.stately.helpers;

/*
                                                .... ,.----..
                                         __  .-`  / /      _ `-.
                                        /  \`    ( (     ,'/    `.
                                       ( /  \__   `-`  .' /..-'`'-.
                                      /| |     `--___S'  /         )
                                     A ( '.   ______    '      .-'`
                                    | \ \    |    __\  / `:  .'(  __
            .--:---,                |  :(\_  l   / .-) |'.|\/   `(_ \
          .'  /     `-.             |   :     \ ( (WW| \W)j `.______/
        .'   |         `.           |    '.    \_\_`_|  ``-.
        |   .'           `,         \      :           \__/
       .'   |              \         \      '. -,______.-'
       |    |       .--.    __________`.__    '.  /
       |    |     .'    `.-'          ./ _)    | (
       |     `.  |      /    A        |\______-|  \
       '.      '.'     |  A  V        |        |   |
        |        '.___.|  V    A      |        |   |
        |            / |       V      \_______/    |
         :.         / / \        /             \__/
  ___  .`  '.      (-'   |      /-,_______\       \
 / _ \'|  r` '-.    \  _/      /     |    |\       \
( (__/ |  |    \'-.__\/       /     |     | `--,    \
 \____/|  l     |\    |      |      |     |   /      )
       '   `.__/  \__/|      |      |      | (       |
        `----'        |      |      |      |  \      |
                      |       \     |       \  `.___/
                       \_______)     \_______)
 */

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.zombie.NightmareHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-30.
 * A collection of helper functions used to get theme and styling data across Stately.
 */

public final class RaraHelper {

    // Empty chart description
    public static final Description EMPTY_CHART_DESCRIPTION = new Description();
    // An array of chart colours
    public static final int[] chartColours = {R.color.colorChart0,
            R.color.colorChart1,
            R.color.colorChart2,
            R.color.colorChart3,
            R.color.colorChart4,
            R.color.colorChart5,
            R.color.colorChart6,
            R.color.colorChart7,
            R.color.colorChart8,
            R.color.colorChart9,
            R.color.colorChart10,
            R.color.colorChart11,
            R.color.colorChart12,
            R.color.colorChart13,
            R.color.colorChart14,
            R.color.colorChart15,
            R.color.colorChart16,
            R.color.colorChart17,
            R.color.colorChart18,
            R.color.colorChart19,
            R.color.colorChart20,
            R.color.colorChart21,
            R.color.colorChart22
    };
    // An array of colours used for the freedom scale
    public static final int[] freedomColours = {R.color.colorFreedom0,
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
    public static final int[] refreshColoursVert = {R.color.colorPrimary,
            R.color.colorPrimaryDark, R.color.colorAccent};
    public static final int[] refreshColoursNoir = {R.color.colorPrimaryNoir,
            R.color.colorPrimaryDarkNoir, R.color.colorAccentNoir};

    /**
     * THEMES
     * These are functions used for getting theme-specific colours.
     */
    public static final int[] refreshColoursBleu = {R.color.colorPrimaryBleu,
            R.color.colorPrimaryDarkBleu, R.color.colorAccentBleu};
    public static final int[] refreshColoursRouge = {R.color.colorPrimaryRouge,
            R.color.colorPrimaryDarkRouge, R.color.colorAccentRouge};
    public static final int[] refreshColoursViolet = {R.color.colorPrimaryViolet,
            R.color.colorPrimaryDarkViolet, R.color.colorAccentViolet};
    // String template used to get nation banners from NationStates
    // @param: banner_id
    public static final String BANNER_TEMPLATE = SparkleHelper.BASE_URI_NOSLASH + "/images" +
            "/banners/%s.jpg";
    public static final int DAY_NORMAL = -1;
    public static final int DAY_Z_DAY = -2;
    public static final int DAY_NEW_YEAR = 11;
    public static final int DAY_STATELY_BIRTHDAY = 130;
    public static final int DAY_APRIL_FOOLS = 41;
    public static final int DAY_CANADA_DAY = 701;
    public static final int DAY_HALLOWEEN = 1031;
    public static final int DAY_NS_BIRTHDAY = 1113;

    /**
     * CHARTS
     * These are functions used to style various charts
     */
    public static final int NS_FOUNDATION_YEAR = 2002;

    static {
        EMPTY_CHART_DESCRIPTION.setText("");
    }

    /**
     * UTILITIES
     * These are helper functions for dealing with different styling issues.
     */

    // Private constructor
    private RaraHelper() {
    }

    /**
     * Gets the primary colour for the current theme.
     * @param c Context
     * @return ColorInt
     */
    public static int getThemePrimaryColour(Context c) {
        int linkColor = R.color.colorPrimary;
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                linkColor = R.color.colorPrimary;
                break;
            case SettingsActivity.THEME_NOIR:
                linkColor = R.color.colorPrimaryNoir;
                break;
            case SettingsActivity.THEME_BLEU:
                linkColor = R.color.colorPrimaryBleu;
                break;
            case SettingsActivity.THEME_ROUGE:
                linkColor = R.color.colorPrimaryRouge;
                break;
            case SettingsActivity.THEME_VIOLET:
                linkColor = R.color.colorPrimaryViolet;
                break;
        }
        return ContextCompat.getColor(c, linkColor);
    }

    /**
     * Gets the card colour for the current theme.
     * @param c Context
     * @return ColorInt
     */
    public static int getThemeCardColour(Context c) {
        int linkColor = R.color.white;
        if (SettingsActivity.getTheme(c) == SettingsActivity.THEME_NOIR) {
            linkColor = R.color.colorPrimaryNoir;
        }
        return ContextCompat.getColor(c, linkColor);
    }

    /**
     * Gets the colours to use for card buttons.
     * @param c Context
     * @return ColorInt
     */
    public static int getThemeButtonColour(Context c) {
        int linkColor = R.color.colorPrimary;
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                linkColor = R.color.colorPrimary;
                break;
            case SettingsActivity.THEME_NOIR:
                linkColor = R.color.colorPrimaryTextNoir;
                break;
            case SettingsActivity.THEME_BLEU:
                linkColor = R.color.colorPrimaryBleu;
                break;
            case SettingsActivity.THEME_ROUGE:
                linkColor = R.color.colorPrimaryRouge;
                break;
            case SettingsActivity.THEME_VIOLET:
                linkColor = R.color.colorPrimaryViolet;
                break;
        }
        return ContextCompat.getColor(c, linkColor);
    }

    /**
     * Gets the colours to use for links.
     * @param c Context
     * @return ColorInt
     */
    public static int getThemeLinkColour(Context c) {
        int linkColor = R.color.colorAccent;
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                linkColor = R.color.colorAccent;
                break;
            case SettingsActivity.THEME_NOIR:
                linkColor = R.color.colorLinkTextNoir;
                break;
            case SettingsActivity.THEME_BLEU:
                linkColor = R.color.colorAccentBleu;
                break;
            case SettingsActivity.THEME_ROUGE:
                linkColor = R.color.colorAccentRouge;
                break;
            case SettingsActivity.THEME_VIOLET:
                linkColor = R.color.colorAccentViolet;
                break;
        }
        return ContextCompat.getColor(c, linkColor);
    }

    /**
     * Gets swipe refresh colours for the current theme.
     * @param c Context
     * @return
     */
    public static int[] getThemeRefreshColours(Context c) {
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                return refreshColoursVert;
            case SettingsActivity.THEME_NOIR:
                return refreshColoursNoir;
            case SettingsActivity.THEME_BLEU:
                return refreshColoursBleu;
            case SettingsActivity.THEME_ROUGE:
                return refreshColoursRouge;
            case SettingsActivity.THEME_VIOLET:
                return refreshColoursViolet;
            default:
                return refreshColoursVert;
        }
    }

    /**
     * Gets the theme for the older MaterialDialogs for the current theme.
     * @param c App context.
     * @return Theme ID for the dialog.
     */
    public static int getThemeMaterialDialog(Context c) {
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                return R.style.MaterialDialog;
            case SettingsActivity.THEME_NOIR:
                return R.style.MaterialDialogNoir;
            case SettingsActivity.THEME_BLEU:
                return R.style.MaterialDialogBleu;
            case SettingsActivity.THEME_ROUGE:
                return R.style.MaterialDialogRouge;
            case SettingsActivity.THEME_VIOLET:
                return R.style.MaterialDialogViolet;
            default:
                return R.style.MaterialDialog;
        }
    }

    /**
     * Gets the theme for the newer Lollipop AlertDialogs for the current theme.
     * @param c App context.
     * @return Theme ID for the dialog.
     */
    public static int getThemeLollipopDialog(Context c) {
        switch (SettingsActivity.getTheme(c)) {
            case SettingsActivity.THEME_VERT:
                return R.style.AlertDialogCustom;
            case SettingsActivity.THEME_NOIR:
                return R.style.AlertDialogCustomNoir;
            case SettingsActivity.THEME_BLEU:
                return R.style.AlertDialogCustomBleu;
            case SettingsActivity.THEME_ROUGE:
                return R.style.AlertDialogCustomRouge;
            case SettingsActivity.THEME_VIOLET:
                return R.style.AlertDialogCustomViolet;
            default:
                return R.style.AlertDialogCustom;
        }
    }

    /**
     * Formats a pie chart in a standardized way
     * @param c Context
     * @param p Pie chart
     * @param shouldShowLegend
     * @return the PieChart, whose data must be set and invalidated
     */
    public static PieChart getFormattedPieChart(Context c, PieChart p, boolean shouldShowLegend) {
        Legend cLegend = p.getLegend();
        if (shouldShowLegend) {
            cLegend.setEnabled(true);
            cLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            cLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            cLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            cLegend.setDrawInside(false);
            cLegend.setForm(Legend.LegendForm.CIRCLE);
            cLegend.setTextSize(15);
            cLegend.setWordWrapEnabled(true);
        } else {
            cLegend.setEnabled(false);
        }

        p.setDrawEntryLabels(false);
        p.setDescription(EMPTY_CHART_DESCRIPTION);
        p.setHoleRadius(60f);
        p.setTransparentCircleRadius(65f);
        p.setCenterTextSize(20);

        if (SettingsActivity.getTheme(c) == SettingsActivity.THEME_NOIR) {
            int colorPrimaryNoir = ContextCompat.getColor(c, R.color.colorPrimaryNoir);
            int colorPrimaryTextNoir = ContextCompat.getColor(c, R.color.colorPrimaryTextNoir);

            p.setHoleColor(colorPrimaryNoir);
            p.setTransparentCircleColor(colorPrimaryNoir);
            p.setCenterTextColor(colorPrimaryTextNoir);
            cLegend.setTextColor(colorPrimaryTextNoir);
        }

        p.setRotationEnabled(false);

        p.setOnChartValueSelectedListener(new PieChartListener(p));
        return p;
    }

    /**
     * Formats a line chart in a standardized manner
     * @param c App context
     * @param chart LineChart to format
     * @param listener Listener to attach to chart
     * @param xLabels Labels to use for the x-axis
     * @param valueFormatter True if large value formatter should be used
     * @param skip Number of values to skip
     * @param legend True if show legend, false if hide legend
     * @param isYAxisLocked True if y-axis should be locked from user interaction, false otherwise
     * @return Formatted linechart
     */
    public static LineChart getFormattedLineChart(Context c,
                                                  LineChart chart,
                                                  OnChartValueSelectedListener listener,
                                                  List<String> xLabels,
                                                  boolean valueFormatter,
                                                  int skip,
                                                  boolean legend,
                                                  boolean isYAxisLocked) {
        Legend cLegend = chart.getLegend();
        cLegend.setEnabled(legend);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(skip);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XAxisLabelFormatter(xLabels));

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = chart.getAxisLeft();
        if (valueFormatter) {
            yAxisLeft.setValueFormatter(new LargeNumberAxisFormatter(c));
        }

        if (SettingsActivity.getTheme(c) == SettingsActivity.THEME_NOIR) {
            int textColorNoir = ContextCompat.getColor(c, R.color.colorPrimaryTextNoir);
            cLegend.setTextColor(textColorNoir);
            xAxis.setTextColor(textColorNoir);
            yAxisLeft.setTextColor(textColorNoir);
        }

        chart.setDoubleTapToZoomEnabled(false);
        chart.setDescription(EMPTY_CHART_DESCRIPTION);
        chart.setDragEnabled(true);
        chart.setScaleYEnabled(!isYAxisLocked);
        chart.setDrawGridBackground(false);
        chart.setOnChartValueSelectedListener(listener);

        return chart;
    }

    /**
     * Returns a StaggeredGridLayoutManager that has 1 column on portrait and 2 columns on
     * landscape.
     * @param c Context
     * @return See above
     */
    public static StaggeredGridLayoutManager getStaggeredLayoutManager(Context c) {
        // One column on portrait, two columns on landscape
        int noColumns =
                c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? 1 : 2;
        return new StaggeredGridLayoutManager(noColumns, StaggeredGridLayoutManager.VERTICAL);
    }

    /**
     * Given a ViewHolder's itemView, set it so that it takes up the full span (i.e. across
     * multiple columns).
     * @param itemView ViewHolder itemView
     */
    public static void setViewHolderFullSpan(View itemView) {
        setViewHolderFullSpan(itemView, true);
    }

    /**
     * Given a ViewHolder's itemView, set it so that it takes up the full span (i.e. across
     * multiple columns).
     * @param itemView ViewHolder itemView
     * @param isFullSpan Full span or not
     */
    public static void setViewHolderFullSpan(View itemView, boolean isFullSpan) {
        StaggeredGridLayoutManager.LayoutParams layoutParams =
                (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
        layoutParams.setFullSpan(isFullSpan);
        itemView.setLayoutParams(layoutParams);
    }

    /**
     * Return the URL of a nation banner.
     * @param id The banner ID.
     * @return The URL to the banner.
     */
    public static String getBannerURL(String id) {
        return String.format(Locale.US, BANNER_TEMPLATE, id);
    }

    /**
     * Determines if the current day in the EST/EDT timezone is a special day.
     * @return The special day mode
     */
    public static int getSpecialDayStatus(Context c) {
        if (NightmareHelper.getIsZDayActive(c)) {
            return DAY_Z_DAY;
        }

        Calendar cal = SparkleHelper.getUtc5Calendar();

        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month == Calendar.JANUARY
                && day == 1) {
            return DAY_NEW_YEAR;
        } else if (month == Calendar.JANUARY
                && day == 30) {
            return DAY_STATELY_BIRTHDAY;
        } else if (month == Calendar.APRIL
                && day == 1) {
            return DAY_APRIL_FOOLS;
        } else if (month == Calendar.JULY
                && day == 1) {
            return DAY_CANADA_DAY;
        } else if (month == Calendar.OCTOBER
                && day == 31) {
            return DAY_HALLOWEEN;
        } else if (month == Calendar.NOVEMBER
                && day == 13) {
            return DAY_NS_BIRTHDAY;
        } else {
            return DAY_NORMAL;
        }
    }

    /**
     * Converts dp value to px.
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
