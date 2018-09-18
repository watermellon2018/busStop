package com.example.ytka.aaaa.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ytka on 04.04.18.
 */

public class DBHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "Favor";
        public static final int version = 1;
        public static final String TABLE_FAVOR_ROUTE = "favorRoute";

        public static final String KEY_END_LOCATION = "endLocation";


        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_FAVOR_ROUTE + "("  + KEY_END_LOCATION+ " text" + ")");
        }
//+KEY_ID+" integer primary key," + KEY_BEGIN_LOCATION + " text,"
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists "+ TABLE_FAVOR_ROUTE);
            onCreate(db);
            //если верссия изменилась, удаляем, обновляем

        }
    }