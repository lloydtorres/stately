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

package com.lloydtorres.stately.issues;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.core.RefreshviewActivity;
import com.lloydtorres.stately.dto.CensusDelta;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueFullHolder;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResult;
import com.lloydtorres.stately.dto.IssueResultHeadline;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;
import com.lloydtorres.stately.helpers.network.NSStringRequest;
import com.lloydtorres.stately.settings.SettingsActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-28.
 * This activity displays options for a particular issue.
 */
public class IssueDecisionActivity extends RefreshviewActivity {
    // Keys for Intent data
    public static final String ISSUE_DATA = "issueData";
    public static final String NATION_DATA = "nationData";

    private static final String LEGISLATION_PASSED = "LEGISLATION PASSED";
    private static final String NOT_AVAILABLE = "Issue Not Available";

    private Issue issue;
    private Nation mNation;

    private boolean isInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removePaddingTop();

        isInProgress = false;
        // Either get data from intent or restore state
        if (getIntent() != null) {
            issue = getIntent().getParcelableExtra(ISSUE_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null) {
            issue = savedInstanceState.getParcelable(ISSUE_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        getSupportActionBar().setTitle(String.format(Locale.US, getString(R.string.issue_activity_title), mNation.name, SparkleHelper.getPrettifiedNumber(issue.id)));
        mSwipeRefreshLayout.setEnabled(false);

        startConfirmIssueAvailable();
    }

    /**
     * Helper call to start check if issue is still available.
     */
    private void startConfirmIssueAvailable() {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                confirmIssueAvailable();
            }
        });
    }

    /**
     * Actually confirms that the issue is still available.
     */
    private void confirmIssueAvailable() {
        String targetURL = String.format(Locale.US, IssueFullHolder.CONFIRM_QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains(NOT_AVAILABLE)) {
                            SparkleHelper.makeSnackbar(mView, String.format(Locale.US, getString(R.string.issue_unavailable), mNation.name));
                        } else {
                            setRecyclerAdapter(issue);
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show the issue anyway on error
                mSwipeRefreshLayout.setRefreshing(false);
                SparkleHelper.logError(error.toString());
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                } else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            // Show the issue anyway if API limit reached
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Either initializes the recycler adapter or resets the data.
     * @param issue
     */
    private void setRecyclerAdapter(Issue issue) {
        if (mRecyclerAdapter == null) {
            mRecyclerAdapter = new IssueDecisionRecyclerAdapter(this, issue);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        } else {
            ((IssueDecisionRecyclerAdapter) mRecyclerAdapter).setIssue(issue);
        }
    }

    /**
     * Helper to confirm the position selected by the user.
     * @param option The option selected.
     */
    public void setAdoptPosition(final IssueOption option) {
        if (isInProgress) {
            SparkleHelper.makeSnackbar(mView, getString(R.string.multiple_request_error));
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, RaraHelper.getThemeMaterialDialog(this));
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startPostAdoptPosition(option);
            }
        };

        if (SettingsActivity.getConfirmIssueDecisionSetting(this)) {
            dialogBuilder
                    .setNegativeButton(getString(R.string.explore_negative), null);

            switch (option.id) {
                case IssueOption.DISMISS_ISSUE_ID:
                    dialogBuilder.setTitle(getString(R.string.issue_option_confirm_dismiss))
                            .setPositiveButton(getString(R.string.issue_option_dismiss), dialogClickListener);
                    break;
                default:
                    dialogBuilder.setTitle(String.format(Locale.US, getString(R.string.issue_option_confirm_adopt), option.index))
                            .setPositiveButton(getString(R.string.issue_option_adopt), dialogClickListener);
                    break;
            }

            if (!isFinishing()) {
                dialogBuilder.show();
            }
        }
        else {
            startPostAdoptPosition(option);
        }
    }

    private void startPostAdoptPosition(final IssueOption option) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                postAdoptPosition(option);
            }
        });
    }

    /**
     * Send the position selected by the user back to the server.
     * @param option The option selected.
     */
    public void postAdoptPosition(final IssueOption option) {
        isInProgress = true;
        String targetURL = String.format(Locale.US, IssueOption.POST_QUERY, issue.id);

        NSStringRequest stringRequest = new NSStringRequest(getApplicationContext(), Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isInProgress = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (option.id != IssueOption.DISMISS_ISSUE_ID) {
                            if (response != null && response.contains(LEGISLATION_PASSED)) {
                                try {
                                    response = URLDecoder.decode(URLEncoder.encode(response, "ISO-8859-1"), "UTF-8");
                                } catch (Exception e) {
                                    SparkleHelper.logError(e.toString());
                                }
                                Intent issueResultsActivity = processAndPackResultsData(response, option);
                                startActivity(issueResultsActivity);
                                finish();
                            }
                            else {
                                SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                            }
                        }
                        else {
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                isInProgress = false;
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put(String.format(Locale.US, IssueOption.POST_HEADER_TEMPLATE, option.id), "1");
        stringRequest.setParams(params);

        if (!DashHelper.getInstance(this).addRequest(stringRequest)) {
            mSwipeRefreshLayout.setRefreshing(false);
            isInProgress = false;
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Builds an intent to IssueResults Activity and packages bits and pieces of the issue decision results HTML into it.
     * @param response
     * @param option
     * @return
     */
    private Intent processAndPackResultsData(String response, IssueOption option) {
        Intent issueResultsActivity = new Intent(IssueDecisionActivity.this, IssueResultsActivity.class);
        issueResultsActivity.putExtra(IssueResultsActivity.NATION_DATA, mNation);

        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);

        // Get talking point and reclassifications
        IssueResult issueResult = new IssueResult();
        issueResult.issueContent = issue.content;
        issueResult.issuePosition = option.content;

        Element resultsContainer = d.select("div.dilemma").first();
        if (resultsContainer != null) {
            for (int i = 0; i < resultsContainer.select("p").size(); i++) {
                String rawResultData = resultsContainer.select("p").get(i).text();
                if (i == 0) {
                    issueResult.mainResult = rawResultData;
                } else {
                    if (issueResult.reclassResults == null) {
                        issueResult.reclassResults = rawResultData;
                    } else {
                        StringBuilder sb = new StringBuilder(issueResult.reclassResults);
                        sb.append(" ");
                        sb.append(rawResultData);
                        issueResult.reclassResults = sb.toString();
                    }
                }
            }
        }
        issueResultsActivity.putExtra(IssueResultsActivity.ISSUE_RESULTS_DATA, issueResult);

        // Get headlines
        ArrayList<IssueResultHeadline> headlines = new ArrayList<IssueResultHeadline>();
        Elements newspapers = d.select("div.dilemmapaper");
        for (Element n : newspapers) {
            Elements newspaperContent = n.getAllElements();
            IssueResultHeadline headline = new IssueResultHeadline();

            Element text = newspaperContent.select("div.dpaper4").first();
            Element img = newspaperContent.select("img.dpaperpic1").first();

            headline.headline = text.text();
            headline.imgUrl = SparkleHelper.BASE_URI_NOSLASH + img.attr("src");
            if ((headline.headline == null || headline.headline.length() <= 0) || (headline.imgUrl == null || headline.imgUrl.length() <= 0)) {
                break;
            }
            headlines.add(headline);
        }
        issueResultsActivity.putExtra(IssueResultsActivity.HEADLINES_DATA, headlines);

        // Get postcards if available
        ArrayList<IssuePostcard> postcards = new ArrayList<IssuePostcard>();
        Element postcardContainer = d.select("div.bannerpostcards").first();
        if (postcardContainer != null) {
            Elements postcardHolders = postcardContainer.select("a.bannerpostcard");
            for (Element p : postcardHolders) {
                IssuePostcard postcard = new IssuePostcard();

                Element img = p.select("img").first();
                Element text = p.select("div.bannerpostcardtitle").first();

                postcard.imgUrl = SparkleHelper.BASE_URI_NOSLASH + img.attr("src");
                postcard.title = text.text();
                postcards.add(postcard);
            }
        }
        issueResultsActivity.putExtra(IssueResultsActivity.POSTCARD_DATA, postcards);

        // Get census deltas
        ArrayList<CensusDelta> censusDeltas = new ArrayList<CensusDelta>();
        Element censusDeltaContainer = d.select("div.wceffects").first();
        if (censusDeltaContainer != null) {
            Elements deltasHolder = censusDeltaContainer.select("a.wc-change");
            for (Element de : deltasHolder) {
                CensusDelta censusDelta = new CensusDelta();
                int idHolder = Integer.valueOf(de.attr("href").replaceAll(CensusDelta.REGEX_ID, ""));
                Element deltaHolder = de.select("span.wc2").first();
                String deltaValue = deltaHolder.text();
                // Remove -/+ symbols if present
                deltaValue = deltaValue.replace("-", "").replace("+", "");
                boolean isPositive = deltaHolder.hasClass("wcg");
                censusDelta.censusId = idHolder;
                censusDelta.delta = deltaValue;
                censusDelta.isPositive = isPositive;
                censusDeltas.add(censusDelta);
            }
        }
        issueResultsActivity.putExtra(IssueResultsActivity.CENSUSDELTA_DATA, censusDeltas);

        return issueResultsActivity;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issue != null) {
            savedInstanceState.putParcelable(ISSUE_DATA, issue);
        }
        if (mNation != null) {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (issue == null) {
                issue = savedInstanceState.getParcelable(ISSUE_DATA);
            }
            if (mNation == null) {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
