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

package com.lloydtorres.stately.helpers.links;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.URLSpan;

import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-01-20.
 * A helper object used to format TextViews with links.
 * In this case, it removes the underline and colours the link with the primary colour.
 */
public class URLSpanNoUnderline extends URLSpan {
    private final Context context;

    public URLSpanNoUnderline(Context c, String url) {
        super(url);
        context = c;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(RaraHelper.getThemeLinkColour(context));
    }
}
