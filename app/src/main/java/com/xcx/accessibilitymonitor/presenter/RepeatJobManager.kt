package com.xcx.accessibilitymonitor.presenter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.service.MyAccessibility
import com.xcx.accessibilitymonitor.utils.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Create By Ruge at 2019-04-16
 */

private val TAG = "RepeatJobManager"

fun startCollectEnergy(context: Context) {
    /**
     * 启动下一轮AlarmManager 不应该解锁完成再设置 那样如果解锁失败
     * 导致轮询失败
     */
    val c = Calendar.getInstance()
    if (c.get(Calendar.HOUR_OF_DAY) in getStartTimeHour()..getEndTimeHour()) {
        repeatAlarm(context)
    }
    MainScope().launch {
        unlockScreen()
        delay(1700)
        isNeedToGoAliPay()
    }

}

private fun repeatAlarm(context: Context) {

    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)

    if (hour == getEndTimeHour() && min >= 45) {
        startDayRepeatAlarm(context)
        //写日志到文件
        writeLogToFile(context.getString(R.string.log_name), MyApp.getLog())
    } else {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var anHour = 10 * 60 * 1000   // 10分钟唤醒一次
        // 7~8点特殊轮询唤醒时间
        if (hour == 7) {
            anHour = 5 * 60 * 1000
        }
        val triggerAtTime = SystemClock.elapsedRealtime() + anHour

        val intent = Intent(context, MyAccessibility::class.java).apply {
            action = context.getString(R.string.collect_energy_action)
        }
        val pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi)//开启提醒
        MyApp.appendLog("${DateUtils.getFormatDate()} 设置重复闹钟 \r\n")

    }

}

private fun startDayRepeatAlarm(context: Context) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, getStartTimeHour())
        set(Calendar.MINUTE, getStartTimeMinute())
        set(Calendar.SECOND, 0)
    }

    // 明天继续执行
    val nextDay = calendar.timeInMillis + 24 * 60 * 60 * 1000
    val intent = Intent(context, MyAccessibility::class.java).apply {
        action = context.getString(R.string.collect_energy_action)
    }
    val pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDay, pi)//开启提醒
    MyApp.appendLog("${DateUtils.getFormatDate()} 设置明天的闹钟 \r\n")
}


fun isNeedToGoAliPay() {
    var curHour = DateUtils.getCurrentHour()
    if (curHour in getStartTimeHour()..getEndTimeHour()) {
        MyApp.appendLog("${DateUtils.getFormatDate()}  startToAliPay \r\n")
        loge(TAG, "startToAliPay")
        startAliPay()
//            repeatAlarm()
    } else {
        loge(TAG, "不在时间段内解锁呢: ")
        MyApp.appendLog("${DateUtils.getFormatDate()} 不在时间段内解锁呢 \r\n")
    }
}

/**
 * 启动支付宝界面
 * adb shell am start com.eg.android.AlipayGphone/com.eg.android.AlipayGphone.AlipayLogin
 */
fun startAliPay() {
    /*val intent = Intent().apply {
        setPackage("com.eg.android.AlipayGphone")
        setClassName("com.eg.android.AlipayGphone", "com.eg.android.AlipayGphone.AlipayLogin")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    MyApp.appContext.startActivity(intent)*/
    val commands = mutableListOf<String>()
    commands.clear()
    commands.add("am start -S com.eg.android.AlipayGphone/.AlipayLogin")
    execShell(commands)
}

fun startFirstAlarm() {

    // Schedule the first alarm!
    val am = MyApp.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val curHour = DateUtils.getCurrentHour()

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, getStartTimeHour())
        set(Calendar.MINUTE, getStartTimeMinute())
        set(Calendar.SECOND, 0)
    }
    var nextDay = calendar.timeInMillis + 24 * 60 * 60 * 1000
    if (curHour in 0..5) {//凌晨时间可以直接设置偷能量
        nextDay = calendar.timeInMillis
    }
    // 明天继续执行
    val intent = Intent(MyApp.appContext, MyAccessibility::class.java).apply {
        action = MyApp.appContext.getString(R.string.collect_energy_action)
    }
    val pi = PendingIntent.getService(MyApp.appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDay, pi)//开启提醒
    Toast.makeText(MyApp.appContext, "定时器开启成功!", Toast.LENGTH_SHORT).show()
}

fun startTestAlarm(activity: AppCompatActivity, hourOfDay: Int, minute: Int) {

    // Schedule the alarm!
    val am = MyApp.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    var nextDay = calendar.timeInMillis
    val intent = Intent(MyApp.appContext, MyAccessibility::class.java).apply {
        action = MyApp.appContext.getString(R.string.collect_energy_action)
    }
    val pi = PendingIntent.getService(MyApp.appContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDay, pi)//开启提醒
    Toast.makeText(MyApp.appContext, "测试定时器开启成功!", Toast.LENGTH_SHORT).show()
}