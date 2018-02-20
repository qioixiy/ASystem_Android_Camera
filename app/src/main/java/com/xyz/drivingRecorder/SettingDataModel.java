package com.xyz.drivingRecorder;

public class SettingDataModel {
    private static int mCollisionDetectionSensitivity = 20;

    public static void setCollisionDetectionSensitivity(int i) {
        mCollisionDetectionSensitivity = i;
    }

    public static int getCollisionDetectionSensitivity() {
        return mCollisionDetectionSensitivity;
    }
}
