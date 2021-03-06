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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.cloudbees.demo.gasp.R;
import com.cloudbees.demo.gasp.fragment.TwitterResponderFragment;

/**
 * Closely modeled on Neil Goodman's Android REST tutorials
 * https://github.com/posco2k8/rest_service_tutorial
 * https://github.com/posco2k8/rest_loader_tutorial.git
 *
 * @author Mark Prichard
 */
public class TwitterStreamActivity extends Activity {
    private final String TAG = TwitterStreamActivity.class.getName();

    // Twitter API v1.1 OAuth Token
    private static String twitterOAuthToken = "";

    public static String getTwitterOAuthToken() {
        return twitterOAuthToken;
    }

    public static void setTwitterOAuthToken(String twitterOAuthToken) {
        TwitterStreamActivity.twitterOAuthToken = twitterOAuthToken;
    }

    private ArrayAdapter<String> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use simple FrameLayout for ListFragment
        setContentView(R.layout.gasp_frame_layout);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ListFragment list = new ListFragment();
        ft.add(R.id.fragment_content, list);

        // Use a simple TextView layout for ArrayAdapter constructor
        mAdapter = new ArrayAdapter<String>(this, R.layout.gasp_generic_textview);

        // Map ArrayAdapter to ListFragment
        list.setListAdapter(mAdapter);

        // RESTResponderFragments call setRetainedInstance(true) in onCreate()
        TwitterResponderFragment responder =
                (TwitterResponderFragment) fm.findFragmentByTag(getString(R.string.twitter_responder));
        if (responder == null) {
            responder = new TwitterResponderFragment();

            ft.add(responder, getString(R.string.twitter_responder));
        }

        ft.commit();
    }

    public ArrayAdapter<String> getArrayAdapter() {
        return mAdapter;
    }
}
