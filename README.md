Gasp! GCM for Android Demo Client
=================================

Android demo client for the [gasp-gcm-server](https://github.com/mqprichard/gasp-gcm-server) application, which uses CloudBees PaaS and Foxweave to provide automatic data sync between the Gasp! server database and Android SQLite on-device data stores.  This demo application uses [Google Cloud Messaging for Android](http://developer.android.com/google/gcm/index.html) and was built with the latest preview of Android Studio with the Android gradle build system.

> <img src="http://www.cloudbees.com/sites/all/themes/custom/cloudbees_zen/css/bidesign/_ui/images/logo.png"/>
>
> <b>Note</b>: <i>This repo is part of the Gasp demo project - a showcase of <a href="https://developer.cloudbees.com/bin/view/Mobile">cloudbees mobile services</a>.
> You can see the big picture of the <a href="http://mobilepaas.cloudbees.com">showcase here</a>.
> Feel free to fork and use this repo as a template.</i>

Quick Overview
--------------

1. On startup, the main Activity (ReviewSyncActivity) will connect to the Gasp! server (using the endpoint configured via Shared Preferences - see res/xml/preferences.xml) to retrieve all reviews aurrently in the database via the REST service interface; the data is loaded into the on-device SQLite database using the ReviewsDataSource wrapper class (via OpenSQLiteHelper).
2. The application will register with both
   - The Google Cloud Messaging for Android Service;
   - The gasp-gcm-server, which will send asynchronous notifications whenever there is an update to the Gasp! database.
3. GCM Notifications are handled by the GCMIntentService, which will do the following:
   - Get the review Id from the notification Intent and fetch the review data via an async REST call to the Gasp! server;
   - Insert the new record into the on-device SQLite database to sync with the Gasp! server;
   - Generate a notification ("New Gasp! Review"), which will appear in the Android pull-down notifications panel to alert the user that new review data has been received.

ReviewSyncActivity uses a simple TextView display to show messages for registration and notification events, but the GCMIntentService will be triggered and run in the background regardless of the current activity - notifications will be visible in the pull-down panel.  

The Options Menu allows you to:
   - Clear all messages
   - Exit the application
   - Edit the Gasp! server endpoint URL
   - View the SQLite database (reviews shown in reverse order)

Pre-reqs
--------
1. Run the [Gasp! Server](https://github.com/cloudbees/gasp-server) application on CloudBees
2. Configure Google APIs for Google Cloud Messaging - see [instructions here](https://github.com/mqprichard/gasp-gcm-server/blob/master/README.md).  You will need to edit CommonUtilities to set SENDER_ID equal to your 12-digit Google API Project Number, to match the API Key configured for the gasp-gcm-server.
3. Configure and run the FoxWeave Integration Service and the gasp-gcm server application - see [instructions here](https://github.com/mqprichard/gasp-gcm-server/blob/master/README.md).

Building the Demo Client
------------------------
You can build the application directly from Android Studio or via gradle. The application needs both the Android support and GCM client libraries from the Android SDK, so build.gradle uses the [CloudBees maven-android-sdk repository](https://repository-maven-android-sdk.forge.cloudbees.com/release/) for these dependencies.  Thanks to [these folk](https://github.com/mosabua/maven-android-sdk-deployer) for enabling this!

Build using `gradle clean build` or use the gradle wrapper.

Running the Demo Client
-----------------------
The easiest way to is run the app directly from Android Studio; alternatively deploy using adb and use `am start -n "com.cloudbees.gasp.gcm/com.cloudbees.gasp.activity.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER`.  Note that if you are running on an AVD emulator, you must configure it with a Google APIs target to use the GCM service.

Resetting the Demo Client
-------------------------
To re-run the demo from a clean state, you should uninstall the app to remove the GCM registration and delete the SQLite database.  

Setting the location for the Android Emulator
---------------------------------------------
This example uses the default (localhost:5554) emulator address and the lat/lng co-ordinates for the CloudBees Los Altos office

    telnet localhost 5554
    Trying ::1...
    telnet: connect to address ::1: Connection refused
    Trying 127.0.0.1...
    Connected to localhost.
    Escape character is '^]'.
    Android Console: type 'help' for a list of commands
    OK
    geo fix -122.113847 37.377527
    OK

Google API Keys
---------------
This Android client makes calls to the Google Maps, Google Places and Google Cloud Messaging APIs.
For details of how to configure Google API keys, see the [Google APIs Console help](https://developers.google.com/console/help/) pages.

Please remember that API keys that are compiled into an Android application can always be de-dexed and read.  Keys used for production applications should always be secured by the Android package name and APK signing key.  Where this is not possible (such as the Google Places API key, below), it is recommended that the keys should be retrieved from the server via authenticated https.

1. The Google Maps for Android v2 API key is included as meta-data for LocationsActivity in AndroidManifest.xml.  The API key is tied to the Android package name and the signing key for the APK.
2. The Google Places API key is stored in `com.cloudbees.demo.gasp.location.GooglePlacesKey`: ideally, this would be Android package-specific but Google currently only support generic server keys for the Places API.  For production use, this key should be retrieved from the Gasp server via authenticated https, or the Places API calls proxied via the server.
3. Google Cloud Messaging (GCM) uses the Google API Project key.  The key is stored in `com.cloudbees.demo.gasp.gcm.GCMProjectKey` since the Android client uses it to register with the GCM services.  For production use, this key should also be retrieved from the Gasp server via authenticated https, although the Gasp GCM server controls message sending for the application, so the key can be tied to the specific IP address for the server.

The online example [Jenkins build](https://mobile-examples.ci.cloudbees.com/job/Android/job/android-gradle/) uses the [Build Secret plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Secret+Plugin) to inject these keys into the build without having them stored in SCM.