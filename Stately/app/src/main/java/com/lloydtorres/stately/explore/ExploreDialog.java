package com.lloydtorres.stately.explore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.NullActionCallback;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-19.
 * A dialog that takes in a nation or region name, lets the user select the type, then launches
 * the appropriate explore activity.
 */
public class ExploreDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_explore_dialog";

    private EditText exploreSearch;
    private RadioGroup exploreToggleState;

    public ExploreDialog() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_explore_dialog, null);

        exploreSearch = (EditText) dialogView.findViewById(R.id.explore_searchbar);
        exploreSearch.setCustomSelectionActionModeCallback(new NullActionCallback());
        exploreToggleState = (RadioGroup) dialogView.findViewById(R.id.explore_radio_group);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startExploreActivity();
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.menu_explore)
                .setView(dialogView)
                .setPositiveButton(R.string.explore_positive, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        // Get focus on edit text and open keyboard
        exploreSearch.requestFocus();
        Dialog d = dialogBuilder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return d;
    }

    private void startExploreActivity()
    {
        int mode;

        switch (exploreToggleState.getCheckedRadioButtonId())
        {
            case R.id.explore_radio_nation:
                mode = SparkleHelper.CLICKY_NATION_MODE;
                break;
            default:
                mode = SparkleHelper.CLICKY_REGION_MODE;
                break;
        }

        String name = exploreSearch.getText().toString();
        SparkleHelper.startExploring(getContext(), name, mode);
    }
}
