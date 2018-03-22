package com.xyz.drivingRecorder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by cndul on 3/23/18.
 */
class MySensorListener implements SensorEventListener {

    private MainActivity mainActivity;
    private Vibrator mVibrator;

    public float[] values = new float[3];

    public MySensorListener(MainActivity mainActivity, Activity activity) {
        this.mainActivity = mainActivity;
        mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
        Log.i(mainActivity.TAG, "onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]
                + ",prev: " + values[0] + ", " + values[1] + ", " + values[2]);

        TextView mSensorInfoA = (TextView) mainActivity.findViewById(R.id.main_textview_sensor_info_a);
        mSensorInfoA.setText("" + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);

        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {

            int limit = SettingDataModel.instance().getCollisionDetectionSensitivity();

            float delta0 = Math.abs(event.values[0] - values[0]);
            float delta1 = Math.abs(event.values[1] - values[1]);
            float delta2 = Math.abs(event.values[2] - values[2]);

            if (delta0 > limit || delta1 > limit || delta2 > limit) {
                mVibrator.vibrate(300);
                //进行手机晃动的监听  ，可以在这里实现 intent 等效果

                mainActivity.requestRecorder("active_trigger");
            }

            values[0] = event.values[0];
            values[1] = event.values[1];
            values[2] = event.values[2];

            Log.e(mainActivity.TAG, "" + delta0 + " " + delta1 + " " + delta2);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(mainActivity.TAG, "onAccuracyChanged");
    }
}
