package com.xyz.DrivingRecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBOpenHelper";
    public static final int VERSION = 1;

    public DBOpenHelper(Context context, String name, CursorFactory factory,
                        int version) {
        //向系统申请一个SqliteTest.db文件存这个数据库，其中VERSION是数据库版本。
        super(context, "video_metadata.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase) {
        String sql=
                "create table if not exists table_video_metadata("+
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
                        "name VARCHAR(255),"+
                        "path VARCHAR(1024))";//如果初次运行，建立一张表，建表的时候注意，自增是AUTOINCREMENT，而不是mysql的AUTO_INCREMENT
        sqliteDatabase.execSQL(sql);
        sql = "create table if not exists table_setting("+
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"+
                        "CollisionDetectionSensitivity VARCHAR(255),"+
                        "VideoStorageSize VARCHAR(255),"+
                        "VideoFileTimeSize VARCHAR(255),"+
                        "VideoFileTimeStepSize VARCHAR(255))";
        sqliteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        //这里是更新数据库版本时所触发的方法
    }
}