package com.xyz.drivingRecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class VideoRecorde2Activity extends AppCompatActivity {

    public MovieRecorderView movieRecorderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recorder2);

        movieRecorderView = (MovieRecorderView)findViewById(R.id.camera);
    }

    public void startRecord(View v) {

        movieRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {
            @Override
            public void onRecordFinish() {
                Log.e("VideoRecorde2Activity", "done");
            }
        });
    }

    public void stopRecord(View v) {
        movieRecorderView.stopRecord();
    }
}
