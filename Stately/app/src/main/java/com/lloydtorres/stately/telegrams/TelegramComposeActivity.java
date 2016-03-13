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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-03-12.
 * This activity lets users compose or reply to telegrams.
 */
public class TelegramComposeActivity extends AppCompatActivity {
    // Keys for intent data
    public static final String REPLY_ID_DATA = "replyIdData";
    public static final String RECIPIENTS_DATA = "recipientsData";
    public static final int NO_REPLY_ID = -1;

    private int replyId = NO_REPLY_ID;
    private String recipients;

    private View mView;
    private EditText recipientsField;
    private TextView senderField;
    private EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_compose);

        // Either get data from intent or restore state
        if (getIntent() != null) {
            replyId = getIntent().getIntExtra(REPLY_ID_DATA, NO_REPLY_ID);
            recipients = getIntent().getStringExtra(RECIPIENTS_DATA);
        }

        mView = findViewById(R.id.telegram_compose_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.telegram_compose_toolbar);
        setToolbar(toolbar);

        recipientsField = (EditText) findViewById(R.id.telegram_compose_recipients);
        recipientsField.setCustomSelectionActionModeCallback(new NullActionCallback());
        if (recipients != null && recipients.length() > 0)
        {
            recipientsField.setText(recipients);
        }

        senderField = (TextView) findViewById(R.id.telegram_compose_sender);
        senderField.setText(SparkleHelper.getActiveUser(this).name);

        content = (EditText) findViewById(R.id.telegram_compose_content);
        content.setCustomSelectionActionModeCallback(new NullActionCallback());
        content.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        String title = getString(R.string.telegrams_compose);
        if (replyId != NO_REPLY_ID)
        {
            title = getString(R.string.telegrams_reply);
        }
        getSupportActionBar().setTitle(title);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_telegram_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                finish();
                return true;
            case R.id.nav_send_telegram:
                // @TODO Send telegram
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
