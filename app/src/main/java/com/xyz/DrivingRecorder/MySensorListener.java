package com.xyz.DrivingRecorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

class MySensorListener implements SensorEventListener {
    private String TAG = "MySensorListener";

    private Vibrator mVibrator;
    private Activity mActivity;
    private IHandler mHandler;

    public interface IHandler {
        void handle(String data);
    }

    public float[] values = new float[3];

    public MySensorListener(Activity activity) {
        mActivity = activity;
        mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void registerHandler(IHandler handler) {
        mHandler = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
            //Log.i(TAG, "onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]
            //        + ",prev: " + values[0] + ", " + values[1] + ", " + values[2]);

            TextView mSensorInfoA = (TextView) mActivity.findViewById(R.id.main_textview_sensor_info_a);
            if (mSensorInfoA != null) {
                mSensorInfoA.setText("" + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
            }

            int sensorType = event.sensor.getType();
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {

                int limit = SettingDataModel.instance().getCollisionDetectionSensitivity();

                float delta0 = Math.abs(event.values[0] - values[0]);
                float delta1 = Math.abs(event.values[1] - values[1]);
                float delta2 = Math.abs(event.values[2] - values[2]);

                if (delta0 > limit || delta1 > limit || delta2 > limit) {
                    mVibrator.vibrate(300);
                    //进行手机晃动的监听  ，可以在这里实现 intent 等效果

                    if (mHandler != null) {
                        mHandler.handle("active_trigger");
                    }
                }

                values[0] = event.values[0];
                values[1] = event.values[1];
                values[2] = event.values[2];

                //Log.e(TAG, "" + delta0 + " " + delta1 + " " + delta2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged");
    }
}
