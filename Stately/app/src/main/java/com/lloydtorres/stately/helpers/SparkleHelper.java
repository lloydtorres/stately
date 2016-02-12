package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.ads.mediation.flurry.FlurryAdapterExtras;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.login.LoginActivity;

import org.atteo.evo.inflector.English;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.ocpsoft.prettytime.PrettyTime;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.NumberFormat;
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
    public static final String API_VERSION = "7";
    // NationStates API
    public static final String BASE_URI = "https://www.nationstates.net/";

    // Keys to user name and autologin
    public static final String VAR_NAME = "var_name";
    public static final String VAR_AUTOLOGIN = "var_autologin";

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

    // An array of colours used for each sector
    public static final int[] sectorColours = { R.color.colorSector0,
            R.color.colorSector1,
            R.color.colorSector2,
            R.color.colorSector3,
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

    // Used for string verification for nation and region IDs
    public static final CharMatcher CHAR_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT
            .or(CharMatcher.WHITESPACE)
            .or(CharMatcher.anyOf("-"))
            .precomputed();

    // Initialized to provide human-readable date strings for Date objects
    public static final PrettyTime prettyTime = new PrettyTime();

    /**
     * VALIDATION
     * These are functions used to validate inputs.
     */

    /**
     * Checks if the passed in name is a valid NationStates name (i.e. A-Z, a-z, 0-9, -, (space)).
     * @param name The name to be checked.
     * @return Bool if valid or not.
     */
    public static boolean isValidName(String name)
    {
        return CHAR_MATCHER.matchesAllOf(name);
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
        return n.toLowerCase().replace(" ", "_");
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
            // IDs can also be separated by dashes, but we want to preserve this.
            String[] subWords = w.split("-");
            // A list of properly-formatted words connected by a dash.
            List<String> properSubWords = new ArrayList<String>();

            for (String sw: subWords)
            {
                // Transform word from lower case to proper case.
                // This is very hacky, I know.
                properSubWords.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, sw));
            }

            // Join the word back with dashes and add it to main list.
            // If the original target word had no dashes, this would only have an element of one.
            String subFin = Joiner.on("-").join(properSubWords);
            properWords.add(subFin);
        }

        // Join all the proper words back together with spaces.
        return Joiner.on(" ").skipNulls().join(properWords);
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
     * @param sec Unix timestamp.
     * @return A human-readable date string (e.g. moments ago, 1 week ago).
     */
    public static String getReadableDateFromUTC(long sec)
    {
        Date d = new Date(sec * 1000L);
        return prettyTime.format(d);
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
            return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(money), English.plural(currency));
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

            return String.format(c.getString(R.string.val_suffix_currency), getPrettifiedNumber(money), suffix, English.plural(currency));
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
        p.setHoleColorTransparent(true);
        p.setHoleRadius(60f);
        p.setTransparentCircleRadius(65f);
        p.setCenterTextSize(20);
        p.setRotationEnabled(false);

        p.setOnChartValueSelectedListener(new PieChartListener(c, p, chartLabels));
        return p;
    }

    /**
     * UTILITY
     * These are convenient tools to call from any class.
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
     * Starts the ExploreNationActivity for the given nation ID.
     * @param c App context
     * @param n The nation ID
     */
    public static void startExploring(Context c, String n, int mode)
    {
        Intent exploreActivityLaunch = new Intent(c, ExploreActivity.class);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_ID, n);
        exploreActivityLaunch.putExtra(ExploreActivity.EXPLORE_MODE, mode);
        c.startActivity(exploreActivityLaunch);
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
     * @param hoursElapsed Number of hours passed since voting started
     * @return Time remaining in human-readable form
     */
    public static String calculateResolutionEnd(int hoursElapsed)
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
        return prettyTime.format(d);
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
            float votePercentFor = (((float) voteFor) * 100f)/voteTotal;
            float votePercentAgainst = (((float) voteAgainst) * 100f)/voteTotal;

            List<String> chartLabels = new ArrayList<String>();
            List<Entry> chartEntries = new ArrayList<Entry>();

            // Set data
            int i = 0;
            chartLabels.add(c.getString(R.string.wa_for));
            chartEntries.add(new Entry((float) votePercentFor, i++));
            chartLabels.add(c.getString(R.string.wa_against));
            chartEntries.add(new Entry((float) votePercentAgainst, i++));

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
     * Initializes an ad.
     * @param view Target view
     * @param viewId ID of the AdView
     */
    public static void initAd(View view, int viewId)
    {
        AdView mAdView = (AdView) view.findViewById(viewId);
        mAdView.setAdListener(new GenericAdListener(mAdView));

        FlurryAdapterExtras flurryAdapterExtras = new FlurryAdapterExtras();
        flurryAdapterExtras.setLogEnabled(true);

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtras(flurryAdapterExtras)
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("03633EF76BA22B61C33C65CDCDB8D8B0")
                .addTestDevice("CB2FCCF13C56C7ADC4F190827D780B91")
                .build();
        mAdView.loadAd(adRequest);
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
        targetActivity = targetActivity + nTarget.toLowerCase().replace(" ", "_");
        targetActivity = String.format(urlFormat, targetActivity, mode, nTarget);

        tempHolder = tempHolder.replace(oTarget, targetActivity);

        if (t instanceof HtmlTextView)
        {
            ((HtmlTextView)t).setHtmlFromString(tempHolder, new HtmlTextView.RemoteImageGetter());
        }
        else
        {
            t.setText(Html.fromHtml(tempHolder));
        }
        // Stylify the TextView.
        styleLinkifiedTextView(c, t);

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
    public static Set<Map.Entry<String, String>> getReplacePairFromRegex(String regex, String content, boolean isName)
    {
        regex = "(?i)" + regex;
        String holder = content;
        // (old, new) replacement pairs
        Map<String, String> replacePairs = new HashMap<String, String>();

        Matcher m = Pattern.compile(regex).matcher(holder);
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

    public static Set<Map.Entry<String, String>> getDoubleReplacePairFromRegex(String regex, String afterFormat, String content)
    {
        regex = "(?s)(?i)" + regex;
        String holder = content;
        // (old, new) replacement pairs
        Map<String, String> replacePairs = new HashMap<String, String>();

        Matcher m = Pattern.compile(regex).matcher(holder);
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
    public static String linkifyHelper(Context c, TextView t, String content, String regex, int mode)
    {
        String holder = content;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder, true);

        for (Map.Entry<String, String> n : set) {
            holder = activityLinkBuilder(c, t, holder, n.getKey(), n.getValue(), mode);
        }

        return holder;
    }

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
        holder = linkifyHelper(c, t, holder, "@@(.*?)@@", CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, "%%(.*?)%%", CLICKY_REGION_MODE);

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
     * Transform NationStates' BBCode-formatted content into HTML
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setBbCodeFormatting(Context c, TextView t, String content)
    {
        String holder = content.trim();
        holder = holder.replace("\n", "<br />");
        holder = holder.replace("&amp;#39;", "'");
        holder = holder.replace("&amp;", "&");
        holder = Jsoup.clean(holder, Whitelist.simpleText().addTags("br"));

        // Replace raw NS nation and region links with Stately versions
        holder = regexReplace(holder, "\\bhttps?:\\/\\/(?:www.|)nationstates\\.net\\/nation=(\\w*)", EXPLORE_TARGET + "%s" + "/" + CLICKY_NATION_MODE, false);
        holder = regexReplace(holder, "\\bhttps?:\\/\\/(?:www.|)nationstates\\.net\\/region=(\\w*)", EXPLORE_TARGET + "%s" + "/" + CLICKY_REGION_MODE, false);

        // Basic BBcode processing
        holder = holder.replace("[hr]", "<br>");
        holder = regexReplace(holder, "(?s)\\[b\\](.*?)\\[\\/b\\]", "<b>%s</b>", false);
        holder = regexReplace(holder, "(?s)\\[i\\](.*?)\\[\\/i\\]", "<i>%s</i>", false);
        holder = regexReplace(holder, "(?s)\\[u\\](.*?)\\[\\/u\\]", "<u>%s</u>", false);
        holder = regexReplace(holder, "(?s)\\[pre\\](.*?)\\[\\/pre\\]", "<code>%s</code>", false);
        holder = regexReplace(holder, "(?s)\\[spoiler\\](.*?)\\[\\/spoiler\\]", "<br /><b>---" + c.getString(R.string.spoiler_warn) + "---</b><br />%s<br/><b>---" + c.getString(R.string.spoiler_warn) + "---</b><br />", false);
        holder = regexDoubleReplace(holder, "(?s)\\[spoiler=(.*?)\\](.*?)\\[\\/spoiler\\]", "<br /><b>---" + c.getString(R.string.spoiler_warn) + ": %s---</b><br />%s<br/><b>---" + c.getString(R.string.spoiler_warn) + "---</b><br />", false);
        holder = regexRemove(holder, "(?s)\\[proposal=.*?\\](.*?)\\[\\/proposal\\]");
        holder = regexRemove(holder, "(?s)\\[resolution=.*?\\](.*?)\\[\\/resolution\\]");
        holder = regexDoubleReplace(holder, "(?s)\\[colou?r=(.*?)\\](.*?)\\[\\/colou?r\\]", "<font color=\"%s\">%s</font>", false);
        holder = regexDoubleReplace(holder, "(?s)\\[url=(.*?)\\](.*?)\\[\\/url\\]", "<a href=\"%s\">%s</a>", false);
        holder = regexReplace(holder, "(?<=^|\\s|<br \\/>|<br>)(http:\\/\\/[^\\s\\[\\<]+)", "<a href=\"%s\">" + c.getString(R.string.clicky_link) + "</a>", false);
        holder = regexReplace(holder, "(?<=^|\\s|<br \\/>|<br>)(https:\\/\\/[^\\s\\[\\<]+)", "<a href=\"%s\">" + c.getString(R.string.clicky_link) + "</a>", false);
        holder = regexQuoteFormat(c, t, holder);

        // Format lists
        holder = regexListFormat(holder);

        // Linkify nations and regions
        holder = linkifyHelper(c, t, holder, "\\[nation\\](.*?)\\[\\/nation\\]", CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, "\\[nation=.*?\\](.*?)\\[\\/nation\\]", CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, "\\[nation=(.*?)\\]", CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, "\\[region\\](.*?)\\[\\/region\\]", CLICKY_REGION_MODE);
        holder = linkifyHelper(c, t, holder, "\\[region=(.*?)\\]", CLICKY_REGION_MODE);

        // In case there are no nations or regions to linkify, set and style TextView here too
        if (t instanceof HtmlTextView)
        {
            ((HtmlTextView)t).setHtmlFromString(holder, new HtmlTextView.RemoteImageGetter());
        }
        else
        {
            t.setText(Html.fromHtml(holder));
        }
        styleLinkifiedTextView(c, t);
    }

    /**
     * Replaces all matches of a given regex with the supplied string template. Only accepts
     * one parameter.
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @param stripInnerBreaks If breaks within content to be replace should be removed
     * @return Returns content with all matched substrings replaced
     */
    public static String regexReplace(String target, String regexBefore, String afterFormat, boolean stripInnerBreaks)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regexBefore, holder, false);

        for (Map.Entry<String, String> n : set) {
            // disabling whitelisting since improperly-nested tags are common in NS BBCode :(
            String replacer = n.getValue();
            if (stripInnerBreaks)
            {
                replacer = replacer.replace("<br>", "").replace("<br />", "");
            }
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
     * @param stripInnerBreaks If breaks within content to be replace should be removed
     * @return
     */
    public static String regexDoubleReplace(String target, String regexBefore, String afterFormat, boolean stripInnerBreaks)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getDoubleReplacePairFromRegex(regexBefore, afterFormat, holder);

        for (Map.Entry<String, String> n : set) {
            // disabling whitelisting since improperly-nested tags are common in NS BBCode :(
            String replacer = n.getValue(); //Jsoup.clean(n.getValue(), Whitelist.basic().addProtocols("a", "href", PROTOCOLS));
            if (stripInnerBreaks)
            {
                replacer = replacer.replace("<br>", "").replace("<br />", "");
            }
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
    public static String regexQuoteFormatHelper(Context c, TextView t, String regex, String content)
    {
        String holder = content;
        Map<String, String> replacePairs = new HashMap<String, String>();
        Matcher m = Pattern.compile(regex).matcher(holder);
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
        holder = linkifyHelper(c, t, holder, "@@(.*?)@@", CLICKY_NATION_MODE);
        return holder;
    }

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
        holder = regexReplace(holder, "(?s)\\[quote\\](.*?)\\[\\/quote\\]", "<blockquote><i>%s</i></blockquote>", false);

        // handle quotes with parameters on them
        // in this case, [quote=name;id]...
        holder = regexQuoteFormatHelper(context, t, "(?s)\\[quote=(.*?);[0-9]+\\](.*?)\\[\\/quote\\]", holder);
        // in this case, just [quote=name]...
        holder = regexQuoteFormatHelper(context, t, "(?s)\\[quote=(.*?)\\](.*?)\\[\\/quote\\]", holder);

        return holder;
    }

    /**
     * Formats a raw BBCode list into a pretty HTML list
     * @param content Original content
     * @return Formatted content
     */
    public static String regexListFormat(String content)
    {
        String holder = content;

        // Switch unordered lists
        holder = regexReplace(holder, "(?s)\\[list\\](.*?)\\[\\/list\\]", "<br /><ul>%s</ul><br />", true);

        // Switch ordered lists
        holder = regexDoubleReplace(holder, "(?s)\\[list=(.*?)\\](.*?)\\[\\/list\\]", "<br /><ol=\"%s\">%s</ol><br />", true);

        // Switch bullets
        holder = regexReplace(holder, "(?s)\\[\\*\\](((?!<ul>|<\\/ul>|<ol|<\\/ol>).)*?)(?=\\[\\*\\])", "<li>%s</li>", false);
        holder = regexReplace(holder, "(?s)\\[\\*\\](.*?)(?=<\\/ul>|<\\/ol>)", "<li>%s</li>", false);

        // Handle nested lists
        holder = regexReplace(holder, "(?s)\\[\\*\\](.*?<\\/li>(?:<\\/ul>|<\\/ol>))", "<li>%s</li>", false);

        return holder;
    }

    /**
     * Removes all substrings which match the regex
     * @param target Target content
     * @param regex Regex
     * @return
     */
    public static String regexRemove(String target, String regex)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder, false);

        for (Map.Entry<String, String> n : set) {
            holder = holder.replace(n.getKey(), n.getValue());
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
