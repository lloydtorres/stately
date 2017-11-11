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

package com.lloydtorres.stately.nation;

import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.feed.EventRecyclerAdapter;

/**
 * Created by lloyd on 2017-11-11.
 * Nation subfragment used to show policy data.
 */
public class PolicySubFragment extends NationSubFragment {

    private static final Event EMPTY_INDICATOR = new Event();

    @Override
    protected void initData() {
        super.initData();

        // Create dummy event to show message when no policies available
        EMPTY_INDICATOR.timestamp = EventRecyclerAdapter.EMPTY_INDICATOR;

        if (mNation.policies != null && !mNation.policies.isEmpty()) {
            cards.addAll(mNation.policies);
        } else {
            cards.add(EMPTY_INDICATOR);
        }
    }
}
