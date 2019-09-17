package com.xcx.accessibilitymonitor.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.DataOutputStream
import java.util.concurrent.TimeUnit

/**
 * Create By Ruge at 2019-04-12
 */

fun unlockScreen() {
    val ps = MyApp.appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    //黑屏界面 此时需要先点亮屏幕
    val isScreenOn = ps.isInteractive
    if (!isScreenOn) {
        val wl = ps.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.FULL_WAKE_LOCK, MyApp.appContext.getString(R.string.wake_lock_tag)
        )
        wl.acquire()
        Observable.timer(5 * 1000, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                wl.release()
            }
        realUnlockScreen(true)
    } else {
        realUnlockScreen(false)
    }
}

private fun realUnlockScreen(needWaitScreenOn: Boolean) {
    val commands = mutableListOf<String>()
    commands.clear()
    if (needWaitScreenOn) {
        commands.add("sleep 1.5")
    }
    val pd = getScreenPassWord().toCharArray()
    commands.add("input swipe 100 1774 100 874")
    commands.add("sleep 1 && input text ${pd[0]}")
    commands.add("sleep 0.1 && input text ${pd[1]}")
    commands.add("sleep 0.1 && input text ${pd[2]}")
    commands.add("sleep 0.1 && input text ${pd[3]}")
    commands.add("sleep 0.1 && input text ${pd[4]}")
    commands.add("sleep 0.1 && input text ${pd[5]}")
    //解锁
    execShell(commands)
}

private fun execShell(commands: MutableList<String>) {
    // 获取Runtime对象
    var os: DataOutputStream? = null
    try {
        Log.e("xyjk", "execShell")
        MyApp.sb.append("${DateUtils.getFormatDate()}  execShell \r\n")
        // 获取root权限，这里大量申请root权限会导致应用卡死，可以把Runtime和Process放在Application中初始化
        // 实际测试不行 必须每次申请
        val process = MyApp.runtime.exec("su")

        os = DataOutputStream(process.outputStream)

        for (command in commands) {
            if (command.isNullOrEmpty()) {
                continue
            }
            // do not use os.writeBytes(command), avoid chinese charset
            // error
            os.write(command.toByteArray())
            os.writeBytes("\n")
            os.flush()
        }

        os.writeBytes("exit\n")
        os.flush()
        process.waitFor()
    } catch (e: Exception) {
        Log.e("xyjk", "execShell: error", e)
        MyApp.sb.append("${DateUtils.getFormatDate()} execShell error \r\n")
        MyApp.sb.append("${DateUtils.getFormatDate()} $e \r\n")
    } finally {
        try {
            os?.close()
//                MyApp.process.outputStream.close()
        } catch (e: Exception) {
            Log.e("xyjk", "close stream error", e)
            MyApp.sb.append("${DateUtils.getFormatDate()}  close stream error \r\n")
            MyApp.sb.append("${DateUtils.getFormatDate()}  $e \r\n")

        }
    }

}

