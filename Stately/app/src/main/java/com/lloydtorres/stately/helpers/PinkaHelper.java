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

                                                  .-'''-.
                                         __     /`       `. .''', .--.
                                     ,-'`__`.  /           Y     X    `.
                                   .`   /  \ `Y                  U      |
                                  /    ( /  \_ ...---..      .--.       |
                                 (     | |    `        '.  .'    `.     j
                                 (     ( '.  \ _____    _v`       |    / ,;
                                  \     \   __/   __\  / `:       |   /.'/
                                   \,..  \_  (   / .-| |'.|        \ _.-'
               _----_             /`   `   ``.\ ( (WW| \W)j
             ,'      `-.         :        _  ; \_\_`/   ``-.
            /           `'.      |      .' `x          \__/
           (               \     l      '--' `.-,______.-'
           (     <:''-.    \ ____`.           \   /
            \      `.   `>-'`       ``>-        )(
       ,'``'.;       \  /   ( )      :         /  \
     ,'      '        )|     S       `.     <:'    |
    /                 )| ( ) S ( )     `'--.  `)   |
   (         ..---.  / |  S     S       `-.._-'    |
    \       (      `X / \ S     S/             \__/
     `.      `-..-'` )   |      /-,_______\       \
    .'               ) _/      /     |    |\       \
    |     __        / /       /     |     | `--,    \
    '.  .'  )     .'  |      |      |     |   /      )
      `x_.-`  _.-`\__/|      |      |      | (       |
   _  (.    <:        |      |      |      |  \      |
   \`-._`>    )       |       \     |       \  `.___/
    `-.___..-'         \_______)     \_______)
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lloydtorres.stately.dto.UserLogin;

/**
 * Created by Lloyd on 2016-09-30.
 * A collection of functions used for handling the current user session.
 */

public final class PinkaHelper {
    // Keys to user name and autologin and other session variables
    public static final String USERSESSION_NAME = "var_name";
    public static final String USERSESSION_AUTOLOGIN = "var_autologin";
    public static final String USERSESSION_PIN = "var_pin";
    public static final String USERSESSION_REGION = "var_region";
    public static final String USERSESSION_WA_MEMBER = "var_wa_member";

    // Private constructor
    private PinkaHelper() {}

    /**
     * Sets the currently logged-in user in shared prefs and saves them into the database.
     * @param c App context
     * @param name User name
     */
    public static void setActiveUser(Context c, String name)
    {
        // Assume that the autologin and PIN in shared prefs are correct
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        String autologin = storage.getString(USERSESSION_AUTOLOGIN, null);
        String pin = storage.getString(USERSESSION_PIN, null);

        // Save user into database
        UserLogin u = new UserLogin(SparkleHelper.getIdFromName(name), name, autologin, pin);
        u.save();

        // Save user into shared preferences
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(USERSESSION_NAME, name);
        editor.apply();
    }

    /**
     * Sets the current user's autologin token in shared prefs.
     * @param c App context
     * @param autologin User autologin cookie
     */
    public static void setActiveAutologin(Context c, String autologin)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(USERSESSION_AUTOLOGIN, autologin);
        editor.apply();
    }

    /**
     * Sets the current user's PIN in shared prefs.
     * @param c App context
     * @param pin  User pin cookie
     */
    public static void setActivePin(Context c, String pin)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(USERSESSION_PIN, pin);
        editor.apply();
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
        editor.putString(USERSESSION_REGION, regionName);
        editor.putBoolean(USERSESSION_WA_MEMBER, SparkleHelper.isWaMember(c, waStatus));
        editor.apply();
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
        editor.putString(USERSESSION_REGION, regionName);
        editor.apply();
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
        editor.putBoolean(USERSESSION_WA_MEMBER, SparkleHelper.isWaMember(c, stat));
        editor.apply();
    }

    /**
     * Retrieve information about the currently logged in user
     * @param c App context
     * @return A UserLogin object with their name and autologin
     */
    public static UserLogin getActiveUser(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        String name = storage.getString(USERSESSION_NAME, null);
        String autologin = storage.getString(USERSESSION_AUTOLOGIN, null);
        String pin = storage.getString(USERSESSION_PIN, null);
        if (name != null && autologin != null)
        {
            return new UserLogin(SparkleHelper.getIdFromName(name), name, autologin, pin);
        }

        return null;
    }

    /**
     * Retrieve the current value for the active pin.
     * @param c App context.
     * @return The stored active pin.
     */
    public static String getActivePin(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getString(USERSESSION_PIN, null);
    }

    /**
     * Returns the current member region in the current session.
     * @param c App context
     * @return ID of region
     */
    public static String getRegionSessionData(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getString(USERSESSION_REGION, null);
    }

    /**
     * Returns current WA membership status in current session.
     * @param c App context
     * @return WA membership status
     */
    public static boolean getWaSessionData(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getBoolean(USERSESSION_WA_MEMBER, false);
    }

    /**
     * Removes data about the logged in user from shared prefs.
     * @param c App context
     */
    public static void removeActiveUser(Context c)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.remove(USERSESSION_NAME);
        editor.remove(USERSESSION_AUTOLOGIN);
        editor.remove(USERSESSION_PIN);
        editor.remove(USERSESSION_REGION);
        editor.remove(USERSESSION_WA_MEMBER);
        editor.apply();
    }
}
