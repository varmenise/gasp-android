package com.cloudbees.demo.gasp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudbees.demo.gasp.R;
import com.cloudbees.demo.gasp.model.User;

import java.util.List;

/**
 * Copyright (c) 2013 Mark Prichard, CloudBees
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * ArrayAdapter subclass for use with ListActivity (UserListActivity)
 * See gasp_user_list.xml for layout views
 */
public class UserArrayAdapter extends ArrayAdapter<User> {
    private final static String TAG = UserArrayAdapter.class.getName();

    private final List<User> mUsers;
    private final int mResource;

    /**
     * Default constructor
     *
     * @param context  The Activity context
     * @param resource The layout resource
     * @param users    The List collection
     */
    public UserArrayAdapter(Context context, int resource, List<User> users) {
        super(context, resource, users);
        this.mUsers = users;
        this.mResource = resource;
    }

    /**
     * Called by ListActivity
     * @param position      Position of this entry in the array
     * @param convertView   Layout view
     * @param parent        Not used
     * @return View object for this entry
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        try {
            if (view == null) {
                LayoutInflater inflater
                        = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(mResource, null);
            }
            TextView viewUrl = (TextView) view.findViewById(R.id.user_url);
            TextView viewName = (TextView) view.findViewById(R.id.user_name);

            User user = mUsers.get(position);

            if (user != null) {
                viewUrl.setText("Url: " + user.getUrl());
                viewName.setText("Name: " + user.getName());
            } else {
                Log.e(TAG, "Error: view is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
