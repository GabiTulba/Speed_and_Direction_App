package com.example.proiectad;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorListener implements SensorEventListener {
    public  interface Listener {
        void onUpdate(SensorEvent event);
    }

    private final SensorManager sensorManager;
    private final Sensor sensorAcc;
    private final Sensor sensorMagnet;
    private Listener listener;

    public SensorListener(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(listener != null)
            listener.onUpdate(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void register() {
        sensorManager.registerListener((SensorEventListener) this, sensorAcc, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener((SensorEventListener) this, sensorMagnet, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() {
        sensorManager.unregisterListener((SensorEventListener) this);
    }
}
