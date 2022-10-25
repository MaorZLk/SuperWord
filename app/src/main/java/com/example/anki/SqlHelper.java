package com.example.anki;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// This class manages the SQLite Database,
// it's main purpose is mainly for creating the database itself
public class SqlHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Anki.db";

    public static String TABLE_NAME;

    // All the column names
    private static final String ID = "id"; // This column is only needed in order to find certain words in the table

    public static final String COLUMN_WORD = "word"; // the word itself
    public static final String COLUMN_DESCRIPTION = "description"; // the description or meaning of the word
    public static final String COLUMN_LEARNER_HELPER = "learner_helper"; // The notes that the user can write to himself
    public static final String COLUMN_LAST_TIME_REVISITED = "last_time_revisited"; // the last time that the word was trained
    public static final String COLUMN_TIME_BETWEEN_REVIEWS = "time_between_reviews"; // the time between the current review of the user, and the time when the word will be ready for another review
    public static final String COLUMN_NEXT_REVIEW_TIME = "next_review_time"; // the next time the word will be ready for reviewing
    public static final String COLUMN_REVIEWED_TIMES = "reviewed_times"; // the amount of times the word has been trained on
    public static final String COLUMN_REVIEWED_CORRECT_TIMES = "correct_reviewd_times"; // the amount of times the user was right while training on the word

    public static String SQL_CREATE_ENTRIES;

    public SqlHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db, String table_name){
        // Create the table it self, with the name the user has chosen
        String TABLE_NAME = table_name;
        String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + table_name + " (" + SqlHelper.ID + " INTEGER PRIMARY KEY," +
                SqlHelper.COLUMN_WORD + " TEXT," + SqlHelper.COLUMN_DESCRIPTION + " TEXT," + SqlHelper.COLUMN_LEARNER_HELPER + " TEXT," +
                SqlHelper.COLUMN_LAST_TIME_REVISITED + " TEXT," + SqlHelper.COLUMN_TIME_BETWEEN_REVIEWS + " INTEGER," + SqlHelper.COLUMN_NEXT_REVIEW_TIME + " TEXT, " +
                SqlHelper.COLUMN_REVIEWED_TIMES + " INTEGER, " + SqlHelper.COLUMN_REVIEWED_CORRECT_TIMES + " INTEGER)";
        System.out.println("SQL_CREATE_ENTRIES ==" + SQL_CREATE_ENTRIES); // for error checking
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onCreate(SQLiteDatabase db) {
        if (SQL_CREATE_ENTRIES != null)
            db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + SqlHelper.TABLE_NAME;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Cursor getAllData(){
        SQLiteDatabase db = MainScreen.mainDB;
        Cursor res = db.rawQuery("SELECT * FROM " + MainScreen.table_name, null);
        return res;
    }
}
