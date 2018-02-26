package com.engineering.nhub.ancaps_project;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.HashMap;

import Util.SessionManager;

/**
 * Created by iduma on 2/22/18.
 */

public class LocationService extends Service {

    //    private static final String TAG = "com.engineering.nhub.ancaps_project.LocationService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 60000 * 10;
    private static final float LOCATION_DISTANCE = 10f;


    private class LocationListener implements android.location.LocationListener
    {

        Location mLastLocation;

        LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(final Location location)
        {
            HashMap<String, String> user;

            String userObjectId = null;
           SessionManager sessionManager = new SessionManager(getApplicationContext());

            user = sessionManager.getUserDetails();
            try {
                userObjectId = user.get(SessionManager.KEY_OBJECT_ID);
            }catch (Exception e){
                e.printStackTrace();
            }
            mLastLocation.set(location);
            final double longitude = location.getLongitude();
            final double latitude = location.getLatitude();

            if (location != null){
                try {
//                    Toast.makeText(com.engineering.nhub.ancaps_project.LocationService.this, "location" + location, Toast.LENGTH_SHORT).show();

                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", userObjectId);
                    query.getFirstInBackground( new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser userObject, ParseException e) {
                            if(e!=null){
                                String msg;
                                if (e.getMessage().contains("i/o")) {
                                    msg = "Check Internet Connection";
                                } else {
                                    msg = "Oops Something Went Wrong";
                                }
                                MDToast.makeText(getApplicationContext(), msg, MDToast.LENGTH_SHORT,
                                        MDToast.TYPE_ERROR).show();
                            }else{
                                ParseGeoPoint geoPoint = new ParseGeoPoint(latitude , longitude );
                                ParseObject locatioObject = new ParseObject("Location");
                                locatioObject.put("userId", userObject);
                                locatioObject.put("location", geoPoint);

                                ParseACL postACL = new ParseACL( ParseUser.getCurrentUser());
                                postACL.setPublicReadAccess(true);
                                postACL.setPublicWriteAccess(true);

                                locatioObject.saveInBackground( new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            // success
                                        } else {
                                            // fail
                                            Log.e("Tag", "getting to fail " + e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    } );


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
        } catch (IllegalArgumentException ex) {
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {

                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
