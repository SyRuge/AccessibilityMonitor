package com.xcx.accessibilitymonitor.utils

import android.content.Context
import android.os.PowerManager
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create By Ruge at 2019-04-12
 */

private const val TAG = "LockScreenUtils"

suspend fun unlockScreen() {
    val ps = MyApp.appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    //黑屏界面 此时需要先点亮屏幕
    val isScreenOn = ps.isInteractive
    if (!isScreenOn) {
        val wl = ps.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.FULL_WAKE_LOCK,
            MyApp.appContext.getString(R.string.wake_lock_tag)
        )
        wl.acquire()
        MainScope().launch {
            delay(5000)
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


