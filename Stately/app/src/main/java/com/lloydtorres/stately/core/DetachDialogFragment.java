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

package com.lloydtorres.stately.core;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by Lloyd on 2016-11-01.
 * Workaround for illegal state exception in fragments,
 * as shown in http://stackoverflow.com/a/15656428
 */
public abstract class DetachDialogFragment extends DialogFragment {
    @Override
    public void show(FragmentManager manager, String tag) {
        if (!isAdded()) {
            super.show(manager, tag);
        }
    }
}
