package com.cloudbees.demo.gasp.location;

import android.os.AsyncTask;
import android.util.Log;

import com.cloudbees.demo.gasp.model.Places;
import com.cloudbees.demo.gasp.model.Query;
import com.google.gson.Gson;

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

public abstract class GaspSearch {
    private static String TAG = GaspSearch.class.getName();

    private Query mQuery;
    private String mJsonOutput;

    public void nearbySearch(Query query) {
        mQuery = query;
        Log.d(TAG, "Lat: " + String.valueOf(query.getLat()));
        Log.d(TAG, "Lng: " + String.valueOf(query.getLng()));
        Log.d(TAG, "Radius: " + query.getRadius());
        Log.d(TAG, "Token: " + query.getNext_page_token());

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String search = GooglePlacesClient.getQueryStringNearbySearch(mQuery);
                    mJsonOutput = GooglePlacesClient.doGet(new URL(search));

                } catch (Exception e) {
                    Log.e(TAG, "Exception: ", e);
                }
                return mJsonOutput;
            }

            @Override
            protected void onPostExecute(String jsonOutput) {
                super.onPostExecute(jsonOutput);
                try {
                    Places places = new Gson().fromJson(jsonOutput, Places.class);

                    if (places.getStatus().equalsIgnoreCase("OK"))
                        onSuccess(places);
                    else
                        onFailure(places.getStatus());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // Callbacks to calling Activity
    abstract public void onSuccess(Places places);
    abstract public void onFailure(String status);
}
