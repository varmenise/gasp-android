/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudbees.gasp.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.cloudbees.gasp.activity.SetPreferencesActivity;
import com.cloudbees.gasp.model.Review;
import com.cloudbees.gasp.model.ReviewsDataSource;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ListIterator;

import static com.cloudbees.gasp.gcm.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.cloudbees.gasp.gcm.CommonUtilities.EXTRA_MESSAGE;
import static com.cloudbees.gasp.gcm.CommonUtilities.SENDER_ID;
import static com.cloudbees.gasp.gcm.CommonUtilities.SERVER_URL;

/**
 * Main UI for the demo app.
 */
public class ReviewSyncActivity extends Activity {
    private static String TAG = ReviewSyncActivity.class.getName();

    private TextView mDisplay;
    private Uri mGaspReviewsUri;
    private List<Review> mList;

    private AsyncTask<Void, Void, Void> mRegisterTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load shared preferences from res/xml/preferences.xml (first time only)
        // Subsequent activations will use the saved shared preferences from the device
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences gaspSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i(TAG, "Using Gasp Server URI: " + gaspSharedPreferences.getString("gasp_endpoint_uri", ""));
        mGaspReviewsUri = Uri.parse(gaspSharedPreferences.getString("gasp_endpoint_uri", ""));

        new ReviewsRESTQuery().execute();

        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        setContentView(R.layout.main);
        mDisplay = (TextView) findViewById(R.id.display);
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            // Automatically registers application on startup.
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            // Device is already registered on GCM, check server.
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                // Skips registration.
                mDisplay.append(getString(R.string.already_registered) + "\n");
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                ServerUtilities.register(context, regId);

                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*
             * Typically, an application registers automatically, so options
             * below are disabled. Uncomment them if you want to manually
             * register or unregister the device (you will also need to
             * uncomment the equivalent options on options_menu.xml).
             */
            /*
            case R.id.options_register:
                GCMRegistrar.register(this, SENDER_ID);
                return true;
            case R.id.options_unregister:
                GCMRegistrar.unregister(this);
                return true;
             */
            case R.id.options_clear:
                mDisplay.setText(null);
                return true;

            case R.id.options_exit:
                finish();
                return true;

            case R.id.gasp_settings:
                Intent intent = new Intent();
                intent.setClass(ReviewSyncActivity.this, SetPreferencesActivity.class);
                startActivityForResult(intent, 0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        unregisterReceiver(mHandleMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            mDisplay.append(newMessage + "\n");
        }
    };

    private class ReviewsRESTQuery extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            ResponseHandler<String> handler = new BasicResponseHandler();
            HttpGet httpGet = new HttpGet(mGaspReviewsUri.toString());
            String responseBody = null;

            try {
                HttpResponse response = httpClient.execute(httpGet, localContext);
                responseBody = handler.handleResponse(response);

                Log.d(TAG, responseBody);
            }
            catch (Exception e) {
                Log.e(TAG, e.getStackTrace().toString());
            }
            return responseBody;
        }

        @Override
        protected void onPostExecute(String results) {
            if (results!=null) {
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Review>>() {}.getType();
                    mList = gson.fromJson(results, type);

                    ReviewsDataSource reviewsDB = new ReviewsDataSource(getApplicationContext());
                    reviewsDB.open();
                    ListIterator<Review> iterator = mList.listIterator();
                    int reviews = 0;
                    while (iterator.hasNext()) {
                        Review review = iterator.next();
                        reviewsDB.insertReview(review);
                        reviews = review.getId();
                    }
                    reviewsDB.close();

                    mDisplay.append("Loaded " + reviews + " reviews from " + mGaspReviewsUri +'\n');

                } catch (Exception e) {
                    Log.e(TAG, e.getStackTrace().toString());
                }
            }
        }
    }
}