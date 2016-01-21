package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.content.Intent;
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

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.nation.ExploreNationActivity;

import org.atteo.evo.inflector.English;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
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
    // Uri to invoke the ExploreNationActivity
    public static final String NATION_TARGET = "com.lloydtorres.stately.nation://";
    // Uri to invoke the ExploreRegionActivity
    public static final String REGION_TARGET = "com.lloydtorres.stately.region://";
    // String template used to get nation banners from NationStates
    // @param: banner_id
    public static final String BANNER_TEMPLATE = "https://www.nationstates.net/images/banners/%s.jpg";

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
     * UTILITY
     * These are convenient tools to call from any class.
     */

    /**
     * Starts the ExploreNationActivity for the given nation ID.
     * @param c App context
     * @param n The nation ID
     */
    public static void startExploring(Context c, String n)
    {
        Intent nationActivityLaunch = new Intent(c, ExploreNationActivity.class);
        nationActivityLaunch.putExtra("nationId", n);
        c.startActivity(nationActivityLaunch);
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
        final String urlFormat = "<a href=\"%s\">%s</a>";
        String tempHolder = template;
        String targetActivity;

        switch (mode)
        {
            case CLICKY_NATION_MODE:
                targetActivity = NATION_TARGET;
                break;
            default:
                targetActivity = REGION_TARGET;
                break;
        }

        // Name needs to be formatted back to its NationStates ID first for the URL.
        targetActivity = targetActivity + nTarget.toLowerCase().replace(" ", "_");
        targetActivity = String.format(urlFormat, targetActivity, nTarget);

        tempHolder = tempHolder.replace(oTarget, targetActivity);

        t.setText(Html.fromHtml(tempHolder));
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
        t.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Given a regex and some content, get all pairs of (old, new) where old is a string matching
     * the regex in the content, and new is the proper name to replace the old string.
     * @param regex Regex statement
     * @param content Target content
     * @return
     */
    public static Set<Map.Entry<String, String>> getReplacePairFromRegex(String regex, String content)
    {
        String holder = content;
        // (old, new) replacement pairs
        Map<String, String> names = new HashMap<String, String>();

        Matcher m = Pattern.compile(regex).matcher(holder);
        while (m.find())
        {
            // Nameify the ID found and put the (old, new) pair into the map
            String properName = getNameFromId(m.group(1));
            names.put(m.group(), properName);
        }

        return names.entrySet();
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
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder);

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
        String holder = content;

        // Linkify nations (@@NATION@@)
        holder = linkifyHelper(c, t, holder, "@@(.*?)@@", CLICKY_NATION_MODE);

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
        return Html.fromHtml(content);
    }

    /**
     * Transform NationStates' BBCode-formatted content into HTML
     * @param c App context
     * @param t TextView
     * @param content Target content
     */
    public static void setBbCodeFormatting(Context c, TextView t, String content)
    {
        String holder = content;

        // Basic BBcode processing
        holder = holder.replace("\n", "<br />");
        holder = regexReplace(holder, "\\[b\\](.*?)\\[\\/b\\]", "<b>%s</b>");
        holder = regexReplace(holder, "\\[i\\](.*?)\\[\\/i\\]", "<i>%s</i>");
        holder = regexReplace(holder, "\\[u\\](.*?)\\[\\/u\\]", "<u>%s</u>");
        holder = regexReplace(holder, "\\[pre\\](.*?)\\[\\/pre\\]", "<pre>%s</pre>");
        holder = regexRemove(holder, "\\[proposal=.*?\\](.*?)\\[\\/proposal\\]");
        holder = regexRemove(holder, "\\[resolution=.*?\\](.*?)\\[\\/resolution\\]");

        // Linkify nations
        holder = linkifyHelper(c, t, holder, "\\[nation\\](.*?)\\[\\/nation\\]", CLICKY_NATION_MODE);
        holder = linkifyHelper(c, t, holder, "\\[nation=.*?\\](.*?)\\[\\/nation\\]", CLICKY_NATION_MODE);

        // In case there are no nations or regions to linkify, set and style TextView here too
        t.setText(Html.fromHtml(holder));
        styleLinkifiedTextView(c, t);
    }

    /**
     * Replaces all matches of a given regex with the supplied string template. Only accepts
     * one parameter.
     * @param target Target content
     * @param regexBefore Regex to use
     * @param afterFormat String template
     * @return Returns content with all matched substrings replaced
     */
    public static String regexReplace(String target, String regexBefore, String afterFormat)
    {
        String holder = target;
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regexBefore, holder);

        for (Map.Entry<String, String> n : set) {
            String properFormat = String.format(afterFormat, n.getValue());
            holder = holder.replace(n.getKey(), properFormat);
        }

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
        Set<Map.Entry<String, String>> set = getReplacePairFromRegex(regex, holder);

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
