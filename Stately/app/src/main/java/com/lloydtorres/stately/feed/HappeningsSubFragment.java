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

package com.lloydtorres.stately.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Event;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-09-12.
 * A subfragment to display happenings.
 */
public class HappeningsSubFragment extends RecyclerSubFragment {
    public static final String EVENTS_DATA = "eventsData";

    private ArrayList<Event> events = new ArrayList<Event>();

    public void setHappenings(ArrayList<Event> e) { events = e; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Restore state
        if (savedInstanceState != null && events == null) {
            events = savedInstanceState.getParcelableArrayList(EVENTS_DATA);
        }
        if (events != null) {
            Collections.sort(events);
            if (mRecyclerAdapter == null) {
                mRecyclerAdapter = new EventRecyclerAdapter(getContext(), events);
            } else {
                ((EventRecyclerAdapter) mRecyclerAdapter).setEvents(events);
            }
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (events != null) {
            outState.putParcelableArrayList(EVENTS_DATA, events);
        }
    }
}
