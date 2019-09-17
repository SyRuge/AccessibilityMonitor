package com.xcx.accessibilitymonitor.utils

import android.content.Context
import android.content.SharedPreferences
import com.xcx.accessibilitymonitor.MyApp

/**
 * Create By Ruge at 2019-04-14
 */

fun setDestoryWtiteLog(boolean: Boolean) {
    getSharedPref("other_info").edit().putBoolean("ds_write", boolean).apply()
}

fun getDestoryWtiteLog(): Boolean {
    return getSharedPref("other_info").getBoolean("ds_write", false)
}

/**
 * 密码相关
 */
fun setScreenPassWord(passwd: String) {
    setString("screen_passwd", passwd)
}

fun getScreenPassWord(): String {
    return getSharedPref("user_info").getString("screen_passwd", "000000")
}

/**
 * 轮询时间
 */
fun setSpecialRepeat(minute: Int) {
    setInt("special_minute", minute)
}

fun setNormalRepeat(minute: Int) {
    setInt("normal_minute", minute)
}

fun getSpecialRepeat(): Int {
    return getSharedPref().getInt("special_minute", 5)
}

fun getNormalRepeat(): Int {
    return getSharedPref().getInt("normal_minute", 10)

}


/**
 * 开始时间
 */
fun setStartTimeHour(hour: Int) {
    setInt("start_hour", hour)
}

fun setStartTimeMinute(minute: Int) {
    setInt("start_minute", minute)
}

fun getStartTimeHour(): Int {
    return getSharedPref().getInt("start_hour", 5)
}

fun getStartTimeMinute(): Int {
    return getSharedPref().getInt("start_minute", 30)
}

/**
 * 结束时间
 */
fun setEndTimeHour(hour: Int) {
    setInt("end_hour", hour)
}

fun setEndTimeMinute(minute: Int) {
    setInt("end_minute", minute)
}

fun getEndTimeHour(): Int {
    return getSharedPref().getInt("end_hour", 9)
}

fun getEndTimeMinute(): Int {
    return getSharedPref().getInt("end_minute", 0)
}

fun setString(key: String, value: String) {
    getSharedPref("user_info").edit().putString(key, value).apply()
}

fun setInt(key: String, value: Int) {
    getSharedPref().edit().putInt(key, value).apply()
}

fun getSharedPref(name: String = "alarm_info"): SharedPreferences {
    return MyApp.appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
}
