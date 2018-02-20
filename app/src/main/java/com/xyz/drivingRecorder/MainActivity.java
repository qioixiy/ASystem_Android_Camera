package com.xyz.drivingRecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";

    private ListView mListView;
    private BaseAdapter adapter;

    private List<FunctionList.FunctionItem> mFunctionList;//实体类

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private MySensorListener mSensorListener;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListView();
        initSensorInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册传感器监听函数
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 注销监听函数
        mSensorManager.unregisterListener(mSensorListener);
    }

    private void initSensorInfo() {
        mSensorListener = new MySensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.function_listView);

        mFunctionList = FunctionList.instance().get();

        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return mFunctionList.size();//数目
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View view;

                if (convertView == null) {
                    //因为getView()返回的对象，adapter会自动赋给ListView
                    view = inflater.inflate(R.layout.item_main, null);
                } else {
                    view = convertView;
                    Log.i(TAG, "有缓存，不需要重新生成" + position);
                }
                TextView tTextView1 = (TextView) view.findViewById(R.id.textViewName);
                tTextView1.setText(mFunctionList.get(position).getName());

                TextView tTextView2 = (TextView) view.findViewById(R.id.textViewContent);
                tTextView2.setText(mFunctionList.get(position).getContext());

                return view;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }
        };
        mListView.setAdapter(adapter);

        //获取当前ListView点击的行数，并且得到该数据
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tTextView1 = (TextView) view.findViewById(R.id.textViewName);
                String str = tTextView1.getText().toString();//得到数据
                //Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0:
                        Intent it = new Intent(MainActivity.this, VideoManageActivity.class);
                        startActivity(it);
                        break;
                    case 1:
                        startRecorderMainActivity("active_normal");
                        break;
                    case 2:
                        startSettingActivity();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void startRecorderMainActivity(String str) {

        Intent it = new Intent(MainActivity.this, VideoRecordeActivity.class); //
        Bundle b = new Bundle();
        b.putString("data", str);  //string
        b.putSerializable("data", str);
        it.putExtra("data", str);
        it.putExtras(b);
        startActivity(it);
    }

    private void startSettingActivity() {
        Intent it = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(it);
    }

    class MySensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
            //Log.i(TAG, "onSensorChanged: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);

            TextView mSensorInfoA = (TextView) findViewById(R.id.main_textview_sensor_info_a);

            mSensorInfoA.setText("" + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);

            int sensorType = event.sensor.getType();
            float[] values = event.values;
            if (sensorType == Sensor.TYPE_ACCELEROMETER){
                int limit = SettingDataModel.getCollisionDetectionSensitivity();
                if (Math.abs(values[0]) > limit || Math.abs(values[1]) > limit || Math.abs(values[2]) > limit){
                    mVibrator.vibrate(100);
                    //进行手机晃动的监听  ，可以在这里实现 intent 等效果

                    startRecorderMainActivity("active_trigger");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "onAccuracyChanged");
        }
    }
}
