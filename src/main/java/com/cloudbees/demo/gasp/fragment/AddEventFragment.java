package com.cloudbees.demo.gasp.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.cloudbees.demo.gasp.location.GooglePlacesClient;
import com.cloudbees.demo.gasp.model.EventRequest;
import com.cloudbees.demo.gasp.model.EventResponse;
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

public abstract class AddEventFragment extends Fragment {
    private static final String TAG = AddEventFragment.class.getName();

    private EventRequest mEventRequest;
    private String mJsonOutput;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void addEvent(EventRequest eventRequest) {
        mEventRequest = eventRequest;

        Log.d(TAG, "Event reference: " + eventRequest.getReference());
        Log.d(TAG, "Event summary: " + eventRequest.getSummary());
        Log.d(TAG, "Event url: "+ eventRequest.getUrl());
        Log.d(TAG, "Event duration: " + eventRequest.getDuration());
        Log.d(TAG, "Event language: " + eventRequest.getLanguage());

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String search = GooglePlacesClient.getQueryStringAddEvent();
                    mJsonOutput = GooglePlacesClient.doPost(
                            new Gson().toJson(mEventRequest, EventRequest.class), new URL(search));

                } catch (Exception e) {
                    Log.e(TAG, "Exception: ", e);
                }
                return mJsonOutput;
            }

            @Override
            protected void onPostExecute(String jsonOutput) {
                super.onPostExecute(jsonOutput);
                try {
                    EventResponse eventResponse = new Gson().fromJson(jsonOutput, EventResponse.class);

                    if (eventResponse.getStatus().equalsIgnoreCase("OK"))
                        onSuccess(eventResponse);
                    else
                        onFailure(eventResponse.getStatus());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // Callbacks to calling Activity
    abstract public void onSuccess(EventResponse eventResponse);
    abstract public void onFailure(String status);
}