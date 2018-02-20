package com.xyz.drivingRecorder;

public class SettingDataModel {
    private static int mCollisionDetectionSensitivity = 20;
    private static int mVideoFileTimeSize = 6; //s

    public static void setCollisionDetectionSensitivity(int i) {
        mCollisionDetectionSensitivity = i;
    }

    public static int getCollisionDetectionSensitivity() {
        return mCollisionDetectionSensitivity;
    }

    public static void setVideoFileTimeSize(int i) {
        mVideoFileTimeSize = i;
    }

    public static int getVideoFileTimeSize() {
        return mVideoFileTimeSize;
    }
}
