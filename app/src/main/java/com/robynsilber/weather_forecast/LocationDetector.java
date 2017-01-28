package com.robynsilber.weather_forecast;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class LocationDetector extends Service {
    // use mIBinder to bind WeatherActivity to LocationDetector Service
    private final IBinder mIBinder = new LocationBinder(); // declare and initialize final IBinder
    private static Location mLocation; // declare static location variable
    private LocationManager mLocationManager = null; // declare a LocationManager
    private LocationListener mLocationListener = null; // declare a LocationListener
    private LocationProvider mLocationProvider;


    private static long MIN_DISTANCE_BW_UPDATES = 0; // 0 meters
    private static long MIN_TIME_BW_UPDATES = 0; // 0 milliseconds
    private static long MAX_TIME_BEFORE_TIMEOUT = 10000; // 10 secs

    private boolean wasOnLocationChangedCalled = false;

    private CountDownTimer mCountDownTimer; // countdown timer used for timeout after 10 seconds
    private boolean isCountDownTimerOn = false; // true if countdown timer is currently ticking


    private Intent mLocationDataIntent;
    private LocalBroadcastManager mLocalBroadcastManager;

    // nested class used for the client Binder.
    public class LocationBinder extends Binder {
        // method that WeatherActivity calls within its ServiceConnection's onServiceConnected() method
        LocationDetector getLocationDetector() { // method for client to get the service
            return LocationDetector.this; // returns a reference to the LocationDetector instantiation
        }
    }

    @Override
    public IBinder onBind(Intent intent) { // the method WeatherActivity will call to bind to service
        return mIBinder; // returns the IBinder
    }


    @Override
    public void onCreate() {
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show(); /* TODO: comment out line */

        mLocationDataIntent = new Intent("some_filter");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);


        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        /* LOCATION_SERVICE is used to retrieve a LocationManager for controlling location updates */

//        isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mLocationProvider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
//        LocationProvider locationProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                wasOnLocationChangedCalled = true; // successful request for location update by LocationManager
                mLocation = location; // set the location member to the new location

                if (isCountDownTimerOn) { // countdown timer is currently ticking
                    Toast.makeText(LocationDetector.this, "Timer on; location changed", Toast.LENGTH_LONG).show();
                    mCountDownTimer.cancel(); // location found before timeout; cancel timer
                    isCountDownTimerOn = false; // timer has been cancelled; set to false
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
//                    isNetworkEnabled = true;
                    mLocationProvider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);;
                }
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
//                    isGpsEnabled = true;
                    mLocationProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
                if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
//                    isNetworkEnabled = false;
                    if(mLocationProvider.toString().equals(LocationManager.NETWORK_PROVIDER)){
                        mLocationProvider = null;
                    }

                }
                else if (provider.equals(LocationManager.GPS_PROVIDER)) {
//                    isGpsEnabled = false;
                    if(mLocationProvider.toString().equals(LocationManager.GPS_PROVIDER)){
                        mLocationProvider = null;
                    }
                }

            }
        };
    }


    @Override
    public void onDestroy() {
        mLocationManager = null;
        mLocationListener = null;
    }

    // public interface method that the binding component calls to receive the user's location data
    public void getLocationFromDetector() {

        double[] locationCoords;

        mCountDownTimer = new CountDownTimer(MAX_TIME_BEFORE_TIMEOUT, MAX_TIME_BEFORE_TIMEOUT) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() { // timeout
                isCountDownTimerOn = false;
            }
        };

        if (((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (mLocationProvider.getName().equals(LocationManager.GPS_PROVIDER) || mLocationProvider.getName().equals(LocationManager.NETWORK_PROVIDER)))
                || ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (mLocationProvider.getName().equals(LocationManager.NETWORK_PROVIDER)))) {

            // begin requesting location updates:
            mLocationManager.requestLocationUpdates(String.valueOf(mLocationProvider), MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, mLocationListener);
            mCountDownTimer.start(); // start countdown timer
            isCountDownTimerOn = true; // timer has been started

            while(isCountDownTimerOn); // wait; loop until either timeout or timer is cancelled
            // countdown timer has finished ticking

            mLocationManager.removeUpdates(mLocationListener); // stop location service from sending updates to the listener

            if(!wasOnLocationChangedCalled){ // LocationManager failed to get the location before timeout
                mLocation = mLocationManager.getLastKnownLocation(mLocationProvider.getName());
            }
        }

        if(mLocation != null){
            locationCoords = new double[]{mLocation.getLatitude(), mLocation.getLongitude()};

        }else{
            locationCoords = new double[]{0.0, 0.0};
        }

        mLocationDataIntent.putExtra("locationCoords", locationCoords);
        mLocalBroadcastManager.sendBroadcast(mLocationDataIntent);
    }



}