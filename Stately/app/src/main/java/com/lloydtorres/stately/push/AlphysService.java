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

/**
 * Created by Lloyd on 2016-09-18.
 * Polls the NS Notices private API and sends them off for processing, to see if any need to be
 * shown as notifications. Alphys is activated as often as the user wants it to via the
 * AlarmManager.
 *
 * For versions below KitKat.
 */
public class AlphysService extends IntentService {

    public AlphysService() {
        super("com.lloydtorres.stately.push.alphys");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TrixHelper.startNoticesQuery(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
