package com.xyz.drivingRecorder;

public class SettingDataModel {
    private static int mCollisionDetectionSensitivity = 20;
    private static int mVideoFileTimeSize = 60; //s
    private static int mVideoStorageSize = 100; //M

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

    public static void setVideoStorageSize(int i) {
        mVideoStorageSize = i;
    }

    public static int getVideoStorageSize() {
        return mVideoStorageSize;
    }
}
