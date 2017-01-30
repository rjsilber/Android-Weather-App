package com.robynsilber.weather_forecast;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import static com.robynsilber.weather_forecast.R.id.progressBar;

/*  Command line recipe for Git:
*
*          git status
*          git add .
*          git commit -a -m 'message'
*          git push
*
* */



/*        Activity Lifecycle: DEVICE CONFIGURATION CHANGES (USER ROTATES DEVICE):
*
*   User clicks on launcher icon to run this app on her device.
*   AndroidManifest.xml specifies which Activity in the app to launch; an intent is constructed to start the activity with startActivity(intent)
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

public class WeatherActivity extends AppCompatActivity implements WeatherDataAsyncTask.IAsyncTaskResponder {

    private LocationDetector.LocationBinder mLocationBinder;
    private LocationDetector mLocationDetector;
    private boolean isBoundToService = false;
    private static Location mLocation;
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private Weather[] mWeatherModel;
    private WeatherDataAsyncTask mWeatherDataAsyncTask;

    private static long TIMEOUT_IN_MILLI = 10000; // 10 seconds


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

        getTheLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_settings:  // when Settings option selected from menu
                // code to start Settings Activity
                Intent intent = new Intent(this, SettingsActivity.class); // intent to start activity
                startActivity(intent); // start the SettingsActivity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        // Bind the service at onStart()
        Intent intent = new Intent(this, LocationDetector.class); // creates the intent
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE); // uses the intent and service connection to bind activity to the service
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
        if(mLatitude == 0.0 && mLongitude == 0.0){ // gets location from LocationDetector
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Log.d("getTheLocation()", "beginning of run()");
                    if(mLocationDetector != null){
                        mLocation = mLocationDetector.getLocationFromDetector();
                        Log.d("getTheLocation()", "mLocationDetector != null");
                        if(mLocation != null){
                            mLatitude = mLocation.getLatitude();
                            mLongitude = mLocation.getLongitude();

                            Log.d("getTheLocation()", "Runnable complete");
                            handler.removeCallbacks(this);
                            Log.d("getTheLocation()", "Testing if line of code accessible");

                            retrieveWeatherData();
                        }
                    }else{
                        Log.d("getTheLocation()", "else()");
                        handler.postDelayed(this, TIMEOUT_IN_MILLI); // update the location every 10 seconds
                    }
                }
            });
        }

        Log.d("getTheLocation()", "outside Runnable");

    }


    public void retrieveWeatherData(){

        mWeatherDataAsyncTask = new WeatherDataAsyncTask(this);
        mWeatherDataAsyncTask.execute(Double.toString(mLatitude), Double.toString(mLongitude));
    }

    @Override
    public void asyncTaskFinished(Weather[] weatherForecast) {
        final ProgressBar progBarView = (ProgressBar)findViewById(progressBar);
        mWeatherModel = new Weather[weatherForecast.length];
        int i = 0;
        for(Weather w : weatherForecast){
            mWeatherModel[i] = w;
            i++;
        }
        progBarView.setIndeterminate(false);
        progBarView.setIndeterminate(false);
        progBarView.setVisibility(View.INVISIBLE);


        for(i=0; i<mWeatherModel.length; i++){
            Log.d("asyncTaskFinished", mWeatherModel[i].toString());
        }
    }

}
