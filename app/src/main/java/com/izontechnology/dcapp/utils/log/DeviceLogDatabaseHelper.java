package com.izontechnology.dcapp.utils.log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;


class DeviceLogDatabaseHelper extends SQLiteOpenHelper implements DeviceLogDataSource {

    private static final String TAG = DeviceLogDatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "com.izondc.common.device_logs.db";
    private static final int DATABASE_VERSION = 2;

    private static DeviceLogDatabaseHelper deviceLogDatabaseHelper;
    private SQLiteDatabase database;

    private DeviceLogDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.initializeDatabase();
    }

    private void initializeDatabase() {
        if (database == null)
            database = this.getWritableDatabase();
    }

    static DeviceLogDatabaseHelper getInstance(Context context) {
        if (deviceLogDatabaseHelper == null) {
            synchronized (DeviceLogDatabaseHelper.class) {
                if (deviceLogDatabaseHelper == null)
                    deviceLogDatabaseHelper = new DeviceLogDatabaseHelper(context);
            }
        }
        return deviceLogDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DeviceLogTable.onCreate(db);
        Log.d(TAG, "DeviceLogDatabaseHelper onCreate called.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DeviceLogTable.onUpgrade(db, oldVersion, newVersion);
        Log.d(TAG, "DeviceLogDatabaseHelper onUpgrade called.");
    }

    @Override
    public long getDeviceLogCount() {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        return DeviceLogTable.getCount(database);
    }

    @Override
    public void addDeviceLog(String deviceLog) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        DeviceLogTable.addDeviceLog(database, deviceLog);
    }

    @Override
    public void deleteDeviceLog(List<DeviceLogModel> deviceLogList) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        DeviceLogTable.deleteDeviceLog(database, deviceLogList);
    }

    @Override
    public void deleteAllDeviceLogs() {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        DeviceLogTable.deleteAllDeviceLogs(database);
    }

    @Override
    public List<DeviceLogModel> getDeviceLogs(int batch) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();
        List<DeviceLogModel> deviceLogList = null;

        try {
            deviceLogList = DeviceLogTable.getDeviceLogs(database,batch);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }

        return deviceLogList;
    }

    @Override
    public int getDeviceLogBatchCount() {
        initializeDatabase();

        return DeviceLogTable.getDeviceLogBatchCount(database);
    }

    @Override
    public void clearOldLogs(int expiryTimeInSeconds) {
        initializeDatabase();

        DeviceLogTable.clearOldLogs(database,expiryTimeInSeconds);
    }
}
