package com.xyz.drivingRecorder;

import android.util.Log;

public class StaticValue {

    private static final String TAG = "StaticValue";

    public static final String SYSTEM_STATUS_IDEL = "SYSTEM_STATUS_IDEL";
    public static final String SYSTEM_STATUS_MAIN_ACTIVITY = "SYSTEM_STATUS_MAIN_ACTIVITY";
    public static final String SYSTEM_STATUS_CAPTURE_ACTIVITY = "SYSTEM_STATUS_CAPTURE_ACTIVITY";
    public static final String SYSTEM_STATUS_CAPTURE_RUNNING = "SYSTEM_STATUS_CAPTURE_RUNNING";

    private static String mSystemStatus = SYSTEM_STATUS_IDEL;

    public static synchronized String getSystemStatus() {
        return mSystemStatus;
    }

    public static synchronized void setSystemStatus(String systemStatus) {
        Log.w(TAG, "set mSystemStatus to " + mSystemStatus);
        mSystemStatus = systemStatus;
    }
}
