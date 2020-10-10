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

import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.dialogs.NameListDialog;

import java.util.ArrayList;

/**
 * Created by Lloyd on 2016-09-15.
 * A clickable span that can show a NameListDialog.
 */
public class NameListSpan extends ClickableSpan {

    private Context context;
    private FragmentManager fragmentManager;
    private NameListDialog nameListDialog;

    public NameListSpan(Context c, FragmentManager fm, String t, ArrayList<String> n, int m) {
        context = c;
        fragmentManager = fm;
        nameListDialog = new NameListDialog();
        nameListDialog.setTitle(t);
        nameListDialog.setNames(n);
        nameListDialog.setTarget(m);
    }

    @Override
    public void onClick(View view) {
        nameListDialog.show(fragmentManager, NameListDialog.DIALOG_TAG);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(RaraHelper.getThemeLinkColour(context));
    }
}
