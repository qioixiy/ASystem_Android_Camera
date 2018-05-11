package com.xyz.DrivingRecorder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;

public class VideoRecordeActivity extends AppCompatActivity {
    private static final String TAG = VideoRecordeActivity.class.getSimpleName();

    private final int MSG_RECORDER_DONE = 0x01;
    private int mRecoderTimeStep = SettingDataModel.instance().getVideoFileTimeStepSize();
    private boolean mRunning = false;
    private boolean mPending = false;
    private enum State {
        State_Started,
        State_Stoped,
    };
    private State mState = State.State_Stoped;

    private State getState() {
        return mState;
    }

    private void setState(State state) {
        mState = state;
    }

    private boolean getPending() {
        return mPending;
    }

    private void setPending(boolean b) {
        mPending = b;
    }

    public interface AcitivityLifeCycle {
        void onResume();
        void onPause();
    }

    public interface VideoRecorderMethod {
        void start();
        void stop();
    }

    public interface VideoRecorderDone {
        void done();
    }

    private CameraView cameraView;
    private File mVideoFile = null;
    private boolean mCanBeStart = true;

    private Handler mDelayHandler = new Handler();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case MSG_RECORDER_DONE:
                    mVideoRecorderDone.done();
                    break;
                default:
                    break;
            }
        }
    };

    private MovieRecorderView movieRecorderView;

    private AcitivityLifeCycle mAcitivityLifeCycle;
    private VideoRecorderMethod mVideoRecorderMethod;
    private VideoRecorderDone mVideoRecorderDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSys();

        //initView();
        initView2();

        initSensorInfo();

        initRecviver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAcitivityLifeCycle != null) {
            mAcitivityLifeCycle.onResume();
        }

        registerReceiver(mMyBroadcastReceiver, intentFilter); //注册监听

        StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_ACTIVITY_SHOW);

        if (getPending()) {
            mDelayHandler.postDelayed(new TriggerRunnable(this), 1500);
            setPending(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAcitivityLifeCycle != null) {
            mAcitivityLifeCycle.onPause();
        }

        unregisterReceiver(mMyBroadcastReceiver); //取消监听

        StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_ACTIVITY_HIDE);

        if (mState == State.State_Started) {
            toggleButtonOnClickStop(null);
            setPending(true);
        }
    }

    private void initSensorInfo() {

        MySensorListener mySensorListener = new MySensorListener(this);
        mySensorListener.registerHandler(new MySensorListener.IHandler() {
            @Override
            public void handle(String data) {
                Log.i(TAG, data);

                mVideoRecorderMethod.stop();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toggleButtonOnClickStop(null);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals("Capture.Stop")) {
                if (StaticValue.getSystemStatus().equals(StaticValue.SYSTEM_STATUS_CAPTURE_RUNNING)) {
                    StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_STOPING);

                    Log.i(TAG, "capture stop request");

                    toggleButtonOnClickStop(null);
                }
            }
        }
    }

    private MyBroadcastReceiver mMyBroadcastReceiver = new MyBroadcastReceiver();
    IntentFilter intentFilter = new IntentFilter();

    private void initRecviver() {
        intentFilter.addAction("Capture.Stop");
    }

    private void initSys() {
        String dir = getStoragePathBase();
        //新建一个File，传入文件夹目录
        File file = new File(dir);
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            file.mkdirs();
        }

        checkPermission(this);

        String data = getIntent().getStringExtra("data");
        if (data.equals("active_trigger")) {
            mDelayHandler.postDelayed(new TriggerRunnable(this), 1500);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_recorder_main);

        Button capture_start = findViewById(R.id.button_capture_start);
        capture_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonOnClickStart(v);
            }
        });
        Button capture_stop = findViewById(R.id.button_capture_stop);
        capture_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonOnClickStop(v);
            }
        });

        initCameraView();

        mAcitivityLifeCycle = new AcitivityLifeCycle() {
            @Override
            public void onResume() {
                cameraView.start();
            }

            @Override
            public void onPause() {
                cameraView.stop();
            }
        };

        class VideoRecorderMethod1 implements VideoRecorderMethod {

            private Context mContext;

            public VideoRecorderMethod1(Context context) {
                mContext = context;
            }

            @Override
            public void start() {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String path = getStoragePathBase() + "video_" + timeStamp + ".mp4";

                mVideoFile = new File(path);
                cameraView.captureVideo(mVideoFile);

                Button btn_start = (Button) findViewById(R.id.button_capture_start);
                Button btn_stop = (Button) findViewById(R.id.button_capture_stop);
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);

                mDelayHandler.postDelayed(new TimeoutRunnable(mContext), mRecodeTime.getTime()*1000);
            }

            @Override
            public void stop() {
                cameraView.stopVideo();

                Button btn_start = (Button) findViewById(R.id.button_capture_start);
                Button btn_stop = (Button) findViewById(R.id.button_capture_stop);
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
            }
        }

        mVideoRecorderMethod = new VideoRecorderMethod1(this);

        mVideoRecorderDone = new VideoRecorderDone() {
            @Override
            public void done() {
                File tFile = mVideoFile;
                mVideoFile = null;
                notifyMediaFile(tFile);
            }
        };
    }

    private void initView2() {

        setContentView(R.layout.activity_video_recorder2);

        movieRecorderView = (MovieRecorderView) findViewById(R.id.camera2);

        Button capture_start = findViewById(R.id.button_capture_start2);
        capture_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonOnClickStart(v);
            }
        });
        Button capture_stop = findViewById(R.id.button_capture_stop2);
        capture_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonOnClickStop(v);
            }
        });

        mVideoRecorderMethod = new VideoRecorderMethod() {

            @Override
            public void start() {
                if (mRunning) {
                    Log.e(TAG, "running");
                    return;
                }

                int time = mRecodeTime.getTime();
                if (time <= 0) {
                    Log.e(TAG, "Recode process done");
                    toggleButtonOnClickStop(null);
                    return;
                }

                movieRecorderView.setRecordMaxTime(time);

                mRunning = true;
                movieRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {
                    @Override
                    public void onRecordFinish() {
                        Log.e(TAG, "done");

                        if (!mRunning) {
                            Log.e(TAG, "not running");
                            return;
                        }
                        mRunning = false;
                        mIsUserInput = false;

                        Message msg = new Message();
                        msg.what = MSG_RECORDER_DONE;
                        handler.sendMessage(msg);
                    }
                });
                Button btn_start = (Button) findViewById(R.id.button_capture_start2);
                Button btn_stop = (Button) findViewById(R.id.button_capture_stop2);
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);

            }

            @Override
            public void stop() {
                if (!mRunning) {
                    Log.e(TAG, "not running");
                    return;
                }
                mRunning = false;

                Button btn_start = (Button) findViewById(R.id.button_capture_start2);
                Button btn_stop = (Button) findViewById(R.id.button_capture_stop2);
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                movieRecorderView.stopRecord();

                Message msg = new Message();
                msg.what = MSG_RECORDER_DONE;
                handler.sendMessage(msg);
            }
        };

        movieRecorderView.setRecordMaxTime(mRecodeTime.getTime());

        mVideoRecorderDone = new VideoRecorderDone() {
            @Override
            public void done() {
                Button btn_start = (Button) findViewById(R.id.button_capture_start2);
                Button btn_stop = (Button) findViewById(R.id.button_capture_stop2);
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);

                File tFile = movieRecorderView.getRecordFile();
                notifyMediaFile(tFile);

                if (mRecodeTime.needRestart() && !mIsUserInput) {
                    Log.i(TAG, "Recoder restart");
                    mRestarting = true;
                    toggleButtonOnClickStart(null);
                }
                mIsUserInput = true;
            }
        };
    }

    private boolean mRestarting = false;
    private boolean mIsUserInput = true;

    private void notifyMediaFile(File file) {

        setState(State.State_Stoped);

        mCanBeStart = true;

        try {
            // 其次把文件插入到系统图库
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            showToast("保存失败" + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showToast("保存成功" + file);

        VideoDataModel videoDataModel = new VideoDataModel();
        videoDataModel.setContext(VideoRecordeActivity.this);
        VideoDataModel.VideoMetaData videoMetaData = new VideoDataModel.VideoMetaData();

        String name = file.getName();
        String path = file.getAbsolutePath();
        videoMetaData.setName(name);
        videoMetaData.setPath(path);
        videoDataModel.insertVideoMetaData(videoMetaData);

        Log.i(TAG, path);

        // 最后通知图库更新
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));

        mRecodeTransaction.addRecordItem(new RecordItem(path));
    }

    private void initCameraView() {
        cameraView = (CameraView) findViewById(R.id.camera);

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

                mVideoRecorderDone.done();
            }
        });
    }

    private synchronized void updateSelect() {
        if (mVideoRecorderMethod != null) {
            if (!mCanBeStart) {
                mVideoRecorderMethod.start();
                StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_RUNNING);
            } else {
                StaticValue.setSystemStatus(StaticValue.SYSTEM_STATUS_CAPTURE_ACTIVITY_SHOW);
                mVideoRecorderMethod.stop();
            }

            setState(mCanBeStart ? State.State_Stoped : State.State_Started);
        }
    }

    public static String getStoragePathBase() {
        // File extDir = Environment.getExternalStorageDirectory();
        return "/storage/emulated/0/DrivingRecorder/";
    }

    void toggleButtonOnClickStart(View v) {

        // check storage space
        if (!checkStorageHaveSpace()) {
            showToast("空间不足，需要先释放存储空间");
            return;
        }

        if (!mRestarting) {
            mRecodeTime.resetTime();
        }
        mRestarting = false;

        if (!mCanBeStart) {
            return;
        }
        mCanBeStart = false;

        updateSelect();
    }

    private boolean checkStorageHaveSpace() {
        double size = FileSizeUtil.getFileOrFilesSize(getStoragePathBase(), FileSizeUtil.SIZETYPE_B);
        int limit = SettingDataModel.instance().getVideoStorageSize() * 1000 * 1000;
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

    public void saveImageToGallery(Context context, Bitmap bmp) {
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

    private class RecodeTime {
        public int getTime() {
            int ret = mRecodeTime;
            if (mRecodeTime > mRecoderTimeStep) {
                mRecodeTime -= mRecoderTimeStep;
                ret = mRecoderTimeStep;
            } else {
                mRecodeTime = 0;
            }
            return ret;
        }

        public void resetTime() {
            mRecodeTime = SettingDataModel.instance().getVideoFileTimeSize();
            Log.i(TAG, "resetTime");
        }

        public boolean needRestart() {
            return mRecodeTime != 0;
        }

        // unit:s
        private int mRecodeTime = SettingDataModel.instance().getVideoFileTimeSize();
    }
    private RecodeTime mRecodeTime = new RecodeTime();

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


    public class RecordItem {
        public RecordItem(String path) {
            this.path = path;
            mcurrentTimeMillis = System.currentTimeMillis();
        }
        private String path;
        private long mcurrentTimeMillis = 0;
    }

    public class RecodeTransaction {

        public void addRecordItem(RecordItem recordItem) {
            mRecordItems.add(recordItem);

            gcRecordItem();
        }

        public void gcRecordItem() {
            if (mRecordItems.size() > mMaxSize) {
                RecordItem recordItem = mRecordItems.get(0);
                mRecordItems.remove(0);


                VideoDataModel videoDataModel = new VideoDataModel();
                videoDataModel.setContext(VideoRecordeActivity.this);
                videoDataModel.deleteVideoMetaDataByPath(recordItem.path);

                DeleteFileUtil.delete(recordItem.path);
            }
        }

        private List<RecordItem> mRecordItems = new ArrayList<>();
        private int mMaxSize = 10;
    }

    private RecodeTransaction mRecodeTransaction = new RecodeTransaction();
}
