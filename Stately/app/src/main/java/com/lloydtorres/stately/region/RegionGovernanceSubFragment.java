package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Embassy;
import com.lloydtorres.stately.dto.Officer;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.NameListDialog;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 */
public class RegionGovernanceSubFragment extends Fragment {
    private Region mRegion;

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
        View view = inflater.inflate(R.layout.fragment_region_sub_governance, container, false);

        // Restore save state
        if (savedInstanceState != null && mRegion == null)
        {
            mRegion = savedInstanceState.getParcelable("mRegion");
        }

        if (mRegion != null)
        {
            initOfficersCard(inflater, view);
            initEmbassyCard(view);
        }

        return view;
    }

    private void initOfficersCard(LayoutInflater inflater, View v)
    {
        LinearLayout officersLayout = (LinearLayout) v.findViewById(R.id.card_region_officers_layout);
        int tracker = 0;

        if (!"0".equals(mRegion.delegate))
        {
            inflateOfficerEntry(inflater, officersLayout, getString(R.string.card_region_wa_delegate), mRegion.delegate);
            tracker++;
        }

        if (!"0".equals(mRegion.founder))
        {
            inflateOfficerEntry(inflater, officersLayout, getString(R.string.card_region_founder), mRegion.founder);
            tracker++;
        }

        List<Officer> officers = mRegion.officers;
        Collections.sort(officers);

        for (int i=0; i<officers.size(); i++)
        {
            inflateOfficerEntry(inflater, officersLayout, officers.get(i).office, officers.get(i).name);
            tracker++;
        }

        if (tracker <= 0)
        {
            TextView noOfficers = (TextView) v.findViewById(R.id.governance_none);
            noOfficers.setText(String.format(getString(R.string.region_filler_no_officers), mRegion.name));
            noOfficers.setVisibility(View.VISIBLE);
        }
    }

    private void inflateOfficerEntry(LayoutInflater inflater, LinearLayout officersLayout, String position, String nation)
    {
        View delegateView = inflater.inflate(R.layout.view_cardentry, null);
        TextView label = (TextView) delegateView.findViewById(R.id.cardentry_label);
        TextView content = (TextView) delegateView.findViewById(R.id.cardentry_content);
        label.setText(position);
        SparkleHelper.activityLinkBuilder(getContext(), content, nation, nation, SparkleHelper.getNameFromId(nation), SparkleHelper.CLICKY_NATION_MODE);
        officersLayout.addView(delegateView);
    }

    private void initEmbassyCard(View v)
    {
        ArrayList<String> embassyList = new ArrayList<String>();
        if (mRegion.embassies != null && mRegion.embassies.size() > 0)
        {
            for (Embassy e : mRegion.embassies)
            {
                if (e.type == null)
                {
                    embassyList.add(e.name);
                }
            }
        }

        Collections.sort(embassyList);
        CardView embassyCard = (CardView) v.findViewById(R.id.card_region_embassies);

        if (embassyList.size() > 0)
        {
            TextView embassyNum = (TextView) v.findViewById(R.id.card_region_embassies_num);
            embassyNum.setText(String.valueOf(embassyList.size()));
            final ArrayList<String> embassies = embassyList;
            embassyCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    NameListDialog nameListDialog = new NameListDialog();
                    nameListDialog.setTitle(getString(R.string.card_region_embassies));
                    nameListDialog.setNames(embassies);
                    nameListDialog.setTarget(SparkleHelper.CLICKY_REGION_MODE);
                    nameListDialog.show(fm, NameListDialog.DIALOG_TAG);
                }
            });
        }
        else
        {
            embassyCard.setVisibility(View.GONE);
        }
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
