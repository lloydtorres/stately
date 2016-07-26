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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.TelegramFolder;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Lloyd on 2016-03-11.
 * Shows the contents of a particular telegram and any telegrams part of its conversation.
 */
public class TelegramReadActivity extends AppCompatActivity {
    // Keys for intent data and saved preferences
    public static final String TITLE_DATA = "titleData";
    public static final String TELEGRAM_DATA_2 = "telegramData2";
    public static final String FOLDER_DATA = "folderData";
    public static final String SEL_FOLDER_DATA = "selectedFolderData";
    public static final String CHK_DATA = "chkData";

    public static final int TELEGRAM_READ_RESULTS = 12345;
    public static final String TELEGRAM_READ_RESULTS_ID = "telegramReadResultsId";
    public static final int TELEGRAM_READ_RESULTS_NULL = -1;

    private String title;
    private Telegram telegram;
    private ArrayList<TelegramFolder> folders;
    private int selectedFolder;
    private String chkValue;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View view;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private DialogInterface.OnClickListener archiveClickListener;
    private DialogInterface.OnClickListener deleteClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_refreshview);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            telegram = getIntent().getParcelableExtra(TELEGRAM_DATA_2);
            folders = getIntent().getParcelableArrayListExtra(FOLDER_DATA);
            selectedFolder = getIntent().getIntExtra(SEL_FOLDER_DATA, 0);
            title = getIntent().getStringExtra(TITLE_DATA);
            chkValue = getIntent().getStringExtra(CHK_DATA);
        }
        if (savedInstanceState != null)
        {
            title = savedInstanceState.getString(TITLE_DATA);
            telegram = savedInstanceState.getParcelable(TELEGRAM_DATA_2);
            folders = savedInstanceState.getParcelableArrayList(FOLDER_DATA);
            selectedFolder = savedInstanceState.getInt(SEL_FOLDER_DATA, 0);
            chkValue = savedInstanceState.getString(CHK_DATA);
        }

        view = findViewById(R.id.refreshview_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.refreshview_toolbar);
        setToolbar(toolbar);

        archiveClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                archiveTelegram();
                dialog.dismiss();
            }
        };
        deleteClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startDeleteTelegram();
                dialog.dismiss();
            }
        };

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshview_refresher);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.refreshview_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerAdapter = new TelegramsAdapter(this, telegram);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        markAsRead();
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle(String.format(getString(R.string.telegram_title), title));

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Sends a GET request to NS to mark a certain telegram as read.
     */
    private void markAsRead() {
        if (chkValue == null) {
            return;
        }

        String targetURL = String.format(Locale.US, Telegram.MARK_READ, telegram.id, chkValue);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(TelegramReadActivity.this);
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        DashHelper.getInstance(this).addRequest(stringRequest);
    }

    private void buildReturnDataAndExit(int id) {
        Intent returnData = new Intent();
        returnData.putExtra(TELEGRAM_READ_RESULTS_ID, id);
        setResult(RESULT_OK, returnData);
        finish();
    }

    /**
     * Wrapper to call on NS to archive a telegram.
     */
    private void archiveTelegram() {
        boolean isFolderFound = false;

        for (TelegramFolder f : folders) {
            Matcher m = TelegramFolder.TELEGRAM_FOLDER_ARCHIVE.matcher(f.name);
            if (m.matches()) {
                startMoveTelegram(f.value);
                isFolderFound = true;
                break;
            }
        }

        if (!isFolderFound) {
            SparkleHelper.makeSnackbar(view, getString(R.string.telegrams_action_error));
        }
    }

    private void startMoveTelegram() {
        ArrayList<TelegramFolder> moveableFolders = new ArrayList<TelegramFolder>();
        for (int i=0; i<folders.size(); i++) {
            String name = folders.get(i).name;
            Matcher m = TelegramFolder.TELEGRAM_FOLDER_ARCHIVE.matcher(name);
            if (i == selectedFolder ||
                    TelegramFolder.TELEGRAM_FOLDER_SENT.equals(name) ||
                    TelegramFolder.TELEGRAM_FOLDER_DELETED.equals(name) ||
                    m.matches()) {
                continue;
            }
            moveableFolders.add(folders.get(i));
        }

        if (moveableFolders.size() <= 0) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
            dialogBuilder
                    .setTitle(getString(R.string.telegrams_move))
                    .setMessage(getString(R.string.telegrams_move_none))
                    .setPositiveButton(getString(R.string.got_it), null)
                    .show();
        }
        else {
            FoldersDialog foldersDialog = new FoldersDialog();
            foldersDialog.setFolders(moveableFolders);
            foldersDialog.setActivity(this);
            foldersDialog.setSelected(FoldersDialog.NO_SELECTION);
            foldersDialog.show(getSupportFragmentManager(), FoldersDialog.DIALOG_TAG);
        }
    }

    /**
     * Wrapper for call to move a telegram to some folder.
     * This lets the app show fancy loading animation in the meantime.
     * @param targetFolder
     */
    public void startMoveTelegram(final String targetFolder) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                moveTelegram(targetFolder);
            }
        });
    }

    /**
     * The actual call to move a telegram to some folder.
     * @param targetFolder
     */
    private void moveTelegram(final String targetFolder) {
        if (chkValue == null) {
            SparkleHelper.makeSnackbar(view, getString(R.string.telegrams_action_error));
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        final int telegramId = telegram.id;
        String finalTarget = targetFolder;
        // Telegrams to be sent to inbox just use a blank parameter
        if (TelegramFolder.TELEGRAM_FOLDER_INBOX_VAL.equals(targetFolder)) {
            finalTarget = "";
        }
        String targetURL = String.format(Locale.US, Telegram.MOVE_TELEGRAM, telegramId, finalTarget, chkValue);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        buildReturnDataAndExit(telegramId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(TelegramReadActivity.this);
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Wrapper for call to delete a telegram
     * This lets the app show fancy loading animation in the meantime.
     */
    private void startDeleteTelegram() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                deleteTelegram();
            }
        });
    }

    /**
     * The actual call to move a telegram to some folder.
     */
    private void deleteTelegram() {
        if (chkValue == null) {
            SparkleHelper.makeSnackbar(view, getString(R.string.telegrams_action_error));
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        String templateURL = Telegram.DELETE_TELEGRAM;
        if (TelegramFolder.TELEGRAM_FOLDER_DELETED.equals(folders.get(selectedFolder).name)) {
            templateURL = Telegram.PERMDELETE_TELEGRAM;
        }
        final int telegramId = telegram.id;
        String targetURL = String.format(Locale.US, templateURL, telegramId, chkValue);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        buildReturnDataAndExit(telegramId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(TelegramReadActivity.this);
                params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        String curNation = "@@" + SparkleHelper.getActiveUser(this).nationId + "@@";
        if (!curNation.equals(SparkleHelper.getIdFromName(telegram.sender))) {
            Matcher m = TelegramFolder.TELEGRAM_FOLDER_ARCHIVE.matcher(folders.get(selectedFolder).name);
            inflater.inflate(m.matches() ? R.menu.activity_telegram_read_noarchive : R.menu.activity_telegram_read, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                buildReturnDataAndExit(TELEGRAM_READ_RESULTS_NULL);
                return true;
            case R.id.telegrams_archive:
                dialogBuilder
                        .setTitle(getString(R.string.telegrams_archive_confirm))
                        .setPositiveButton(getString(R.string.telegrams_archive), archiveClickListener)
                        .setNegativeButton(getString(R.string.explore_negative), null)
                        .show();
                return true;
            case R.id.telegrams_move:
                startMoveTelegram();
                return true;
            case R.id.telegrams_delete:
                dialogBuilder
                        .setTitle(getString(R.string.telegrams_delete_confirm))
                        .setPositiveButton(getString(R.string.telegrams_delete), deleteClickListener)
                        .setNegativeButton(getString(R.string.explore_negative), null)
                        .show();
                return true;
            case R.id.telegrams_report:
                // @TODO: Implement reporting
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (title != null)
        {
            savedInstanceState.putString(TITLE_DATA, title);
        }
        if (telegram != null)
        {
            savedInstanceState.putParcelable(TELEGRAM_DATA_2, telegram);
        }
        if (folders != null)
        {
            savedInstanceState.putParcelableArrayList(FOLDER_DATA, folders);
            savedInstanceState.putInt(SEL_FOLDER_DATA, selectedFolder);
        }
        if (chkValue != null)
        {
            savedInstanceState.putString(CHK_DATA, chkValue);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (title == null)
            {
                title = savedInstanceState.getString(TITLE_DATA);
            }
            if (telegram == null)
            {
                telegram = savedInstanceState.getParcelable(TELEGRAM_DATA_2);
            }
            if (folders == null)
            {
                folders = savedInstanceState.getParcelableArrayList(FOLDER_DATA);
                selectedFolder = savedInstanceState.getInt(SEL_FOLDER_DATA, 0);
            }
            if (chkValue == null)
            {
                chkValue = savedInstanceState.getString(CHK_DATA);
            }
        }
    }
}
