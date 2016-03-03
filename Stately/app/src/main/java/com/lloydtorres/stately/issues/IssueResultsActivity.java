package com.lloydtorres.stately.issues;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResultHeadline;
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

    private static final String RECLASSIFICATION = "Reclassification";

    private String news;
    private IssueOption option;
    private ArrayList<IssueResultHeadline> headlines;
    private ArrayList<IssuePostcard> postcards;

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
        }
        if (savedInstanceState != null)
        {
            news = savedInstanceState.getString(NEWS_DATA);
            option = savedInstanceState.getParcelable(OPTION_DATA);
            headlines = savedInstanceState.getParcelableArrayList(HEADLINES_DATA);
            postcards = savedInstanceState.getParcelableArrayList(POSTCARD_DATA);
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
                news = news + "\n\n" + resultsContainer.select("p").get(1).text();
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

        mRecyclerAdapter = new IssueResultsRecyclerAdapter(this, resultsContent);
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
        }
    }
}
