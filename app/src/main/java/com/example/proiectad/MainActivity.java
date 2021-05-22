package com.example.proiectad;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final float nanoToSec = 1.0f / 1000000000.0f;
    private SensorListener sensorListener;
    private GPSListener gpsListener;
    private ImageView compassImage;
    private ImageView arrowImage;
    private TextView textSpeed;
    private TextView textDist;
    private Location prevLocation;
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth;
    private float currentAzimuth;
    private float totalDistance;
    private float prevAngle;
    private float angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gravity = new float[3];
        geomagnetic = new float[3];
        azimuth = 0.0f;
        currentAzimuth = 0.0f;
        totalDistance = 0.0f;
        prevAngle = 315.0f;

        compassImage = (ImageView) findViewById(R.id.compass);
        arrowImage = (ImageView) findViewById(R.id.arrow);
        textSpeed = (TextView) findViewById(R.id.textSpeed);
        textSpeed.setText("Speed: 0 m/s");
        textDist = (TextView) findViewById(R.id.textDist);
        textDist.setText("Distance: 0 m");

        prevLocation = null;

        gpsListener = new GPSListener(this);
        gpsListener.setListener(new GPSListener.Listener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onUpdate(Location location) {
                if(location == null) {
                    textSpeed.setText("Speed: 0 m/s - 0 km/h");
                } else {
                    float speed = location.getSpeed();
                    if(prevLocation != null) {
                        totalDistance += speed * ((location.getElapsedRealtimeNanos() - prevLocation.getElapsedRealtimeNanos()) * nanoToSec);

                        Location locationLong = new Location(prevLocation);
                        locationLong.setLatitude(location.getLatitude());
                        Location locationLat = new Location(prevLocation);
                        locationLat.setLongitude(location.getLongitude());

                        float distanceLong = location.distanceTo(locationLong);
                        float distanceLat = location.distanceTo(locationLat);

                        if(location.getLongitude() < prevLocation.getLongitude()) {
                            distanceLong = -distanceLong;
                        }

                        if(location.getLatitude() < prevLocation.getLatitude()) {
                            distanceLat = -distanceLat;
                        }

                        float totalDist = (float) Math.sqrt(distanceLat * distanceLat + distanceLong * distanceLong);

                        distanceLat /= totalDist;
                        distanceLong /= totalDist;

                        angle = (float) (Math.atan2(distanceLat, distanceLong) * 180 / Math.PI);
                        angle = (angle + 315 + currentAzimuth) % 360;

                        Animation animation = new RotateAnimation(-prevAngle, -angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(500);
                        animation.setRepeatCount(0);
                        animation.setFillAfter(true);

                        prevAngle = angle;

                        arrowImage.startAnimation(animation);
                    }
                    prevLocation = location;
                    textSpeed.setText(String.format("Speed: %.2f m/s - %.2f km/h", speed, speed * 3.6f));
                    textDist.setText(String.format("Distance: %d m", (int)totalDistance));
                }
            }
        });

        sensorListener = new SensorListener(this);
        sensorListener.setListener(new SensorListener.Listener() {
            @Override
            public void onUpdate(SensorEvent event) {
                final float alpha = 0.97f;
                synchronized (this) {
                    if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                    }

                    if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
                        geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
                        geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
                    }

                    float[] R = new float[9];
                    float[] I = new float[9];
                    if(SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        float[] orientation = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        azimuth = (float) Math.toDegrees(orientation[0]);
                        azimuth = (azimuth + 360) % 360;

                        Animation animation = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(500);
                        animation.setRepeatCount(0);
                        animation.setFillAfter(true);

                        currentAzimuth = azimuth;

                        compassImage.startAnimation(animation);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorListener.register();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorListener.unregister();
    }
}