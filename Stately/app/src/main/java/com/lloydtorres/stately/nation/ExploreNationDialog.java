package com.lloydtorres.stately.nation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-01-19.
 */
public class ExploreNationDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_explore_dialog";

    private EditText exploreSearch;
    private RadioGroup exploreToggleState;

    public ExploreNationDialog() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_explore_dialog, null);

        exploreSearch = (EditText) dialogView.findViewById(R.id.explore_searchbar);
        exploreToggleState = (RadioGroup) dialogView.findViewById(R.id.explore_radio_group);

        exploreSearch.requestFocus();

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (exploreToggleState.getCheckedRadioButtonId())
                {
                    case R.id.explore_radio_nation:
                        startExploreActivity();
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.menu_explore)
                .setView(dialogView)
                .setPositiveButton(R.string.explore_positive, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        Dialog d = dialogBuilder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;
    }

    private void startExploreActivity()
    {
        String name = exploreSearch.getText().toString();
        Intent nationActivityLaunch = new Intent(getContext(), ExploreNationActivity.class);
        nationActivityLaunch.putExtra("nationId", name);
        startActivity(nationActivityLaunch);
    }
}
