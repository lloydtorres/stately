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

package com.lloydtorres.stately.telegrams;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.MuffinsHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-03-09.
 * An adapter used for displaying telegrams. Can be used for previews and full telegrams.
 */
public class TelegramsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_INDICATOR = -1;

    // constants for the different types of cards
    private final int PREVIEW_CARD = 0;
    private final int FULL_CARD = 1;
    private final int EMPTY_CARD = 2;

    private Context context;
    private List<Telegram> telegrams;

    public TelegramsAdapter(Context c, List<Telegram> t)
    {
        context = c;
        setTelgrams(t);
    }

    /**
     * Sets the contents of this telegram adapter.
     * @param t List of telegrams
     */
    public void setTelgrams(List<Telegram> t)
    {
        telegrams = t;
        if (telegrams.size() <= 0)
        {
            Telegram empty = new Telegram();
            empty.id = EMPTY_INDICATOR;
            telegrams.add(empty);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FULL_CARD:
                View fullCard = inflater.inflate(R.layout.card_telegram, parent, false);
                viewHolder = new TelegramCard(context, fullCard);
                break;
            case PREVIEW_CARD:
                View previewCard = inflater.inflate(R.layout.card_telegram_preview, parent, false);
                viewHolder = new TelegramPreviewCard(context, previewCard);
                break;
            default:
                View emptyCard = inflater.inflate(R.layout.card_happening, parent, false);
                viewHolder = new NoTelegramsCard(context, emptyCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case FULL_CARD:
                TelegramCard telegramCard = (TelegramCard) holder;
                telegramCard.init(telegrams.get(position));
                break;
            case PREVIEW_CARD:
                TelegramPreviewCard telegramPreviewCard = (TelegramPreviewCard) holder;
                telegramPreviewCard.init(telegrams.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return telegrams.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (telegrams.get(position).content != null)
        {
            return FULL_CARD;
        }
        if (telegrams.get(position).preview != null)
        {
            return PREVIEW_CARD;
        }
        else
        {
            return EMPTY_CARD;
        }
    }

    public int getIndexOfId(int id)
    {
        for (int i=0; i<telegrams.size(); i++)
        {
            if (telegrams.get(i).id == id)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Changes the telegram alert to a certain style depending on its type.
     * @param type Telegram type
     * @param holder
     * @param icon
     * @param text
     */
    public void setAlertState(int type, boolean isPreview, RelativeLayout holder, ImageView icon, TextView text)
    {
        if (type != Telegram.TELEGRAM_GENERIC)
        {
            holder.setVisibility(View.VISIBLE);

            int iconRes = isPreview ? R.drawable.ic_alert_recruitment : R.drawable.ic_alert_recruitment_white;
            int alertColor = R.color.colorChart1;
            int alertContent = R.string.telegrams_alert_recruitment;

            switch (type)
            {
                case Telegram.TELEGRAM_REGION:
                    iconRes = isPreview ? R.drawable.ic_region_green : R.drawable.ic_region_white;
                    alertColor = R.color.colorChart3;
                    alertContent = R.string.telegrams_alert_region;
                    break;
                case Telegram.TELEGRAM_WELCOME:
                    iconRes = isPreview ? R.drawable.ic_region_green : R.drawable.ic_region_white;
                    alertColor = R.color.colorChart3;
                    alertContent = R.string.telegram_alert_welcome;
                    break;
                case Telegram.TELEGRAM_MODERATOR:
                    iconRes = isPreview ? R.drawable.ic_alert_moderator : R.drawable.ic_alert_moderator_white;
                    alertColor = R.color.colorChart3;
                    alertContent = R.string.telegrams_alert_mod;
                    break;
            }

            icon.setImageResource(iconRes);
            text.setText(context.getString(alertContent));
            if (isPreview) {
                text.setTextColor(ContextCompat.getColor(context, alertColor));
            }
            else {
                holder.setBackgroundColor(ContextCompat.getColor(context, alertColor));
            }
        }
        else
        {
            holder.setVisibility(View.GONE);
        }
    }

    public class TelegramCard extends RecyclerView.ViewHolder {

        private Context context;
        private Telegram telegram;

        private TextView sender;
        private TextView recipients;
        private TextView timestamp;

        private RelativeLayout alertHolder;
        private ImageView alertIcon;
        private TextView alertText;

        private HtmlTextView content;
        private LinearLayout replyHolder;
        private ImageView reply;
        private ImageView replyAll;

        private LinearLayout regionVisitButton;
        private TextView regionVisitButtonContent;

        public TelegramCard(Context c, View v) {
            super(v);
            context = c;
            sender = (TextView) v.findViewById(R.id.card_telegram_from);
            recipients = (TextView) v.findViewById(R.id.card_telegram_to);
            timestamp = (TextView) v.findViewById(R.id.card_telegram_time);
            alertHolder = (RelativeLayout) v.findViewById(R.id.card_telegram_alert_holder);
            alertIcon = (ImageView) v.findViewById(R.id.card_telegram_alert_icon);
            alertText = (TextView) v.findViewById(R.id.card_telegram_alert_message);
            content = (HtmlTextView) v.findViewById(R.id.card_telegram_content);
            replyHolder = (LinearLayout) v.findViewById(R.id.card_telegram_actions_holder);
            reply = (ImageView) v.findViewById(R.id.card_telegram_reply);
            replyAll = (ImageView) v.findViewById(R.id.card_telegram_reply_all);
            regionVisitButton = (LinearLayout) v.findViewById(R.id.card_telegram_region_holder);
            regionVisitButtonContent = (TextView) v.findViewById(R.id.card_telegram_region_text);
        }

        public void init(Telegram t)
        {
            telegram = t;
            SparkleHelper.setHappeningsFormatting(context, sender, telegram.sender);

            if (telegram.recipients != null && telegram.recipients.size() > 0)
            {
                String recipientsContent = String.format(context.getString(R.string.telegrams_recipients), SparkleHelper.joinStringList(telegram.recipients, ", "));
                SparkleHelper.setHappeningsFormatting(context, recipients, recipientsContent);
            }
            else
            {
                recipients.setVisibility(View.GONE);
            }

            timestamp.setText(SparkleHelper.getReadableDateFromUTC(context, telegram.timestamp));
            setAlertState(telegram.type, false, alertHolder, alertIcon, alertText);
            SparkleHelper.setTelegramHtmlFormatting(context, content, telegram.content);

            final String curNation = SparkleHelper.getActiveUser(context).nationId;
            String senderNationCheck = MuffinsHelper.getNationIdFromFormat(telegram.sender);
            if (senderNationCheck != null && senderNationCheck.equals(curNation))
            {
                replyHolder.setVisibility(View.GONE);
            }
            else if (senderNationCheck == null)
            {
                replyHolder.setVisibility(View.GONE);
            }
            else
            {
                replyHolder.setVisibility(View.VISIBLE);
            }

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String recipient = MuffinsHelper.getNationIdFromFormat(telegram.sender);
                    SparkleHelper.startTelegramCompose(context, SparkleHelper.getNameFromId(recipient), telegram.id);
                }
            });

            replyAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add sender first
                    List<String> recipientsRaw = new ArrayList<String>();
                    recipientsRaw.add(MuffinsHelper.getNationIdFromFormat(telegram.sender));
                    // Go through recipients if they exist
                    if (telegram.recipients != null)
                    {
                        for (int i=0; i<telegram.recipients.size(); i++)
                        {
                            String idChk = MuffinsHelper.getNationIdFromFormat(telegram.recipients.get(i));
                            if (idChk != null && !idChk.equals(curNation))
                            {
                                recipientsRaw.add(idChk);
                            }
                        }
                    }

                    List<String> recipients = new ArrayList<String>();
                    for (String r : recipientsRaw)
                    {
                        recipients.add(SparkleHelper.getNameFromId(r));
                    }
                    String fRecipients = SparkleHelper.joinStringList(recipients, ", ");
                    SparkleHelper.startTelegramCompose(context, fRecipients, telegram.id);
                }
            });

            if (telegram.type == Telegram.TELEGRAM_RECRUITMENT
                    && telegram.regionTarget != null
                    && telegram.regionTarget.length() > 0) {
                regionVisitButton.setVisibility(View.VISIBLE);
                regionVisitButtonContent.setText(String.format(context.getString(R.string.telegrams_region_explore), telegram.regionTarget));
                regionVisitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SparkleHelper.startExploring(context, SparkleHelper.getIdFromName(telegram.regionTarget), SparkleHelper.CLICKY_REGION_MODE);
                    }
                });
            }
            else {
                regionVisitButton.setVisibility(View.GONE);
                regionVisitButton.setOnClickListener(null);
            }
        }
    }

    public class TelegramPreviewCard extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private Telegram telegram;
        private TextView header;
        private TextView timestamp;
        private RelativeLayout alertHolder;
        private ImageView alertIcon;
        private TextView alertText;
        private HtmlTextView preview;

        public TelegramPreviewCard(Context c, View v) {
            super(v);
            context = c;
            header = (TextView) v.findViewById(R.id.card_telegram_preview_from);
            timestamp = (TextView) v.findViewById(R.id.card_telegram_preview_time);
            alertHolder = (RelativeLayout) v.findViewById(R.id.card_telegram_preview_alert_holder);
            alertIcon = (ImageView) v.findViewById(R.id.card_telegram_preview_alert_icon);
            alertText = (TextView) v.findViewById(R.id.card_telegram_preview_alert_message);
            preview = (HtmlTextView) v.findViewById(R.id.card_telegram_preview_content);
            v.setOnClickListener(this);
        }

        public void init(Telegram t)
        {
            telegram = t;
            List<String> headerContents = new ArrayList<String>();
            headerContents.add(telegram.sender);
            if (t.recipients != null)
            {
                headerContents.addAll(t.recipients);
            }
            SparkleHelper.setHappeningsFormatting(context, header, SparkleHelper.joinStringList(headerContents, ", "));
            timestamp.setText(SparkleHelper.getReadableDateFromUTC(context, telegram.timestamp));
            setAlertState(telegram.type, true, alertHolder, alertIcon, alertText);
            preview.setText(SparkleHelper.getHtmlFormatting(telegram.preview).toString());

            if (telegram.isUnread) {
                header.setTypeface(null, Typeface.BOLD);
                timestamp.setTypeface(null, Typeface.BOLD_ITALIC);
                alertText.setTypeface(null, Typeface.BOLD);
            }
            else {
                header.setTypeface(null, Typeface.NORMAL);
                timestamp.setTypeface(null, Typeface.ITALIC);
                alertText.setTypeface(null, Typeface.NORMAL);
            }
        }

        @Override
        public void onClick(View v) {
            if (telegram != null)
            {
                Intent readActivityIntent = new Intent(context, TelegramReadActivity.class);
                readActivityIntent.putExtra(TelegramReadActivity.ID_DATA, telegram.id);
                readActivityIntent.putExtra(TelegramReadActivity.TITLE_DATA, header.getText().toString());

                telegram.isUnread = false;
                header.setTypeface(null, Typeface.NORMAL);
                timestamp.setTypeface(null, Typeface.ITALIC);
                alertText.setTypeface(null, Typeface.NORMAL);
                notifyItemChanged(getAdapterPosition());

                context.startActivity(readActivityIntent);
            }
        }
    }

    public class NoTelegramsCard extends RecyclerView.ViewHolder {
        public NoTelegramsCard(Context c, View v)
        {
            super(v);
            TextView cardTime = (TextView) v.findViewById(R.id.card_happening_time);
            TextView cardContent = (TextView) v.findViewById(R.id.card_happening_content);
            cardTime.setVisibility(View.GONE);
            cardContent.setText(c.getString(R.string.rmb_no_content));
            cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 0);
            cardContent.setLayoutParams(params);
        }
    }
}
