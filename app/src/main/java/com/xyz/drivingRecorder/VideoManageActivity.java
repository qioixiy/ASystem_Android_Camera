package com.xyz.drivingRecorder;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class VideoManageActivity extends AppCompatActivity {

    private String TAG = "VideoManageActivity";

    private SlideListView listView;
    private List<VideoDataModel.VideoMetaData> videoMetaDataList = new ArrayList<VideoDataModel.VideoMetaData>();
    private ListViewSlideAdapter listViewSlideAdapter;
    private VideoDataModel mVideoDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_manage);
        getData();
        initView();
    }

    private void initView() {
        listView = (SlideListView) findViewById(R.id.list);
        listViewSlideAdapter = new ListViewSlideAdapter(this, videoMetaDataList);
        listView.setAdapter(listViewSlideAdapter);
        listViewSlideAdapter.setOnClickListenerEditOrDelete(new ListViewSlideAdapter.OnClickListenerEditOrDelete() {
            @Override
            public void OnClickListenerEdit(int position) {
                Toast.makeText(VideoManageActivity.this, "edit position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnClickListenerDelete(int position) {
                int id = videoMetaDataList.get(position).getId();
                String name = videoMetaDataList.get(position).getName();
                String path = videoMetaDataList.get(position).getPath();

                String text = "删除" + name + "成功";
                if (!DeleteFileUtil.delete(path)) {
                    text = "删除" + name + "失败";
                }

                mVideoDataModel.deleteVideoMetaData(id);
                videoMetaDataList.remove(position);
                listViewSlideAdapter.notifyDataSetChanged();

                Toast.makeText(VideoManageActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });

        listViewSlideAdapter.setOnInnerItemOnClickListener(new ListViewSlideAdapter.InnerItemOnClickListener() {
            @Override
            public void onClick(int position) {
                Uri uri = Uri.parse(videoMetaDataList.get(position).getPath());
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Log.v(TAG, uri.toString());
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);
            }
        });
    }

    private void getData() {
        mVideoDataModel = new VideoDataModel();
        mVideoDataModel.setContext(this);
        ArrayList<VideoDataModel.VideoMetaData> videoMetaDataList = mVideoDataModel.queryAll();

        for (VideoDataModel.VideoMetaData data : videoMetaDataList) {
            this.videoMetaDataList.add(data);
        }
    }
}
