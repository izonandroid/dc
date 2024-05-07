package com.izontechnology.dcapp.downloadmanagerplus.utils;

import com.izontechnology.dcapp.BuildConfig;

import java.lang.reflect.Field;



public class Log {
    private static String TAG = "DownloadManagerPlusTag";

    public static <T> void i(T msg) {
        if (BuildConfig.DEBUG)
            android.util.Log.i(TAG, "" + msg);
    }

    public static <T> void print(T msg) {
        android.util.Log.e(TAG, "" + msg);
    }

    public static String printItems(Object obj) {
        if(obj==null){
            Log.i("Object is null");
            return null;
        }
        Log.i("*** " + obj.getClass().getSimpleName() + " Values ***");
        String result = "";
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = null;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Log.i(name + ": " + value);
            result = result + name + ": " + value + "\n";
        }
        if (result.isEmpty())
            return null;
        else
            return result;
    }
}
