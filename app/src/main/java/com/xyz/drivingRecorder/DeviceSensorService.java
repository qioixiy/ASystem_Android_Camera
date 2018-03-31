package com.xyz.drivingRecorder; /**
 * 加速度传感,陀螺仪，压力传感器，数据收集
 *
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class DeviceSensorService extends Service {
    private static final String TAG = "DeviceSensorService";
    private Sensor sensorAcc, sensoGyros, sensoPress;
    private SensorManager mSensorManager;
    private WakeLock mWakeLock;
    private IHandler mHandler;

    public interface IHandler {
        void handle(String data);
    }

    public class DeviceSensorServiceBinder extends Binder {
        public DeviceSensorService getDeviceSensorService(){
            return DeviceSensorService.this;
        }
    }

    public void registerHandler(IHandler handler) {
        mHandler = handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        }
        /*List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0){
            Sensor sensor = sensors.get(0);
            mSensorManager.registerListener(this,
                    sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }*/

        // 加速度感应器
        Sensor sensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensoGyros = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);// 陀螺仪
        Sensor sensoPress = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);// 压力

        /*
         * 最常用的一个方法 注册事件
         * 参数1 ：SensorEventListener监听器
         * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
         * 参数3 ：模式 可选数据变化的刷新频率，采样率
         * SENSOR_DELAY_FASTEST,100次左右
         * SENSOR_DELAY_GAME,50次左右
         * SENSOR_DELAY_UI,20次左右
         * SENSOR_DELAY_NORMAL,5次左右
         */
        mSensorManager.registerListener(mySensorListener, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL); //以普通采样率注册监听器
//        mSensorManager.registerListener(mySensorListener, sensoGyros, SensorManager.SENSOR_DELAY_NORMAL);
//        mSensorManager.registerListener(mySensorListener, sensoPress, SensorManager.SENSOR_DELAY_NORMAL);

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DeviceSensorService.class.getName());
//        mWakeLock.acquire();

        registerHandler(new IHandler() {
            @Override
            public void handle(String str) {

                if (StaticValue.getSystemStatus().equals(StaticValue.SYSTEM_STATUS_IDEL)) {

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Bundle b = new Bundle();
                    b.putString("data", str);  //string
                    b.putSerializable("data", str);
                    intent.putExtra("data", str);
                    intent.putExtras(b);

                    getApplication().startActivity(intent);
                }

                StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_MAIN_ACTIVITY);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mySensorListener);
            mySensorListener = null;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    };

    /*
     * SensorEventListener 接口的实现，需要实现两个方法
     * 方法1 onSensorChanged 当数据变化的时候被触发调用
     * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
     */
    private SensorEventListener mySensorListener = new SensorEventListener() {

        private boolean firstTime = true;
        private float[] values = new float[3];
        private static final String TAG = "SensorEventListener";

        public void onSensorChanged(SensorEvent event) {

            try {
                // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
                //Log.i(TAG, "onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]
                //        + ",prev: " + values[0] + ", " + values[1] + ", " + values[2]);

                int sensorType = event.sensor.getType();
                if (sensorType == Sensor.TYPE_ACCELEROMETER) {

                    int limit = SettingDataModel.instance().getCollisionDetectionSensitivity();

                    float delta0 = Math.abs(event.values[0] - values[0]);
                    float delta1 = Math.abs(event.values[1] - values[1]);
                    float delta2 = Math.abs(event.values[2] - values[2]);

                    values[0] = event.values[0];
                    values[1] = event.values[1];
                    values[2] = event.values[2];
                    if (firstTime) {
                        firstTime = false;
                        return;
                    }

                    if (delta0 > limit || delta1 > limit || delta2 > limit) {

                        if (mHandler != null) {
                            mHandler.handle("active_trigger");
                        }
                    }

                    //Log.e(TAG, "" + delta0 + " " + delta1 + " " + delta2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "onAccuracyChanged:"+ sensor + ",acc:" + accuracy);

        }
    };
}