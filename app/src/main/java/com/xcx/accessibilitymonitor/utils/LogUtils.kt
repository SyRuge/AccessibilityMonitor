package com.xcx.accessibilitymonitor.utils

import android.util.Log

/**
 * Create By Ruge at 2019-09-26
 */

val LOG_TAG = "xcx"


fun logd(tag: String = LOG_TAG, msg: String) {
    Log.d(tag, msg)
}

fun logi(tag: String = LOG_TAG, msg: String) {
    Log.i(tag, msg)
}

fun loge(tag: String = LOG_TAG, msg: String, tr: Throwable? = null) {
    if (tr == null) {
        Log.e(tag, msg)
    } else {
        Log.e(tag, msg, tr)
    }
}