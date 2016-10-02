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
import android.support.v4.content.ContextCompat;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.settings.SettingsActivity;

/**
 * Created by Lloyd on 2016-09-30.
 * A collection of helper functions used to get theme-related data across Stately.
 */

public final class RaraHelper {
    // Private constructor
    private RaraHelper() {}

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

    public static final int[] refreshColoursVert = { R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent };
    public static final int[] refreshColoursNoir = { R.color.colorPrimaryNoir, R.color.colorPrimaryDarkNoir, R.color.colorAccentNoir };
    public static final int[] refreshColoursBleu = { R.color.colorPrimaryBleu, R.color.colorPrimaryDarkBleu, R.color.colorAccentBleu };
    public static final int[] refreshColoursRouge = { R.color.colorPrimaryRouge, R.color.colorPrimaryDarkRouge, R.color.colorAccentRouge };
    public static final int[] refreshColoursViolet = { R.color.colorPrimaryViolet, R.color.colorPrimaryDarkViolet, R.color.colorAccentViolet };

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
}
