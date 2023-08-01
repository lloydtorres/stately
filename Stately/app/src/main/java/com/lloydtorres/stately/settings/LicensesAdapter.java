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

package com.lloydtorres.stately.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.DataPair;
import com.lloydtorres.stately.helpers.RaraHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

/**
 * Created by lloyd on 2017-03-16.
 * Adapter used to display licenses.
 */
public class LicensesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // types of viewholders to display
    private static final int CARD_HEADER = 0;
    private static final int CARD_LICENSE = 1;

    private List<DataPair> licenses;

    public LicensesAdapter(List<DataPair> ls) {
        setContent(ls);
    }

    public void setContent(List<DataPair> ls) {
        licenses = ls;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View licenseCard = inflater.inflate(R.layout.card_license, parent, false);
        LicenseCard viewHolder = new LicenseCard(licenseCard);
        viewHolder.setIsLicense(viewType == CARD_LICENSE);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LicenseCard viewHolder = (LicenseCard) holder;
        viewHolder.init(licenses.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? CARD_HEADER : CARD_LICENSE;
    }

    @Override
    public int getItemCount() {
        return licenses.size();
    }

    // View holder
    private class LicenseCard extends RecyclerView.ViewHolder {
        private final TextView title;
        private final HtmlTextView content;

        private boolean isLicense = true;

        public LicenseCard(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_license_title);
            content = itemView.findViewById(R.id.card_license_content);
        }

        public void init(DataPair license) {
            RaraHelper.setViewHolderFullSpan(itemView);

            title.setText(license.key);
            String licenseContent = license.value;
            licenseContent = licenseContent.replace("<", "&lt;");
            licenseContent = licenseContent.replace(">", "&gt;");
            licenseContent = licenseContent.replace("\n", "<br>");
            if (isLicense) {
                licenseContent = "<code>" + licenseContent + "</code>";
            }
            content.setHtml(licenseContent);
        }

        public void setIsLicense(boolean il) {
            isLicense = il;
        }
    }
}
