package com.lloydtorres.stately.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-02-03.
 * This dialog is shown for switching active nations.
 */
public class SwitchNationDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_switch_dialog";
    public static final String LOGINS_KEY = "logins";

    // RecyclerView variables
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<UserLogin> logins;

    public void setLogins(ArrayList<UserLogin> l)
    {
        logins = l;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogCustom);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_paddedrecycler, null);

        // Restore saved state
        if (savedInstanceState != null)
        {
            logins = savedInstanceState.getParcelableArrayList(LOGINS_KEY);
        }

        initRecycler(view);

        // Build actual dialog
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SparkleHelper.startAddNation(getContext());
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.menu_switch)
                .setView(view)
                .setPositiveButton(R.string.add_nation, dialogListener);

        return dialogBuilder.create();
    }

    private void initRecycler(View view)
    {
        // Base recycler stuff
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(logins);
        mRecyclerAdapter = new SwitchNationRecyclerAdapter(getContext(), this, logins);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        // Add swipe to delete
        ItemTouchHelper.SimpleCallback deleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // not needed
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // remove on swipe
                int pos = viewHolder.getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION)
                {
                    logins.get(pos).delete();
                    logins.remove(pos);
                    mRecyclerAdapter.notifyItemRemoved(pos);
                    mRecyclerAdapter.notifyItemRangeChanged(pos, logins.size());
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deleteCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LOGINS_KEY, logins);
    }
}
