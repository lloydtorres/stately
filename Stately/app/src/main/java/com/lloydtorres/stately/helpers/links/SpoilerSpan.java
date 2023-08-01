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
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Spoiler;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.dialogs.HtmlDialog;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-04-13.
 * A ClickableSpan implementation for spoiler links.
 */
public class SpoilerSpan extends ClickableSpan {

    private final Context context;
    private final FragmentManager fm;
    private final Spoiler spoiler;

    public SpoilerSpan(Context c, Spoiler s, FragmentManager f) {
        context = c;
        spoiler = s;
        fm = f;
    }

    @Override
    public void onClick(View widget) {
        HtmlDialog htmlDialog = new HtmlDialog();
        htmlDialog.setTitle(spoiler.title != null ? String.format(Locale.US,
                context.getString(R.string.spoiler_warn_title), spoiler.title) :
                context.getString(R.string.spoiler_warn));
        htmlDialog.setRawContent(spoiler.content);
        htmlDialog.setFragmentManager(fm);
        htmlDialog.show(fm, HtmlDialog.DIALOG_TAG);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(RaraHelper.getThemeLinkColour(context));
    }
}
