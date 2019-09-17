package com.xcx.accessibilitymonitor.utils

import android.app.KeyguardManager
import android.content.Context
import com.xcx.accessibilitymonitor.MyApp

/**
 * Create By Ruge at 2019-03-31
 */
class AppUtils {
    companion object {
        fun isLockScreen(): Boolean {
            val keyguardManager = MyApp.appContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            return keyguardManager.isKeyguardLocked
        }
    }
}