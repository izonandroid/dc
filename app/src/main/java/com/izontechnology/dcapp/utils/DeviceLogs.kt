package com.izontechnology.dcapp.utils

import android.util.Log

/**
 * Purpose: Class for displaying logs. So we can this method for displaying logs.
 */
object DeviceLogs {

    fun v(tag: String?, msg: String?) {
        if (msg != null) {
            Log.v(tag, msg)
        }
    }

    fun i(tag: String?, msg: String?) {
        if (msg != null) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String?, msg: String?) {
        if (msg != null) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String?, msg: String?) {
        if (msg != null) {
            Log.e(tag, msg)
        }
    }
}