package com.xxzoo.xyz.drivingRecorder;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCamera();
    }

    private void initCamera() {
        int cameras = Camera.getNumberOfCameras();
    }
}
