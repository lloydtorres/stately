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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.IToolbarActivity;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.TelegramFolder;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.NSStringRequest;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Created by Lloyd on 2016-03-08.
 * This is the Telegrams section of the main Stately activity.
 */
public class TelegramsFragment extends Fragment {
    public static final String KEY_PAST_OFFSET = "keyPastOffset";
    public static final String KEY_TELEGRAMS = "keyTelegrams";
    public static final String KEY_FOLDERS = "keyFolders";
    public static final String KEY_ACTIVE = "keyActive";

    // Direction to scan for messages
    private static final int SCAN_BACKWARD = 0;
    private static final int SCAN_FORWARD = 1;

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipyRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private AlertDialog.Builder dialogBuilder;

    private ArrayList<Telegram> telegrams;
    private ArrayList<TelegramFolder> folders;
    private int selectedFolder;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;
    private String chkValue;

    @Override
    public void onAttach(Context context) {
        // Get activity for manipulation
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.content_message_board, container, false);
        telegrams = new ArrayList<Telegram>();
        folders = new ArrayList<TelegramFolder>();
        TelegramFolder activeFolder = new TelegramFolder();
        activeFolder.name = "Inbox";
        activeFolder.value = "inbox";
        folders.add(activeFolder);
        selectedFolder = 0;
        uniqueEnforcer = new HashSet<Integer>();

        // Restore state
        if (savedInstanceState != null)
        {
            pastOffset = savedInstanceState.getInt(KEY_PAST_OFFSET, 0);
            telegrams = savedInstanceState.getParcelableArrayList(KEY_TELEGRAMS);
            folders = savedInstanceState.getParcelableArrayList(KEY_FOLDERS);
            selectedFolder = savedInstanceState.getInt(KEY_ACTIVE, 0);
            rebuildUniqueEnforcer();
        }

        toolbar = (Toolbar) mView.findViewById(R.id.message_board_toolbar);
        toolbar.setTitle(getString(R.string.menu_telegrams));

        if (mActivity != null && mActivity instanceof IToolbarActivity)
        {
            ((IToolbarActivity) mActivity).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipyRefreshLayout) mView.findViewById(R.id.message_board_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction.equals(SwipyRefreshLayoutDirection.TOP))
                {
                    queryTelegrams(0, SCAN_FORWARD, false);
                }
                else
                {
                    queryTelegrams(pastOffset, SCAN_BACKWARD, false);
                }
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.message_board_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        dialogBuilder = new AlertDialog.Builder(getContext(), R.style.MaterialDialog);

        startQueryTelegrams(SCAN_FORWARD);

        return mView;
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     * @param direction Direction to scan in
     */
    public void startQueryTelegrams(final int direction)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryTelegrams(0, direction, true);
            }
        });
    }

    /**
     * Scrape and parse telegrams from NS site.
     */
    private void queryTelegrams(final int offset, final int direction, final boolean firstRun)
    {
        TelegramFolder activeFolder = null;

        if (selectedFolder < folders.size())
        {
            activeFolder = folders.get(selectedFolder);
        }
        else
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
            return;
        }

        String targetURL = String.format(Locale.US, Telegram.GET_TELEGRAM, activeFolder.value, offset);

        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded())
                        {
                            return;
                        }
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element chkHolder = d.select("input[name=chk]").first();
                        if (chkHolder != null) {
                            chkValue = chkHolder.attr("value");
                        }
                        processRawTelegrams(d, direction, firstRun);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded())
                {
                    return;
                }
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually parse through the response sent by NS and build telegram objects.
     * @param d Document containing parsed response.
     * @param direction Direction the user is loading telegrams
     * @param firstRun if first time running this process
     */
    private void processRawTelegrams(Document d, int direction, boolean firstRun)
    {
        Element telegramsContainer = d.select("div#tglist").first();
        Element foldersContainer = d.select("select#tgfolder").first();

        if (telegramsContainer == null || foldersContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
            return;
        }

        // Build list of folders
        folders = new ArrayList<TelegramFolder>();
        Elements rawFolders = foldersContainer.select("option[value]");
        for (Element rf : rawFolders)
        {
            TelegramFolder telFolder = new TelegramFolder();
            String rfValue = rf.attr("value");
            if (!rfValue.equals("_new"))
            {
                String rfName = rf.text();
                telFolder.name = rfName;
                telFolder.value = rfValue;
                folders.add(telFolder);
            }
        }

        // Build telegram objects from raw telegrams
        ArrayList<Telegram> scannedTelegrams = MuffinsHelper.processRawTelegrams(telegramsContainer, SparkleHelper.getActiveUser(getContext()).nationId);
        switch (direction)
        {
            case SCAN_FORWARD:
                processTelegramsForward(scannedTelegrams, firstRun);
                break;
            default:
                processTelegramsBackward(scannedTelegrams);
                break;
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Processes the scanned telegrams if scanning forward (i.e. new telegrams).
     * @param scannedTelegrams Telegrams scanned from NS
     * @param firstRun if running this process for first time
     */
    private void processTelegramsForward(ArrayList<Telegram> scannedTelegrams, boolean firstRun)
    {
        int uniqueMessages = 0;

        for (Telegram t : scannedTelegrams)
        {
            if (!uniqueEnforcer.contains(t.id))
            {
                telegrams.add(t);
                uniqueEnforcer.add(t.id);
                uniqueMessages++;
            }
        }

        pastOffset += uniqueMessages;

        if (uniqueMessages <= 0 && !firstRun)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.rmb_caught_up));
        }

        // We've reached the point where we already have the messages, so put everything back together
        refreshRecycler(SCAN_FORWARD);
    }

    /**
     * Processes the scanned telegrams if scanning backwards (i.e. past telegrams).
     * @param scannedTelegrams Telegrams scanned from NS
     */
    private void processTelegramsBackward(ArrayList<Telegram> scannedTelegrams)
    {
        // If there's nothing in the current telegrams, then there's probably nothing in the past
        if (telegrams.size() <= 0 || scannedTelegrams.size() <= 0)
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rmb_caught_up));
            return;
        }

        Collections.sort(scannedTelegrams);

        // Count the number of obtained telegrams that are earlier than the current earliest
        long earliestCurrentDate = telegrams.get(telegrams.size()-1).timestamp;
        int timeCounter = 0;
        for (Telegram t : scannedTelegrams)
        {
            if (t.timestamp < earliestCurrentDate && !uniqueEnforcer.contains(t.id))
            {
                telegrams.add(t);
                uniqueEnforcer.add(t.id);
                timeCounter++;
                pastOffset++;
            }
        }

        // if no telegrams are from the past, complain
        if (timeCounter < 1)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_backtrack_error));
            mSwipeRefreshLayout.setRefreshing(false);
        }
        else
        {
            refreshRecycler(SCAN_BACKWARD);
        }
    }

    /**
     * Refreshes the contents of the recycler adapter.
     */
    private void refreshRecycler(int direction)
    {
        int oldSize = 0;
        Collections.sort(telegrams);
        if (mRecyclerAdapter == null)
        {
            mRecyclerAdapter = new TelegramsAdapter(telegrams, this, folders.get(selectedFolder).name);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
        else
        {
            oldSize = mRecyclerAdapter.getItemCount();
            ((TelegramsAdapter) mRecyclerAdapter).setTelegrams(telegrams);
            ((TelegramsAdapter) mRecyclerAdapter).setFolder(folders.get(selectedFolder).name);
        }
        mSwipeRefreshLayout.setRefreshing(false);

        switch (direction)
        {
            case SCAN_FORWARD:
                mLayoutManager.scrollToPosition(0);
                break;
            case SCAN_BACKWARD:
                mLayoutManager.scrollToPosition(oldSize+1);
                break;
        }
    }

    /**
     * Displays a dialog showing a list of folders.
     * @param fm
     */
    private void showFoldersDialog(FragmentManager fm)
    {
        FoldersDialog foldersDialog = new FoldersDialog();
        foldersDialog.setFragment(this);
        foldersDialog.setFolders(folders);
        foldersDialog.setSelected(selectedFolder);
        foldersDialog.show(fm, FoldersDialog.DIALOG_TAG);
    }

    public void setSelectedFolder(int selected)
    {
        if (selected < folders.size())
        {
            selectedFolder = selected;
            telegrams = new ArrayList<Telegram>();
            uniqueEnforcer = new HashSet<Integer>();
            pastOffset = 0;
            startQueryTelegrams(SCAN_FORWARD);
        }
    }

    /**
     * Sends a GET request to NS to mark a certain telegram as read.
     * @param id Telegram ID.
     */
    public void markAsRead(int id) {
        if (chkValue == null) {
            return;
        }

        String targetURL = String.format(Locale.US, Telegram.MARK_READ, id, chkValue);
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        DashHelper.getInstance(getContext()).addRequest(stringRequest);
    }

    /**
     * Remove a given telegram ID from the recycler list.
     * @param id
     */
    private void invalidateTelegram(int id) {
        ((TelegramsAdapter) mRecyclerAdapter).invalidateTelegram(id);
        pastOffset = Math.max(0, pastOffset-1);
    }

    /**
     * Wrappers to call on NS to archive a telegram.
     */
    public void showArchiveTelegramDialog(final int id) {
        dialogBuilder
                .setTitle(getString(R.string.telegrams_archive_confirm))
                .setPositiveButton(getString(R.string.telegrams_archive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        archiveTelegram(id);
                        dialog.dismiss();
                    }})
                .setNegativeButton(getString(R.string.explore_negative), null)
                .show();
    }

    private void archiveTelegram(final int id) {
        boolean isFolderFound = false;

        for (TelegramFolder f : folders) {
            Matcher m = TelegramFolder.TELEGRAM_FOLDER_ARCHIVE.matcher(f.name);
            if (m.matches()) {
                startMoveTelegram(id, f.value);
                isFolderFound = true;
                break;
            }
        }

        if (!isFolderFound) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_action_error));
        }
    }

    public void showMoveTelegramDialog(final int id) {
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
            dialogBuilder
                    .setTitle(getString(R.string.telegrams_move))
                    .setMessage(getString(R.string.telegrams_move_none))
                    .setPositiveButton(getString(R.string.got_it), null)
                    .show();
        }
        else {
            FoldersDialog foldersDialog = new FoldersDialog();
            foldersDialog.setFolders(moveableFolders);
            foldersDialog.setFragment(this);
            foldersDialog.setMoveMode(true);
            foldersDialog.setMoveTelegramId(id);
            foldersDialog.setSelected(FoldersDialog.NO_SELECTION);
            foldersDialog.show(getFragmentManager(), FoldersDialog.DIALOG_TAG);
        }
    }

    /**
     * Wrapper for call to move a telegram to some folder.
     * This lets the app show fancy loading animation in the meantime.
     * @param id
     * @param targetFolder
     */
    public void startMoveTelegram(final int id, final String targetFolder) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                moveTelegram(id, targetFolder);
            }
        });
    }

    /**
     * The actual call to move a telegram to some folder.
     * @param id
     * @param targetFolder
     */
    private void moveTelegram(final int id, final String targetFolder) {
        if (chkValue == null) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_action_error));
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        String finalTarget = targetFolder;
        // Telegrams to be sent to inbox just use a blank parameter
        if (TelegramFolder.TELEGRAM_FOLDER_INBOX_VAL.equals(targetFolder)) {
            finalTarget = "";
        }
        String targetURL = String.format(Locale.US, Telegram.MOVE_TELEGRAM, id, finalTarget, chkValue);
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        invalidateTelegram(id);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    public void showDeleteTelegramDialog(final int id) {
        dialogBuilder
            .setTitle(getString(R.string.telegrams_delete_confirm))
            .setPositiveButton(getString(R.string.telegrams_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startDeleteTelegram(id);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(getString(R.string.explore_negative), null)
            .show();
    }

    /**
     * Wrapper for call to delete a telegram
     * This lets the app show fancy loading animation in the meantime.
     * @param id
     */
    private void startDeleteTelegram(final int id) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                deleteTelegram(id);
            }
        });
    }

    /**
     * The actual call to move a telegram to some folder.
     * @param id
     */
    private void deleteTelegram(final int id) {
        if (chkValue == null) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.telegrams_action_error));
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        String templateURL = Telegram.DELETE_TELEGRAM;
        if (TelegramFolder.TELEGRAM_FOLDER_DELETED.equals(folders.get(selectedFolder).name)) {
            templateURL = Telegram.PERMDELETE_TELEGRAM;
        }
        String targetURL = String.format(Locale.US, templateURL, id, chkValue);
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        invalidateTelegram(id);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * This function rebuilds the set used to track unique messages after a restart.
     * Because set isn't parcelable :(
     */
    private void rebuildUniqueEnforcer()
    {
        uniqueEnforcer = new HashSet<Integer>();
        for (Telegram t : telegrams)
        {
            uniqueEnforcer.add(t.id);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_PAST_OFFSET, pastOffset);
        savedInstanceState.putInt(KEY_ACTIVE, selectedFolder);
        if (telegrams != null)
        {
            savedInstanceState.putParcelableArrayList(KEY_TELEGRAMS, telegrams);
        }
        if (folders != null)
        {
            savedInstanceState.putParcelableArrayList(KEY_FOLDERS, folders);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_telegrams, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getChildFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_folders:
                showFoldersDialog(fm);
                return true;
            case R.id.nav_compose:
                SparkleHelper.startTelegramCompose(getContext(), null, TelegramComposeActivity.NO_REPLY_ID);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
