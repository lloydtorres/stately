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

package com.lloydtorres.stately.login;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;

import java.util.List;

/**
 * Created by Lloyd on 2016-02-03.
 * This is the recycler adapter used for SwitchNationDialog.
 */
public class SwitchNationRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private SwitchNationDialog selfDialog;
    private List<UserLogin> logins;

    public SwitchNationRecyclerAdapter(Context c, SwitchNationDialog d, List<UserLogin> u) {
        context = c;
        selfDialog = d;
        logins = u;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new SwitchNationEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SwitchNationEntry switchNationEntry = (SwitchNationEntry) holder;
        switchNationEntry.init(logins.get(position));
    }

    @Override
    public int getItemCount() {
        return logins.size();
    }

    public class SwitchNationEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private UserLogin login;
        private TextView nationName;

        public SwitchNationEntry(View v) {
            super(v);
            nationName = v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(UserLogin u) {
            login = u;
            nationName.setText(login.name);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                Intent loginActivityLaunch = new Intent(context, LoginActivity.class);
                loginActivityLaunch.putExtra(LoginActivity.USERDATA_KEY, login);
                loginActivityLaunch.putExtra(LoginActivity.NOAUTOLOGIN_KEY, true);
                selfDialog.dismiss();
                context.startActivity(loginActivityLaunch);
            }
        }
    }
}
