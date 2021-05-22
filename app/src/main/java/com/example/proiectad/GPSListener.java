package com.example.proiectad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;

public class GPSListener implements LocationListener {
    public interface Listener {
        void onUpdate(Location location);
    }

    Listener listener;
    LocationManager locationManager;

    @SuppressLint("MissingPermission")
    public GPSListener(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(listener != null)
            listener.onUpdate(location);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
