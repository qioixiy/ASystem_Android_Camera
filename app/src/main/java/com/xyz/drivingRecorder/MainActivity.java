package com.xyz.drivingRecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    private String TAG = "MainActivity";

    private ListView mListView;
    private BaseAdapter adapter;

    private List<FunctionList.FunctionItem> mFunctionList;//实体类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    case 1:it = new Intent(MainActivity.this, RecorderMainActivity.class); //
                        Bundle b = new Bundle();
                        b.putString("data", str);  //string
                        b.putSerializable("data", str);
                        it.putExtra("data", str);
                        it.putExtras(b);
                        startActivity(it);
                    default:
                        break;
                }
            }
        });
    }
}
