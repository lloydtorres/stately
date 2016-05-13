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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Spoiler;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.login.LoginActivity;
import com.lloydtorres.stately.telegrams.TelegramComposeActivity;

import org.atteo.evo.inflector.English;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

                                         __         _____   _
                                        /  \__..--""  ;-.",'/
                                       ( /  \_         `.' / `.
                                       | |    )  `;.  ,'  / \  \
                                       ( '.  /___/_j_    / ) |  )
                                       '\     /   __\``::'/__'  |
                                        |\_  (   / .-| |-.|  `-,|
                                       .| (   \ ( (WW| \W)j     '
                 ..-----,             .|'  ',  \_\_`_|  ``-.
              .-` ..::.  `,___        |,   ._:7        \__/
            ,'  .:::'':::.|.`.`-.    |:'.   \    ______.-'
          .'  .::'      '::\`.`. `-._| \ \   `"7  /
         /   ./:'  ,.--''>-'\ `.`-.(`'  `.`.._/  (
        -   :/:'  |     /    \  `.(   `.  `._/    \
        |  :::'  .'    | * \|/`. (     |`-_./      |
       .'  |||  .'     |   /|\ *`.___.-'           |
       |   |||  |      | *                         |
       |   ':|| '.    / \    *   /             \__/
       | .  |||  |.--'   |      /-,_______\       \
       |/|  |||  |     _/      /     |    |\       \
       ` )  '::. '.   /       /     |     | `--,    \
         \   |||  |   |      |      |     |   /      )
          `. |||  | _/|      |      |      | (       |
            `::||  |  |      |      |      |  \      |
               `-._|  |       \     |       \  `.___/
                       \_______)     \_______)


 */
/**
 * Created by Lloyd on 2016-01-16.
 *
 * SparkleHelper is a collection of common functions and constants used across Stately's
 * many different classes. These include things such as formatters and linkers.
 */

public class SparkleHelper {
    // Tag used to mark system log print calls
    public static final String APP_TAG = "com.lloydtorres.stately";
    // Uri to invoke the ExploreActivity
    public static final String EXPLORE_PROTOCOL = "com.lloydtorres.stately.explore";
    public static final String EXPLORE_TARGET = EXPLORE_PROTOCOL + "://";
    // Whitelisted protocols
    public static final String[] PROTOCOLS = {"http", "https", EXPLORE_PROTOCOL};
    // Current NationStates API version
    public static final String API_VERSION = "8";
    // NationStates API
    public static final String DOMAIN_URI = "nationstates.net";
    public static final String BASE_URI = "https://www." + DOMAIN_URI + "/";
    public static final String BASE_URI_NOSLASH = "https://www." + DOMAIN_URI;
    public static final String BASE_URI_REGEX = "https:\\/\\/www\\.nationstates\\.net\\/";

    // Keys to user name and autologin and other session variables
    public static final String VAR_NAME = "var_name";
    public static final String VAR_AUTOLOGIN = "var_autologin";
    public static final String VAR_REGION = "var_region";
    public static final String VAR_WA_MEMBER = "var_wa_member";

    // String template used to get nation banners from NationStates
    // @param: banner_id
    public static final String BANNER_TEMPLATE = "https://www.nationstates.net/images/banners/%s.jpg";

    // The number of hours a resolution is on the WA chamber floor
    public static final int WA_RESOLUTION_DURATION = 96;

    // Constants used by activityLinkBuilder() to determine if target is nation or region
    public static final int CLICKY_NATION_MODE = 1;
    public static final int CLICKY_REGION_MODE = 2;

    // An array of chart colours
    public static final int[] chartColours = {  R.color.colorChart0,
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
    public static final int[] freedomColours = {  R.color.colorFreedom0,
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

    // An array of colours used to decorate the SwipeRefresher
    public static final int[] refreshColours = {    R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
    };

    // An array of colours used for WA votes
    public static final int[] waColours = { R.color.colorChart0,
            R.color.colorChart1,
            R.color.colorChart12
    };

    // Convenience variable to colour WA for and against votes
    public static final int[] waColourFor = { R.color.colorChart0 };
    public static final int[] waColourAgainst = { R.color.colorChart1 };

    // Initialized to provide human-readable date strings for Date objects
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    public static final SimpleDateFormat sdfNoYear = new SimpleDateFormat("dd MMM", Locale.US);

    /**
     * VALIDATION
     * These are functions used to validate inputs.
     */

    public static final Pattern VALID_NATION_NAME = Pattern.compile("^[A-za-z0-9-_ ]+$");

    /**
     * Checks if the passed in name is a valid NationStates name (i.e. A-Z, a-z, 0-9, -, (space)).
     * @param name The name to be checked.
     * @return Bool if valid or not.
     */
    public static boolean isValidName(String name)
    {
        Matcher validator = VALID_NATION_NAME.matcher(name);
        return validator.matches();
    }

    /**
     * FORMATTING
     * These are functions used to change an input's format to something nicer.
     */

    /**
     * Turns a proper name into a NationStates ID.
     * @param n the name
     * @return the NS ID
     */
    public static String getIdFromName(String n)
    {
        return n.toLowerCase(Locale.US).replace(" ", "_");
    }

    /**
     * This turns a NationStates ID like greater_tern to a nicely formatted string.
     * In the example's case, greater_tern -> Greater Tern
     * @param id The ID to format.
     * @return String of the nicely-formatted name.
     */
    public static String getNameFromId(String id)
    {
        // IDs have no whitespace and are only separated by underscores.
        String[] words = id.split("_");
        // A list of properly-formatted words.
        List<String> properWords = new ArrayList<String>();

        for (String w : words)
        {
            // Transform word from lower case to proper case.
            properWords.add(toNormalCase(w));
        }

        // Join all the proper words back together with spaces.
        return joinStringList(properWords, " ");
    }

    /**
     * Return the URL of a nation banner.
     * @param id The banner ID.
     * @return The URL to the banner.
     */
    public static String getBannerURL(String id)
    {
        return String.format(BANNER_TEMPLATE, id);
    }

    /**
     * Return a human-readable date string from a UTC timestamp.
     * @param c App context
     * @param sec Unix timestamp.
     * @return A human-readable date string (e.g. moments ago, 1 week ago).
     */
    public static String getReadableDateFromUTC(Context c, long sec)
    {
        long curTime = System.currentTimeMillis();
        long inputTime = sec * 1000L;
        long timeDiff = inputTime - curTime;
        long timeDiffAbs = Math.abs(timeDiff);

        // If the time diff is zero or positive, it's in the future; past otherwise
        String pastIndicator = (timeDiff >= 0) ? c.getString(R.string.time_from_now) : c.getString(R.string.time_ago);
        String template = c.getString(R.string.time_generic_template);

        if (timeDiffAbs < 60000L)
        {
            // less than a minute
            template = String.format(c.getString(R.string.time_moments_template), c.getString(R.string.time_moments), pastIndicator);
        }
        else if (timeDiffAbs < 3600000L)
        {
            // less than an hour
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 60000D);
            int minutes = calc.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            template = String.format(template, minutes, English.plural(c.getString(R.string.time_minute), minutes), pastIndicator);
        }
        else if (timeDiffAbs < 86400000L)
        {
            // less than a day
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 3600000D);
            int hours = calc.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            template = String.format(template, hours, English.plural(c.getString(R.string.time_hour), hours), pastIndicator);
        }
        else if (timeDiffAbs < 604800000L)
        {
            // less than a week
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 86400000D);
            int days = calc.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            template = String.format(template, days, English.plural(c.getString(R.string.time_day), days), pastIndicator);
        }
        else
        {
            template = sdf.format(new Date(inputTime));
        }

        return template;
    }

    /**
     * Returns a formatted date (with no year) given a time in UTC seconds.
     * @param sec UTC seconds
     * @return Formatted date with no year
     */
    public static String getDateNoYearFromUTC(long sec)
    {
        return sdfNoYear.format(new Date(sec * 1000L));
    }

    /**
     * Returns a number formatted like so: ###,###.## (i.e. US formatting).
     * @param i number to format (can be int, double or long)
     * @return The properly-formatted number as a string.
     */
    public static String getPrettifiedNumber(int i)
    {
        return NumberFormat.getInstance(Locale.US).format(i);
    }

    public static String getPrettifiedNumber(double d)
    {
        return NumberFormat.getInstance(Locale.US).format(d);
    }

    public static String getPrettifiedNumber(long l)
    {
        return NumberFormat.getInstance(Locale.US).format(l);
    }

    /**
     * Takes in the population number from the NationStates API and format it to the NS format.
     * The API returns the population numbers in millions (i.e. 1 million = 1).
     * The NS format is ### million or ##.### billion.
     * @param c Context to get resources.
     * @param pop The population number.
     * @return A nicely-formatted population number with suffix.
     */
    public static String getPopulationFormatted(Context c, double pop)
    {
        // The lowest population suffix is a million.
        String suffix = c.getString(R.string.million);
        double popHolder = pop;

        if (popHolder >= 1000D)
        {
            suffix = c.getString(R.string.billion);
            popHolder /= 1000D;
        }

        return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(popHolder), suffix);
    }

    /**
     * Similar to getPrettifiedNumber, but adds a suffix as needed.
     * But this is the same code as getMoneyFormatted!, you say.
     * Well this uses doubles and the other one uses longs.
     * Something something unnecessary casting.
     * @param c app context
     * @param d number to format
     * @return Properly-formatted number as a string
     */
    public static String getPrettifiedSuffixedNumber(Context c, double d)
    {
        if (d < 1000000L)
        {
            // If the money is less than 1 million, we don't need a suffix.
            return getPrettifiedNumber(d);
        }
        else
        {
            // NS drops the least significant digits depending on the suffix needed.
            // e.g. A value like 10,000,000 is simply 10 million.
            String suffix = "";
            if (d >= 1000000D && d < 1000000000D)
            {
                suffix = c.getString(R.string.million);
                d /= 1000000D;
            }
            else if (d >= 1000000000D && d < 1000000000000D)
            {
                suffix = c.getString(R.string.billion);
                d /= 1000000000D;
            }
            else if (d >= 1000000000000D)
            {
                suffix = c.getString(R.string.trillion);
                d /= 1000000000000D;
            }

            return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(d), suffix);
        }
    }

    /**
     * Helper function that capitalizes the first letter of a word.
     * @param w
     * @return
     */
    public static String toNormalCase(String w)
    {
        String prop = "";
        if (w.length() == 0)
        {
            prop = w;
        }
        else if (w.length() == 1)
        {
            prop = w.substring(0, 1).toUpperCase(Locale.US);
        }
        else
        {
            prop = w.substring(0, 1).toUpperCase(Locale.US) + w.substring(1);
        }
        return prop;
    }
    
    public static final Pattern CURRENCY_PLURALIZE = Pattern.compile("^(.+?)( +of .+)?$");
    
    /**
     * Takes in a currency name from the NationStates API and formats it to the
     * plural form using NS format.
     * @param currency The currency unit.
     * @return A nicely-formatted pluralized currency string in NS format.
     */
    public static String getCurrencyPlural(String currency)
    {
        Matcher m = CURRENCY_PLURALIZE.matcher(currency);
        m.matches();
        String pluralize = m.group(1);
        String suffix = m.group(2);
        pluralize = English.plural(pluralize);

        if (suffix != null) {
            return pluralize + suffix;
        }
        else
        {
            return pluralize;
        }
    }

    /**
     * Takes in a money value and currency name from the NationStates API and formats it to the
     * NS format.
     * The NationStates API returns money value as a long, but in-game money is represented like
     * so: #,### [suffix].
     * @param c Context to get string.
     * @param money The amount of money as a long.
     * @param currency The currency unit.
     * @return A nicely-formatted string in NS format.
     */
    public static String getMoneyFormatted(Context c, long money, String currency)
    {
        if (money < 1000000L)
        {
            // If the money is less than 1 million, we don't need a suffix.
            return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(money), getCurrencyPlural(currency));
        }
        else
        {
            // NS drops the least significant digits depending on the suffix needed.
            // e.g. A value like 10,000,000 is simply 10 million.
            String suffix = "";
            if (money >= 1000000L && money < 1000000000L)
            {
                suffix = c.getString(R.string.million);
                money /= 1000000L;
            }
            else if (money >= 1000000000L && money < 1000000000000L)
            {
                suffix = c.getString(R.string.billion);
                money /= 1000000000L;
            }
            else if (money >= 1000000000000L)
            {
                suffix = c.getString(R.string.trillion);
                money /= 1000000000000L;
            }

            return String.format(c.getString(R.string.val_suffix_currency), getPrettifiedNumber(money), suffix, getCurrencyPlural(currency));
        }

    }

    /**
     * Formats a pie chart in a standardized way
     * @param c Context
     * @param p Pie chart
     * @param chartLabels x-labels
     * @return the PieChart, whose data must be set and invalidated
     */
    public static PieChart getFormattedPieChart(Context c, PieChart p, List<String> chartLabels)
    {
        Legend cLegend = p.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        p.setDrawSliceText(false);
        p.setDescription("");
        p.setHoleRadius(60f);
        p.setTransparentCircleRadius(65f);
        p.setCenterTextSize(20);
        p.setRotationEnabled(false);

        p.setOnChartValueSelectedListener(new PieChartListener(c, p, chartLabels));
        return p;
    }

    /**
     * Formats a line chart in a standardized manner
     * @param chart LineChart to format
     * @param listener Listener to attach to chart
     * @param valueFormatter True if large value formatter should be used
     * @param skip Number of values to skip
     * @param legend True if show legend, false if hide legend
     * @return Formatted linechart
     */
    public static LineChart getFormattedLineChart(LineChart chart, OnChartValueSelectedListener listener, boolean valueFormatter, int skip, boolean legend)
    {
        Legend cLegend = chart.getLegend();
        cLegend.setEnabled(legend);

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelsToSkip(skip);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = chart.getAxisLeft();
        if (valueFormatter)
        {
            yAxisLeft.setValueFormatter(new LargeValueFormatter());
        }

        chart.setDoubleTapToZoomEnabled(false);
        chart.setDescription("");
        chart.setDragEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setOnChartValueSelectedListener(listener);

        return chart;
    }

    /**
     * LOGINS & SESSION DATA
     * These update, return and remove data about the current login and its session data.
     */

    /**
     * Sets the currently logged-in user in shared prefs and saves them into the database.
     * @param c App context
     * @param name User name
     * @param autologin User autologin cookie
     */
    public static void setActiveUser(Context c, String name, String autologin)
    {
        // Save user into database
        UserLogin u = new UserLogin(getIdFromName(name), name, autologin);
        u.save();

        // Save user into shared preferences
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(VAR_NAME, name);
        editor.putString(VAR_AUTOLOGIN, autologin);
        editor.commit();
    }

    /**
     * Sets data on region and WA membership for the current session.
     * @param c App context
     * @param regionName Current region ID
     * @param waStatus WA membership status
     */
    public static void setSessionData(Context c, String regionName, String waStatus)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(VAR_REGION, regionName);
        editor.putBoolean(VAR_WA_MEMBER, isWaMember(c, waStatus));
        editor.commit();
    }

    /**
     * Used for updating the session region name if it changes.
     * @param c App context
     * @param regionName Current region ID
     */
    public static void setRegionSessionData(Context c, String regionName)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(VAR_REGION, regionName);
        editor.commit();
    }

    /**
     * Used for updating the session WA membership if it changes.
     * @param c
     * @param stat
     */
    public static void setWaSessionData(Context c, String stat)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putBoolean(VAR_WA_MEMBER, isWaMember(c, stat));
        editor.commit();
    }

    /**
     * Retrieve information about the currently logged in user
     * @param c App context
     * @return A UserLogin object with their name and autologin
     */
    public static UserLogin getActiveUser(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        String name = storage.getString(VAR_NAME, null);
        String autologin = storage.getString(VAR_AUTOLOGIN, null);
        if (name != null && autologin != null)
        {
            UserLogin u = new UserLogin(getIdFromName(name), name, autologin);
            return u;
        }

        return null;
    }

    /**
     * Returns the current member region in the current session.
     * @param c App context
     * @return ID of region
     */
    public static String getRegionSessionData(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getString(VAR_REGION, null);
    }

    /**
     * Returns current WA membership status in current session.
     * @param c App context
     * @return WA membership status
     */
    public static boolean getWaSessionData(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(VAR_WA_MEMBER, false);
    }

    /**
     * Removes data about the logged in user from shared prefs.
     * @param c App context
     */
    public static void removeActiveUser(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.remove(VAR_NAME);
        editor.remove(VAR_AUTOLOGIN);
        editor.commit();
    }

    /**
     * Removes session data.
     * @param c App context
     */
    public static void removeSessionData(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.remove(VAR_REGION);
        editor.remove(VAR_WA_MEMBER);
        editor.commit();
    }

    /**
     * UTILITY
     * These are convenient tools to call from any class.
     */

    /**
     * Takes in a list of strings and a delimiter and returns a string that combines
     * the elements of the list, separated by the delimiter.
     * @param list List of strings to join.
     * @param delimiter Delimiter to separate each string.
     * @return Merged string.
     */
    public static String joinStringList(List<String> list, String delimiter)
    {
        if (list == null || list.size() < 0) { return ""; }

        StringBuilder mergedString = new StringBuilder();
        for (int i=0; i < list.size(); i++) {
            if (list.get(i) != null)
            {
                mergedString.append(list.get(i));

                if (i < list.size() - 1) {
                    mergedString.append(delimiter);
                }
            }
        }
        return mergedString.toString();
    }

    /**
     * Starts the ExploreActivity for the given ID and mode.
     * @param c App context
     * @param n The nation ID
     * @param mode Mode if nation or region
     */
    public static void startExploring(Context c, String n, int mode)
    {
        Intent exploreActivityLaunch = new Intent(c, ExploreActivity.class);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_ID, n);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_MODE, mode);
        c.startActivity(exploreActivityLaunch);
    }

    /**
     * Starts the TrendsActivity for the given target and census ID.
     * @param c App context
     * @param target Target ID
     * @param mode Mode if nation or region
     * @param id Census ID
     */
    public static void startTrends(Context c, String target, int mode, int id)
    {
        Intent trendsActivityLaunch = new Intent(c, TrendsActivity.class);
        trendsActivityLaunch.putExtra(TrendsActivity.TREND_TARGET, target);
        trendsActivityLaunch.putExtra(TrendsActivity.TREND_MODE, mode);
        trendsActivityLaunch.putExtra(TrendsActivity.TREND_ID, id);
        c.startActivity(trendsActivityLaunch);
    }

    /**
     * Starts the TelegramComposeActivity and prefills it with data (if provided).
     * @param c App context
     * @param recipients A string of recipients, can be null or empty
     * @param replyId Reply ID, can be filled or TelegramComposeActivity.NO_REPLY_ID
     */
    public static void startTelegramCompose(Context c, String recipients, int replyId)
    {
        Intent telegramComposeActivityLaunch = new Intent(c, TelegramComposeActivity.class);
        telegramComposeActivityLaunch.putExtra(TelegramComposeActivity.RECIPIENTS_DATA, recipients);
        telegramComposeActivityLaunch.putExtra(TelegramComposeActivity.REPLY_ID_DATA, replyId);
        c.startActivity(telegramComposeActivityLaunch);
    }

    /**
     * Launches a LoginActivity without autologging in.
     * @param c App context
     */
    public static void startAddNation(Context c)
    {
        Intent loginActivityLaunch = new Intent(c, LoginActivity.class);
        loginActivityLaunch.putExtra(LoginActivity.NOAUTOLOGIN_KEY, true);
        c.startActivity(loginActivityLaunch);
    }

    /**
     * Calculates the remaining time for a WA resolution in human-readable form.
     * @param c App context
     * @param hoursElapsed Number of hours passed since voting started
     * @return Time remaining in human-readable form
     */
    public static String calculateResolutionEnd(Context c, int hoursElapsed)
    {
        Calendar cal = new GregorianCalendar();

        // Round up to nearest hour
        if (cal.get(Calendar.MINUTE) >= 1)
        {
            cal.add(Calendar.HOUR, 1);
        }
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        cal.add(Calendar.HOUR, WA_RESOLUTION_DURATION - hoursElapsed);

        Date d = cal.getTime();
        return getReadableDateFromUTC(c, d.getTime() / 1000L);
    }

    /**
     * Formats a pie chart displaying current voting breakdown for a WA resolution.
     * @param c Context
     * @param p Pie chart
     * @param voteFor Number of votes for
     * @param voteAgainst Number of votes against
     */
    public static boolean setWaVotingBreakdown(Context c, PieChart p, float voteFor, float voteAgainst)
    {
        // Calculate percentages (floating point math FTW!)
        float voteTotal = voteFor + voteAgainst;

        if (voteTotal > 0)
        {
            float votePercentFor = (voteFor * 100f)/voteTotal;
            float votePercentAgainst = (voteAgainst * 100f)/voteTotal;

            List<String> chartLabels = new ArrayList<String>();
            List<Entry> chartEntries = new ArrayList<Entry>();

            // Set data
            int i = 0;
            chartLabels.add(c.getString(R.string.wa_for));
            chartEntries.add(new Entry(votePercentFor, i++));
            chartLabels.add(c.getString(R.string.wa_against));
            chartEntries.add(new Entry(votePercentAgainst, i++));

            // Set colour and disable chart labels
            PieDataSet dataSet = new PieDataSet(chartEntries, "");
            dataSet.setDrawValues(false);
            dataSet.setColors(waColours, c);
            PieData dataFull = new PieData(chartLabels, dataSet);

            // formatting
            p = getFormattedPieChart(c, p, chartLabels);
            p.setData(dataFull);
            p.invalidate();

            return true;
        }

        return false;
    }

    /**
     * Checks if the given string indicates that the given stat is for a WA member.
     * @param c App context
     * @param stat WA state indicator
     * @return bool if stat indicates its a WA member
     */
    public static boolean isWaMember(Context c, String stat)
    {
        return stat.equals(c.getString(R.string.nation_wa_member)) || stat.equals(c.getString(R.string.nation_wa_delegate));
    }

    /**
     * LINK AND HTML PROCESSING
     * These are functions used to transform raw NationStates BBCode and formatting into clickable
     * links and formatted text. Separate from the other formatting functions due to their unique
     * nature.
     */

    /**
     * Builds a link invoking an explore activity to the specified ID, and puts it into the
     * appropriate TextView.
     * @param c App context
     * @param t Target TextView
     * @param template The original text with the old formatting.
     * @param oTarget The old format that needs to be replaced.
     * @param nTarget The new format (usually a name) to replace the old.
     * @param mode If target is a nation or a region.
     * @return Returns the new text content for further manipulation.
     */
    public static String activityLinkBuilder(Context c, TextView t, String template, String oTarget, String nTarget, int mode)
    {
        final String urlFormat = "<a href=\"%s/%d\">%s</a>";
        String tempHolder = template;
        String targetActivity = EXPLORE_TARGET;

        // Name needs to be formatted back to its NationStates ID first for the URL.
        targetActivity = targetActivity + nTarget.toLowerCase(Locale.US).replace(" ", "_");
        targetActivity = String.format(urlFormat, targetActivity, mode, nTarget);

        tempHolder = tempHolder.replace(oTarget, targetActivity);
        setStyledTextView(c, t, tempHolder);

        return tempHolder;
    }

    /**
     * Stylify text view to primary colour and no underline
     * @param c App context
     * @param t TextView
     */
    public static void styleLinkifiedTextView(Context c, TextView t)
    {
        // Get individual spans and replace them with clickable ones.
        Spannable s = new SpannableString(t.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(c, span.getURL());
            s.setSpan(span, start, end, 0);
        }

        t.setText(s);
        // Need to set this to allow for clickable TextView links.
        if (!(t instanceof HtmlTextView))
        {
            t.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    /**
     * Given a regex and some content, get all pairs of (old, new) where old is a string matching
     * the regex in the content, and new is the proper name to replace the old string.
     * @param regex Regex statement
     * @param content Target content
     * @return
     */
    public static Set<Map.Entry<String, String>> getReplacePairFromRegex(Pattern regex, String content, boolean isName)
    {
        String holder = content;
        // (old, new) replacement pairs
        Map<String, String> replacePairs = new HashMap<String, String>();

        Matcher m = regex.matcher(holder);
        while (m.find())
        {
            String properFormat;
            if (isName)
            {
                // Nameify the ID found and put the (old, new) pair into the map
                properFormat = getNameFromId(m.group(1));
            }
            else
            {
                properFormat = m.group(1);
            }
            replacePairs.put(m.group(), properFormat);
        }

        return replacePairs.entrySet();
    }

    public static Set<Map.Entry<String, String>> getDoubleReplacePairFromRegex(Pattern regex, String afterFormat, String content)
    {
        String holder = content;
        // (old, new) replacement pairs
        Map<String, String> replacePairs = new HashMap<String, String>();

        Matcher m = regex.matcher(holder);
        while (m.find())
        {
            String properFormat = String.format(afterFormat, m.group(1), m.group(2));
            replacePairs.put(m.group(), properFormat);
        }

        return replacePairs.entrySet();
    }

    /**
     * A helper function used to 1) find all strings to be replaced and 2) linkifies them.
     * @param c App context
     * @param t TextView
     * @param content Target content
     * @param regex Regex statement
     * @param mode If nation or region
     * @return
     */
    public static String linkifyHelper(Context c, TextView t, String content, Pattern regex, int mode)
    {
        String holder = content;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder, true);

        for (Map.Entry<String, String> n : set) {
            holder = activityLinkBuilder(c, t, holder, n.getKey(), n.getValue(), mode);
        }

        return holder;
    }

    public static final Pattern NS_HAPPENINGS_NATION = Pattern.compile("@@(.*?)@@");
    public static final Pattern NS_HAPPENINGS_REGION = Pattern.compile("%%(.*?)%%");

    /**
     * A formatter used to linkify @@nation@@ and %%region%% text in NationStates' happenings.
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setHappeningsFormatting(Context c, TextView t, String content)
    {
        String holder = getHtmlFormatting(content).toString();

        // Linkify nations (@@NATION@@)
        holder = linkifyHelper(c, t, holder, NS_HAPPENINGS_NATION, CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, NS_HAPPENINGS_REGION, CLICKY_REGION_MODE);

        if (holder.contains("EO:"))
        {
            String[] newTargets = holder.split(":");
            String newTarget = newTargets[1].substring(0, newTargets[1].length() - 1);
            String template = String.format(c.getString(R.string.region_eo), holder);
            holder = activityLinkBuilder(c, t, template, "EO:"+newTarget+".", getNameFromId(newTarget), CLICKY_REGION_MODE);
        }

        if (holder.contains("EC:"))
        {
            String[] newTargets = holder.split(":");
            String newTarget = newTargets[1].substring(0, newTargets[1].length() - 1);
            String template = String.format(c.getString(R.string.region_ec), holder);
            holder = activityLinkBuilder(c, t, template, "EC:"+newTarget+".", getNameFromId(newTarget), CLICKY_REGION_MODE);
        }

        // In case there are no nations or regions to linkify, set and style TextView here too
        t.setText(Html.fromHtml(holder));
        styleLinkifiedTextView(c, t);
    }

    public static void setIssueResultsFormatting(Context c, TextView t, Nation nationData, String target)
    {
        if (nationData != null && target != null)
        {
            target = target.replace("@@NAME@@", nationData.name);
            target = target.replace("@@REGION@@", nationData.region);
            target = target.replace("@@MAJORINDUSTRY@@", nationData.industry);
            target = target.replace("@@POPULATION@@", getPrettifiedNumber(nationData.popBase));
            target = target.replace("@@TYPE@@", nationData.prename);
            target = target.replace("@@ANIMAL@@", nationData.animal);
            target = target.replace("@@ucfirst(ANIMAL)@@", toNormalCase(nationData.animal));
            target = target.replace("@@PL(ANIMAL)@@", English.plural(nationData.animal));
            target = target.replace("@@ucfirst(PL(ANIMAL))@@", toNormalCase(English.plural(nationData.animal)));
            target = target.replace("@@CURRENCY@@", nationData.currency);
            target = target.replace("@@PL(CURRENCY)@@", getCurrencyPlural(nationData.currency));
            target = target.replace("@@ucfirst(PL(CURRENCY))@@", toNormalCase(getCurrencyPlural(nationData.currency)));
            target = target.replace("@@SLOGAN@@", nationData.motto);
            target = target.replace("@@DEMONYM@@", nationData.demAdjective);
            target = target.replace("@@DEMONYM2@@", nationData.demNoun);
            target = target.replace("@@PL(DEMONYM2)@@", nationData.demPlural);

            String valCapital = String.format(c.getString(R.string.issue_capital_none), nationData.name);
            if (nationData.capital != null)
            {
                valCapital = nationData.capital;
            }
            target = target.replace("@@CAPITAL@@", valCapital);
            target = target.replace("@@$nation->query_capital()@@", valCapital);

            String valLeader = c.getString(R.string.issue_leader_none);
            if (nationData.leader != null)
            {
                valLeader = nationData.leader;
            }
            target = target.replace("@@LEADER@@", valLeader);
            target = target.replace("@@$nation->query_leader()@@", valLeader);

            String valReligion = c.getString(R.string.issue_religion_none);
            if (nationData.religion != null)
            {
                valReligion = nationData.religion;
            }
            target = target.replace("@@FAITH@@", valReligion);
            target = target.replace("@@$nation->query_faith()@@", valReligion);
        }

        t.setText(getHtmlFormatting(target).toString());
    }

    /**
     * Basic HTML formatter that returns a styled version of the string.
     * @param content Target content
     * @return Styled spanned object
     */
    public static Spanned getHtmlFormatting(String content)
    {
        String holder = Jsoup.clean(content, Whitelist.none().addTags("br"));
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        return Html.fromHtml(holder);
    }

    /**
     * Regex patterns
     */

    public static final Pattern NS_RAW_NATION_LINK = Pattern.compile("(?i)\\b(?:https?:\\/\\/|)(?:www\\.|)nationstates\\.net\\/nation=([\\w-]*)(?:\\/|)$");
    public static final Pattern NS_RAW_REGION_LINK = Pattern.compile("(?i)\\b(?:https?:\\/\\/|)(?:www\\.|)nationstates\\.net\\/region=([\\w-]*)(?:\\/|)$");
    public static final Pattern NS_RAW_REGION_LINK_TG = Pattern.compile("(?i)\\b(?:https?:\\/\\/|)(?:www\\.|)nationstates\\.net\\/region=([\\w-]*)\\?tgid=[0-9].*");
    public static final Pattern NS_BBCODE_NATION = Pattern.compile("(?i)\\[nation\\](.*?)\\[\\/nation\\]");
    public static final Pattern NS_BBCODE_NATION_2 = Pattern.compile("(?i)\\[nation=.*?\\](.*?)\\[\\/nation\\]");
    public static final Pattern NS_BBCODE_NATION_3 = Pattern.compile("(?i)\\[nation=(.*?)\\]");
    public static final Pattern NS_BBCODE_REGION = Pattern.compile("(?i)\\[region\\](.*?)\\[\\/region\\]");
    public static final Pattern NS_BBCODE_REGION_2 = Pattern.compile("(?i)\\[region=(.*?)\\]");
    public static final Pattern NS_BBCODE_URL_NATION = Pattern.compile("(?i)\\[url=(?:https?:\\/\\/|)(?:www\\.|)nationstates\\.net\\/nation=([\\w-]*)(?:\\/|)\\]");
    public static final Pattern NS_BBCODE_URL_REGION = Pattern.compile("(?i)\\[url=(?:https?:\\/\\/|)(?:www\\.|)nationstates\\.net\\/region=([\\w-]*)(?:\\/|)\\]");

    public static final Pattern BBCODE_B = Pattern.compile("(?i)(?s)\\[b\\](.*?)\\[\\/b\\]");
    public static final Pattern BBCODE_I = Pattern.compile("(?i)(?s)\\[i\\](.*?)\\[\\/i\\]");
    public static final Pattern BBCODE_U = Pattern.compile("(?i)(?s)\\[u\\](.*?)\\[\\/u\\]");
    public static final Pattern BBCODE_PRE = Pattern.compile("(?i)(?s)\\[pre\\](.*?)\\[\\/pre\\]");
    public static final Pattern BBCODE_PROPOSAL = Pattern.compile("(?i)(?s)\\[proposal=.*?\\](.*?)\\[\\/proposal\\]");
    public static final Pattern BBCODE_RESOLUTION = Pattern.compile("(?i)(?s)\\[resolution=.*?\\](.*?)\\[\\/resolution\\]");
    public static final Pattern BBCODE_COLOR = Pattern.compile("(?i)(?s)\\[colou?r=(.*?)\\](.*?)\\[\\/colou?r\\]");
    public static final Pattern BBCODE_INTERNAL_URL = Pattern.compile("(?i)(?s)\\[url=((?:pages\\/|page=).*?)\\](.*?)\\[\\/url\\]");
    public static final Pattern BBCODE_URL = Pattern.compile("(?i)(?s)\\[url=(.*?)\\](.*?)\\[\\/url\\]");

    /**
     * Transform NationStates' BBCode-formatted content into HTML
     * @param c App context
     * @param t TextView
     * @param content Target content
     * @param fm FragmentManager to show spoiler dialogs in
     */
    public static void setBbCodeFormatting(Context c, TextView t, String content, FragmentManager fm)
    {
        String holder = content.trim();
        holder = holder.replace("\n", "<br>");
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        holder = Jsoup.clean(holder, Whitelist.simpleText().addTags("br").addTags("strike"));

        // Replace raw NS nation and region links with Stately versions
        holder = linkifyHelper(c, t, holder, NS_RAW_NATION_LINK, CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, NS_RAW_REGION_LINK, CLICKY_REGION_MODE);
        holder = linkifyHelper(c, t, holder, NS_RAW_REGION_LINK_TG, CLICKY_REGION_MODE);
        holder = regexReplace(holder, NS_BBCODE_URL_NATION, "[url=" + EXPLORE_TARGET + "%s/" + CLICKY_NATION_MODE + "]");
        holder = regexReplace(holder, NS_BBCODE_URL_REGION, "[url=" + EXPLORE_TARGET + "%s/" + CLICKY_REGION_MODE + "]");

        // Basic BBcode processing
        holder = holder.replace("[hr]", "<br>");

        // Process lists first (they're problematic!)
        TextProcessor processor = BBProcessorFactory.getInstance().create(c.getResources().openRawResource(R.raw.bbcode));
        holder = processor.process(holder);
        holder = holder.replace("&lt;", "<");
        holder = holder.replace("&gt;", ">");
        holder = holder.replace("[*]", "<li>");
        holder = Jsoup.clean(holder, Whitelist.relaxed());

        // Q: Why don't you use the BBCode parser instead of doing this manually? :(
        // A: Because it misses some tags for some reason, so it's limited to lists for now.
        holder = regexReplace(holder, BBCODE_B, "<b>%s</b>");
        holder = regexReplace(holder, BBCODE_I, "<i>%s</i>");
        holder = regexReplace(holder, BBCODE_U, "<u>%s</u>");
        holder = regexReplace(holder, BBCODE_PRE, "<code>%s</code>");
        holder = regexExtract(holder, BBCODE_PROPOSAL);
        holder = regexExtract(holder, BBCODE_RESOLUTION);
        holder = regexDoubleReplace(holder, BBCODE_COLOR, "<font color=\"%s\">%s</font>");
        holder = regexDoubleReplace(holder, BBCODE_INTERNAL_URL, "<a href=\"https://www.nationstates.net/%s\">%s</a>");
        holder = regexDoubleReplace(holder, BBCODE_URL, "<a href=\"%s\">%s</a>");
        holder = holder.replaceAll("(?i)(?<=^|\\s|<br \\/>|<br>|<b>|<i>|<u>)(https?:\\/\\/[^\\s\\[\\<]+)", "<a href=\"$1\">" + c.getString(R.string.clicky_link) + "</a>");
        holder = holder.replaceAll("(?i)(?<=^|\\s|<br \\/>|<br>|<b>|<i>|<u>)(www\\.[^\\s\\?\\[\\<]+)", "<a href=\"$1\">" + c.getString(R.string.clicky_link) + "</a>");
        holder = regexQuoteFormat(c, t, holder);

        // Extract and replace spoilers
        List<Spoiler> spoilers = getSpoilerReplacePairs(c, holder);
        for (int i=0; i < spoilers.size(); i++)
        {
            Spoiler s = spoilers.get(i);
            holder = holder.replace(s.raw, s.replacer);
        }

        // Linkify nations and regions
        holder = linkifyHelper(c, t, holder, NS_BBCODE_NATION, CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, NS_BBCODE_NATION_2, CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, NS_BBCODE_NATION_3, CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, NS_BBCODE_REGION, CLICKY_REGION_MODE);
        holder = linkifyHelper(c, t, holder, NS_BBCODE_REGION_2, CLICKY_REGION_MODE);

        // In case there are no nations or regions to linkify, set and style TextView here too
        setStyledTextView(c, t, holder, spoilers, fm);
    }

    public static final Pattern BBCODE_SPOILER = Pattern.compile("(?i)(?s)\\[spoiler\\](.*?)\\[\\/spoiler\\]");
    public static final Pattern BBCODE_SPOILER_2 = Pattern.compile("(?i)(?s)\\[spoiler=(.*?)\\](.*?)\\[\\/spoiler\\]");

    /**
     * Helper function that extracts spoilers from BBCode for later use.
     * @param c App context
     * @param target Target content
     * @return List of spoilers
     */
    public static List<Spoiler> getSpoilerReplacePairs(Context c, String target)
    {
        String holder = target;
        List<Spoiler> spoilers = new ArrayList<Spoiler>();
        int id = 0;

        // Handle spoilers without titles first
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(BBCODE_SPOILER, holder, false);
        for (Map.Entry<String, String> n : set) {
            Spoiler s = new Spoiler();
            s.content = n.getValue();
            s.raw = n.getKey();
            s.replacer = String.format(c.getString(R.string.spoiler_warn_link), id++);
            spoilers.add(s);
        }

        // Handle spoilers with titles next
        Matcher m = BBCODE_SPOILER_2.matcher(holder);
        while (m.find())
        {
            Spoiler s = new Spoiler();
            // Gets rid of HTML in title
            s.title = Jsoup.parse(m.group(1)).text();
            s.content = m.group(2);
            s.raw = m.group();
            s.replacer = String.format(c.getString(R.string.spoiler_warn_title_link), id++, s.title);
            spoilers.add(s);
        }

        return spoilers;
    }

    public static final Pattern NS_TG_RAW_NATION_LINK = Pattern.compile("(?i)<a href=\"(?:" + BASE_URI_REGEX + "|)nation=([\\w-]*?)\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern NS_TG_RAW_REGION_LINK_TG = Pattern.compile("(?i)<a href=\"(?:" + BASE_URI_REGEX + "|)region=([\\w-]*?)\\?tgid=[0-9].*\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern NS_TG_RAW_REGION_LINK = Pattern.compile("(?i)<a href=\"(?:" + BASE_URI_REGEX + "|)region=([\\w-]*?)\" rel=\"nofollow\">(.*?)<\\/a>");
    public static final Pattern PARAGRAPH = Pattern.compile("(?i)(?s)<p>(.*?)<\\/p>");

    /**
     * Formats raw HTML from a telegram into something the app can understand.
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setTelegramHtmlFormatting(Context c, TextView t, String content)
    {
        String holder = content.trim();
        holder = holder.replace("\n", "<br />");
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        holder = "<base href=\"" + SparkleHelper.BASE_URI_NOSLASH + "\">" + holder;
        holder = Jsoup.clean(holder, Whitelist.basic().preserveRelativeLinks(true).addTags("br"));
        holder = holder.replace("<a href=\"//" + DOMAIN_URI + "/", "<a href=\"" + BASE_URI);
        holder = holder.replace("<a href=\"//www." + DOMAIN_URI + "/", "<a href=\"" + BASE_URI);
        holder = holder.replace("<a href=\"/", "<a href=\"" + BASE_URI);

        holder = regexDoubleReplace(holder, NS_TG_RAW_NATION_LINK, "<a href=\"" + EXPLORE_TARGET + "%s/" + CLICKY_NATION_MODE + "\">%s</a>");

        holder = regexDoubleReplace(holder, NS_TG_RAW_REGION_LINK_TG, "<a href=\"" + EXPLORE_TARGET + "%s/" + CLICKY_REGION_MODE + "\">%s</a>");
        holder = regexDoubleReplace(holder, NS_TG_RAW_REGION_LINK, "<a href=\"" + EXPLORE_TARGET + "%s/" + CLICKY_REGION_MODE + "\">%s</a>");

        holder = regexReplace(holder, PARAGRAPH, "<br>%s");

        holder = holder.replaceAll("(?i)(?<=^|\\s|<br \\/>|<br>|<b>|<i>|<u>)(https?:\\/\\/[^\\s\\[\\<]+)", "<a href=\"$1\">" + c.getString(R.string.clicky_link) + "</a>");
        holder = holder.replaceAll("(?i)(?<=^|\\s|<br \\/>|<br>|<b>|<i>|<u>)(www\\.[^\\s\\?\\[\\<]+)", "<a href=\"$1\">" + c.getString(R.string.clicky_link) + "</a>");

        setStyledTextView(c, t, holder);
    }

    /**
     * Helper used for setting and styling an HTML string into a TextView.
     * @param c App context
     * @param t Target TextView
     * @param holder Content
     */
    public static void setStyledTextView(Context c, TextView t, String holder)
    {
        if (t instanceof HtmlTextView)
        {
            try
            {
                ((HtmlTextView)t).setHtmlFromString(holder, new HtmlTextView.RemoteImageGetter());
            }
            catch(Exception e) {
                logError(e.toString());
                t.setText(c.getString(R.string.bbcode_parse_error));
                t.setTypeface(t.getTypeface(), Typeface.ITALIC);
            }
        }
        else
        {
            t.setText(Html.fromHtml(holder));
        }
        styleLinkifiedTextView(c, t);
    }

    /**
     * Overloaded to deal with spoilers.
     */
    public static void setStyledTextView(Context c, TextView t, String holder, List<Spoiler> spoilers, FragmentManager fm)
    {
        if (t instanceof HtmlTextView)
        {
            try
            {
                ((HtmlTextView)t).setHtmlFromString(holder, new HtmlTextView.RemoteImageGetter());
            }
            catch(Exception e) {
                logError(e.toString());
                t.setText(c.getString(R.string.bbcode_parse_error));
                t.setTypeface(t.getTypeface(), Typeface.ITALIC);
            }
        }
        else
        {
            t.setText(Html.fromHtml(holder));
        }

        // Deal with spoilers here
        styleLinkifiedTextView(c, t);   // Ensures TextView contains a spannable
        Spannable span = (Spannable) t.getText();
        String rawSpan = span.toString();

        for (int i=0; i < spoilers.size(); i++)
        {
            Spoiler s = spoilers.get(i);
            int start = rawSpan.indexOf(s.replacer);
            if (start != -1)
            {
                int end = start + s.replacer.length();
                SpoilerSpan clickyDialog = new SpoilerSpan(c, s, fm);
                span.setSpan(clickyDialog, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        t.setText(span);
    }

    /**
     * Replaces all matches of a given regex with the supplied string template. Only accepts
     * one parameter.
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @return Returns content with all matched substrings replaced
     */
    public static String regexReplace(String target, Pattern regexBefore, String afterFormat)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regexBefore, holder, false);

        for (Map.Entry<String, String> n : set) {
            // disabling whitelisting since improperly-nested tags are common in NS BBCode :(
            String replacer = n.getValue();
            String properFormat = String.format(afterFormat, replacer); //Jsoup.clean(String.format(afterFormat, n.getValue()), Whitelist.basic().addProtocols("a", "href", PROTOCOLS));
            holder = holder.replace(n.getKey(), properFormat);
        }

        return holder;
    }

    /**
     * Similar to regexReplace, but takes in two characters
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @return
     */
    public static String regexDoubleReplace(String target, Pattern regexBefore, String afterFormat)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getDoubleReplacePairFromRegex(regexBefore, afterFormat, holder);

        for (Map.Entry<String, String> n : set) {
            // disabling whitelisting since improperly-nested tags are common in NS BBCode :(
            String replacer = n.getValue(); //Jsoup.clean(n.getValue(), Whitelist.basic().addProtocols("a", "href", PROTOCOLS));
            holder = holder.replace(n.getKey(), replacer);
        }

        return holder;
    }

    /**
     * Convenience class used by regexQuoteFormat() to format blockquotes with author attrib.
     * @param c App context
     * @param t Target TextView
     * @param regex Regex to use
     * @param content Original string
     * @return Formatted string
     */
    public static String regexQuoteFormatHelper(Context c, TextView t, Pattern regex, String content)
    {
        String holder = content;
        Map<String, String> replacePairs = new HashMap<String, String>();
        Matcher m = regex.matcher(holder);
        while (m.find())
        {
            String properFormat = String.format("<blockquote><i>@@%s@@:<br />%s</i></blockquote>", getNameFromId(m.group(1)), m.group(2));
            replacePairs.put(m.group(), properFormat);
        }
        Set<Map.Entry<String, String>> set = replacePairs.entrySet();
        for (Map.Entry<String, String> n : set) {
            String replacer = n.getValue();
            holder = holder.replace(n.getKey(), replacer);
        }
        holder = linkifyHelper(c, t, holder, NS_HAPPENINGS_NATION, CLICKY_NATION_MODE);
        return holder;
    }

    public static final Pattern BBCODE_QUOTE = Pattern.compile("(?i)(?s)\\[quote\\](.*?)\\[\\/quote\\]");
    public static final Pattern BBCODE_QUOTE_1 = Pattern.compile("(?i)(?s)\\[quote=(.*?);[0-9]+\\](.*?)\\[\\/quote\\]");
    public static final Pattern BBCODE_QUOTE_2 = Pattern.compile("(?i)(?s)\\[quote=(.*?)\\](.*?)\\[\\/quote\\]");

    /**
     * Used for formatting blockquotes
     * @param context App context
     * @param content Original string
     * @return Formatted string
     */
    public static String regexQuoteFormat(Context context, TextView t, String content)
    {
        String holder = content;

        // handle basic quotes
        holder = regexReplace(holder, BBCODE_QUOTE, "<blockquote><i>%s</i></blockquote>");

        // handle quotes with parameters on them
        // in this case, [quote=name;id]...
        holder = regexQuoteFormatHelper(context, t, BBCODE_QUOTE_1, holder);
        // in this case, just [quote=name]...
        holder = regexQuoteFormatHelper(context, t, BBCODE_QUOTE_2, holder);

        return holder;
    }

    /**
     * Extracts a capture group from a regex
     * @param target Target content
     * @param regex Regex
     * @return
     */
    public static String regexExtract(String target, Pattern regex)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder, false);

        for (Map.Entry<String, String> n : set) {
            holder = holder.replace(n.getKey(), n.getValue());
        }

        return holder;
    }

    /**
     * Removes all substrings which match the regex
     * @param target Target content
     * @param regex Regex
     * @return
     */
    public static String regexRemove(String target, Pattern regex)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder, false);

        for (Map.Entry<String, String> n : set) {
            holder = holder.replace(n.getKey(), "");
        }

        return holder;
    }

    /**
     * LOGGING
     * These are function calls used to log events and other things.
     */

    /**
     * Shows a long snackbar in the given view.
     * @param view View
     * @param str Snackbar message
     */
    public static void makeSnackbar(View view, String str)
    {
        Snackbar.make(view, str, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Logs a system error. Mostly used so that APP_TAG doesn't have to repeat.
     * @param message Message
     */
    public static void logError(String message)
    {
        Log.e(APP_TAG, message);
    }
}
