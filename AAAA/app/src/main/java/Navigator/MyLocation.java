package Navigator;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


public class MyLocation extends Service implements LocationListener {

    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    public double Longitude, Latitude;//AAAAAAAAA public!!!

    Location location = null;
    protected LocationManager locationManager;

    public MyLocation(Context context) {
        this.context = context;
    }

    public Location getLocation() {

        final int MIN_TIME_UPDATE = 10000;
        final int MIN_DISTANCE_UPDATE = 5;

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled((locationManager.GPS_PROVIDER));
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled) {
                    //location=null;

                    if(location==null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);

                    if (locationManager != null) {

                          location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                             Latitude=location.getLatitude();
                             Longitude=location.getLongitude();
                            return location;
                        }
                    }
                    }
                } else {

                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            return location;
                        }
                    }
                }


            }

        } catch (Exception ex) {

        }
        Log.d("CallBusStop", "AAAA");
         return location;
    }



    @Override
    public void onLocationChanged(Location location) {

        this.location=location;
        this.Longitude = location.getLongitude();
        this.Latitude = location.getLatitude();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
