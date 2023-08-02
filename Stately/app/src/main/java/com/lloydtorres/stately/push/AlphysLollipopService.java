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

package com.lloydtorres.stately.push;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.lloydtorres.stately.helpers.network.DashHelper;

/**
 * Created by lloyd on 2017-03-31.
 * Same as AlphysService, but invoked for Lollipop and above.
 * Used to meet requirements for Android O.
 */
public class AlphysLollipopService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        TrixHelper.startNoticesQuery(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        DashHelper.getInstance(this).getRequestQueue().cancelAll(TrixHelper.TAG_NOTICES_REQUEST);
        return false;
    }
}
