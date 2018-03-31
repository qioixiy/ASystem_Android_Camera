package com.xyz.drivingRecorder;

public class StaticValue {
    private static String mSystemStatus;

    public static final String SYSTEM_STATUS_IDEL = "SYSTEM_STATUS_IDEL";
    public static final String SYSTEM_STATUS_MAIN_ACTIVITY = "SYSTEM_STATUS_MAIN_ACTIVITY";
    public static final String SYSTEM_STATUS_CAPTURE_ACTIVITY = "SYSTEM_STATUS_CAPTURE_ACTIVITY";
    public static final String SYSTEM_STATUS_CAPTURE_RUNNING = "SYSTEM_STATUS_CAPTURE_RUNNING";

    public static String getSystemStatus() {
        return mSystemStatus;
    }

    public static synchronized void setSystemStatus(String systemStatus) {
        mSystemStatus = systemStatus;
    }
}
