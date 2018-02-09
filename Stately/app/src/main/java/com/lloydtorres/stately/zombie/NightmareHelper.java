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

package com.lloydtorres.stately.zombie;

/*

                                              .,
                                             / :
                                            / :'--..
                                     .^.,-`/  :     `.
                                 .-``| 'j./  :''-.   )
                              ..`-'`(..-./  :'    \  |
                             ( ,\'``        '-  .  | `._   _.---.
                             |(  '`-..__      `.   (    ```_._   \
                            (( `.(.---. ``\    )    `-..-'`   \   )
                            |(```:``'-.\  /   .'            .  : (
 .-..-.                     |(`. (  ((WW)''--'  *       x      (  `..,__
(   \  \          _.._.--.  ( \|  '. \`,|  `.                   `-_     `-
 '   '._`-._..-'``__  `-. '. \ |    `'--'  ..7               .     ``-..  \
  '.    ```   /```  `'.  \  \ \|   `-.-~-.`-/      *          ___       )  |   .
 .-.:>.       \__.     '. \  \ |      (`'.`' `-..        .-'``.--`'-._.'   `--'|
(               (   .    . \  \)       \  `'._   `--..-'`.-'`      `'-.____..-'
 `'-._           `'(__.   : \ /''-.     \     ```''''```
     .7               (_. (  \:``\ `'---'\
   .'     ,                \  '-  \ ._)`. )
   \__--':     .     .      `'     \ -..'/
        /    ,:    ,/\   /\__.   . .`'--`
        '--'`  `--'   `'  /      |  : /|.-'```''.
                         /       |  |` |.-'``-.  `-.
                        /        |  :  (   .  x`-   )
                      .'_  ()  .'|  '.  '        ) (  ..--.
                     ( ..`L_  / \|   :   '.      `-.`'  .-.`.__
                     (( C`  )\   (    \    \  .   * `--' ,.)   ``.
                      \`'` )o )./-\    \.-.(\ .-.      .'. .-'``/
                      (`--',./.-''-:. -( \)  `.-.`'---'  .'
                       `-._        _(`.:  \-...)  ``'''``
                           `''''=:_`   |---'
                                   ````
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.CensusDetailedRank;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.Zombie;
import com.lloydtorres.stately.helpers.RaraHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Lloyd on 2016-10-16.
 * Helper for Z-Day operations.
 */
public final class NightmareHelper {
    // Empty constructor
    private NightmareHelper() {}

    public static final String ZDAY_REFERENCE = "https://embed.nationstates.net/page=world";
    public static final String ZDAY_REFERENCE_DIV = "div#zchart-container";
    public static final String USERSESSION_IS_ZDAY = "var_is_zday";

    /**
     * Sets if Z-Day mode should be active during the user's current session.
     * @param c App context
     * @param isZDayActive Set if Z-Day mode should be active
     */
    public static void setIsZDayActive(Context c, boolean isZDayActive) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putBoolean(USERSESSION_IS_ZDAY, isZDayActive);
        editor.apply();
    }

    /**
     * Gets if Z-Day mode is active during the user's current session.
     * @param c App context
     * @return If Z-Day is active
     */
    public static boolean getIsZDayActive(Context c) {
        if (c == null) {
            return false;
        }

        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(USERSESSION_IS_ZDAY, false);
    }

    public static final String HEADER_MILITARY = "m8";
    public static final String HEADER_CURE = "n3";
    public static final String HEADER_ZOMBIE = "x5";

    /**
     * Given a nation's current zombie action, returns the appropriate banner image to use for Z-Day.
     * @param action Current zombie action
     * @return URL to banner
     */
    public static String getZombieBanner(String action) {
        String zombieHeader = HEADER_ZOMBIE;
        if (action != null) {
            switch (action) {
                case Zombie.ZACTION_CURE:
                    zombieHeader = HEADER_CURE;
                    break;
                case Zombie.ZACTION_MILITARY:
                    zombieHeader = HEADER_MILITARY;
                    break;
            }
        }
        return RaraHelper.getBannerURL(zombieHeader);
    }

    /**
     * Helper used to get rid of Z-Day related datasets from the raw map of census datasets.
     * @param censusDatasets Raw census datasets
     * @return Census datasets without Z-Day datasets
     */
    public static LinkedHashMap<Integer, CensusScale> trimZDayCensusDatasets(LinkedHashMap<Integer, CensusScale> censusDatasets) {
        int start = TrendsActivity.CENSUS_ZDAY_SURVIVORS;
        int end = TrendsActivity.CENSUS_ZDAY_ZOMBIFICATION + 1;
        for (int i = start; i < end; i++) {
            censusDatasets.remove(i);
        }
        return censusDatasets;
    }

    /**
     * Helper used to get rid of Z-Day related data from census rankings.
     * @param censusData Raw census rankings
     * @return Census rankings without Z-Day data
     */
    public static List<CensusDetailedRank> trimZDayCensusData(List<CensusDetailedRank> censusData) {
        List<CensusDetailedRank> trimmedData = new ArrayList<CensusDetailedRank>();
        for (CensusDetailedRank rankData : censusData) {
            if (rankData.id < TrendsActivity.CENSUS_ZDAY_SURVIVORS || rankData.id > TrendsActivity.CENSUS_ZDAY_ZOMBIFICATION) {
                trimmedData.add(rankData);
            }
        }
        return trimmedData;
    }
}
