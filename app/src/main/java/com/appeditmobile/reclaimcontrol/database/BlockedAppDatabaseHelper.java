package com.appeditmobile.reclaimcontrol.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Date;
import java.util.ArrayList;

public class BlockedAppDatabaseHelper extends SQLiteOpenHelper {

    // Define database name and version
    private static final String DATABASE_NAME = "BlockedApps.db";
    private static final int DATABASE_VERSION = 5;

    // Define table name and columns
    private static final String TABLE_NAME = "BlockedApp";
    private static final String COLUMN_PACKAGE_NAME = "packageName";
    private static final String COLUMN_LAUNCH_COUNT = "launchCount";
    private static final String COLUMN_LAST_ACCESSED_DATE = "lastAccessedDate";
    private static final String COLUMN_BLOCK_TYPE = "blockType";
    private static final String COLUMN_LAUNCH_ATTEMPT_COUNT = "attemptCount";

    // Constructor
    public BlockedAppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the BlockedApp table
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_PACKAGE_NAME + " TEXT PRIMARY KEY, "
                + COLUMN_LAUNCH_COUNT + " INTEGER, " + COLUMN_LAST_ACCESSED_DATE + " INTEGER, "
                + COLUMN_BLOCK_TYPE + " INTEGER, " + COLUMN_LAUNCH_ATTEMPT_COUNT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop the BlockedApp table and recreate it
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    // Method to insert a new BlockedApp record into the database
    public void insertBlockedApp(BlockedApp blockedApp) {
        // Get a writable database
        SQLiteDatabase db = getWritableDatabase();

        // Create a ContentValues object to hold the BlockedApp data
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME, blockedApp.getPackageName());
        values.put(COLUMN_LAUNCH_COUNT, blockedApp.getLaunchCount());
        values.put(COLUMN_LAST_ACCESSED_DATE, blockedApp.getLastAccessedDate().getTime());
        values.put(COLUMN_BLOCK_TYPE, blockedApp.getBlockType());
        values.put(COLUMN_LAUNCH_ATTEMPT_COUNT, blockedApp.getAttemptCount());

        // Insert the data into the BlockedApp table
        long isInserted = db.insert(TABLE_NAME, null, values);
        Log.d("DBTEST","inserted to the database id is " + Long.toString(isInserted));

        // Close the database
        db.close();
    }

    // Method to update an existing BlockedApp record in the database
    public void updateBlockedApp(BlockedApp blockedApp) {
        // Get a writable database
        SQLiteDatabase db = getWritableDatabase();

        // Create a ContentValues object to hold the BlockedApp data
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAUNCH_COUNT, blockedApp.getLaunchCount());
        values.put(COLUMN_LAST_ACCESSED_DATE, blockedApp.getLastAccessedDate().getTime());
        values.put(COLUMN_BLOCK_TYPE, blockedApp.getBlockType());
        values.put(COLUMN_LAUNCH_ATTEMPT_COUNT, blockedApp.getAttemptCount());

        // Update the data in the BlockedApp table
        db.update(TABLE_NAME, values, COLUMN_PACKAGE_NAME + " = ?",
                new String[] { blockedApp.getPackageName() });

        // Close the database
        db.close();
    }

    // Method to delete a BlockedApp record from the database
    public void deleteBlockedApp(String packageName) {
        // Get a writable database
        SQLiteDatabase db = getWritableDatabase();

        // Delete the record from the BlockedApp table
        db.delete(TABLE_NAME, COLUMN_PACKAGE_NAME + " = ?", new String[] { packageName });

        // Close the database
        db.close();
    }

    // Method to get a BlockedApp record from the database by package name
    public BlockedApp getBlockedApp(String packageName) {
        // Get a readable database
        SQLiteDatabase db = getReadableDatabase();

        // Define a cursor to hold the query result
        Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_PACKAGE_NAME, COLUMN_LAUNCH_COUNT,
                        COLUMN_LAST_ACCESSED_DATE, COLUMN_BLOCK_TYPE, COLUMN_LAUNCH_ATTEMPT_COUNT }, COLUMN_PACKAGE_NAME + " = ?",
                new String[] { packageName }, null, null, null, null);

        // Create a BlockedApp object from the query result
        BlockedApp blockedApp = null;
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_PACKAGE_NAME));
            @SuppressLint("Range") int launchCount = cursor.getInt(cursor.getColumnIndex(COLUMN_LAUNCH_COUNT));
            @SuppressLint("Range") long lastAccessedDate = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_ACCESSED_DATE));
            @SuppressLint("Range") int blockType = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOCK_TYPE));
            @SuppressLint("Range") int attemptCount = cursor.getInt(cursor.getColumnIndex(COLUMN_LAUNCH_ATTEMPT_COUNT));
            blockedApp = new BlockedApp(name, launchCount, new Date(lastAccessedDate), blockType, attemptCount);
        }

        // Close the cursor and database
        cursor.close();
        db.close();

        // Return the BlockedApp object
        return blockedApp;
    }

    // Method to retrieve all BlockedApp records from the database as an array of BlockedApp objects
    public BlockedApp[] getAllBlockedApps() {
        // Get a readable database
        SQLiteDatabase db = getReadableDatabase();

        // Define a cursor to hold the query result
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // Create an array of BlockedApp objects from the query result
        ArrayList<BlockedApp> blockedApps = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_PACKAGE_NAME));
            @SuppressLint("Range") int launchCount = cursor.getInt(cursor.getColumnIndex(COLUMN_LAUNCH_COUNT));
            @SuppressLint("Range") long lastAccessedDate = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_ACCESSED_DATE));
            @SuppressLint("Range") int blockType = cursor.getInt(cursor.getColumnIndex(COLUMN_BLOCK_TYPE));
            @SuppressLint("Range") int attemptCount = cursor.getInt(cursor.getColumnIndex(COLUMN_LAUNCH_ATTEMPT_COUNT));
            BlockedApp blockedApp = new BlockedApp(name, launchCount, new Date(lastAccessedDate),blockType,attemptCount);
            blockedApps.add(blockedApp);
        }

        // Close the cursor and database
        cursor.close();
        db.close();
        Log.d("DBTEST","length of array is " + Integer.toString(blockedApps.size()));
        // Convert the ArrayList to an array of BlockedApp objects and return it
        return blockedApps.toArray(new BlockedApp[blockedApps.size()]);
    }
}
