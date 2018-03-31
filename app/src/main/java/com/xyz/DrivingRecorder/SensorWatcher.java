package com.xyz.DrivingRecorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorWatcher implements ISensorWatcher {

    private String TAG = "SensorWatcher";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener mSensorEventListener;
    private Activity mActivity;

    public SensorWatcher(Activity activity) {
        mActivity = activity;

        mSensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void registerSensorEventListener(SensorEventListener sensorEventListener) {
        mSensorEventListener = sensorEventListener;
    }

    @Override
    public void onResume() {
        // 注册传感器监听函数
        mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // 注销监听函数
        mSensorManager.unregisterListener(mSensorEventListener);
    }
}
