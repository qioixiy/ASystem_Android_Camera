package com.xyz.DrivingRecorder;

import android.util.Log;

public class StaticValue {

    private static final String TAG = "StaticValue";

    public static final String SYSTEM_STATUS_IDEL = "SYSTEM_STATUS_IDEL";
    public static final String SYSTEM_STATUS_CAPTURE_STARTING = "SYSTEM_STATUS_CAPTURE_STARTING";
    public static final String SYSTEM_STATUS_CAPTURE_STOPING = "SYSTEM_STATUS_CAPTURE_STOPING";
    public static final String SYSTEM_STATUS_MAIN_ACTIVITY_HIDE = "SYSTEM_STATUS_MAIN_ACTIVITY_HIDE";
    public static final String SYSTEM_STATUS_MAIN_ACTIVITY_SHOW = "SYSTEM_STATUS_MAIN_ACTIVITY_SHOW";
    public static final String SYSTEM_STATUS_CAPTURE_ACTIVITY_HIDE = "SYSTEM_STATUS_CAPTURE_ACTIVITY_HIDE";
    public static final String SYSTEM_STATUS_CAPTURE_ACTIVITY_SHOW = "SYSTEM_STATUS_CAPTURE_ACTIVITY_SHOW";
    public static final String SYSTEM_STATUS_CAPTURE_RUNNING = "SYSTEM_STATUS_CAPTURE_RUNNING";
    public static final String SYSTEM_STATUS_SETTING_ACTIVITY_SHOW = "SYSTEM_STATUS_SETTING_ACTIVITY_SHOW";

    private static String mSystemStatus = SYSTEM_STATUS_IDEL;

    public static synchronized String getSystemStatus() {
        return mSystemStatus;
    }

    public static synchronized void setSystemStatus(String systemStatus) {
        Log.w(TAG, "set mSystemStatus to " + mSystemStatus);
        mSystemStatus = systemStatus;
    }
}
