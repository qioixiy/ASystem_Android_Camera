package com.xyz.DrivingRecorder; /**
 * 加速度传感,陀螺仪，压力传感器，数据收集
 *
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.Date;

public class DeviceSensorService extends Service {
    private static final String TAG = "DeviceSensorService";
    private Sensor sensorAcc, sensoGyros, sensoPress;
    private SensorManager mSensorManager;
    private WakeLock mWakeLock;
    private IHandler mHandler;

    private Date mPrevActionDate;

    private LocationManager locationManager;
    private String locationProvider;
    private Location location;

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

    public DeviceSensorService() {
        mPrevActionDate = new Date(System.currentTimeMillis());
    }

    private boolean checkActionValid() {
        boolean ret = false;

        Date curActionDate = new Date(System.currentTimeMillis());

        long cur = curActionDate.getTime();
        long pre = mPrevActionDate.getTime();
        if(pre + 2*1000 <= cur) {
            mPrevActionDate = curActionDate;
            ret = true;
        }

        return ret;
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
                if (!checkActionValid()) {
                    Log.w(TAG, "drop");
                    return;
                }

                String systemStatus = StaticValue.getSystemStatus();

                Log.i(TAG, "before SystemStatus:" + systemStatus);

                if (systemStatus.equals(StaticValue.SYSTEM_STATUS_IDEL)
                        || systemStatus.equals(StaticValue.SYSTEM_STATUS_MAIN_ACTIVITY_HIDE)
                        || systemStatus.equals(StaticValue.SYSTEM_STATUS_MAIN_ACTIVITY_SHOW)
                        || systemStatus.equals(StaticValue.SYSTEM_STATUS_CAPTURE_ACTIVITY_HIDE)) {

                    StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_STARTING);

                    Intent intent = new Intent(getBaseContext(), VideoRecordeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    Bundle b = new Bundle();
                    b.putString("data", str);  //string
                    b.putSerializable("data", str);
                    intent.putExtra("data", str);
                    intent.putExtras(b);

                    getApplication().startActivity(intent);
                } else if (systemStatus.equals(StaticValue.SYSTEM_STATUS_CAPTURE_RUNNING)) {
                    Intent intent = new Intent("Capture.Stop");
                    sendBroadcast(intent);
                }

                Log.i(TAG, "after SystemStatus:" + StaticValue.getSystemStatus());
            }
        });

        initLocation();
    }

    private void initLocation() {

        // 要申请的权限 数组 可以同时申请多个权限
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT >= 23) {
            //如果超过6.0才需要动态权限，否则不需要动态权限
            //如果同时申请多个权限，可以for循环遍历
            int check = ContextCompat.checkSelfPermission(this,permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //写入你需要权限才能使用的方法
                //run();
            } else {
                //手动去请求用户打开权限(可以在数组中添加多个权限) 1 为请求码 一般设置为final静态变量
                //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        } else {
            //写入你需要权限才能使用的方法
            //run();
        }

        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

        //从可用的位置提供器中，匹配以上标准的最佳提供器
        locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: 没有权限 ");
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, "onCreate: " + (location == null) + "..");
        if (location != null) {
            Log.d(TAG, "onCreate: location");
            //不为空,显示地理位置经纬度
            //showLocation(location);
        }
        //监视地理位置变化
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + ".." + Thread.currentThread().getName()
                + location.getLatitude() + "," + location.getLongitude());
            //如果位置发生变化,重新显示

            double tCurLatitude = location.getLatitude();
            double tCurLongitude = location.getLongitude();

            double dist = getDistance(tCurLatitude, tCurLongitude, mPrevLatitude, mPrevLongitude); // unit:KM

            if (mIsFirst) {
                mIsFirst = false;

                mPrevLatitude = tCurLatitude;
                mPrevLongitude = tCurLongitude;

                return;
            }

            if (dist > 50/1000.) {
                mPrevLatitude = tCurLatitude;
                mPrevLongitude = tCurLongitude;

                if (mHandler != null) {
                    mHandler.handle("active_trigger");
                }
            }
        }

        public double getDistance(double lat1, double lon1, double lat2, double lon2) {

            float[] results=new float[1];

            Location.distanceBetween(lat1, lon1, lat2, lon2, results);

            return results[0];

        }

        private boolean mIsFirst = true;
        private double mPrevLatitude = 0.0;
        private double mPrevLongitude = 0.0;
    };

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