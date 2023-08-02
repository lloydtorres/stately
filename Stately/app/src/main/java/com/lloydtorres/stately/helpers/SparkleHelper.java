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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.dto.Spoiler;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.links.SpoilerSpan;
import com.lloydtorres.stately.helpers.links.URLSpanNoUnderline;
import com.lloydtorres.stately.login.LoginActivity;
import com.lloydtorres.stately.region.MessageBoardActivity;
import com.lloydtorres.stately.report.ReportActivity;
import com.lloydtorres.stately.telegrams.TelegramComposeActivity;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.atteo.evo.inflector.English;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-01-16.
 *
 * SparkleHelper is a collection of common functions and constants used across Stately's
 * many different classes. These include things such as formatters and linkers.
 */

public final class SparkleHelper {
    // Tag used to mark system log print calls
    public static final String APP_TAG = "Stately";
    // Allowlisted protocols
    public static final String[] PROTOCOLS = {"http", "https",
            ExploreActivity.EXPLORE_PROTOCOL,
            MessageBoardActivity.RMB_PROTOCOL,
            ResolutionActivity.RESOLUTION_PROTOCOL,
            ReportActivity.REPORT_PROTOCOL};
    // Current NationStates API version
    public static final String API_VERSION = "12";
    // NationStates API
    public static final String DOMAIN_URI = "nationstates.net";
    public static final String BASE_URI = "https://www." + DOMAIN_URI + "/";
    public static final String BASE_URI_NOSLASH = "https://www." + DOMAIN_URI;
    public static final String BASE_URI_REGEX = "https:\\/\\/www\\.nationstates\\.net\\/";
    // Initialized to provide human-readable date strings for Date objects
    public static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yyyy", Locale.US);
    public static final SimpleDateFormat SDF_MONTH_YEAR = new SimpleDateFormat("MMM yyyy",
            Locale.US);
    // Reference time zone for update-related calculations
    public static final TimeZone TIMEZONE_TORONTO = TimeZone.getTimeZone("America/Toronto");
    public static final String VALID_ID_BASE = "[A-Za-z0-9-_]";
    public static final String VALID_NAME_BASE = "[A-Za-z0-9-_ ]";
    public static final Pattern VALID_NAME_PATTERN = Pattern.compile("^" + VALID_NAME_BASE + "+$");
    public static final String[] ARTICLES_THE = {"the", "le", "la", "les", "el", "lo", "los",
            "las", "al", "der", "die", "das", "des", "dem", "il", "het"};
    public static final String[] ARTICLES_OF = {"of", "du", "de", "del", "dello", "della", "dei",
            "degli", "delle", "von", "no"};
    public static final String[] ARTICLES_AN = {"an", "a", "un", "une", "ein", "eine", "einer",
            "eines", "einem", "einen", "uno", "una", "unos", "unas"};

    public static final String[] ARTICLES_TO = {"to", "au", "ad", "in", "zu", "zum"};
    public static final String[] ARTICLES_AND = {"and", "et", "e", "ac", "atque", "und", "y"};
    public static final List<String> ARTICLES_EXCEPTIONS = new ArrayList<String>() {
        {
            addAll(Arrays.asList(ARTICLES_THE));
            addAll(Arrays.asList(ARTICLES_OF));
            addAll(Arrays.asList(ARTICLES_AN));
            addAll(Arrays.asList(ARTICLES_TO));
            addAll(Arrays.asList(ARTICLES_AND));
        }
    };
    // Pattern for matching Roman numerals, taken from: http://stackoverflow.com/a/267405
    public static final Pattern ROMAN_NUMERALS = Pattern.compile("(?i)(?s)^M{0,4}(CM|CD|D?C{0,3})" +
            "(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");
    public static final String SHORT_SUFFIXED_NUMBER_TEMPLATE = "%s%s";
    public static final Pattern CURRENCY_PLURALIZE = Pattern.compile("^(.+?)( +of .+)?$");
    public static final String CURRENCY_NOSUFFIX_TEMPLATE = "%s %s";
    public static final String CURRENCY_SUFFIX_TEMPLATE = "%s %s %s";
    // The number of hours a resolution is on the WA chamber floor
    public static final int WA_RESOLUTION_DURATION = 96;
    public static final Pattern NS_HAPPENINGS_NATION = Pattern.compile("@@(" + VALID_NAME_BASE +
            "+?)@@");
    public static final Pattern NS_HAPPENINGS_REGION = Pattern.compile("%%(" + VALID_NAME_BASE +
            "+?)%%");
    public static final Pattern NS_RMB_POST_LINK =
            Pattern.compile("<a href=\"\\/region=(" + VALID_ID_BASE + "+?)\\/page" +
                    "=display_region_rmb\\?postid=(\\d+?)#p\\d+?\" rel=\"nofollow\">");
    public static final Pattern NS_INTERNAL_LINK = Pattern.compile("<a href=\"(page=.+?)\" " +
            "rel=\"nofollow\">");
    public static final String NS_REGEX_URI_SCHEME = "(?:(?:http|https):\\/\\/nationstates\\" +
            ".net\\/|www\\.nationstates\\.net\\/|(?:http|https):\\/\\/www\\.nationstates\\" +
            ".net\\/|\\/|nationstates\\.net\\/|)";
    public static final Pattern NS_RAW_NATION_LINK =
            Pattern.compile("(?i)\\b" + NS_REGEX_URI_SCHEME + "nation=(" + VALID_ID_BASE + "+?)" +
                    "(?:\\/|)(?:$|\\s)");
    public static final Pattern NS_RAW_REGION_LINK =
            Pattern.compile("(?i)\\b" + NS_REGEX_URI_SCHEME + "region=(" + VALID_ID_BASE + "+?)" +
                    "(?:\\/|)(?:$|\\s)");
    public static final Pattern NS_RAW_REGION_LINK_TG =
            Pattern.compile("(?i)\\b" + NS_REGEX_URI_SCHEME + "region=(" + VALID_ID_BASE + "+?)" +
                    "\\?tgid=[0-9]+?");
    public static final Pattern NS_BBCODE_NATION =
            Pattern.compile("(?i)\\[nation\\](" + VALID_NAME_BASE + "+?)\\[\\/nation\\]");
    public static final Pattern NS_BBCODE_NATION_2 =
            Pattern.compile("(?i)\\[nation=.+?\\](" + VALID_NAME_BASE + "+?)\\[\\/nation\\]");
    public static final Pattern NS_BBCODE_NATION_3 =
            Pattern.compile("(?i)\\[nation=(" + VALID_NAME_BASE + "+?)\\]");
    public static final Pattern NS_BBCODE_REGION =
            Pattern.compile("(?i)\\[region\\](" + VALID_NAME_BASE + "+?)\\[\\/region\\]");
    public static final Pattern NS_BBCODE_REGION_2 =
            Pattern.compile("(?i)\\[region=(" + VALID_NAME_BASE + "+?)\\]");
    public static final Pattern NS_BBCODE_URL_NATION =
            Pattern.compile("(?i)\\[url=" + NS_REGEX_URI_SCHEME + "nation=(" + VALID_ID_BASE +
                    "+?)(?:\\/|)\\]");
    public static final Pattern NS_BBCODE_URL_REGION =
            Pattern.compile("(?i)\\[url=" + NS_REGEX_URI_SCHEME + "region=(" + VALID_ID_BASE +
                    "+?)(?:\\/|)\\]");
    public static final Pattern BBCODE_B = Pattern.compile("(?i)(?s)\\[b\\](.*?)\\[\\/b\\]");
    public static final Pattern BBCODE_I = Pattern.compile("(?i)(?s)\\[i\\](.*?)\\[\\/i\\]");
    public static final Pattern BBCODE_U = Pattern.compile("(?i)(?s)\\[u\\](.*?)\\[\\/u\\]");
    public static final Pattern BBCODE_SUP = Pattern.compile("(?i)(?s)\\[sup\\](.*?)\\[\\/sup\\]");
    public static final Pattern BBCODE_SUB = Pattern.compile("(?i)(?s)\\[sub\\](.*?)\\[\\/sub\\]");
    public static final Pattern BBCODE_STRIKE = Pattern.compile("(?i)(?s)\\[strike\\](.*?)" +
            "\\[\\/strike\\]");
    public static final Pattern BBCODE_PROPOSAL = Pattern.compile("(?i)(?s)\\[proposal=(.*?)\\](" +
            ".*?)\\[\\/proposal\\]");
    public static final Pattern BBCODE_COLOR = Pattern.compile("(?i)(?s)\\[color=(.*?)\\](.*?)" +
            "\\[\\/color\\]");
    public static final Pattern BBCODE_COLOUR = Pattern.compile("(?i)(?s)\\[colour=(.*?)\\](.*?)" +
            "\\[\\/colour\\]");
    public static final Pattern BBCODE_INTERNAL_URL = Pattern.compile("(?i)(?s)\\[url=(" +
            "(?:pages\\/|page=).*?)\\](.*?)\\[\\/url\\]");
    public static final int BBCODE_PERMISSIONS_GENERAL = 0;
    public static final int BBCODE_PERMISSIONS_RMB = 1;
    public static final int BBCODE_PERMISSIONS_REGION = 2;
    public static final Pattern BBCODE_PRE = Pattern.compile("(?i)(?s)\\[pre\\](.*?)\\[\\/pre\\]");
    public static final Pattern HTML_CODE_TAG = Pattern.compile("(?i)(?s)<code>(.*?)<\\/code>");
    public static final String HTML_LEFT_SQUARE_BRACKET = "&#91;";
    public static final String HTML_RIGHT_SQUARE_BRACKET = "&#93;";
    public static final String HTML_COLON = "&#58;";
    public static final String HTML_FORWARD_SLASH = "&#47;";
    public static final String HTML_EQUALS_SIGN = "&#61;";
    public static final String HTML_QUESTION_MARK = "&#63;";
    public static final String PRE_HTML_TEMPLATE = "<code>%s</code>";
    public static final Pattern BBCODE_LIST_ORDERED = Pattern.compile("(?i)(?s)\\[list=(1|a|i)\\]");
    public static final String BBCODE_END_LIST_RAW = "[/list]";
    public static final String BBCODE_END_LIST_REGEX = "\\[\\/list\\]";
    public static final String HTML_UL_FRAGMENT = "<ul";
    public static final String HTML_OL_FRAGMENT = "<ol";
    public static final String HTML_UL_CLOSE = "</ul>";
    public static final String HTML_OL_CLOSE = "</ol>";
    public static final Pattern BBCODE_RESOLUTION_GA_SC = Pattern.compile("(?i)(?s)\\[resolution=" +
            "(GA|SC)#([0-9]+?)\\](.*?)\\[\\/resolution\\]");
    public static final Pattern BBCODE_RESOLUTION_GENERIC = Pattern.compile("(?i)(?s)" +
            "\\[resolution=.+?\\](.*?)\\[\\/resolution\\]");
    public static final Pattern BBCODE_URL_GA =
            Pattern.compile("(?i)(?s)\\[url=" + NS_REGEX_URI_SCHEME + "page=ga(?:\\/|)\\](.*?)" +
                    "\\[\\/url\\]");
    public static final Pattern BBCODE_URL_SC =
            Pattern.compile("(?i)(?s)\\[url=" + NS_REGEX_URI_SCHEME + "page=sc(?:\\/|)\\](.*?)" +
                    "\\[\\/url\\]");
    public static final Pattern BBCODE_URL_RESOLUTION =
            Pattern.compile("(?i)(?s)\\[url=" + NS_REGEX_URI_SCHEME + "page=WA_past_resolutions" +
                    "\\/council=(1|2)\\/start=([0-9]+?)(?:\\/|)\\](.*?)\\[\\/url\\]");
    public static final Pattern BBCODE_URL_RESOLUTION_2 =
            Pattern.compile("(?i)(?s)\\[url=" + NS_REGEX_URI_SCHEME + "page=WA_past_resolutions" +
                    "\\/council=(1|2)\\?start=([0-9]+?)(?:\\/|)\\](.*?)\\[\\/url\\]");
    public static final Pattern BBCODE_URL_RESOLUTION_3 =
            Pattern.compile("(?i)(?s)\\[url=" + NS_REGEX_URI_SCHEME + "page=WA_past_resolution" +
                    "(?:s|)\\/id=([0-9]+?)\\/council=(1|2)(?:\\/|)\\](.*?)\\[\\/url\\]");
    public static final String BBCODE_RESOLUTION_GA = "GA";
    public static final Pattern BBCODE_SPOILER = Pattern.compile("(?i)(?s)\\[spoiler\\](.*?)" +
            "\\[\\/spoiler\\]");
    public static final Pattern BBCODE_SPOILER_2 = Pattern.compile("(?i)(?s)\\[spoiler=(.*?)\\](" +
            ".*?)\\[\\/spoiler\\]");
    public static final Pattern BBCODE_URL = Pattern.compile("(?i)(?s)\\[url=(.*?)\\](.*?)" +
            "\\[\\/url\\]");
    public static final Pattern BBCODE_URL_NOCLOSE = Pattern.compile("(?i)(?s)\\[url=(.*?)\\]");
    public static final Pattern RAW_HTTP_LINK = Pattern.compile("(?i)(?<=^|\\s|<br " +
            "\\/>|<br>|<b>|<i>|<u>)((?:http|https):\\/\\/[^\\s\\[\\<]+)");
    public static final Pattern RAW_WWW_LINK = Pattern.compile("(?i)(?<=^|\\s|<br " +
            "\\/>|<br>|<b>|<i>|<u>)(www\\.[^\\s\\[\\<]+)");
    public static final Pattern BBCODE_QUOTE = Pattern.compile("(?i)(?s)\\[quote\\](.*?)" +
            "\\[\\/quote\\]");
    public static final Pattern BBCODE_QUOTE_1 = Pattern.compile("(?i)(?s)\\[quote=(.*?);" +
            "[0-9]+?\\](.*?)\\[\\/quote\\]");
    public static final Pattern BBCODE_QUOTE_2 = Pattern.compile("(?i)(?s)\\[quote=(.*?)\\](.*?)" +
            "\\[\\/quote\\]");

    // Private constructor
    private SparkleHelper() {
    }

    /**
     * Normalizes a given String to ASCII characters.
     * Source: http://stackoverflow.com/a/15191508
     * @param target
     * @return
     */
    public static String normalizeToAscii(String target) {
        StringBuilder sb = new StringBuilder(target.length());
        target = Normalizer.normalize(target, Normalizer.Form.NFD);
        for (char c : target.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Checks if the passed in name is a valid NationStates name (i.e. A-Z, a-z, 0-9, -, (space)).
     * @param name The name to be checked.
     * @return Bool if valid or not.
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String normalizedName = normalizeToAscii(name);
        Matcher validator = VALID_NAME_PATTERN.matcher(normalizedName);
        return validator.matches();
    }

    /**
     * Turns a proper name into a NationStates ID.
     * @param n the name
     * @return the NS ID
     */
    public static String getIdFromName(String n) {
        if (n != null) {
            String normalizedName = normalizeToAscii(n);
            return normalizedName.toLowerCase(Locale.US).replace(" ", "_");
        }
        return null;
    }

    /**
     * This turns a NationStates ID like greater_tern to a nicely formatted string.
     * In the example's case, greater_tern -> Greater Tern
     * @param id The ID to format.
     * @return String of the nicely-formatted name.
     */
    public static String getNameFromId(String id) {
        if (id != null) {
            // Make sure it's in ~ID form~ first
            id = getIdFromName(id);

            // Split main ID by "_" -- IDs in NationStates have no whitespace, these are replaced
            // by _
            String[] words = id.split("_");
            List<String> properWords = new ArrayList<String>();

            // Loop through each token separated by "_"
            for (int i = 0; i < words.length; i++) {
                String w = words[i].toLowerCase(Locale.US);

                // Further split token by "-"
                String[] subwords = w.split("-");
                List<String> properSubWords = new ArrayList<String>();

                // Loop through the base subtokens
                for (int j = 0; j < subwords.length; j++) {
                    String sw = subwords[j].toLowerCase(Locale.US);

                    // If detected to be a Roman numeral, set to upper case
                    if (ROMAN_NUMERALS.matcher(sw).matches()) {
                        properSubWords.add(sw.toUpperCase(Locale.US));
                    }
                    // If first word, definitely set to normal case
                    else if (i == 0 && j == 0) {
                        properSubWords.add(toNormalCase(sw));
                    }
                    // If part of list of exceptions, don't normal-case
                    else if (ARTICLES_EXCEPTIONS.contains(sw)) {
                        properSubWords.add(sw);
                    }
                    // Otherwise just normal case as usual
                    else {
                        properSubWords.add(toNormalCase(sw));
                    }
                }

                String newSubWord = joinStringList(properSubWords, "-");

                // If last item was a "-", add it back (gets lost in the split)
                if ("-".equals(w.substring(Math.max(0, w.length() - 1)))) {
                    newSubWord = newSubWord + "-";
                }

                properWords.add(newSubWord);
            }

            // Join all the proper words back together with spaces.
            return joinStringList(properWords, " ");
        }
        return null;
    }

    /**
     * Return a human-readable date string from a UTC timestamp.
     * @param c App context
     * @param sec Unix timestamp.
     * @return A human-readable date string (e.g. moments ago, 1 week ago).
     */
    public static String getReadableDateFromUTC(Context c, long sec) {
        long curTime = System.currentTimeMillis();
        long inputTime = sec * 1000L;
        long timeDiff = inputTime - curTime;
        long timeDiffAbs = Math.abs(timeDiff);

        // If the time diff is zero or positive, it's in the future; past otherwise
        String pastIndicator = (timeDiff >= 0) ? c.getString(R.string.time_from_now) :
                c.getString(R.string.time_ago);
        String template = c.getString(R.string.time_generic_template);

        if (timeDiffAbs < 60000L) {
            // less than a minute
            template = String.format(Locale.US, c.getString(R.string.time_moments_template),
                    c.getString(R.string.time_moments), pastIndicator);
        } else if (timeDiffAbs < 3600000L) {
            // less than an hour
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 60000D);
            int minutes = calc.setScale(0, RoundingMode.HALF_UP).intValue();
            template = String.format(Locale.US, template, minutes,
                    c.getResources().getQuantityString(R.plurals.time_minute, minutes),
                    pastIndicator);
        } else if (timeDiffAbs < 86400000L) {
            // less than a day
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 3600000D);
            int hours = calc.setScale(0, RoundingMode.HALF_UP).intValue();
            template = String.format(Locale.US, template, hours,
                    c.getResources().getQuantityString(R.plurals.time_hour, hours), pastIndicator);
        } else if (timeDiffAbs < 604800000L) {
            // less than a week
            BigDecimal calc = BigDecimal.valueOf(timeDiffAbs / 86400000D);
            int days = calc.setScale(0, RoundingMode.HALF_UP).intValue();
            template = String.format(Locale.US, template, days,
                    c.getResources().getQuantityString(R.plurals.time_day, days), pastIndicator);
        } else {
            template = SDF.format(new Date(inputTime));
        }

        return template;
    }

    /**
     * Returns a formatted date given a time in UTC seconds.
     * @param sec UTC seconds
     * @return Formatted date
     */
    public static String getDateFromUTC(long sec) {
        return SDF.format(new Date(sec * 1000L));
    }

    /**
     * Returns the month and year of the given time in UTC seconds.
     * @param sec UTC seconds
     * @return Month and year of the given time
     */
    public static String getMonthYearDateFromUTC(long sec) {
        return SDF_MONTH_YEAR.format(new Date(sec * 1000L));
    }

    /**
     * Returns a number formatted like so: ###,###.## (i.e. US formatting).
     * @param i number to format (can be int, double or long)
     * @return The properly-formatted number as a string.
     */
    public static String getPrettifiedNumber(int i) {
        return NumberFormat.getInstance(Locale.US).format(i);
    }

    public static String getPrettifiedNumber(double d) {
        return getPrettifiedNumber(d, 1);
    }

    public static String getPrettifiedNumber(double d, int numFractionDigits) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(numFractionDigits);
        return nf.format(d);
    }

    public static String getPrettifiedNumber(long l) {
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
    public static String getPopulationFormatted(Context c, double pop) {
        // The lowest population suffix is a million.
        String suffix = c.getString(R.string.million);
        double popHolder = pop;

        if (popHolder >= 1000D) {
            suffix = c.getString(R.string.billion);
            popHolder /= 1000D;
        }

        return String.format(Locale.US, CURRENCY_NOSUFFIX_TEMPLATE,
                getPrettifiedNumber(popHolder), suffix);
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
    public static String getPrettifiedSuffixedNumber(Context c, double d) {
        if (d < 1000000D) {
            // If the money is less than 1 million, we don't need a suffix.
            return getPrettifiedNumber(d);
        } else {
            // NS drops the least significant digits depending on the suffix needed.
            // e.g. A value like 10,000,000 is simply 10 million.
            String suffix = "";
            if (d >= 1000000D && d < 1000000000D) {
                suffix = c.getString(R.string.million);
                d /= 1000000D;
            } else if (d >= 1000000000D && d < 1000000000000D) {
                suffix = c.getString(R.string.billion);
                d /= 1000000000D;
            } else if (d >= 1000000000000D) {
                suffix = c.getString(R.string.trillion);
                d /= 1000000000000D;
            }

            return String.format(Locale.US, CURRENCY_NOSUFFIX_TEMPLATE, getPrettifiedNumber(d),
                    suffix);
        }
    }

    /**
     * Same as above, but starts at 1000 and uses short suffixes.
     * @param c App context
     * @param d Number to format
     * @return Prettified number with short suffix
     */
    public static String getPrettifiedShortSuffixedNumber(Context c, double d) {
        final double absoluteValue = Math.abs(d);
        if (absoluteValue < 1000D) {
            // We only care about cases greater than 1000
            return getPrettifiedNumber(d);
        } else {
            String suffix = "";
            if (absoluteValue >= 1000D && absoluteValue < 1000000D) {
                suffix = c.getString(R.string.thousand_short);
                d /= 1000D;
            } else if (absoluteValue >= 1000000D && absoluteValue < 1000000000D) {
                suffix = c.getString(R.string.million_short);
                d /= 1000000D;
            } else if (absoluteValue >= 1000000000D && absoluteValue < 1000000000000D) {
                suffix = c.getString(R.string.billion_short);
                d /= 1000000000D;
            } else if (absoluteValue >= 1000000000000D) {
                suffix = c.getString(R.string.trillion_short);
                d /= 1000000000000D;
            }

            return String.format(Locale.US, SHORT_SUFFIXED_NUMBER_TEMPLATE,
                    getPrettifiedNumber(d), suffix);
        }
    }

    /**
     * Helper function that capitalizes the first letter of a word.
     * @param w
     * @return
     */
    public static String toNormalCase(String w) {
        String prop = "";
        if (w.length() == 0) {
            prop = w;
        } else if (w.length() == 1) {
            prop = w.substring(0, 1).toUpperCase(Locale.US);
        } else {
            prop = w.substring(0, 1).toUpperCase(Locale.US) + w.substring(1);
        }
        return prop;
    }

    /**
     * Takes in a currency name from the NationStates API and formats it to the
     * plural form using NS format.
     * @param currency The currency unit.
     * @return A nicely-formatted pluralized currency string in NS format.
     */
    public static String getCurrencyPlural(String currency) {
        Matcher m = CURRENCY_PLURALIZE.matcher(currency);
        if (m.matches()) {
            String pluralize = m.group(1);
            String suffix = m.group(2);
            pluralize = English.plural(pluralize);

            if (suffix != null) {
                return pluralize + suffix;
            } else {
                return pluralize;
            }
        }
        return English.plural(currency);
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
    public static String getMoneyFormatted(Context c, long money, String currency) {
        if (money < 1000000L) {
            // If the money is less than 1 million, we don't need a suffix.
            return String.format(Locale.US, CURRENCY_NOSUFFIX_TEMPLATE,
                    getPrettifiedNumber(money), getCurrencyPlural(currency));
        } else {
            // NS drops the least significant digits depending on the suffix needed.
            // e.g. A value like 10,000,000 is simply 10 million.
            String suffix = "";
            if (money >= 1000000L && money < 1000000000L) {
                suffix = c.getString(R.string.million);
                money /= 1000000L;
            } else if (money >= 1000000000L && money < 1000000000000L) {
                suffix = c.getString(R.string.billion);
                money /= 1000000000L;
            } else if (money >= 1000000000000L) {
                suffix = c.getString(R.string.trillion);
                money /= 1000000000000L;
            }

            return String.format(Locale.US, CURRENCY_SUFFIX_TEMPLATE, getPrettifiedNumber(money),
                    suffix, getCurrencyPlural(currency));
        }
    }

    /**
     * Takes in a list of strings and a delimiter and returns a string that combines
     * the elements of the list, separated by the delimiter.
     * @param list List of strings to join.
     * @param delimiter Delimiter to separate each string.
     * @return Merged string.
     */
    public static String joinStringList(Collection<String> list, String delimiter) {
        if (list == null || list.size() < 0) {
            return "";
        }

        StringBuilder mergedString = new StringBuilder();
        int i = 0;
        for (String s : list) {
            if (s != null) {
                mergedString.append(s);

                if (i < list.size() - 1) {
                    mergedString.append(delimiter);
                }
                i++;
            }
        }
        return mergedString.toString();
    }

    /**
     * Makes sure that the specified ID is within range, then returns the appropriate scale.
     * @param censusData Array of raw census units from arrays.xml
     * @param id Census ID to use
     * @return Formatted census data
     */
    public static CensusScale getCensusScale(LinkedHashMap<Integer, CensusScale> censusData,
                                             int id) {
        if (censusData.containsKey(id)) {
            return censusData.get(id);
        }

        // Get last entry (Unknown)
        List<CensusScale> censusScalesList = new ArrayList<CensusScale>(censusData.values());
        return censusScalesList.get(censusScalesList.size() - 1);
    }

    /**
     * Transforms the raw census data from arrays.xml to Java objects.
     * @param rawCensusData Raw census array from arrays.xml
     * @return Linked hashmap of census scale objects
     */
    public static LinkedHashMap<Integer, CensusScale> getCensusScales(String[] rawCensusData) {
        LinkedHashMap<Integer, CensusScale> scales = new LinkedHashMap<Integer, CensusScale>();
        for (int i = 0; i < rawCensusData.length; i++) {
            String[] censusType = rawCensusData[i].split("##");
            CensusScale scale = new CensusScale();
            scale.id = i;
            scale.name = censusType[0];
            scale.unit = censusType[1];
            scale.banner = censusType[2];
            scales.put(i, scale);
        }
        return scales;
    }

    /**
     * Returns a calendar in UTC-5 (specifically, Toronto's calendar).
     * @return
     */
    public static Calendar getUtc5Calendar() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TIMEZONE_TORONTO);
        return cal;
    }

    /**
     * Calculates the remaining time for a WA resolution in human-readable form.
     * @param c App context
     * @param hoursElapsed Number of hours passed since voting started
     * @return Time remaining in human-readable form
     */
    public static String calculateResolutionEnd(Context c, int hoursElapsed) {
        Calendar cal = getUtc5Calendar();

        // Round up to nearest hour
        if (cal.get(Calendar.MINUTE) >= 1) {
            cal.add(Calendar.HOUR, 1);
        }
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        int hoursRemaining = WA_RESOLUTION_DURATION - hoursElapsed;
        // Add an hour on top if DST is not active in EDT/EST time zone
        if (!TIMEZONE_TORONTO.inDaylightTime(cal.getTime())) {
            hoursRemaining += 1;
        }

        cal.add(Calendar.HOUR, hoursRemaining);

        long resolutionEndTimeInMs = cal.getTime().getTime();
        long currentTimeInMs = System.currentTimeMillis();
        // If the calculated end time is in the past, add another 11 hours to the estimate
        if (resolutionEndTimeInMs < currentTimeInMs) {
            cal.add(Calendar.HOUR, 11);
        }

        return getReadableDateFromUTC(c, cal.getTime().getTime() / 1000L);
    }

    /**
     * Checks if the given string indicates that the given stat is for a WA member.
     * @param stat WA state indicator
     * @return bool if stat indicates its a WA member
     */
    public static boolean isWaMember(String stat) {
        return stat.equals(Nation.WA_MEMBER) || stat.equals(Nation.WA_DELEGATE);
    }

    /**
     * Starts the ExploreActivity for the given ID and mode.
     * @param c App context
     * @param n The nation ID
     * @param mode Mode if nation or region
     */
    public static void startExploring(Context c, String n, int mode) {
        Intent exploreActivityLaunch = new Intent(c, ExploreActivity.class);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_ID, n);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_MODE, mode);
        c.startActivity(exploreActivityLaunch);
    }

    /**
     * Returns an OnClickListener that starts the ExploreActivity targeting the specified
     * nation/region.
     * @param c Invoking context
     * @param n Nation/region ID
     * @param mode Mode if nation or region
     * @return OnClickListener invoking ExploreActivity
     */
    public static View.OnClickListener getExploreOnClickListener(final Context c, final String n,
                                                                 final int mode) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExploring(c, n, mode);
            }
        };
    }

    /**
     * Starts the MessageBoardActivity, which opens the specified region's RMB.
     * @param c App context
     * @param regionName Target region name
     */
    public static void startRegionRMB(Context c, String regionName) {
        Intent messageBoardActivity = new Intent(c, MessageBoardActivity.class);
        messageBoardActivity.putExtra(MessageBoardActivity.BOARD_REGION_NAME, regionName);
        c.startActivity(messageBoardActivity);
    }

    /**
     * Starts the TrendsActivity for the given target and census ID.
     * @param c App context
     * @param target Target ID
     * @param mode Mode if nation or region
     * @param id Census ID
     */
    public static void startTrends(Context c, String target, int mode, int id) {
        Intent trendsActivityLaunch = new Intent(c, TrendsActivity.class);
        if (target != null) {
            trendsActivityLaunch.putExtra(TrendsActivity.TREND_DATA_TARGET, target);
        }
        trendsActivityLaunch.putExtra(TrendsActivity.TREND_DATA_MODE, mode);
        trendsActivityLaunch.putExtra(TrendsActivity.TREND_DATA_ID, id);
        c.startActivity(trendsActivityLaunch);
    }

    /**
     * Starts the TelegramComposeActivity and prefills it with data (if provided).
     * @param c App context
     * @param recipients A string of recipients, can be null or empty
     * @param replyId Reply ID, can be filled or TelegramComposeActivity.NO_REPLY_ID
     */
    public static void startTelegramCompose(Context c, String recipients, int replyId) {
        startTelegramCompose(c, recipients, replyId, false);
    }

    public static void startTelegramCompose(Context c, String recipients, int replyId,
                                            boolean isDeveloperTg) {
        Intent telegramComposeActivityLaunch = new Intent(c, TelegramComposeActivity.class);
        telegramComposeActivityLaunch.putExtra(TelegramComposeActivity.RECIPIENTS_DATA, recipients);
        telegramComposeActivityLaunch.putExtra(TelegramComposeActivity.REPLY_ID_DATA, replyId);
        telegramComposeActivityLaunch.putExtra(TelegramComposeActivity.DEVELOPER_TG_DATA,
                isDeveloperTg);
        c.startActivity(telegramComposeActivityLaunch);
    }

    /**
     * Launches a LoginActivity without autologging in.
     * @param c App context
     */
    public static void startAddNation(Context c) {
        Intent loginActivityLaunch = new Intent(c, LoginActivity.class);
        loginActivityLaunch.putExtra(LoginActivity.NOAUTOLOGIN_KEY, true);
        c.startActivity(loginActivityLaunch);
    }

    /**
     * Launches a ReportActivity with the fields filled in.
     * @param c App context
     * @param type Type of report to file
     * @param id Target ID of the report
     * @param user Target user of the report
     */
    public static void startReport(Context c, int type, int id, String user) {
        Intent reportActivityLaunch = new Intent(c, ReportActivity.class);
        reportActivityLaunch.putExtra(ReportActivity.REPORT_TYPE, type);
        reportActivityLaunch.putExtra(ReportActivity.REPORT_ID, id);
        reportActivityLaunch.putExtra(ReportActivity.REPORT_USER, user);
        c.startActivity(reportActivityLaunch);
    }

    /**
     * Launches the ResolutionActivity to show a given resolution (or the current resolution).
     * @param c App context
     * @param councilId ID of target council (GA or SC)
     * @param resolutionId ID of target resolution; shows current if null
     */
    public static void startResolution(Context c, Integer councilId, Integer resolutionId) {
        Intent resolutionActivityIntent = new Intent(c, ResolutionActivity.class);
        resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_COUNCIL_ID, councilId);
        if (resolutionId != null) {
            resolutionActivityIntent.putExtra(ResolutionActivity.TARGET_OVERRIDE_RES_ID,
                    resolutionId);
        }
        c.startActivity(resolutionActivityIntent);
    }

    /**
     * Embeds a link to ExploreActivity for a given nation/region target in an arbitrary text.
     * @param template The original text where a link will be embedded.
     * @param oldContent The substring in the template to be replaced with a linkified nation.
     * @param exploreTarget The name of the nation/region to be used in the link.
     * @param mode If target is a nation or a region.
     * @return Returns the linkified text.
     */
    public static String addExploreActivityLink(String template, String oldContent,
                                                String exploreTarget, int mode) {
        final String urlFormat = "<a href=\"%s/%d\">%s</a>";
        String tempHolder = template;
        // Name needs to be formatted back to its NationStates ID first for the URL.
        String targetActivity = ExploreActivity.EXPLORE_TARGET + getIdFromName(exploreTarget);
        targetActivity = String.format(Locale.US, urlFormat, targetActivity, mode, exploreTarget);
        tempHolder = tempHolder.replace(oldContent, targetActivity);
        return tempHolder;
    }

    /**
     * A helper function used to get all instances found of a given regex statement
     * and linkify them to ExploreActivity.
     * @param content Target content
     * @param regex Regex statement
     * @param mode If nation or region
     * @return
     */
    public static String addExploreActivityLinks(String content, Pattern regex, int mode) {
        String holder = content;
        List<DataPair> set = getReplacePairsFromRegex(regex, holder, true);

        for (DataPair n : set) {
            holder = addExploreActivityLink(holder, n.key, n.value, mode);
        }

        return holder;
    }

    /**
     * Wrapper for Html.fromHtml, which has different calls depending on the API version.
     * @param src
     * @return
     */
    public static Spanned fromHtml(String src) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(src, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(src);
        }
    }

    /**
     * Properly escapes HTML to be POSTed to NS servers.
     * @param src Original string.
     * @return Escaped string.
     */
    public static String escapeHtml(String src) {
        if (src != null && src.length() > 0) {
            src = Html.escapeHtml(src);
            src = src.replace("&#10;", "\n");
            return src;
        }
        return "";
    }

    /**
     * Replaces malformed HTML characters and artifacts with their proper form.
     * @param content
     * @return
     */
    public static String replaceMalformedHtmlCharacters(String content) {
        String holder = content;
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        holder = holder.replace("\u0081", "");
        holder = holder.replace("&#129;", "");
        return holder;
    }

    /**
     * Basic HTML formatter that returns a styled version of the string.
     * @param content Target content
     * @return Styled spanned object
     */
    public static Spanned getHtmlFormatting(String content) {
        return getHtmlFormatting(content, true);
    }

    public static Spanned getHtmlFormatting(String content, boolean shouldClean) {
        if (content == null) {
            content = "";
        }
        String holder = content;
        if (shouldClean) {
            holder = Jsoup.clean(holder, Safelist.none().addTags("br"));
        }
        holder = replaceMalformedHtmlCharacters(holder);
        return fromHtml(holder);
    }

    /**
     * Given HTML content, returns a string with all HTML tags stripped out.
     * @param content HTML content
     * @return String with no tags
     */
    public static String getStrippedHtml(final String content) {
        return Jsoup.clean(content, Safelist.none());
    }

    /**
     * A formatter used to linkify @@nation@@ and %%region%% text in NationStates' happenings.
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setHappeningsFormatting(Context c, TextView t, String content) {
        content = Jsoup.clean(content, BASE_URI_NOSLASH,
                Safelist.basic().preserveRelativeLinks(true));
        content = replaceMalformedHtmlCharacters(content);

        // Replace RMB links with targets to the RMB activity
        content = regexDoubleReplace(content, NS_RMB_POST_LINK,
                "<a href=\"" + MessageBoardActivity.RMB_TARGET + "%s/%s\">");

        // Replace internal links with valid links
        content = regexReplace(content, NS_INTERNAL_LINK, "<a href=\"" + BASE_URI + "%s\">");

        // Linkify nations (@@NATION@@)
        content = addExploreActivityLinks(content, NS_HAPPENINGS_NATION,
                ExploreActivity.EXPLORE_NATION);
        content = addExploreActivityLinks(content, NS_HAPPENINGS_REGION,
                ExploreActivity.EXPLORE_REGION);

        if (content.contains("EO:")) {
            String[] newTargets = content.split(":");
            String newTarget = newTargets[1].substring(0, newTargets[1].length() - 1);
            String template = String.format(Locale.US, c.getString(R.string.region_eo), content);
            content = addExploreActivityLink(template, "EO:" + newTarget + ".",
                    getNameFromId(newTarget), ExploreActivity.EXPLORE_REGION);
        }

        if (content.contains("EC:")) {
            String[] newTargets = content.split(":");
            String newTarget = newTargets[1].substring(0, newTargets[1].length() - 1);
            String template = String.format(Locale.US, c.getString(R.string.region_ec), content);
            content = addExploreActivityLink(template, "EC:" + newTarget + ".",
                    getNameFromId(newTarget), ExploreActivity.EXPLORE_REGION);
        }

        // In case there are no nations or regions to linkify, set and style TextView here too
        setStyledTextView(c, t, content);
    }

    /**
     * Transform NationStates' BBCode-formatted content into HTML
     * @param c App context
     * @param content Raw BBCode content
     * @return HTML-formatted text
     */
    public static String transformBBCodeToHtml(Context c, String content) {
        return transformBBCodeToHtml(c, content, BBCODE_PERMISSIONS_GENERAL);
    }

    /**
     * Transform NationStates' BBCode-formatted content into HTML
     * @param c App context
     * @param content Raw BBCode content
     * @param permissions Level of permissions to transform BBCode
     * @return HTML-formatted text
     */
    public static String transformBBCodeToHtml(Context c, String content, int permissions) {
        if (content == null) {
            return null;
        } else if (content.length() <= 0) {
            return "";
        }

        String holder = content.trim();
        holder = holder.replace("\n", "<br>");
        holder = replaceMalformedHtmlCharacters(holder);
        holder = Jsoup.clean(holder, Safelist.simpleText().addTags("br"));

        // First handle the [pre] tag -- anything inside must not be formatted
        holder = regexPreFormat(holder);

        // Then process lists (they're problematic!)
        holder = regexListFormat(holder);
        // Re-escape all the stuff Jsoup cleared out
        holder = regexPreFormat(holder);

        // Handle the [hr] tag; there's no Android equivalent so we'll cheat here
        holder = holder.replaceAll("(?s)(?i)(?:<br>|\\s)*\\[hr\\](?:<br>|\\s)*", "<br><br><center" +
                ">—————</center><br>");

        // Linkify nations and regions
        holder = addExploreActivityLinks(holder, NS_BBCODE_NATION, ExploreActivity.EXPLORE_NATION);
        holder = addExploreActivityLinks(holder, NS_BBCODE_NATION_2,
                ExploreActivity.EXPLORE_NATION);
        holder = addExploreActivityLinks(holder, NS_BBCODE_NATION_3,
                ExploreActivity.EXPLORE_NATION);
        holder = addExploreActivityLinks(holder, NS_BBCODE_REGION, ExploreActivity.EXPLORE_REGION);
        holder = addExploreActivityLinks(holder, NS_BBCODE_REGION_2,
                ExploreActivity.EXPLORE_REGION);

        // Replace raw NS nation and region links with Stately versions
        // It's in this order since the last three lines grab raw URLs and formats them
        holder = regexReplace(holder, NS_BBCODE_URL_NATION,
                "[url=" + ExploreActivity.EXPLORE_TARGET + "%s/" + ExploreActivity.EXPLORE_NATION + "]");
        holder = regexReplace(holder, NS_BBCODE_URL_REGION,
                "[url=" + ExploreActivity.EXPLORE_TARGET + "%s/" + ExploreActivity.EXPLORE_REGION + "]");
        holder = addExploreActivityLinks(holder, NS_RAW_NATION_LINK,
                ExploreActivity.EXPLORE_NATION);
        holder = addExploreActivityLinks(holder, NS_RAW_REGION_LINK,
                ExploreActivity.EXPLORE_REGION);
        holder = addExploreActivityLinks(holder, NS_RAW_REGION_LINK_TG,
                ExploreActivity.EXPLORE_REGION);

        // Other BBCode transforms
        holder = regexReplace(holder, BBCODE_B, "<b>%s</b>");
        holder = regexReplace(holder, BBCODE_I, "<i>%s</i>");
        holder = regexReplace(holder, BBCODE_U, "<u>%s</u>");
        holder = regexReplace(holder, BBCODE_SUP, "<sup>%s</sup>");
        holder = regexReplace(holder, BBCODE_SUB, "<sub>%s</sub>");
        holder = regexReplace(holder, BBCODE_STRIKE, "<strike>%s</strike>");
        holder = regexDoubleReplace(holder, BBCODE_PROPOSAL,
                "<a href=\"" + Resolution.PATH_PROPOSAL + "\">%s</a>");
        holder = regexResolutionFormat(holder);
        holder = regexExtract(holder, BBCODE_RESOLUTION_GENERIC);

        if (permissions == BBCODE_PERMISSIONS_REGION) {
            holder = regexDoubleReplace(holder, BBCODE_COLOR, "<font color=\"%s\">%s</font>");
            holder = regexDoubleReplace(holder, BBCODE_COLOUR, "<font color=\"%s\">%s</font>");
        }

        holder = regexDoubleReplace(holder, BBCODE_INTERNAL_URL,
                "<a href=\"" + BASE_URI_NOSLASH + "/%s\">%s</a>");
        holder = regexGenericUrlFormat(c, holder);

        if (permissions == BBCODE_PERMISSIONS_RMB) {
            holder = regexQuoteFormat(holder);
        }

        return holder;
    }

    /**
     * Helper used for setting and styling an HTML string into a TextView.
     * @param c App context
     * @param t Target TextView
     * @param holder Content
     */
    public static void setStyledTextView(Context c, TextView t, String holder) {
        if (t instanceof HtmlTextView) {
            try {
                ((HtmlTextView) t).setHtml(holder);
            } catch (Exception e) {
                logError(e.toString());
                logError(holder);
                t.setText(c.getString(R.string.bbcode_parse_error));
                t.setTypeface(t.getTypeface(), Typeface.ITALIC);
            }
        } else {
            t.setText(fromHtml(holder));
        }
        styleLinkifiedTextView(c, t);
    }

    /**
     * Overloaded to deal with spoilers.
     */
    public static void setStyledTextView(Context c, TextView t, String holder, FragmentManager fm) {
        // Extract and replace spoilers beforehand
        List<Spoiler> spoilers = getSpoilerReplacePairs(c, holder);
        for (int i = 0; i < spoilers.size(); i++) {
            Spoiler s = spoilers.get(i);
            holder = holder.replace(s.raw, s.replacer);
        }

        // Stylize the existing text
        setStyledTextView(c, t, holder);

        // Linkify spoilers, if they exist
        if (spoilers.size() > 0) {
            Spannable span = new SpannableString(t.getText());
            String rawSpan = span.toString();
            int startFromIndex = 0;

            for (int i = 0; i < spoilers.size(); i++) {
                Spoiler s = spoilers.get(i);
                int start = rawSpan.indexOf(s.replacer, startFromIndex);
                if (start != -1) {
                    int end = start + s.replacer.length();
                    startFromIndex = end;
                    SpoilerSpan clickyDialog = new SpoilerSpan(c, s, fm);
                    span.setSpan(clickyDialog, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            t.setText(span);
        }
    }

    /**
     * Stylify text view to primary colour and no underline
     * @param c App context
     * @param t TextView
     */
    public static void styleLinkifiedTextView(Context c, TextView t) {
        // Get individual spans and replace them with clickable ones.
        Spannable s = new SpannableString(t.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(c, span.getURL());
            s.setSpan(span, start, end, 0);
        }

        t.setText(s);
        // Need to set this to allow for clickable TextView links.
        if (!(t instanceof HtmlTextView)) {
            t.setMovementMethod(LinkMovementMethod.getInstance());
            t.setLongClickable(false);
        }
    }

    /**
     * Given a regex and some content, get all pairs of (old, new) where old is a string matching
     * the regex in the content, and new is the proper name to replace the old string.
     * @param regex Regex statement
     * @param content Target content
     * @return
     */
    public static List<DataPair> getReplacePairsFromRegex(Pattern regex, String content,
                                                          boolean isName) {
        String holder = content;
        // (old, new) replacement pairs
        List<DataPair> replacePairs = new ArrayList<DataPair>();

        Matcher m = regex.matcher(holder);
        while (m.find()) {
            String properFormat;
            if (isName) {
                // Nameify the ID found and put the (old, new) pair into the map
                properFormat = getNameFromId(m.group(1));
            } else {
                properFormat = m.group(1);
            }
            replacePairs.add(new DataPair(m.group(), properFormat));
        }
        return replacePairs;
    }

    public static List<DataPair> getDoubleReplacePairsFromRegex(Pattern regex,
                                                                String afterFormat,
                                                                String content) {
        String holder = content;
        // (old, new) replacement pairs
        List<DataPair> replacePairs = new ArrayList<DataPair>();

        Matcher m = regex.matcher(holder);
        while (m.find()) {
            String properFormat = String.format(Locale.US, afterFormat, m.group(1), m.group(2));
            replacePairs.add(new DataPair(m.group(), properFormat));
        }
        return replacePairs;
    }

    /**
     * Given a list of (old, new) pairs and a string, replace all instances of old with new in
     * that string.
     * @param content
     * @param replacePairs
     * @return
     */
    public static String replaceFromReplacePairs(String content, List<DataPair> replacePairs) {
        String holder = content;
        for (DataPair dp : replacePairs) {
            holder = holder.replace(dp.key, dp.value);
        }
        return holder;
    }

    /**
     * Replaces all matches of a given regex with the supplied string template. Only accepts
     * one parameter.
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @return Returns content with all matched substrings replaced
     */
    public static String regexReplace(String target, Pattern regexBefore, String afterFormat) {
        String holder = target;
        List<DataPair> set = getReplacePairsFromRegex(regexBefore, holder, false);

        for (DataPair n : set) {
            n.value = String.format(Locale.US, afterFormat, n.value);
        }
        holder = replaceFromReplacePairs(holder, set);

        return holder;
    }

    /**
     * Similar to regexReplace, but takes in two characters
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @return
     */
    public static String regexDoubleReplace(String target, Pattern regexBefore,
                                            String afterFormat) {
        String holder = target;
        List<DataPair> set = getDoubleReplacePairsFromRegex(regexBefore, afterFormat, holder);
        holder = replaceFromReplacePairs(holder, set);
        return holder;
    }

    /**
     * Extracts a capture group from a regex
     * @param target Target content
     * @param regex Regex
     * @return
     */
    public static String regexExtract(String target, Pattern regex) {
        String holder = target;
        List<DataPair> set = getReplacePairsFromRegex(regex, holder, false);
        holder = replaceFromReplacePairs(holder, set);
        return holder;
    }

    /**
     * Removes all substrings which match the regex
     * @param target Target content
     * @param regex Regex
     * @return
     */
    public static String regexRemove(String target, Pattern regex) {
        String holder = target;
        List<DataPair> set = getReplacePairsFromRegex(regex, holder, false);
        for (DataPair n : set) {
            n.value = "";
        }
        holder = replaceFromReplacePairs(holder, set);
        return holder;
    }

    /**
     * Processes [pre] tags -- must be wrapped using <code> tag and contents must not be formatted.
     * @param target
     * @return
     */
    public static String regexPreFormat(String target) {
        String holder = target;
        holder = regexPreFormatHelper(BBCODE_PRE, holder);
        holder = regexPreFormatHelper(HTML_CODE_TAG, holder);
        return holder;
    }

    /**
     * Helper function for regexPreFormat -- actually does the processing based on the passed in
     * pattern.
     * @param pattern
     * @param target
     * @return
     */
    public static String regexPreFormatHelper(Pattern pattern, String target) {
        String holder = target;
        Matcher m = pattern.matcher(holder);
        while (m.find()) {
            String rawContent = m.group(1);
            rawContent = rawContent.replace("[", HTML_LEFT_SQUARE_BRACKET);
            rawContent = rawContent.replace("]", HTML_RIGHT_SQUARE_BRACKET);
            rawContent = rawContent.replace(":", HTML_COLON);
            rawContent = rawContent.replace("/", HTML_FORWARD_SLASH);
            rawContent = rawContent.replace("=", HTML_EQUALS_SIGN);
            rawContent = rawContent.replace("?", HTML_QUESTION_MARK);
            holder = holder.replace(m.group(), String.format(Locale.US, PRE_HTML_TEMPLATE,
                    rawContent));
        }
        return holder;
    }

    /**
     * Processes [list] tags and their children.
     * @param content
     * @return
     */
    public static String regexListFormat(String content) {
        String holder = content;
        holder = holder.replace("[list]", "<ul>");
        holder = regexReplace(holder, BBCODE_LIST_ORDERED, "<ol type='%s'>");

        // Properly close <ul> and <ol> tags using an honest-to-Holo parser
        if (holder.contains(BBCODE_END_LIST_RAW)) {
            LinkedList<String> htmlStack = new LinkedList<String>();
            int scanIndex = 0;
            while (scanIndex < holder.length()) {
                int nextIndexUl = holder.indexOf(HTML_UL_FRAGMENT, scanIndex);
                int nextIndexOl = holder.indexOf(HTML_OL_FRAGMENT, scanIndex);
                int nextIndexEnd = holder.indexOf(BBCODE_END_LIST_RAW, scanIndex);

                // If the next element detected is <ul>, add that to the stack and move forward
                if (nextIndexUl != -1 && nextIndexUl < nextIndexOl && nextIndexUl < nextIndexEnd) {
                    htmlStack.push(HTML_UL_FRAGMENT);
                    scanIndex = nextIndexUl + HTML_UL_FRAGMENT.length();
                }
                // If the next element detected is <ol>, add that to the stack and move forward
                else if (nextIndexOl != -1 && nextIndexOl < nextIndexEnd) {
                    htmlStack.push(HTML_OL_FRAGMENT);
                    scanIndex = nextIndexOl + HTML_OL_FRAGMENT.length();
                }
                // If the next element detected is [/list], add that to the stack and move forward
                else if (nextIndexEnd != -1) {
                    String replacer = HTML_UL_CLOSE;

                    if (!htmlStack.isEmpty()) {
                        String previousElement = htmlStack.pop();
                        if (HTML_OL_FRAGMENT.equals(previousElement)) {
                            replacer = HTML_OL_CLOSE;
                        }
                    }

                    holder = holder.replaceFirst(BBCODE_END_LIST_REGEX, replacer);
                    scanIndex = nextIndexEnd + HTML_UL_CLOSE.length();
                }
                // If no other instances of [/list] exists, stop.
                else {
                    break;
                }
            }
        }

        // Transform and cleanup <li> elements
        holder = holder.replace("[*]", "<li>");
        holder = holder.replace("<li><br><br>", "<li>");
        holder = holder.replace("<li><br>", "<li>");
        holder = Jsoup.clean(holder, Safelist.relaxed());
        return holder;
    }

    /**
     * Processes the resolution tag by linkifying it to ResolutionActivity.
     * @param content
     * @return
     */
    public static String regexResolutionFormat(String content) {
        String holder = content;

        Matcher m = BBCODE_RESOLUTION_GA_SC.matcher(holder);
        while (m.find()) {
            int councilId = BBCODE_RESOLUTION_GA.equals(m.group(1)) ? Assembly.GENERAL_ASSEMBLY :
                    Assembly.SECURITY_COUNCIL;
            int resolutionId = Integer.valueOf(m.group(2)) - 1;
            String properFormat = regexResolutionFormatHelper(councilId, resolutionId, m.group(3));
            holder = holder.replace(m.group(), properFormat);
        }

        holder = regexReplace(holder, BBCODE_URL_GA,
                regexResolutionFormatHelper(Assembly.GENERAL_ASSEMBLY, -2, "%s"));
        holder = regexReplace(holder, BBCODE_URL_SC,
                regexResolutionFormatHelper(Assembly.SECURITY_COUNCIL, -2, "%s"));

        holder = regexResolutionReplacer(holder, BBCODE_URL_RESOLUTION, false, false);
        holder = regexResolutionReplacer(holder, BBCODE_URL_RESOLUTION_2, false, false);
        holder = regexResolutionReplacer(holder, BBCODE_URL_RESOLUTION_3, true, true);

        return holder;
    }

    public static String regexResolutionReplacer(String target, Pattern pattern,
                                                 boolean shouldOffsetByOne,
                                                 boolean shouldSwitchCouncilId) {
        String holder = target;
        Matcher m = pattern.matcher(target);
        while (m.find()) {
            int councilId = Integer.valueOf(m.group(shouldSwitchCouncilId ? 2 : 1));
            int resolutionId = Integer.valueOf(m.group(shouldSwitchCouncilId ? 1 : 2));
            if (shouldOffsetByOne) {
                resolutionId--;
            }
            String properFormat = regexResolutionFormatHelper(councilId, resolutionId, m.group(3));
            holder = holder.replace(m.group(), properFormat);
        }
        return holder;
    }

    /**
     * Helper function for building links to ResolutionActivity
     * @param councilId
     * @param resolutionId
     * @param content
     * @return
     */
    public static String regexResolutionFormatHelper(int councilId, int resolutionId,
                                                     String content) {
        return String.format(Locale.US, "<a href=\"" + ResolutionActivity.RESOLUTION_TARGET + "%d" +
                "/%d\">%s</a>", councilId, resolutionId, content);
    }

    /**
     * Helper function that extracts spoilers from BBCode for later use.
     * @param c App context
     * @param target Target content
     * @return List of spoilers
     */
    public static List<Spoiler> getSpoilerReplacePairs(Context c, String target) {
        String holder = target;
        List<Spoiler> spoilers = new ArrayList<Spoiler>();

        // Handle spoilers without titles first
        Matcher m1 = BBCODE_SPOILER.matcher(holder);
        while (m1.find()) {
            Spoiler s = new Spoiler();
            s.content = m1.group(1);
            s.raw = m1.group();
            s.replacer = c.getString(R.string.spoiler_warn_link);
            spoilers.add(s);
        }

        // Handle spoilers with titles next
        Matcher m2 = BBCODE_SPOILER_2.matcher(holder);
        while (m2.find()) {
            Spoiler s = new Spoiler();
            // Gets rid of HTML in title
            s.title = Jsoup.parse(m2.group(1)).text();
            s.content = m2.group(2);
            s.raw = m2.group();
            s.replacer = String.format(Locale.US, c.getString(R.string.spoiler_warn_title_link),
                    s.title);
            spoilers.add(s);
        }

        return spoilers;
    }

    /**
     * Finds all raw URL links and URL tags and linkifies them properly in a nice format.
     * @param c App context.
     * @param content Target string.
     * @return Parsed results.
     */
    public static String regexGenericUrlFormat(Context c, String content) {
        String holder = content;

        List<DataPair> replaceBasic = new ArrayList<DataPair>();
        Matcher m0 = BBCODE_URL.matcher(holder);
        while (m0.find()) {
            String template = "<a href=\"%s\">%s</a>";
            Uri link = Uri.parse(m0.group(1)).normalizeScheme();
            if (link.getScheme() == null) {
                template = "<a href=\"http://%s\">%s</a>";
            }
            String replaceText = String.format(Locale.US, template, link, m0.group(2));
            replaceBasic.add(new DataPair(m0.group(), replaceText));
        }
        holder = replaceFromReplacePairs(holder, replaceBasic);

        List<DataPair> replaceRaw = new ArrayList<DataPair>();

        Matcher m1 = RAW_HTTP_LINK.matcher(holder);
        while (m1.find()) {
            Uri link = Uri.parse(m1.group(1)).normalizeScheme();
            String replaceText = String.format(Locale.US, c.getString(R.string.clicky_link_http),
                    link.toString(), link.getHost());
            replaceRaw.add(new DataPair(m1.group(), replaceText));
        }

        Matcher m2 = RAW_WWW_LINK.matcher(holder);
        while (m2.find()) {
            Uri link = Uri.parse("http://" + m2.group(1)).normalizeScheme();
            String replaceText = String.format(Locale.US, c.getString(R.string.clicky_link_http),
                    link.toString(), link.getHost());
            replaceRaw.add(new DataPair(m2.group(), replaceText));
        }

        for (DataPair e : replaceRaw) {
            holder = holder.replaceAll("(?<=^|\\s|<br \\/>|<br>|<b>|<i>|<u>)\\Q" + e.key + "\\E" +
                    "(?=$|[\\s\\[\\<])", e.value);
        }

        // Do this last so it doesn't interfere with complete tags
        List<DataPair> replaceNoClose = new ArrayList<DataPair>();
        Matcher m3 = BBCODE_URL_NOCLOSE.matcher(holder);
        while (m3.find()) {
            String replaceText = "";
            Uri link = Uri.parse(m3.group(1)).normalizeScheme();
            if (link.getScheme() == null) {
                String finalLink = "http://" + link;
                Uri finalLinkUri = Uri.parse(finalLink).normalizeScheme();
                replaceText = String.format(Locale.US, c.getString(R.string.clicky_link_http),
                        finalLink, finalLinkUri.getHost());
            } else if (link.getScheme().equals(ExploreActivity.EXPLORE_PROTOCOL)) {
                replaceText = String.format(Locale.US, c.getString(R.string.clicky_link_internal),
                        link, getNameFromId(link.getHost()));
            } else {
                replaceText = String.format(Locale.US, c.getString(R.string.clicky_link_http),
                        link, link.getHost());
            }
            replaceNoClose.add(new DataPair(m3.group(), replaceText));
        }
        holder = replaceFromReplacePairs(holder, replaceNoClose);

        return holder;
    }

    /**
     * Used for formatting blockquotes
     * @param content Original string
     * @return Formatted string
     */
    public static String regexQuoteFormat(String content) {
        String holder = content;

        // handle basic quotes
        holder = regexReplace(holder, BBCODE_QUOTE, "<blockquote><i>%s</i></blockquote>");

        // handle quotes with parameters on them
        // in this case, [quote=name;id]...
        holder = regexQuoteFormatHelper(BBCODE_QUOTE_1, holder);
        // in this case, just [quote=name]...
        holder = regexQuoteFormatHelper(BBCODE_QUOTE_2, holder);

        return holder;
    }

    /**
     * Convenience class used by regexQuoteFormat() to format blockquotes with author attrib.
     * @param regex Regex to use
     * @param content Original string
     * @return Formatted string
     */
    public static String regexQuoteFormatHelper(Pattern regex, String content) {
        String holder = content;
        List<DataPair> replacePairs = new ArrayList<DataPair>();
        Matcher m = regex.matcher(holder);
        while (m.find()) {
            String properFormat = String.format(Locale.US, "<blockquote><i>@@%s@@:<br " +
                    "/>%s</i></blockquote>", getNameFromId(m.group(1)), m.group(2));
            if (Post.POST_NS_MODERATORS.equals(m.group(1))) {
                properFormat = String.format(Locale.US, "<blockquote><i>%s:<br " +
                        "/>%s</i></blockquote>", Post.POST_NS_MODERATORS, m.group(2));
            }
            replacePairs.add(new DataPair(m.group(), properFormat));
        }
        holder = replaceFromReplacePairs(holder, replacePairs);
        holder = addExploreActivityLinks(holder, NS_HAPPENINGS_NATION,
                ExploreActivity.EXPLORE_NATION);
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
    public static void makeSnackbar(View view, String str) {
        Snackbar.make(view, str, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Logs a system error. Mostly used so that APP_TAG doesn't have to repeat.
     * @param message Message
     */
    public static void logError(String message) {
        Log.e(APP_TAG, message);
    }
}
