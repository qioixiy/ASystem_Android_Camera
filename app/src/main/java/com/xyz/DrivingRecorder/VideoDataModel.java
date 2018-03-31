package com.xyz.DrivingRecorder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class VideoDataModel {
    private static String TAG = "VideoDataModel";

    public static class VideoMetaData {
        private int id;
        private String name;
        private String path;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private Context context;
    private DBOpenHelper dbOpenHelper;// 创建DBOpenHelper对象
    private SQLiteDatabase sqliteDatabase;// 创建SQLiteDatabase对象

    public VideoDataModel() {
        ;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private DBOpenHelper getDBOpenHelper() {
        if (dbOpenHelper == null) {
            dbOpenHelper = new DBOpenHelper(context, null, null, 0);// 初始化DBOpenHelper对象
        }

        return dbOpenHelper;
    }

    public void insertVideoMetaData(VideoMetaData metaData) {
        sqliteDatabase = getDBOpenHelper().getWritableDatabase();// 以读写方法打开数据库，不仅仅是写，getReadableDatabase()是只读
        String sql = "insert into table_video_metadata(name,path) values (?,?)";
        // 传递过来的name与path分别按顺序替换上面sql语句的两个?，自动转换类型，下同，不再赘述
        Object bindArgs[] = new Object[]{metaData.getName(), metaData.getPath()};
        // 执行这条无返回值的sql语句
        sqliteDatabase.execSQL(sql, bindArgs);
    }

    // 求出表中有多少条数据
    public int getVideoMetaDataSize() {
        sqliteDatabase = getDBOpenHelper().getWritableDatabase();
        String sql = "select count(*) from table_video_metadata where 1";
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
        if (cursor.moveToNext())// 判断Cursor中是否有数据
        {
            return cursor.getInt(0);// 返回总记录数
        }
        return 0;// 如果没有数据，则返回0
    }

    // 查询
    public VideoMetaData queryOneByName(String name) {
        sqliteDatabase = getDBOpenHelper().getWritableDatabase();
        String sql = "select * from table_video_metadata where name=?";
        String[] selectionArgs = new String[]{name};
        Cursor cursor = sqliteDatabase.rawQuery(sql, selectionArgs);
        if (cursor.moveToNext())// 判断Cursor中是否有数据
        {
            // 如果有用户，则把查到的值填充这个用户实体
            VideoMetaData data = new VideoMetaData();
            data.setId(cursor.getInt(cursor.getColumnIndex("id")));
            data.setName(cursor.getString(cursor.getColumnIndex("name")));
            data.setPath(cursor.getString(cursor.getColumnIndex("path")));
            return data;
        }
        return null;// 没有返回null
    }

    // 修改
    public void updateVideoMetaData(VideoMetaData metaData) {
        sqliteDatabase = getDBOpenHelper().getWritableDatabase();
        String sql = "update table_video_metadata set password=? where username=? and isDel=0";
        Object bindArgs[] = new Object[]{metaData.getName(), metaData.getPath()};
        sqliteDatabase.execSQL(sql, bindArgs);
    }

    // 查询所有
    public ArrayList<VideoMetaData> queryAll() {
        ArrayList<VideoMetaData> dataArrayList = new ArrayList<VideoMetaData>();
        try {
            sqliteDatabase = getDBOpenHelper().getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG,  e.toString());
        }
        String sql = "select * from table_video_metadata where 1";
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
        // 游标从头读到尾
        for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
            VideoMetaData data = new VideoMetaData();
            data.setId(cursor.getInt(cursor.getColumnIndex("id")));
            data.setName(cursor.getString(cursor
                    .getColumnIndex("name")));
            data.setPath(cursor.getString(cursor
                    .getColumnIndex("path")));
            dataArrayList.add(data);
        }
        return dataArrayList;
    }

    // 删除
    public void deleteVideoMetaData(int id) {
        try {
            sqliteDatabase = getDBOpenHelper().getWritableDatabase();
            String sql = "delete from table_video_metadata where id=?";
            Object bindArgs[] = new Object[]{id};
            sqliteDatabase.execSQL(sql, bindArgs);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
