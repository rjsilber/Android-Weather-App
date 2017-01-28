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
import android.util.Log;

public class LocationDetector extends Service {
    // use mIBinder to bind WeatherActivity to LocationDetector Service
    private final IBinder mIBinder = new LocationBinder(); // declare and initialize final IBinder
    private static Location mLocation; // declare static location variable
    private LocationManager mLocationManager = null; // declare a LocationManager
    private LocationListener mLocationListener = null; // declare a LocationListener
    private LocationProvider mLocationProvider = null;


    private static long MIN_DISTANCE_BW_UPDATES = 0; // 0 meters
    private static long MIN_TIME_BW_UPDATES = 0; // 0 milliseconds
    private static long MAX_TIME_BEFORE_TIMEOUT = 10000; // 10 secs

    private boolean wasOnLocationChangedCalled = false;


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
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        /* LOCATION_SERVICE is used to retrieve a LocationManager for controlling location updates */

        mLocationProvider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location; // set the location member to the new location
                wasOnLocationChangedCalled = true; // successful request for location update by LocationManager
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        CountDownTimer countDownTimer = new CountDownTimer(MAX_TIME_BEFORE_TIMEOUT, MAX_TIME_BEFORE_TIMEOUT) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() { // timeout
                if (ActivityCompat.checkSelfPermission(LocationDetector.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(LocationDetector.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    mLocationManager.removeUpdates(mLocationListener); // stop location service from sending updates to the listener
                    if(!wasOnLocationChangedCalled){ // LocationManager failed to get the location before timeout
                        Log.d("getLastKnownLocation", "getLastKnownLocation");
                        mLocation = mLocationManager.getLastKnownLocation(mLocationProvider.getName());
                    }
                }
            }
        };

        countDownTimer.start();
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, mLocationListener);

    }


    @Override
    public void onDestroy() {
//        mLocationManager = null;
//        mLocationListener = null;
    }

    // public interface method that the binding component calls to receive the user's location data
    public Location getLocationFromDetector() {
        if(mLocation != null){
            return this.mLocation;
        }else return null;
    }



}