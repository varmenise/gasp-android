/*
 * Copyright (c) 2013 Mark Prichard, CloudBees
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.demo.gasp.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cloudbees.demo.gasp.R;
import com.cloudbees.demo.gasp.adapter.UserArrayAdapter;
import com.cloudbees.demo.gasp.adapter.UserDataAdapter;
import com.cloudbees.demo.gasp.model.User;

import java.util.List;

public class UserListActivity extends ListActivity {
    private UserDataAdapter userAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userAdapter = new UserDataAdapter(this);
        userAdapter.open();

        // Get all users in descending order
        List<User> users = userAdapter.getAllDesc();

        UserArrayAdapter userArrayAdapter =
                new UserArrayAdapter(this, R.layout.gasp_user_list, users);
        setListAdapter(userArrayAdapter);
    }

    @Override
    protected void onResume() {
        userAdapter.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        userAdapter.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_short, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.gasp_settings:
                Intent intent = new Intent();
                intent.setClass(this, SetPreferencesActivity.class);
                startActivityForResult(intent, 0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}