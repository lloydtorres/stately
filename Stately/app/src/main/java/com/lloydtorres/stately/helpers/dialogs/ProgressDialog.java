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

package com.lloydtorres.stately.helpers.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.DetachDialogFragment;
import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by lloyd on 2017-04-04.
 * Shows a dialog with a spinning progress circle thing.
 * Made this since ProgressDialog got deprecated in Android O.
 */
public class ProgressDialog extends DetachDialogFragment {
    public static final String DIALOG_TAG = "fragment_dialog_progress";
    public static final String KEY_CONTENT = "dialog_progress_content";

    public TextView contentTextView;

    public String content;

    public void setContent(String con) {
        content = con;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = new AppCompatDialog(getActivity(),
                RaraHelper.getThemeMaterialDialog(getContext()));
        dialog.setCanceledOnTouchOutside(false);
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_progress, container, false);

        // Restore saved state
        if (savedInstanceState != null) {
            content = savedInstanceState.getString(KEY_CONTENT);
        }

        contentTextView = view.findViewById(R.id.fragment_dialog_progress_content);
        contentTextView.setText(content);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, content);
    }
}
