package com.xyz.drivingRecorder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SettingDataModel {

    static SettingDataModel settingDataModel = null;

    DBStore mDBStore = new DBStore();

    public static SettingDataModel instance() {
        if (settingDataModel == null) {
            settingDataModel = new SettingDataModel();
        }
        return settingDataModel;
    }

    public void setContext(Context context) {
        mDBStore.setContext(context);

        loadDataFromeDB();
    }

    private final int CollisionDetectionSensitivity = 5;
    private final int VideoFileTimeSize = 60;
    private final int VideoStorageSize = 100;

    private int mCollisionDetectionSensitivity = CollisionDetectionSensitivity;
    private int mVideoFileTimeSize = 60; //s
    private int mVideoStorageSize = 100; //M

    private int loadDataFromeDB() {
        SQLiteDatabase sqliteDatabase = mDBStore.getDBOpenHelper().getWritableDatabase();
        String sql = "select * from table_setting where 1";
        String[] selectionArgs = new String[]{};
        Cursor cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
        if (cursor.moveToNext())// 判断Cursor中是否有数据
        {
            String CollisionDetectionSensitivity = cursor.getString(cursor.getColumnIndex("CollisionDetectionSensitivity"));
            String videoFileTimeSize = cursor.getString(cursor.getColumnIndex("VideoFileTimeSize"));
            String VideoStorageSize = cursor.getString(cursor.getColumnIndex("VideoStorageSize"));

            mCollisionDetectionSensitivity = Integer.parseInt(CollisionDetectionSensitivity);
            mVideoFileTimeSize = Integer.parseInt(videoFileTimeSize);
            mVideoStorageSize = Integer.parseInt(VideoStorageSize);
        }

        return 0;
    }

    public void setCollisionDetectionSensitivity(int i) {
        mCollisionDetectionSensitivity = i;

        SQLiteDatabase sqliteDatabase = mDBStore.getDBOpenHelper().getWritableDatabase();
        String sql = "update table_setting set CollisionDetectionSensitivity=? where 1";
        Object bindArgs[] = new Object[]{""+mCollisionDetectionSensitivity};
        sqliteDatabase.execSQL(sql, bindArgs);
    }

    public int getCollisionDetectionSensitivity() {
        return mCollisionDetectionSensitivity;
    }

    public void setVideoFileTimeSize(int i) {
        mVideoFileTimeSize = i;

        SQLiteDatabase sqliteDatabase = mDBStore.getDBOpenHelper().getWritableDatabase();
        String sql = "update table_setting set VideoFileTimeSize=? where 1";
        Object bindArgs[] = new Object[]{""+mVideoFileTimeSize};
        sqliteDatabase.execSQL(sql, bindArgs);
    }

    public int getVideoFileTimeSize() {
        return mVideoFileTimeSize;
    }

    public void setVideoStorageSize(int i) {
        mVideoStorageSize = i;

        SQLiteDatabase sqliteDatabase = mDBStore.getDBOpenHelper().getWritableDatabase();
        String sql = "update table_setting set VideoStorageSize=? where 1";
        Object bindArgs[] = new Object[]{""+mVideoStorageSize};
        sqliteDatabase.execSQL(sql, bindArgs);
    }

    public int getVideoStorageSize() {
        return mVideoStorageSize;
    }

    public class DBStore {
        private Context context;
        private DBOpenHelper dbOpenHelper;// 创建DBOpenHelper对象
        private SQLiteDatabase sqliteDatabase;// 创建SQLiteDatabase对象

        public void setContext(Context context) {
            this.context = context;
        }

        public DBOpenHelper getDBOpenHelper() {
            String TAG = getClass().getSimpleName();
            if (context == null) {
                Log.e(TAG, "context is null");
            }
            if (dbOpenHelper == null) {
                dbOpenHelper = new DBOpenHelper(context, null, null, 0);// 初始化DBOpenHelper对象
            }

            sqliteDatabase = dbOpenHelper.getWritableDatabase();
            String sql = "select count(*) from table_setting where 1";
            Cursor cursor = sqliteDatabase.rawQuery(sql, null);
            if (cursor.moveToNext())// 判断Cursor中是否有数据
            {
                int count = cursor.getInt(0);
                if (count == 0) {
                    sql = "insert into table_setting(CollisionDetectionSensitivity,VideoStorageSize,VideoFileTimeSize) values (?,?,?)";
                    // 传递过来的name与path分别按顺序替换上面sql语句的两个?，自动转换类型，下同，不再赘述
                    Object bindArgs[] = new Object[]{""+CollisionDetectionSensitivity,""+VideoFileTimeSize,""+VideoStorageSize};
                    // 执行这条无返回值的sql语句
                    sqliteDatabase.execSQL(sql, bindArgs);
                }
            }

            return dbOpenHelper;
        }
    }
}
