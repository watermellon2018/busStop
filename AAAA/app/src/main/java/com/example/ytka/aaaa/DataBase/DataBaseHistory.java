package com.example.ytka.aaaa.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ytka on 10.04.18.
 */

public class DataBaseHistory extends SQLiteOpenHelper {
    public static final String NAME_DATABASE = "DataBaseHistory";
    public static final int version = 1;
    public static final String TABLE_HISTORY = "History";
    public static final String KEY_ID = "_id";

    public static final String ROUTE = "route";


    public DataBaseHistory(Context context) {
        super(context, NAME_DATABASE, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_HISTORY + "(" +KEY_ID+" integer primary key,"  + ROUTE+ " text" + ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TABLE_HISTORY);
        onCreate(db);
    }
}
