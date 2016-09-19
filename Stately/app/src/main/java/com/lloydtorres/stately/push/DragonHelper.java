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

package com.lloydtorres.stately.push;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Notice;
import com.lloydtorres.stately.dto.NoticeHolder;
import com.lloydtorres.stately.explore.ExploreActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.region.MessageBoardActivity;
import com.lloydtorres.stately.settings.SettingsActivity;
import com.lloydtorres.stately.telegrams.TelegramHistoryActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lloyd on 2016-09-18.
 * Singleton for processing and handling NS notices and notifications.
 */
public class DragonHelper {
    // Tags and identifiers for different types of notifications
    public static final String TAG_PREFIX = "com.lloydtorres.stately.push.";
    public static final String NOTIFS_ISSUE = "I";
    public static final String NOTIFS_TG = "TG";
    public static final String NOTIFS_RMB_MENTION = "RMB";
    public static final String NOTIFS_RMB_QUOTE = "RMBQ";
    public static final String NOTIFS_RMB_LIKE = "RMBL";
    public static final String NOTIFS_ENDORSE = "END";

    // Keys for shared prefs stuff
    public static final String KEY_FIREBASE = "spike_firebase_token";
    public static final String KEY_LASTACTIVITY = "spike_last_activity";

    // #JustSingletonThings
    private static DragonHelper mAssistant;
    private static Context mContext;
    private static NotificationManager mNotificationManager;
    private static int mNotifsTg = 0;
    private static int mNotifsRmbM = 0;
    private static int mNotifsRmbQ = 0;
    private static int mNotifsRmbL = 0;
    private static int mNotifsEndorse = 0;

    /**
     * Private constructor
     * @param c App context
     */
    private DragonHelper(Context c) {
        mContext = c;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Returns the SpikeHelper singleton
     * @param c App context
     * @return Singleton
     */
    public static synchronized DragonHelper getInstance(Context c) {
        if (mAssistant == null) {
            mAssistant = new DragonHelper(c.getApplicationContext());
        }
        return mAssistant;
    }

    /**
     * Updates the stored last active time, in Unix seconds.
     * @param c App context.
     */
    public synchronized static void updateLastActiveTime(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = storage.edit();
        editor.putLong(KEY_LASTACTIVITY, System.currentTimeMillis() / 1000L);
        editor.commit();
    }

    /**
     * Returns the stored last active time, in Unix seconds.
     * @param c App context.
     */
    public static long getLastActiveTime(Context c) {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(c);
        return storage.getLong(KEY_LASTACTIVITY, System.currentTimeMillis() / 1000L);
    }

    private static final long FIVE_MIN_IN_MS = 5L * 60L * 1000L;

    /**
     * Sets an alarm for Alphys to query NS for new notices. The alarm time is on whatever the user
     * selected in settings, starting from the time the function was called. A "jitter" of up to
     * 5 minutes is added on top to prevent overwhelming the NS servers.
     * @param c App context
     */
    public static void setAlarmForAlphys(Context c) {
        // First check if alarms should be set to begin with.
        if (!SettingsActivity.getNotificationSetting(c)) {
            return;
        }

        Intent alphysIntent = new Intent(c, AlphysService.class);
        PendingIntent pendingIntent = PendingIntent.getService(c, 0, alphysIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long timeToNextAlarm = System.currentTimeMillis() + SettingsActivity.getNotificationIntervalSetting(c) * 1000L;
        // add "jitter" from 0 min to 5 min to next alarm to prevent overwhelming NS servers
        Random r = new Random();
        timeToNextAlarm += (long)(r.nextDouble() * FIVE_MIN_IN_MS);

        // Source:
        // https://www.reddit.com/r/Android/comments/44opi3/reddit_sync_temporarily_blocked_for_bad_api_usage/czs3ne4
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToNextAlarm, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, timeToNextAlarm, pendingIntent);
        }
    }

    /**
     * Cancels any previous alarms set for Alphys.
     * @param c App context.
     */
    public static void stopAlarmForAlphys(Context c) {
        Intent alphysIntent = new Intent(c, AlphysService.class);
        PendingIntent pendingIntent = PendingIntent.getService(c, 0, alphysIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    /**
     * Takes in a list of notices (in the NoticeHolder wrapper) and shows the appropriate
     * notification for each.
     * @param account Name/ID of the nation the notices belong to
     * @param holder NoticeHolder wrapper
     */
    public void processNotices(String account, NoticeHolder holder) {
        long lastActiveTime = getLastActiveTime(mContext);
        List<Notice> issueNotices = new ArrayList<Notice>();

        for (Notice n : holder.notices) {
            // Only care about new notices since the last activity time
            if (lastActiveTime < n.timestamp && n.unread == Notice.NOTICE_UNREAD) {
                switch (n.type) {
                    // Only care about the notices we can handle
                    case NOTIFS_ISSUE:
                        issueNotices.add(n);
                        break;
                    case NOTIFS_TG:
                    case NOTIFS_RMB_MENTION:
                    case NOTIFS_RMB_QUOTE:
                    case NOTIFS_RMB_LIKE:
                    case NOTIFS_ENDORSE:
                        processGenericNotice(account, n);
                }
            }
        }

        // @TODO: Process issues
    }

    // Duration of the LED on/off for notifications
    private static final int LED_DURATION_MS = 250;

    /**
     * Returns a NotificationCompat builder with set parameters common to all notifications from Stately.
     * @return See above
     */
    private NotificationCompat.Builder getBaseBuilder() {
        int primaryColour = SparkleHelper.getThemePrimaryColour(mContext);
        return new NotificationCompat.Builder(mContext)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setColor(primaryColour)
                .setLights(primaryColour, LED_DURATION_MS, LED_DURATION_MS);
    }

    private static final Pattern NOTIFS_URL_TG = Pattern.compile("^page=tg\\/tgid=(\\d+)$");
    private static final Pattern NOTIFS_URL_RMB = Pattern.compile("^region=(.+)\\/page=display_region_rmb\\?postid=(\\d+)#p\\d+$");
    private static final Pattern NOTIFS_URL_ENDORSE = Pattern.compile("^nation=(.+)$");

    /**
     * Builds and shows a notification for every type of notice except for issues.
     * @param account Name/ID of the nation whom the notices belong to.
     * @param notice The notice to show as a notification.
     */
    public void processGenericNotice(String account, Notice notice) {
        // Check if the user wants to see notifications for this kind of notice
        if (NOTIFS_TG.equals(notice.type) && !SettingsActivity.getTelegramsNotificationSetting(mContext)) {
            return;
        }
        else if (NOTIFS_RMB_MENTION.equals(notice.type) && !SettingsActivity.getRmbMentionNotificationSetting(mContext)) {
            return;
        }
        else if (NOTIFS_RMB_QUOTE.equals(notice.type) && !SettingsActivity.getRmbQuoteNotificationSetting(mContext)) {
            return;
        }
        else if (NOTIFS_RMB_LIKE.equals(notice.type) && !SettingsActivity.getRmbLikeNotificationSetting(mContext)) {
            return;
        }
        else if (NOTIFS_ENDORSE.equals(notice.type) && !SettingsActivity.getEndorsementNotificationSetting(mContext)) {
            return;
        }

        String title = String.format(Locale.US, mContext.getString(R.string.time_moments_template),
                notice.subject, notice.content);

        String tagSuffix = notice.type;

        // Handle notification ID
        int tagId = 0;
        switch (notice.type) {
            case NOTIFS_TG:
                tagId = mNotifsTg++;
                break;
            case NOTIFS_RMB_MENTION:
                tagId = mNotifsRmbM++;
                break;
            case NOTIFS_RMB_QUOTE:
                tagId = mNotifsRmbQ++;
                break;
            case NOTIFS_RMB_LIKE:
                tagId = mNotifsRmbL++;
                break;
            case NOTIFS_ENDORSE:
                tagId = mNotifsEndorse++;
                break;
        }

        // Handle notification icon and intent
        int smallIcon = 0;
        Intent nextActivity = new Intent();
        switch (notice.type) {
            case NOTIFS_TG:
                smallIcon = R.drawable.ic_menu_telegrams;
                nextActivity = new Intent(mContext, TelegramHistoryActivity.class);
                Matcher matcherTg = NOTIFS_URL_TG.matcher(notice.link);
                matcherTg.matches();
                int telegramId = Integer.valueOf(matcherTg.group(1));
                nextActivity.putExtra(TelegramHistoryActivity.ID_DATA, telegramId);
                break;
            case NOTIFS_RMB_MENTION:
            case NOTIFS_RMB_QUOTE:
            case NOTIFS_RMB_LIKE:
                smallIcon = R.drawable.ic_region_white;
                nextActivity = new Intent(mContext, MessageBoardActivity.class);
                Matcher rMatcher = NOTIFS_URL_RMB.matcher(notice.link);
                rMatcher.matches();
                String rName = SparkleHelper.getNameFromId(rMatcher.group(1));
                int postId = Integer.valueOf(rMatcher.group(2));
                nextActivity.putExtra(MessageBoardActivity.BOARD_REGION_NAME, rName);
                nextActivity.putExtra(MessageBoardActivity.BOARD_TARGET_ID, postId);
                break;
            case NOTIFS_ENDORSE:
                tagId = mNotifsEndorse++;
                smallIcon = R.drawable.ic_endorse_yes;
                nextActivity = new Intent(mContext, ExploreActivity.class);
                Matcher matcherEndorse = NOTIFS_URL_ENDORSE.matcher(notice.link);
                matcherEndorse.matches();
                nextActivity.putExtra(ExploreActivity.EXPLORE_ID, matcherEndorse.group(1));
                nextActivity.putExtra(ExploreActivity.EXPLORE_MODE, SparkleHelper.CLICKY_NATION_MODE);
                break;
        }
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Apparently this is necessary: http://stackoverflow.com/a/3168653
        nextActivity.setAction(Long.toString(System.currentTimeMillis()));

        NotificationCompat.Builder builder = getBaseBuilder()
                .setContentTitle(title)
                .setContentText(SparkleHelper.getNameFromId(account))
                .setSmallIcon(smallIcon)
                .setContentIntent(PendingIntent.getActivity(mContext, 0, nextActivity, PendingIntent.FLAG_ONE_SHOT));
        mNotificationManager.notify(TAG_PREFIX+tagSuffix, tagId, builder.build());
    }
}
