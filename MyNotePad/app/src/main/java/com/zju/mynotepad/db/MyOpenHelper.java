package com.zju.mynotepad.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zju.mynotepad.util.CommonUtil;

import java.util.Date;

/**
 * 描述：数据库帮助类
 */

public class MyOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "note.db";// 数据库文件名
    private final static int DB_VERSION = 1;// 数据库版本

    public MyOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建分类表
        db.execSQL("create table db_group(g_id integer primary key autoincrement, " +
                "g_name varchar, g_order integer, g_color varchar, g_encrypt integer," +
                "g_create_time datetime, g_update_time datetime,g_url)");
        //创建笔记表
        db.execSQL("create table db_note(n_id integer primary key autoincrement, n_title varchar, " +
                "n_content varchar, n_group_id integer, n_group_name varchar, n_type integer, " +
                "n_bg_color varchar, n_encrypt integer, n_create_time datetime," +
                "n_update_time datetime,n_url)");
        db.execSQL("insert into db_group(g_name, g_order, g_color, g_encrypt, g_create_time, g_update_time,g_url) " +
                "values(?,?,?,?,?,?,?)", new String[]{"默认笔记", "1", "#FFFFFF", "0", CommonUtil.date2string(new Date()),CommonUtil.date2string(new Date()),"https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=1269978988,2084254240&fm=26&gp=0.jpg"});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
