package com.lloydtorres.stately.core;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lloyd on 2017-02-21.
 * Superclass containing logic allowing for handling of broadcast registrations and mass unregistrations.
 */
public class BroadcastableActivity extends AppCompatActivity {

    private List<BroadcastReceiver> broadcastReceivers = new ArrayList<BroadcastReceiver>();

    public void registerBroadcastReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        registerReceiver(receiver, filter);
        broadcastReceivers.add(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastReceivers != null) {
            for (BroadcastReceiver br : broadcastReceivers) {
                unregisterReceiver(br);
            }
        }
    }
}
