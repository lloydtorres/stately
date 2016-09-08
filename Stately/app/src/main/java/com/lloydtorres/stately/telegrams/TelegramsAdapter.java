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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.TelegramFolder;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.report.ReportActivity;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-03-09.
 * An adapter used for displaying telegrams. Can be used for previews and full telegrams.
 */
public class TelegramsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // constants for setting the mode for the expanded telegram popup menu
    public static final int POPUP_NONE = 0;
    public static final int POPUP_ARCHIVE = 1;
    public static final int POPUP_SENT = 2;
    public static final int POPUP_NORMAL = 3;

    private static final int EMPTY_INDICATOR = -1;

    // constants for the different types of cards
    private static final int PREVIEW_CARD = 0;
    private static final int FULL_CARD = 1;
    private static final int EMPTY_CARD = 2;

    private Context context;
    private List<Telegram> telegrams;
    private TelegramsFragment fragment;
    private int displayMode;
    private boolean isHistory;

    public TelegramsAdapter(List<Telegram> t, TelegramsFragment tf, String folderName)
    {
        setTelegrams(t);
        context = tf.getContext();
        fragment = tf;
        setFolder(folderName);
    }

    public TelegramsAdapter(Context c, List<Telegram> t)
    {
        context = c;
        setTelegrams(t);
        displayMode = POPUP_NONE;
        isHistory = true;
    }

    /**
     * Sets the contents of this telegram adapter.
     * @param t List of telegrams
     */
    public void setTelegrams(List<Telegram> t)
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

    /**
     * Defines the display mode of the PopupMenu used for expanded telegrams.
     * @param name
     */
    public void setFolder(String name) {
        if (TelegramFolder.TELEGRAM_FOLDER_SENT.equals(name)) {
            displayMode = POPUP_SENT;
        } else {
            displayMode = TelegramFolder.TELEGRAM_FOLDER_ARCHIVE.matcher(name).matches() ? POPUP_ARCHIVE : POPUP_NORMAL;
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
        if (telegrams.get(position).id == EMPTY_INDICATOR)
        {
            return EMPTY_CARD;
        }
        else {
            return isHistory ? FULL_CARD : (telegrams.get(position).isExpanded ? FULL_CARD : PREVIEW_CARD);
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
     * Deletes a telegram and notifies the adapter of removal if its ID matches the specified Id.
     * @param id
     */
    public void invalidateTelegram(int id) {
        for (int i=0; i<telegrams.size(); i++) {
            if (telegrams.get(i).id == id) {
                telegrams.remove(i);
                notifyItemRemoved(i);

                if (telegrams.size() <= 0) {
                    Telegram empty = new Telegram();
                    empty.id = EMPTY_INDICATOR;
                    telegrams.add(empty);
                    notifyDataSetChanged();
                }
                break;
            }
        }
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

    public class TelegramCard extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        private Context context;
        private Telegram telegram;

        private TextView sender;
        private TextView recipients;
        private TextView timestamp;

        private ImageView popupMenuButton;

        private RelativeLayout alertHolder;
        private ImageView alertIcon;
        private TextView alertText;

        private HtmlTextView content;
        private ImageView telegramHistoryButton;
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
            popupMenuButton = (ImageView) v.findViewById(R.id.card_telegram_popup_menu);
            alertHolder = (RelativeLayout) v.findViewById(R.id.card_telegram_alert_holder);
            alertIcon = (ImageView) v.findViewById(R.id.card_telegram_alert_icon);
            alertText = (TextView) v.findViewById(R.id.card_telegram_alert_message);
            content = (HtmlTextView) v.findViewById(R.id.card_telegram_content);
            telegramHistoryButton = (ImageView) v.findViewById(R.id.card_telegram_history);
            replyHolder = (LinearLayout) v.findViewById(R.id.card_telegram_actions_holder);
            reply = (ImageView) v.findViewById(R.id.card_telegram_reply);
            replyAll = (ImageView) v.findViewById(R.id.card_telegram_reply_all);
            regionVisitButton = (LinearLayout) v.findViewById(R.id.card_telegram_region_holder);
            regionVisitButtonContent = (TextView) v.findViewById(R.id.card_telegram_region_text);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (telegram != null && fragment != null && context != null) {
                switch (item.getItemId()) {
                    case R.id.telegrams_archive:
                        fragment.showArchiveTelegramDialog(telegram.id);
                        return true;
                    case R.id.telegrams_move:
                        fragment.showMoveTelegramDialog(telegram.id);
                        return true;
                    case R.id.telegrams_delete:
                        fragment.showDeleteTelegramDialog(telegram.id);
                        return true;
                    case R.id.telegrams_report:
                        SparkleHelper.startReport(context, ReportActivity.REPORT_TYPE_TELEGRAM, telegram.id, telegram.sender.replace("@@", ""));
                        return true;
                    case R.id.telegrams_close:
                        telegram.isExpanded = false;
                        notifyItemChanged(getAdapterPosition());
                        return true;
                }
            }
            return false;
        }

        public void init(Telegram t)
        {
            telegram = t;
            SparkleHelper.setHappeningsFormatting(context, sender, telegram.sender);

            if (fragment != null) {
                fragment.markAsRead(telegram.id);
            }

            switch (displayMode) {
                case POPUP_NONE:
                    popupMenuButton.setVisibility(View.GONE);
                    popupMenuButton.setOnClickListener(null);
                    break;
                case POPUP_SENT:
                    popupMenuButton.setVisibility(View.VISIBLE);
                    popupMenuButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popup = new PopupMenu(context, popupMenuButton);
                            popup.getMenuInflater().inflate(R.menu.popup_telegram_read_sent, popup.getMenu());
                            popup.setOnMenuItemClickListener(TelegramCard.this);
                            popup.show();
                        }
                    });
                    break;
                case POPUP_ARCHIVE:
                    popupMenuButton.setVisibility(View.VISIBLE);
                    popupMenuButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popup = new PopupMenu(context, popupMenuButton);
                            popup.getMenuInflater().inflate(R.menu.popup_telegram_read_archive, popup.getMenu());
                            popup.setOnMenuItemClickListener(TelegramCard.this);
                            popup.show();
                        }
                    });
                    break;
                case POPUP_NORMAL:
                    popupMenuButton.setVisibility(View.VISIBLE);
                    popupMenuButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popup = new PopupMenu(context, popupMenuButton);
                            popup.getMenuInflater().inflate(R.menu.popup_telegram_read_normal, popup.getMenu());
                            popup.setOnMenuItemClickListener(TelegramCard.this);
                            popup.show();
                        }
                    });
                    break;
            }

            if (telegram.recipients != null && telegram.recipients.size() > 0)
            {
                String recipientsContent = String.format(context.getString(R.string.telegrams_recipients), SparkleHelper.joinStringList(telegram.recipients, ", "));
                SparkleHelper.setHappeningsFormatting(context, recipients, recipientsContent);
                recipients.setVisibility(View.VISIBLE);
            }
            else
            {
                recipients.setVisibility(View.GONE);
            }

            timestamp.setText(SparkleHelper.getReadableDateFromUTC(context, telegram.timestamp));
            setAlertState(telegram.type, false, alertHolder, alertIcon, alertText);
            MuffinsHelper.setTelegramHtmlFormatting(context, content, telegram.content);

            if (isHistory) {
                telegramHistoryButton.setVisibility(View.GONE);
                telegramHistoryButton.setOnClickListener(null);
            } else {
                telegramHistoryButton.setVisibility(View.VISIBLE);
                telegramHistoryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent telegramHistoryIntent = new Intent(context, TelegramHistoryActivity.class);
                        telegramHistoryIntent.putExtra(TelegramHistoryActivity.ID_DATA, telegram.id);
                        context.startActivity(telegramHistoryIntent);
                    }
                });
            }
            telegramHistoryButton.setVisibility(isHistory ? View.GONE : View.VISIBLE);

            final String curNation = SparkleHelper.getActiveUser(context).nationId;
            String senderNationCheck = MuffinsHelper.getNationIdFromFormat(telegram.sender);
            if (senderNationCheck == null || (senderNationCheck != null && senderNationCheck.equals(curNation)))
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
               telegram.isUnread = false;
                header.setTypeface(null, Typeface.NORMAL);
                timestamp.setTypeface(null, Typeface.ITALIC);
                alertText.setTypeface(null, Typeface.NORMAL);

                telegram.isExpanded = true;

                notifyItemChanged(getAdapterPosition());
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
