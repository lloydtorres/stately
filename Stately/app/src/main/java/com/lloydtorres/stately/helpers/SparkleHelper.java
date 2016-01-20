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

/**
 * Created by Lloyd on 2016-01-16.
 */
public class SparkleHelper {
    public static final String APP_TAG = "com.lloydtorres.stately";
    public static final String NATION_TARGET = "com.lloydtorres.stately.nation://";
    public static final String REGION_TARGET = "com.lloydtorres.stately.region://";
    public static final String BANNER_TEMPLATE = "https://www.nationstates.net/images/banners/%s.jpg";

    public static final int CLICKY_NATION_MODE = 1;
    public static final int CLICKY_REGION_MODE = 2;

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

    public static final int[] sectorColours = { R.color.colorSector0,
            R.color.colorSector1,
            R.color.colorSector2,
            R.color.colorSector3,
    };

    public static final int[] refreshColours = {    R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
    };

    public static final int[] waColours = { R.color.colorChart0,
            R.color.colorChart1,
            R.color.colorChart12
    };

    public static final int[] waColourFor = { R.color.colorChart0 };

    public static final int[] waColourAgainst = { R.color.colorChart1 };

    public static final CharMatcher CHAR_MATCHER = CharMatcher.JAVA_LETTER_OR_DIGIT
            .or(CharMatcher.WHITESPACE)
            .or(CharMatcher.anyOf("-"))
            .precomputed();

    public static final PrettyTime prettyTime = new PrettyTime();

    // Validation

    public static boolean isValidNationName(String name)
    {
        return CHAR_MATCHER.matchesAllOf(name);
    }

    // Formatting

    public static String getNameFromId(String id)
    {
        String[] words = id.split("_");
        List<String> properWords = new ArrayList<String>();

        for (String w : words)
        {
            String[] subWords = w.split("-");
            List<String> properSubWords = new ArrayList<String>();

            for (String sw: subWords)
            {
                properSubWords.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, sw));
            }

            String subFin = Joiner.on("-").join(properSubWords);
            properWords.add(subFin);
        }

        return Joiner.on(" ").skipNulls().join(properWords);
    }

    public static String getBannerURL(String id)
    {
        return String.format(BANNER_TEMPLATE, id);
    }

    public static String getReadableDateFromUTC(long sec)
    {
        Date d = new Date(sec * 1000L);
        return prettyTime.format(d);
    }

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

    public static String getPopulationFormatted(Context c, double pop)
    {
        String suffix = c.getString(R.string.million);
        double popHolder = pop;

        if (popHolder >= 1000D)
        {
            suffix = c.getString(R.string.billion);
            popHolder /= 1000D;
        }

        return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(popHolder), suffix);
    }

    public static String getMoneyFormatted(Context c, long money, String currency)
    {
        if (money < 1000000L)
        {
            return String.format(c.getString(R.string.val_currency), getPrettifiedNumber(money), English.plural(currency));
        }
        else
        {
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

    // Utility

    public static void startExploring(Context c, String n)
    {
        Intent nationActivityLaunch = new Intent(c, ExploreNationActivity.class);
        nationActivityLaunch.putExtra("nationId", n);
        c.startActivity(nationActivityLaunch);
    }

    // Link and HTML Processing

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
        targetActivity = targetActivity + nTarget.toLowerCase().replace(" ", "_");
        targetActivity = String.format(urlFormat, targetActivity, nTarget);

        tempHolder = tempHolder.replaceAll(oTarget, targetActivity);

        t.setText(Html.fromHtml(tempHolder));
        styleLinkifiedTextView(c, t);

        return tempHolder;
    }

    public static void styleLinkifiedTextView(Context c, TextView t)
    {
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
        t.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void setHappeningsFormatting(Context c, TextView t, String content)
    {
        String holder = content;
        Map<String, String> nationIds = new HashMap<String, String>();

        Matcher m = Pattern.compile("@@(.*?)@@").matcher(holder);
        if (m.find())
        {
            String properName = getNameFromId(m.group(1));
            nationIds.put(m.group(), properName);
        }

        Set<Map.Entry<String, String>> set = nationIds.entrySet();

        for (Map.Entry<String, String> n : set) {
            holder = activityLinkBuilder(c, t, holder, n.getKey(), n.getValue(), CLICKY_NATION_MODE);
        }
    }

    public static Spanned getHtmlFormatting(String content)
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

        return Html.fromHtml(holder);
    }

    public static String regexReplace(String target, String regexBefore, String afterFormat)
    {
        Matcher m = Pattern.compile(regexBefore).matcher(target);
        if (m.find())
        {
            target = m.replaceAll(String.format(afterFormat, m.group(1)));
        }

        return target;
    }

    public static String regexRemove(String target, String regex)
    {
        Matcher m = Pattern.compile(regex).matcher(target);
        if (m.find())
        {
            target = m.replaceAll(m.group(1));
        }

        return target;
    }

    // Logging

    public static void makeSnackbar(View view, String str)
    {
        Snackbar.make(view, str, Snackbar.LENGTH_LONG).show();
    }

    public static void logError(String message)
    {
        Log.e(APP_TAG, message);
    }
}
