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

package com.lloydtorres.stately.region;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RecyclerSubFragment;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.Embassy;
import com.lloydtorres.stately.dto.EmbassyHolder;
import com.lloydtorres.stately.dto.Officer;
import com.lloydtorres.stately.dto.OfficerHolder;
import com.lloydtorres.stately.dto.Poll;
import com.lloydtorres.stately.dto.PollOption;
import com.lloydtorres.stately.dto.RMBButtonHolder;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.dto.WaVote;
import com.lloydtorres.stately.dto.WaVoteStatus;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.wa.ResolutionActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-24.
 * This is a subfragment of the Region fragment showing information about the Region's community.
 * Accepts a Region object.
 */
public class RegionCommunitySubFragment extends RecyclerSubFragment {
    public static final String CARDS_DATA = "cards";
    public static final String NAME_DATA = "name";

    private Region mRegion;
    private String rmbUnreadCountText;
    private ArrayList<Parcelable> cards = new ArrayList<Parcelable>();
    private String regionName;
    private View mainFragmentView;

    private boolean isInProgress = false;

    public void setRegion(Region r)
    {
        mRegion = r;
    }
    public void setRMBUnreadCountText(String countText) { rmbUnreadCountText = countText; }
    public void setMainFragmentView(View v) { mainFragmentView = v; }

    // Receiver for WA vote broadcasts
    private BroadcastReceiver resolutionVoteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || !isAdded()) {
                return;
            }

            WaVoteStatus currentVoteStatus = intent.getParcelableExtra(ResolutionActivity.TARGET_VOTE_STATUS);
            WaVoteStatus oldVoteStatus = intent.getParcelableExtra(ResolutionActivity.TARGET_OLD_VOTE_STATUS);

            if (oldVoteStatus != null) {
                // Remove old tallies
                updateWaVoteCount(mRegion.gaVote, oldVoteStatus.gaVote, -1);
                updateWaVoteCount(mRegion.scVote, oldVoteStatus.scVote, -1);

                // Add in new tallies
                updateWaVoteCount(mRegion.gaVote, currentVoteStatus.gaVote, 1);
                updateWaVoteCount(mRegion.scVote, currentVoteStatus.scVote, 1);

                // Update data
                initData();
                initRecyclerAdapter(true);
            }
        }
    };

    /**
     * Helper function for updating WA vote counts.
     * @param waVote WaVote object to update.
     * @param voteStatus The current vote status for the given chamber.
     * @param change The amount to change the vote by.
     */
    private void updateWaVoteCount(WaVote waVote, String voteStatus, int change) {
        if (WaVoteStatus.VOTE_FOR.equals(voteStatus)) {
            waVote.voteFor += change;
        } else if (WaVoteStatus.VOTE_AGAINST.equals(voteStatus)) {
            waVote.voteAgainst += change;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        isInProgress = false;

        // Restore save state
        if (savedInstanceState != null) {
            if (cards == null) {
                cards = savedInstanceState.getParcelableArrayList(CARDS_DATA);
            }
            if (regionName == null) {
                regionName = savedInstanceState.getString(NAME_DATA);
            }
        }

        // Register resolution vote receiver
        IntentFilter resolutionVoteFilter = new IntentFilter();
        resolutionVoteFilter.addAction(ResolutionActivity.RESOLUTION_BROADCAST);
        getActivity().registerReceiver(resolutionVoteReceiver, resolutionVoteFilter);

        // Check if regional poll can be voted on
        if (mRegion != null && mRegion.poll != null) {
            regionName = mRegion.name;
            processPoll();
        }

        if ((cards == null || cards.size() <= 0) && mRegion != null) {
            initData();
        }

        initRecyclerAdapter(false);

        return view;
    }

    private void initData() {
        cards = new ArrayList<Parcelable>();
        regionName = mRegion.name;

        // This adds a button to the RMB
        RMBButtonHolder button = new RMBButtonHolder(mRegion.name, rmbUnreadCountText);
        cards.add(button);

        if (mRegion.poll != null) {
            cards.add(mRegion.poll);
        }

        if (mRegion.gaVote != null && (mRegion.gaVote.voteFor + mRegion.gaVote.voteAgainst) > 0) {
            mRegion.gaVote.chamber = Assembly.GENERAL_ASSEMBLY;
            cards.add(mRegion.gaVote);
        }

        if (mRegion.scVote != null && (mRegion.scVote.voteFor + mRegion.scVote.voteAgainst) > 0) {
            mRegion.scVote.chamber = Assembly.SECURITY_COUNCIL;
            cards.add(mRegion.scVote);
        }

        List<Officer> officers = new ArrayList<Officer>(mRegion.officers);
        if (!"0".equals(mRegion.delegate)) {
            officers.add(new Officer(mRegion.delegate, getString(R.string.card_region_wa_delegate), Officer.DELEGATE_ORDER));
        }
        if (!"0".equals(mRegion.founder)) {
            officers.add(new Officer(mRegion.founder, getString(R.string.card_region_founder), Officer.FOUNDER_ORDER));
        }
        Collections.sort(officers);
        cards.add(new OfficerHolder(officers));

        ArrayList<String> embassyList = new ArrayList<String>();
        if (mRegion.embassies != null && mRegion.embassies.size() > 0) {
            // Only add active embassies
            for (Embassy e : mRegion.embassies) {
                if (e.type == null) {
                    embassyList.add(e.name);
                }
            }
        }
        Collections.sort(embassyList, String.CASE_INSENSITIVE_ORDER);
        if (embassyList.size() > 0) {
            cards.add(new EmbassyHolder(embassyList));
        }
    }

    private void initRecyclerAdapter(boolean isOnlySetAdapterOnNull) {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new CommunityRecyclerAdapter(this, cards, regionName);
            if (isOnlySetAdapterOnNull) {
                mRecyclerView.setAdapter(mRecyclerAdapter);
            }
        } else {
            ((CommunityRecyclerAdapter) mRecyclerAdapter).setCards(cards);
        }
        if (!isOnlySetAdapterOnNull) {
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
    }

    /**
     * Checks to see if the user can vote on a regional poll.
     */
    private void processPoll() {
        final Poll poll = mRegion.poll;
        poll.votedOption = Poll.NO_VOTE;

        String targetURL = String.format(Locale.US, Region.QUERY_HTML, SparkleHelper.getIdFromName(mRegion.name));
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element voteButton = d.select("button[name=poll_submit]").first();
                        if (voteButton != null) {
                            poll.isVotingEnabled = true;

                            String userId = PinkaHelper.getActiveUser(getContext()).nationId;
                            for (PollOption option : poll.options) {
                                if (option.voters != null && option.voters.contains(userId)) {
                                    poll.votedOption = option.id;
                                    break;
                                }
                            }

                            ((CommunityRecyclerAdapter) mRecyclerAdapter).updatePoll(poll);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
            }
        });

        DashHelper.getInstance(getContext()).addRequest(stringRequest);
    }

    /**
     * Shows a dialog allowing the user to vote on a regional poll.
     * @param pollData Data for the target poll.
     */
    public void showPollVoteDialog(Poll pollData) {
        if (getActivity() == null || !isAdded()) {
            return;
        }

        PollVoteDialog voteDialog = new PollVoteDialog();
        voteDialog.setData(this, pollData);
        voteDialog.show(getFragmentManager(), PollVoteDialog.DIALOG_TAG);
    }

    /**
     * Starts the process of submitting a poll vote by getting the CHK value, then
     * POSTing the actual vote call.
     * @param pollData
     */
    public void startSubmitPollVote(final Poll pollData) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.multiple_request_error));
            return;
        }

        isInProgress = true;

        String targetURL = String.format(Locale.US, Region.QUERY_HTML, SparkleHelper.getIdFromName(regionName));
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        Element input = d.select("input[name=chk]").first();

                        if (input == null) {
                            SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_parsing));
                            return;
                        }

                        String chk = input.attr("value");
                        postPollVote(chk, pollData);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            isInProgress = false;
            SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * POSTs the actuall poll vote to NationStates and updates the poll card.
     * @param chk NS chk value
     * @param pollData Poll data to submit
     */
    private void postPollVote(final String chk, final Poll pollData) {
        String targetURL = String.format(Locale.US, Region.QUERY_HTML, SparkleHelper.getIdFromName(regionName));
        NSStringRequest stringRequest = new NSStringRequest(getContext(), Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        if (response.contains(Poll.RESPONSE_VOTE) || response.contains(Poll.RESPONSE_WITHDRAW)) {
                            // Add user to voted list then update poll data
                            String userId = PinkaHelper.getActiveUser(getContext()).nationId;
                            for (int i=0; i<pollData.options.size(); i++) {
                                PollOption option = pollData.options.get(i);
                                List<String> voters = new ArrayList<String>();

                                if (option.voters != null && option.voters.length() > 0) {
                                    voters = new ArrayList<String>(Arrays.asList(option.voters.split(":")));
                                }

                                if (voters.contains(userId)) {
                                    voters.remove(userId);
                                    option.votes = Math.max(0, option.votes - 1);
                                }
                                else if (option.id == pollData.votedOption) {
                                    voters.add(userId);
                                    option.votes += 1;
                                }

                                Collections.sort(voters, String.CASE_INSENSITIVE_ORDER);
                                option.voters = SparkleHelper.joinStringList(voters, ":");
                                pollData.options.set(i, option);
                            }

                            ((CommunityRecyclerAdapter) mRecyclerAdapter).updatePoll(pollData);
                        } else {
                            SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_generic));
                        }

                        isInProgress = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("pollid", String.valueOf(pollData.id));
        params.put("chk", chk);
        if (pollData.votedOption != Poll.NO_VOTE) {
            params.put("q1", String.valueOf(pollData.votedOption));
            params.put("poll_submit", "1");
        } else {
            params.put("q1", "0");
            params.put("poll_withdraw", "1");
        }
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest)) {
            isInProgress = false;
            SparkleHelper.makeSnackbar(mainFragmentView, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (cards != null) {
            outState.putParcelableArrayList(CARDS_DATA, cards);
        }
        if (regionName != null) {
            outState.putString(NAME_DATA, regionName);
        }
    }
}
