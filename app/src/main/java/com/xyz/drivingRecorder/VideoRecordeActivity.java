package com.xyz.drivingRecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;

public class VideoRecordeActivity extends AppCompatActivity {
    private static final String TAG = VideoRecordeActivity.class.getSimpleName();

    private CameraView cameraView;
    private File mVideoFile = null;
    private boolean mCanBeStart = true;

    Handler mDelayHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String data = getIntent().getStringExtra("data");

        setContentView(R.layout.activity_recorder_main);

        String dir = getStoragePathBase();
        //新建一个File，传入文件夹目录
        File file = new File(dir);
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            file.mkdirs();
        }

        checkPermission(this);
        initCameraView();

        if (data.equals("active_trigger")) {
            mDelayHandler.postDelayed(new TriggerRunnable(this), 200);
        }
    }

    private void initCameraView() {
        cameraView = (CameraView)findViewById(R.id.camera);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
                ;
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
                ;
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Log.i(TAG, cameraKitImage.getMessage());
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
                Log.i(TAG, cameraKitVideo.getVideoFile().getName());

                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            mVideoFile.getAbsolutePath(), mVideoFile.getName(), null);
                    showToast("保存成功" + mVideoFile);

                    VideoDataModel videoDataModel = new VideoDataModel();
                    videoDataModel.setContext(VideoRecordeActivity.this);
                    VideoDataModel.VideoMetaData videoMetaData = new VideoDataModel.VideoMetaData();

                    String name = mVideoFile.getName();
                    String path = mVideoFile.getAbsolutePath();
                    videoMetaData.setName(name);
                    videoMetaData.setPath(path);
                    videoDataModel.insertVideoMetaData(videoMetaData);

                    Log.i(TAG, path);
                } catch (FileNotFoundException e) {
                    showToast("保存失败");
                    e.printStackTrace();
                }
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(mVideoFile)));

                mVideoFile = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    private void updateSelect() {

        Button btn_start = (Button)findViewById(R.id.button_capture_start);
        Button btn_stop = (Button)findViewById(R.id.button_capture_stop);

        if (!mCanBeStart) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String path = getStoragePathBase() + "video_" + timeStamp + ".mp4";

            mVideoFile = new File(path);
            cameraView.captureVideo(mVideoFile);
        } else {
            cameraView.stopVideo();
        }

        btn_start.setEnabled(mCanBeStart);
        btn_stop.setEnabled(!mCanBeStart);
    }

    public static String getStoragePathBase() {
        // File extDir = Environment.getExternalStorageDirectory();
        return "/storage/emulated/0/drivingRecorder/";
    }

    void toggleButtonOnClickStart(View v) {

        // check storage space
        if (!checkStorageHaveSpace()) {
            showToast("空间不足，需要先释放存储空间");
            return;
        }

        if (!mCanBeStart) {
            return;
        }
        mCanBeStart = false;

        updateSelect();

        mDelayHandler.postDelayed(new TimeoutRunnable(this), SettingDataModel.getVideoFileTimeSize()*1000);
    }

    private boolean checkStorageHaveSpace() {
        double size = FileSizeUtil.getFileOrFilesSize(getStoragePathBase(), FileSizeUtil.SIZETYPE_B);
        int limit = SettingDataModel.getVideoStorageSize() * 1000 * 1000;
        if (size > limit) {
            return false;
        }

        return true;
    }

    void toggleButtonOnClickStop(View v) {
        if (mCanBeStart) {
            return;
        }
        mCanBeStart = true;

        updateSelect();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void checkPermission(Activity context) {
        HiPermission.create(context)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Log.i(TAG, "onClose");
                        showToast("用户关闭权限申请");
                    }

                    @Override
                    public void onFinish() {
                        /*showToast("所有权限申请完成");*/
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Log.i(TAG, "onDeny");
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Log.i(TAG, "onGuarantee");
                    }
                });
    }

    public  void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

    private class TriggerRunnable implements Runnable {

        private Context context;
        public TriggerRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            toggleButtonOnClickStart(null);
        }
    }

    private class TimeoutRunnable implements Runnable {

        private Context context;
        public TimeoutRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            toggleButtonOnClickStop(null);
        }
    }
}
