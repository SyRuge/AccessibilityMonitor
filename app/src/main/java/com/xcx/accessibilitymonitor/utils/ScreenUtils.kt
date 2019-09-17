package com.xcx.accessibilitymonitor.utils

import com.xcx.accessibilitymonitor.MyApp

/**
 * Create By Ruge at 2019-05-15
 */

fun getScreenWidth(): Int {
    return MyApp.appContext.resources.displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return MyApp.appContext.resources.displayMetrics.heightPixels
}
