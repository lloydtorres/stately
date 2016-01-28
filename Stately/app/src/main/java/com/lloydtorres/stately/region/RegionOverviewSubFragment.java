package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.atteo.evo.inflector.English;
import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * Created by Lloyd on 2016-01-22.
 * A sub-fragment of the Region fragment displaying an overview about a region.
 * Takes in a Region object.
 */
public class RegionOverviewSubFragment extends Fragment {
    private Region mRegion;

    private TextView delegate;
    private TextView founder;
    private TextView power;
    private HtmlTextView factbook;
    private TextView tags;

    public void setRegion(Region r)
    {
        mRegion = r;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_region_sub_overview, container, false);

        // Restore state
        if (savedInstanceState != null && mRegion == null)
        {
            mRegion = savedInstanceState.getParcelable("mRegion");
        }

        if (mRegion != null)
        {
            delegate = (TextView) view.findViewById(R.id.region_delegate);
            if (!"0".equals(mRegion.delegate))
            {
                String delegateProper = SparkleHelper.getNameFromId(mRegion.delegate);
                String delegateTemplate = String.format(getString(R.string.region_delegate_votes), mRegion.delegate, mRegion.delegateVotes, English.plural(getString(R.string.region_filler_vote), mRegion.delegateVotes));
                SparkleHelper.activityLinkBuilder(getContext(), delegate, delegateTemplate, mRegion.delegate, delegateProper, SparkleHelper.CLICKY_NATION_MODE);
            }
            else
            {
                delegate.setText(getString(R.string.region_filler_none));
            }

            founder = (TextView) view.findViewById(R.id.region_founder);
            if (!"0".equals(mRegion.founder))
            {
                String founderProper = SparkleHelper.getNameFromId(mRegion.founder);
                SparkleHelper.activityLinkBuilder(getContext(), founder, mRegion.founder, mRegion.founder, founderProper, SparkleHelper.CLICKY_NATION_MODE);
            }
            else
            {
                founder.setText(getString(R.string.region_filler_none));
            }

            power = (TextView) view.findViewById(R.id.region_power);
            power.setText(mRegion.power);

            factbook = (HtmlTextView) view.findViewById(R.id.region_factbook);
            SparkleHelper.setBbCodeFormatting(getContext(), factbook, mRegion.factbook);

            tags = (TextView) view.findViewById(R.id.region_tags);
            String tagCombine = Joiner.on(", ").skipNulls().join(mRegion.tags);
            tags.setText(tagCombine);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (mRegion != null)
        {
            outState.putParcelable("mRegion", mRegion);
        }
    }
}
