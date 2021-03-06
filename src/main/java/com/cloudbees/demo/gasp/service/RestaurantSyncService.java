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

package com.cloudbees.demo.gasp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cloudbees.demo.gasp.R;
import com.cloudbees.demo.gasp.activity.LocationsActivity;
import com.cloudbees.demo.gasp.adapter.RestaurantDataAdapter;
import com.cloudbees.demo.gasp.model.Restaurant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ListIterator;

public class RestaurantSyncService extends IntentService implements IRESTListener {
    private static final String TAG = RestaurantSyncService.class.getName();

    private Uri mGaspRestaurantsUri;

    private void getGaspRestaurantsUriSharedPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences gaspSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String gaspRestaurantsUri = gaspSharedPreferences.getString(getString(R.string.gasp_server_uri_preferences), "")
                + getString(R.string.gasp_restaurants_location);

        this.mGaspRestaurantsUri = Uri.parse(gaspRestaurantsUri);
    }

    private Uri getGaspRestaurantsUri() {
        return mGaspRestaurantsUri;
    }

    public RestaurantSyncService() {
        super(RestaurantSyncService.class.getName());
    }

    private long checkLastId() {
        long lastId = 0;

        RestaurantDataAdapter restaurantData = new RestaurantDataAdapter(this);
        restaurantData.open();
        try {
            lastId = restaurantData.getLastId();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            restaurantData.close();
        }

        return lastId;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getGaspRestaurantsUriSharedPreferences();
        Log.i(TAG, "Using Gasp Server Restaurants URI: " + getGaspRestaurantsUri());

        AsyncRESTClient asyncRestCall = new AsyncRESTClient(getGaspRestaurantsUri(), this);
        asyncRestCall.getAll();
    }

    @Override
    public void onCompleted(String results) {
        Log.i(TAG, "Response from " + mGaspRestaurantsUri.toString() + " :" + results + '\n');

        if (results != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Restaurant>>() {
                }.getType();
                List<Restaurant> restaurants = gson.fromJson(results, type);

                // Check how many records already in local SQLite database
                long localRecords = checkLastId();

                RestaurantDataAdapter restaurantsDB = new RestaurantDataAdapter(getApplicationContext());
                restaurantsDB.open();
                ListIterator<Restaurant> iterator = restaurants.listIterator();
                int index = 0;

                while (iterator.hasNext()) {
                    try {
                        Restaurant restaurant = iterator.next();
                        if (restaurant.getId() > localRecords) {
                            restaurantsDB.insert(restaurant);
                            index = restaurant.getId();
                        }
                    } catch (SQLiteConstraintException e) {
                        e.printStackTrace();
                    }
                }
                restaurantsDB.close();

                String resultTxt = "Sync: Found " + localRecords + ", Loaded " + index
                        + " restaurants from " + mGaspRestaurantsUri;
                Log.i(TAG, resultTxt + '\n');

                // Notify LocationsActivity that Gasp restaurant data has been synced
                LocalBroadcastManager.getInstance(this)
                                     .sendBroadcast(new Intent(LocationsActivity.SYNC_COMPLETED));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
