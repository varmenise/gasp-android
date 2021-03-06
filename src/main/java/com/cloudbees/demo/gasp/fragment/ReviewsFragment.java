package com.cloudbees.demo.gasp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cloudbees.demo.gasp.adapter.RestaurantDataAdapter;
import com.cloudbees.demo.gasp.adapter.ReviewDataAdapter;
import com.cloudbees.demo.gasp.adapter.UserDataAdapter;
import com.cloudbees.demo.gasp.model.Review;
import com.cloudbees.demo.gasp.utils.Preferences;

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

public class ReviewsFragment extends ListFragment {
    private static final String TAG = ReviewsFragment.class.getName();

    private ReviewDataAdapter mReviewAdapter;
    private RestaurantDataAdapter mRestaurantDataAdapter;
    private UserDataAdapter mUserDataAdapter;

    private List<Review> mReviews;
    private Context mContext;
    private String mBaseUrl;

    public ReviewsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = inflater.getContext();
        mBaseUrl = Preferences.getGaspServerUrl().replaceAll("/$", "");

        mReviewAdapter = new ReviewDataAdapter(inflater.getContext());
        mReviewAdapter.open();

        // Get all reviews in descending order
        mReviews = mReviewAdapter.getAllDesc();
        ArrayAdapter<Review> adapter =
                new ArrayAdapter<Review>(inflater.getContext(),
                                         android.R.layout.simple_list_item_1,
                                         mReviews);
        mReviewAdapter.close();

        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(TAG, "Review Id: " + mReviews.get(position).getId()
                + " " + mReviews.get(position).getComment());

        FragmentManager fm = getFragmentManager();
        ReviewDialogFragment frag = ReviewDialogFragment.newInstance("Gasp! Reviews", mReviews.get(position));
        frag.show(fm, "Review Dialog Fragment");
    }
}
