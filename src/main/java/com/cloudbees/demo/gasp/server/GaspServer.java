package com.cloudbees.demo.gasp.server;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

public class GaspServer {
    private static final String TAG = GaspServer.class.getName();

    /**
     * Adds a new Gasp entity via HTTP POST
     *
     * @param input JSON-formatted request body
     * @param url Gasp server URL for HTTP POST
     * @return URL of the newly-created resource
     */
    public static String newGaspEntity (String input, URL url) {
        HttpURLConnection conn = null;
        String location = "";

        try {
            Log.d(TAG, "Request URL: " + url.toString());
            Log.d(TAG, "Request Body: " + input);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Length", "" +
                    Integer.toString(input.getBytes().length));

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream ());
            wr.writeBytes (input);
            wr.flush ();
            wr.close ();
            if (conn.getHeaderField("Location") != null) {
                location = conn.getHeaderField("Location");
                Log.d(TAG, "Location: " + conn.getHeaderField("Location"));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Malformed Gasp Server URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Gasp Server API", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return location;
    }

    /**
     * Removes a Gasp entity via HTTP DELETE
     *
     * @param url Gasp server URL for HTTP DELETE
     */
    public static void deleteGaspEntity (URL url) {
        HttpURLConnection conn = null;
        try {
            Log.d(TAG, "Request URL: " + url.toString());

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Response code: " + responseCode);
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Malformed Gasp Server URL", e);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Gasp Server API", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
