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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.CensusDelta;
import com.lloydtorres.stately.dto.CensusScale;
import com.lloydtorres.stately.dto.IssuePostcard;
import com.lloydtorres.stately.dto.IssueResult;
import com.lloydtorres.stately.dto.IssueResultContainer;
import com.lloydtorres.stately.dto.IssueResultHeadlinesContainer;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.Policy;
import com.lloydtorres.stately.dto.Reclassification;
import com.lloydtorres.stately.helpers.PinkaHelper;
import com.lloydtorres.stately.helpers.RaraHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.lloydtorres.stately.helpers.network.DashHelper;

import org.atteo.evo.inflector.English;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-02-29.
 * An adapter for showing the results of an issue resolution.
 */
public class IssueResultsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LinkedHashMap<Integer, CensusScale> censusScales;
    private HashMap<String, String> postcardData;
    private List<String> unifiedFreedomScale;

    private static final int ISSUE_RESULT_CARD = 0;
    private static final int HEADLINE_CARD = 1;
    private static final int POSTCARD_CARD = 2;
    private static final int CENSUSDELTA_CARD = 3;
    private static final int POLICY_CARD = 4;

    private static final String PERCENT_TEMPLATE = "%s%%";

    private Context context;
    private List<Object> content;
    private Nation mNation;

    public IssueResultsRecyclerAdapter(Context c, IssueResultContainer con, Nation n) {
        context = c;
        String[] WORLD_CENSUS_ITEMS = context.getResources().getStringArray(R.array.census);
        censusScales = SparkleHelper.getCensusScales(WORLD_CENSUS_ITEMS);

        String[] rawPostcardData = context.getResources().getStringArray(R.array.postcards);
        postcardData = new HashMap<String, String>();
        for (String r : rawPostcardData) {
            String[] postcard = r.split("##");
            postcardData.put(postcard[1], postcard[0]);
        }

        unifiedFreedomScale = Arrays.asList(context.getResources().getStringArray(R.array.freedom_scale));

        setContent(con, n);
    }

    public void setContent(IssueResultContainer con, Nation n) {
        content = new ArrayList<Object>();
        content.add(con.results);
        if (con.results.enactedPolicies != null && !con.results.enactedPolicies.isEmpty()) {
            content.addAll(con.results.enactedPolicies);
        }
        if (con.results.abolishedPolicies != null && !con.results.abolishedPolicies.isEmpty()) {
            content.addAll(con.results.abolishedPolicies);
        }
        if (con.results.nicePostcards != null && !con.results.nicePostcards.isEmpty()) {
            content.addAll(con.results.nicePostcards);
        }
        if (con.results.niceHeadlines != null && !con.results.niceHeadlines.headlines.isEmpty()) {
            content.add(con.results.niceHeadlines);
        }
        if (con.results.rankings != null && !con.results.rankings.isEmpty()) {
            content.addAll(con.results.rankings);
        }
        mNation = n;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ISSUE_RESULT_CARD:
                View issueResultCard = inflater.inflate(R.layout.card_issue_result, parent, false);
                viewHolder = new IssueResultCard(issueResultCard);
                break;
            case POSTCARD_CARD:
                View postcardCard = inflater.inflate(R.layout.card_postcard, parent, false);
                viewHolder = new PostcardCard(postcardCard);
                break;
            case CENSUSDELTA_CARD:
                View censusDeltaCard = inflater.inflate(R.layout.card_census_delta, parent, false);
                viewHolder = new CensusDeltaCard(censusDeltaCard);
                break;
            case POLICY_CARD:
                View policyCard = inflater.inflate(R.layout.card_policy, parent, false);
                viewHolder = new PolicyCard(context, policyCard);
                break;
            default:
                View headlineCard = inflater.inflate(R.layout.card_world_breaking_news, parent, false);
                viewHolder = new HeadlinesCard(headlineCard);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ISSUE_RESULT_CARD:
                IssueResultCard issueResultCard = (IssueResultCard) holder;
                issueResultCard.init((IssueResult) content.get(position));
                break;
            case POSTCARD_CARD:
                PostcardCard postcardCard = (PostcardCard) holder;
                postcardCard.init((IssuePostcard) content.get(position));
                break;
            case CENSUSDELTA_CARD:
                CensusDeltaCard censusDeltaCard = (CensusDeltaCard) holder;
                censusDeltaCard.init((CensusDelta) content.get(position));
                break;
            case POLICY_CARD:
                PolicyCard policyCard = (PolicyCard) holder;
                policyCard.init((Policy) content.get(position));
                break;
            default:
                HeadlinesCard headlinesCard = (HeadlinesCard) holder;
                headlinesCard.init((IssueResultHeadlinesContainer) content.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (content.get(position) instanceof IssueResult) {
            return ISSUE_RESULT_CARD;
        }
        else if (content.get(position) instanceof IssueResultHeadlinesContainer) {
            return HEADLINE_CARD;
        }
        else if (content.get(position) instanceof IssuePostcard) {
            return POSTCARD_CARD;
        }
        else if (content.get(position) instanceof CensusDelta) {
            return CENSUSDELTA_CARD;
        } else if (content.get(position) instanceof  Policy) {
            return POLICY_CARD;
        }
        return -1;
    }

    private static final String NS_DEFAULT_CAPITAL = "%s City";
    private static final String NS_DEFAULT_LEADER = "Leader";
    private static final String NS_DEFAULT_RELIGION = "a major religion";

    /**
     * Replaces NationStates template artifacts with their proper values.
     * @param t Target textview
     * @param nationData User nation's data
     * @param target String to replace data
     */
    private static void setIssueResultsFormatting(TextView t, Nation nationData, String target) {
        if (nationData != null && target != null) {
            target = target.replace("@@NAME@@", nationData.name);
            target = target.replace("@@$nation->query(\"name\")@@", nationData.name);
            target = target.replace("@@uc($nation->query(\"name\"))@@", nationData.name.toUpperCase(Locale.US));
            target = target.replace("@@REGION@@", nationData.region);
            target = target.replace("@@MAJORINDUSTRY@@", nationData.industry);
            target = target.replace("@@POPULATION@@", SparkleHelper.getPrettifiedNumber(nationData.popBase));
            target = target.replace("@@TYPE@@", nationData.prename);
            target = target.replace("@@ANIMAL@@", nationData.animal);
            target = target.replace("@@ucfirst(ANIMAL)@@", SparkleHelper.toNormalCase(nationData.animal));
            target = target.replace("@@PL(ANIMAL)@@", English.plural(nationData.animal));
            target = target.replace("@@ucfirst(PL(ANIMAL))@@", SparkleHelper.toNormalCase(English.plural(nationData.animal)));
            target = target.replace("@@CURRENCY@@", nationData.currency);
            target = target.replace("@@PL(CURRENCY)@@", SparkleHelper.getCurrencyPlural(nationData.currency));
            target = target.replace("@@ucfirst(PL(CURRENCY))@@", SparkleHelper.toNormalCase(SparkleHelper.getCurrencyPlural(nationData.currency)));
            target = target.replace("@@SLOGAN@@", nationData.motto);
            target = target.replace("@@DEMONYM@@", nationData.demAdjective);
            target = target.replace("@@DEMONYM2@@", nationData.demNoun);
            target = target.replace("@@PL(DEMONYM2)@@", nationData.demPlural);

            String valCapital = String.format(Locale.US, NS_DEFAULT_CAPITAL, nationData.name);
            if (nationData.capital != null) {
                valCapital = nationData.capital;
            }
            target = target.replace("@@CAPITAL@@", valCapital);
            target = target.replace("@@$nation->query_capital()@@", valCapital);

            String valLeader = NS_DEFAULT_LEADER;
            if (nationData.leader != null) {
                valLeader = nationData.leader;
            }
            target = target.replace("@@LEADER@@", valLeader);
            target = target.replace("@@$nation->query_leader()@@", valLeader);

            String valReligion = NS_DEFAULT_RELIGION;
            if (nationData.religion != null) {
                valReligion = nationData.religion;
            }
            target = target.replace("@@FAITH@@", valReligion);
            target = target.replace("@@$nation->query_faith()@@", valReligion);
        }

        t.setText(SparkleHelper.getHtmlFormatting(target, false));
    }

    public class IssueResultCard extends RecyclerView.ViewHolder {
        private ImageView image;
        private HtmlTextView mainResult;
        private TextView reclassResult;
        private HtmlTextView issueContent;
        private TextView expander;

        public IssueResultCard(View v) {
            super(v);
            image = v.findViewById(R.id.card_issue_result_image);
            mainResult = v.findViewById(R.id.card_issue_result_main_result);
            reclassResult = v.findViewById(R.id.card_issue_result_reclass_result);
            issueContent = v.findViewById(R.id.card_issue_result_issue_content);
            expander = v.findViewById(R.id.card_issue_result_expand);
        }

        public void init(IssueResult result) {
            if (result.image != null) {
                image.setVisibility(View.VISIBLE);
                DashHelper dashie = DashHelper.getInstance(context);
                dashie.loadImage(RaraHelper.getBannerURL(result.image), image, false);
            } else {
                image.setVisibility(View.GONE);
            }

            setIssueResultsFormatting(mainResult, mNation, result.mainResult);

            if (result.reclassifications != null && !result.reclassifications.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Reclassification rec : result.reclassifications) {
                    if (Reclassification.TYPE_GOVERNMENT.equals(rec.type)) {
                        sb.append(String.format(Locale.US, context.getString(R.string.issue_template_reclass_govt), mNation.name, rec.from, rec.to));
                    } else {
                        int templateId = R.string.issue_template_reclass_civil_rights;
                        switch (rec.type) {
                            case Reclassification.TYPE_CIVILRIGHTS:
                                templateId = R.string.issue_template_reclass_civil_rights;
                                break;
                            case Reclassification.TYPE_ECONOMY:
                                templateId = R.string.issue_template_reclass_economy;
                                break;
                            case Reclassification.TYPE_POLITICALFREEDOM:
                                templateId = R.string.issue_template_reclass_political_freedom;
                                break;
                        }

                        int deltaDesc = R.string.issue_reclass_unknown;
                        int indexFrom = unifiedFreedomScale.indexOf(rec.from);
                        int indexTo = unifiedFreedomScale.indexOf(rec.to);
                        if (indexFrom != -1 && indexTo != -1) {
                            if (indexFrom < indexTo) {
                                deltaDesc = R.string.issue_reclass_went_up;
                            } else {
                                deltaDesc = R.string.issue_reclass_went_down;
                            }
                            sb.append(String.format(Locale.US, context.getString(templateId), mNation.name, context.getString(deltaDesc), rec.from, rec.to));
                        }
                    }
                    sb.append("\n");
                }
                reclassResult.setVisibility(View.VISIBLE);
                reclassResult.setText(sb.toString().trim());
            } else {
                reclassResult.setVisibility(View.GONE);
            }

            StringBuilder sb = new StringBuilder(result.issueContent);
            sb.append("<br><br>");
            sb.append(result.issuePosition);
            issueContent.setText(SparkleHelper.getHtmlFormatting(sb.toString(), false));

            expander.setText(context.getString(R.string.issue_read_more));
            expander.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    issueContent.setVisibility(issueContent.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    expander.setText(context.getString(issueContent.getVisibility() == View.VISIBLE ? R.string.issue_show_less : R.string.issue_read_more));
                }
            });
        }
    }

    public class HeadlinesCard extends RecyclerView.ViewHolder {
        private TextView headlinesTitle;
        private LinearLayout headlinesHolder;

        public HeadlinesCard(View v) {
            super(v);
            headlinesTitle = v.findViewById(R.id.card_world_breaking_news_title);
            headlinesHolder = v.findViewById(R.id.card_world_breaking_news_holder);
        }

        public void init(IssueResultHeadlinesContainer headlines) {
            headlinesTitle.setText(context.getString(R.string.issue_trending));
            LayoutInflater inflater = LayoutInflater.from(context);
            headlinesHolder.removeAllViews();
            int index = 0;
            for (String h : headlines.headlines) {
                View headlineTextHolder = inflater.inflate(R.layout.view_world_breaking_news_entry, null);
                HtmlTextView newsContent = headlineTextHolder.findViewById(R.id.card_world_breaking_news_content);
                h = h.trim();
                setIssueResultsFormatting(newsContent, mNation, h);

                if (++index >= headlines.headlines.size()) {
                    headlineTextHolder.findViewById(R.id.view_divider).setVisibility(View.GONE);
                }

                headlinesHolder.addView(headlineTextHolder);
            }
        }
    }

    public class PostcardCard extends RecyclerView.ViewHolder {
        private TextView nationName;
        private ImageView img;
        private TextView description;

        public PostcardCard(View v) {
            super(v);
            nationName = v.findViewById(R.id.card_postcard_nation);
            img = v.findViewById(R.id.card_postcard_img);
            description = v.findViewById(R.id.card_postcard_title);
        }

        public void init(IssuePostcard card) {
            nationName.setText(mNation.name);
            DashHelper.getInstance(context).loadImage(card.imgUrl, img, false);
            if (postcardData.containsKey(card.rawId)) {
                description.setVisibility(View.VISIBLE);
                description.setText(postcardData.get(card.rawId));
            } else {
                description.setVisibility(View.GONE);
            }
        }
    }

    public class CensusDeltaCard extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CensusDelta delta;

        private CardView cardHolder;
        private TextView title;
        private TextView unit;
        private ImageView trend;
        private TextView value;

        public CensusDeltaCard(View v) {
            super(v);
            cardHolder = v.findViewById(R.id.card_census_delta_main);
            title = v.findViewById(R.id.card_delta_name);
            unit = v.findViewById(R.id.card_delta_unit);
            trend = v.findViewById(R.id.card_delta_trend);
            trend.setVisibility(View.VISIBLE);
            value = v.findViewById(R.id.card_delta_value);
            v.setOnClickListener(this);
        }

        public void init(CensusDelta d) {
            delta = d;
            cardHolder.setCardBackgroundColor(ContextCompat.getColor(context, delta.percentDelta >= 0 ? R.color.colorFreedom14 : R.color.colorFreedom0));

            CensusScale censusType = SparkleHelper.getCensusScale(censusScales, delta.censusId);
            title.setText(censusType.name);
            unit.setText(censusType.unit);
            trend.setImageResource(delta.percentDelta >= 0 ? R.drawable.ic_trend_up : R.drawable.ic_trend_down);
            int maxDecimals = 1;
            double absValue = Math.abs(delta.percentDelta);
            if (absValue > 1) {
                maxDecimals = 0;
            }
            else if (absValue < 0.001) {
                maxDecimals = 4;
            } else if (absValue < 0.01) {
                maxDecimals = 3;
            } else if (absValue < 0.1) {
                maxDecimals = 2;
            }
            String deltaText = SparkleHelper.getPrettifiedNumber(delta.percentDelta >= 0 ? delta.percentDelta : delta.percentDelta * -1, maxDecimals);
            value.setText(String.format(Locale.US, PERCENT_TEMPLATE, deltaText));
        }

        @Override
        public void onClick(View v) {
            String userId = PinkaHelper.getActiveUser(context).nationId;
            SparkleHelper.startTrends(context, userId, TrendsActivity.TREND_NATION, delta.censusId);
        }
    }
}
