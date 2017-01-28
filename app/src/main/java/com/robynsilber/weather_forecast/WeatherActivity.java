package com.robynsilber.weather_forecast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

/*
*   User clicks on launcher icon to run this app on her device.
*   AndroidManifest.xml specifies which Activity in the app to launch; an intent is constructed to start thr activity with startActivity(intent)
*   Android checks if there's already a process in which to run the app in, and if not, it creates a new process.
*   Android creates a new activity object for the activity specified as the launcher activity in the manifest.
*   Activity is in the LAUNCHED state.
*   The activity's onCreate() method gets called; includes a call to setContentView() to specify layout.
*   Initialize all the necessary state variables of the activity within the onCreate() method.
*   After finishing call to onCreate(), activity is in the RUNNING (or ACTIVE) state (its in the foreground of the screen).
*
*   PROBLEM: USER ROTATES DEVICE! ==> Device Configuration's screen orientation and screen size have changed!
*
*   Rotation of device is sensed by Android, and so activity's onDestroy() method is called.
*   Android destroys the activity (including its state variables) and enters the DESTROYED state;
*   then onCreate() is called again and it becomes RUNNING again.
*
*   SOLUTION: Prior to the call to onDestroy(), save the current state of the activity and retrieve the state within the onCreate() method.
*   SAVE the STATE: implement the onSaveInstanceState() callback, which will get called before the call to onDestroy().
*   Within onSaveInstanceState(), call savedInstanceState.put*("name", value); replace * with the data type of the value
*   Within onCreate(), retrieve the saved state values using savedInstanceState.put*("name", value);
*   Note: onSaveInstanceState() will be called prior to onStop() - if onStop() is not bypassed; otherwise, before onDestroy()
*
* */

public class WeatherActivity extends AppCompatActivity {

    private LocationDetector.LocationBinder mLocationBinder;
    private LocationDetector mLocationDetector;
    private boolean isBoundToService = false;

    private static long TIMEOUT_IN_MILLI = 10000;


    private class LocationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){

            double[] coords = intent.getDoubleArrayExtra("locationCoords");
            if(coords[0] == 0.0 && coords[1] == 0.0){ // failed to get lat and long

            }else{ // success
                // TODO: pass Location data to WeatherData
            }
        }
    }




    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mLocationBinder = (LocationDetector.LocationBinder) iBinder; // Service binder
            mLocationDetector = mLocationBinder.getLocationDetector(); // cast binder to get ref to LocationDetector
            isBoundToService = true; // service is connected; set to true
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBoundToService = false; // service is disconnected; set to false
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        LocationReceiver receiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter("some_filter");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
//        registerReceiver(receiver, filter); // register the receiver

    }

    @Override
    protected void onStart(){
        super.onStart();
        // Bind the service at onStart()
        Intent intent = new Intent(this, LocationDetector.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE); // uses the intent and service connection to bind activity to the service
//        mLocationDetector.getLocationFromDetector();
    }

    @Override
    protected void onResume(){
        mLocationDetector.getLocationFromDetector();
    }





    // saves the state of WeatherActivity prior to being destroyed
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("isBoundToService", isBoundToService);

    }

    @Override
    protected void onStop(){
        super.onStop();
        // Unbind service at onStop()
        if(isBoundToService){
            unbindService(mServiceConnection);
            isBoundToService = false;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }



    private void getTheLocation(){


        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                double latitude = 0.0;
                double longitude = 0.0;
                if(mLocationDetector != null){
//                    Location location =
                    mLocationDetector.getLocationFromDetector();
//                    if(location != null){
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                    }
                }
                handler.postDelayed(this, TIMEOUT_IN_MILLI); // update the location every 10 seconds
            }
        });

    }


}
