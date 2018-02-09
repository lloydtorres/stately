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

package com.lloydtorres.stately.helpers.dialogs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * Created by Lloyd on 2016-04-13.
 * A dialog that can display HTML content.
 */
public class HtmlDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_dialog_html";
    public static final String KEY_RAW = "dialog_html_raw";
    public static final String KEY_TITLE = "dialog_html_title";

    private HtmlTextView content;
    private FragmentManager fm;
    private String raw;
    private String title;

    public void setRawContent(String c) {
        raw = c;
    }

    public void setTitle(String t) {
        title = t;
    }

    public void setFragmentManager(FragmentManager f) {
        fm = f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AppCompatDialog(getActivity(), RaraHelper.getThemeLollipopDialog(getContext()));
        } else {
            dialog = new AppCompatDialog(getActivity(), RaraHelper.getThemeMaterialDialog(getContext()));
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_html, container, false);

        // Restore saved state
        if (savedInstanceState != null) {
            raw = savedInstanceState.getString(KEY_RAW);
            title = savedInstanceState.getString(KEY_TITLE);
        }

        content = view.findViewById(R.id.dialog_html_content);
        SparkleHelper.setStyledTextView(getContext(), content, raw, fm);
        getDialog().setTitle(title);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putString(KEY_RAW, raw);
        outState.putString(KEY_TITLE, title);
    }
}
