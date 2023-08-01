/**
 * Copyright 2017 Lloyd Torres
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

package com.lloydtorres.stately.core;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lloyd on 2017-02-21.
 * Superclass containing logic allowing for handling of broadcast registrations and mass
 * unregistrations.
 */
public abstract class BroadcastableActivity extends AppCompatActivity {

    private final List<BroadcastReceiver> broadcastReceivers = new ArrayList<BroadcastReceiver>();

    public void registerBroadcastReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        broadcastReceivers.add(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceivers != null) {
            for (BroadcastReceiver br : broadcastReceivers) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
            }
        }
    }
}
