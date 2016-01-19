package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.lloydtorres.stately.R;

import org.atteo.evo.inflector.English;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-16.
 */
public class SparkleHelper {
    public static final String APP_TAG = "com.lloydtorres.stately";
    public static final String BANNER_TEMPLATE = "https://www.nationstates.net/images/banners/%s.jpg";

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

    public static Spanned getHtmlFormatting(String content)
    {
        String holder = content;

        // BBcode "processing"
        holder = holder.replace("\n", "<br />")
                .replace("[i]", "<i>")
                .replace("[/i]", "</i>")
                .replace("[b]", "<b>")
                .replace("[/b]", "</b>")
                .replace("[u]", "<u>")
                .replace("[/u]","</u>")
                .replace("[pre]", "<pre>")
                .replace("[/pre]", "</pre>");

        return Html.fromHtml(holder);
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
