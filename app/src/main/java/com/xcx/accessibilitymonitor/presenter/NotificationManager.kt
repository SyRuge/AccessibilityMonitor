package com.xcx.accessibilitymonitor.presenter

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.utils.DateUtils

/**
 * Create By Ruge at 2019-03-31
 */
var id=0
fun createNotifyChannel(context: Context){
    var zzz=100
    zzz.apply {  }
}

fun showNotification(context: Context,titile:String=""){

}

fun Activity.showNotification(block:Activity.()->Unit){
    block()
}

private fun showNotification() {
    val notifyManager = MyApp.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var builder = NotificationCompat.Builder(MyApp.appContext, "com.xcx")
    if (id > 1000000) {
        id = 1
    }
    id++
    builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("通知标题 $id")
            .setContentText("通知内容 $id")
            .setLargeIcon(BitmapFactory.decodeResource(MyApp.appContext.resources, R.mipmap.ic_launcher))
            .setAutoCancel(true)
            .setTicker("ruge_ya")

    notifyManager.notify(id, builder.build())
    MyApp.sb.append("${DateUtils.getFormatDate()} showNotification() \r\n")

    /**
     * 启动下一轮AlarmManager 不应该解锁完成再设置 那样如果解锁失败
     * 导致轮询失败
     */
    var curHour = DateUtils.getCurrentHour()
    if (curHour in 5..9) {
//            repeatAlarm()
    }
}

