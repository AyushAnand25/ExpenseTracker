package com.expensetracker.expensetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqlHelper extends SQLiteOpenHelper {

    public SqlHelper(@Nullable Context context) {
        super(context, "Database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String create1 = "CREATE TABLE CATEGORIES (CategoryId VARCHAR PRIMARY KEY, Category VARCHAR, IconUrl VARCHAR, Type VARCHAR)";
        db.execSQL(create1);

        String create2 = "CREATE TABLE BANKS (Name VARCHAR PRIMARY KEY, IconUrl VARCHAR)";
        db.execSQL(create2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
