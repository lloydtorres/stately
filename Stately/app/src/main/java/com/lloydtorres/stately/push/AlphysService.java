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

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.dto.NoticeHolder;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.simpleframework.xml.core.Persister;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-09-18.
 * Polls the NS Notices private API and sends them off for processing, to see if any need to be
 * shown as notifications. Alphys is activated as often as the user wants it to via the
 * AlarmManager.
 */
public class AlphysService extends IntentService {

    public AlphysService() {
        super("com.lloydtorres.stately.push.alphys");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the user doesn't want notifications, don't bother
        if (!SettingsActivity.getNotificationSetting(this)) {
            return;
        }

        // If there's no active user, don't even bother.
        final UserLogin active = PinkaHelper.getActiveUser(this);
        if (active == null) {
            TrixHelper.setAlarmForAlphys(this);
            return;
        }

        String query = String.format(Locale.US, NoticeHolder.QUERY, active.nationId);
        NSStringRequest stringRequest = new NSStringRequest(this, Request.Method.GET, query,
                new Response.Listener<String>() {
                    NoticeHolder notices = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            notices = serializer.read(NoticeHolder.class, response);
                            TrixHelper.processNotices(AlphysService.this, active.name, notices);
                            TrixHelper.updateLastActiveTime(AlphysService.this);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
            }
        });

        DashHelper.getInstance(this).addRequest(stringRequest);
        TrixHelper.setAlarmForAlphys(this);
    }
}
