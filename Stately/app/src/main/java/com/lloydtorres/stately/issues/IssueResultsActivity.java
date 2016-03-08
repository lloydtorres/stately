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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.CensusDelta;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResultHeadline;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-02-29.
 * This activity shows the results of an issue decision.
 */
public class IssueResultsActivity extends AppCompatActivity {
    public static final String RESPONSE_DATA = "responseData";
    public static final String OPTION_DATA = "optionData";
    public static final String NEWS_DATA = "newsData";
    public static final String HEADLINES_DATA = "headlinesData";
    public static final String POSTCARD_DATA = "postcardData";
    public static final String CENSUSDELTA_DATA = "censusDeltaData";
    public static final String NATION_DATA = "nationData";

    private static final String RECLASSIFICATION = "Reclassification";

    private String news;
    private IssueOption option;
    private ArrayList<IssueResultHeadline> headlines;
    private ArrayList<IssuePostcard> postcards;
    private ArrayList<CensusDelta> censusDeltas;
    private Nation mNation;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_results);

        String response = null;
        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            response = getIntent().getStringExtra(RESPONSE_DATA);
            option = getIntent().getParcelableExtra(OPTION_DATA);
            mNation = getIntent().getParcelableExtra(NATION_DATA);
        }
        if (savedInstanceState != null)
        {
            news = savedInstanceState.getString(NEWS_DATA);
            option = savedInstanceState.getParcelable(OPTION_DATA);
            headlines = savedInstanceState.getParcelableArrayList(HEADLINES_DATA);
            postcards = savedInstanceState.getParcelableArrayList(POSTCARD_DATA);
            censusDeltas = savedInstanceState.getParcelableArrayList(CENSUSDELTA_DATA);
            mNation = savedInstanceState.getParcelable(NATION_DATA);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.results_toolbar);
        setToolbar(toolbar);

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.results_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (response == null && news != null)
        {
            setRecyclerAdapter();
        }
        else
        {
            processResultsData(response);
        }
    }

    private void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Sets up the activity's contents based on the response data.
     * @param response HTML response for resolving an issue
     */
    private void processResultsData(String response)
    {
        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);

        // Get talking point and reclassification
        Element resultsContainer = d.select("div.dilemma").first();
        if (resultsContainer != null)
        {
            news = resultsContainer.select("p").first().text();
            if (resultsContainer.text().contains(RECLASSIFICATION))
            {
                news = news + "<br><br>" + resultsContainer.select("p").get(1).text();
            }
        }

        // Get headlines
        headlines = new ArrayList<IssueResultHeadline>();
        Elements newspapers = d.select("div.dilemmapaper");
        for (Element n : newspapers)
        {
            Elements newspaperContent = n.getAllElements();
            IssueResultHeadline headline = new IssueResultHeadline();

            Element text = newspaperContent.select("div.dpaper4").first();
            Element img = newspaperContent.select("img.dpaperpic1").first();

            headline.headline = text.text();
            headline.imgUrl = SparkleHelper.BASE_URI_NOSLASH + img.attr("src");
            headlines.add(headline);
        }

        // Get postcards if available
        postcards = new ArrayList<IssuePostcard>();
        Element postcardContainer = d.select("div.bannerpostcards").first();
        if (postcardContainer != null)
        {
            Elements postcardHolders = postcardContainer.select("a.bannerpostcard");
            for (Element p : postcardHolders)
            {
                IssuePostcard postcard = new IssuePostcard();

                Element img = p.select("img").first();
                Element text = p.select("div.bannerpostcardtitle").first();

                postcard.imgUrl = SparkleHelper.BASE_URI_NOSLASH + img.attr("src");
                postcard.title = text.text();
                postcards.add(postcard);
            }
        }

        // Get census deltas
        censusDeltas = new ArrayList<CensusDelta>();
        Element censusDeltaContainer = d.select("div.wceffects").first();
        if (censusDeltaContainer != null)
        {
            Elements deltasHolder = censusDeltaContainer.select("a.wc-change");
            for (Element de : deltasHolder)
            {
                CensusDelta censusDelta = new CensusDelta();
                int idHolder = Integer.valueOf(de.attr("href").replaceAll(CensusDelta.REGEX_ID, ""));
                Element deltaHolder = de.select("span.wc2").first();
                String deltaValue = deltaHolder.text();
                boolean isPositive = deltaHolder.hasClass("wcg");
                censusDelta.censusId = idHolder;
                censusDelta.delta = deltaValue;
                censusDelta.isPositive = isPositive;
                censusDeltas.add(censusDelta);
            }
        }

        setRecyclerAdapter();
    }

    /**
     * Helper class for initializing the recycler adapter.
     */
    private void setRecyclerAdapter()
    {
        List<Object> resultsContent = new ArrayList<Object>();
        if (news != null)
        {
            resultsContent.add(news);
        }
        resultsContent.add(option);
        resultsContent.addAll(headlines);
        if (postcards != null)
        {
            resultsContent.addAll(postcards);
        }
        if (censusDeltas != null)
        {
            resultsContent.addAll(censusDeltas);
        }

        mRecyclerAdapter = new IssueResultsRecyclerAdapter(this, resultsContent, mNation);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (news != null)
        {
            savedInstanceState.putString(NEWS_DATA, news);
        }
        if (option != null)
        {
            savedInstanceState.putParcelable(OPTION_DATA, option);
        }
        if (headlines != null)
        {
            savedInstanceState.putParcelableArrayList(HEADLINES_DATA, headlines);
        }
        if (postcards != null)
        {
            savedInstanceState.putParcelableArrayList(POSTCARD_DATA, postcards);
        }
        if (censusDeltas != null)
        {
            savedInstanceState.putParcelableArrayList(CENSUSDELTA_DATA, censusDeltas);
        }
        if (mNation != null)
        {
            savedInstanceState.putParcelable(NATION_DATA, mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (news != null)
            {
                news = savedInstanceState.getString(NEWS_DATA);
            }
            if (option != null)
            {
                option = savedInstanceState.getParcelable(OPTION_DATA);
            }
            if (headlines != null)
            {
                headlines = savedInstanceState.getParcelableArrayList(HEADLINES_DATA);
            }
            if (postcards != null)
            {
                postcards = savedInstanceState.getParcelableArrayList(POSTCARD_DATA);
            }
            if (censusDeltas != null)
            {
                censusDeltas = savedInstanceState.getParcelableArrayList(CENSUSDELTA_DATA);
            }
            if (mNation != null)
            {
                mNation = savedInstanceState.getParcelable(NATION_DATA);
            }
        }
    }
}
