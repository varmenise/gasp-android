package com.cloudbees.demo.gasp.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cloudbees.demo.gasp.R;
import com.cloudbees.demo.gasp.location.GaspEvents;
import com.cloudbees.demo.gasp.model.EventRequest;
import com.cloudbees.demo.gasp.model.EventResponse;
import com.cloudbees.demo.gasp.model.Review;
import com.cloudbees.demo.gasp.server.GaspReviews;

import java.net.URL;

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

public class ReviewActivity extends Activity {
    private static final String TAG = ReviewActivity.class.getName();

    public static final String REVIEW_RESTAURANT_NAME = "review_restaurant_name";
    public static final String REVIEW_RESTAURANT_ID = "review_restaurant_id";
    public static final String REVIEW_REFERENCE = "review_reference";

    private String mRestaurantName;     // The restaurant name
    private int mGaspRestaurantId;      // The Gasp restaurant id
    private String mPlacesReference;    // The Google Places reference
    private URL mGaspUrl;               // The Gasp reviews URI

    // Gasp proxy objects
    private GaspReviews mGaspReviews = new GaspReviews() {
        @Override
        public void onSuccess(String location) {
            try {
                Log.d(TAG, "Gasp! review added: " + location);
                addGaspEvent(mPlacesReference, new URL(location));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure() {
            Log.e(TAG, "Error adding Gasp! review");
        }
    };
    private GaspEvents mGaspEvents = new GaspEvents() {
        @Override
        public void onEventAdded(EventResponse eventResponse) {
            Log.d(TAG, "Event added: " + eventResponse.getEvent_id());
        }

        @Override
        public void onErrorAddEvent(String status) {
            Log.d(TAG, "Error adding event via Google Places API");
        }

        @Override
        public void onEventDeleted(EventResponse eventResponse) {
            Log.d(TAG, "Event deleted: " + eventResponse.getEvent_id());
        }

        @Override
        public void onErrorDeleteEvent(String status) {
            Log.d(TAG, "Error deleting event via Google Places API");
        }
    };

    // Layout views
    private Spinner mStars;
    private EditText mComment;
    private Button mAddReviewButton;


    private void setViews() {
        mStars = (Spinner) findViewById(R.id.gasp_review_stars_spinner);
        TextView mTitle = (TextView) findViewById(R.id.gasp_review_name);
        mTitle.setText(mRestaurantName);
        mComment = (EditText) findViewById(R.id.gasp_review_comments);
        mAddReviewButton = (Button) findViewById(R.id.gasp_review_button);
    }

    private void addGaspReview(int stars, String comment, int restaurantId) {
        Review review = new Review();
        review.setStar(stars);
        review.setComment(comment);
        review.setRestaurant_id(restaurantId);
        review.setUser_id(1);

        mGaspReviews.addReview(review, mGaspUrl);
    }

    private void addGaspEvent(String reference, URL reviewId) {
        final int duration = 86400;
        final String language = "EN-US";
        final String summary = "New Gasp! Review";

        EventRequest eventRequest = new EventRequest();
        eventRequest.setReference(reference);
        eventRequest.setDuration(duration);
        eventRequest.setLanguage(language);
        eventRequest.setSummary(summary);
        eventRequest.setUrl(reviewId.toString());

        mGaspEvents.addEvent(eventRequest);
    }

    private void addButtonListener() {
        mAddReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addGaspReview(getStars(), getComment(), mGaspRestaurantId);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int getStars() {
        return mStars.getSelectedItemPosition() + 1;
    }

    private String getComment() {
        return mComment.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            SharedPreferences gaspSharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mGaspUrl = new URL(gaspSharedPreferences.getString(getString(R.string.gasp_server_uri_preferences), "")
                    + getString(R.string.gasp_reviews_location));

            Intent intent = getIntent();
            mRestaurantName = intent.getStringExtra(REVIEW_RESTAURANT_NAME);
            mGaspRestaurantId = intent.getIntExtra(REVIEW_RESTAURANT_ID, 0);
            mPlacesReference = intent.getStringExtra(REVIEW_REFERENCE);

            setContentView(R.layout.gasp_add_review_layout);
            setViews();
            addButtonListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
