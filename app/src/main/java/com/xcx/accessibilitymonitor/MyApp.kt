package com.xcx.accessibilitymonitor

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary
import kotlin.properties.Delegates

/**
 * Create By Ruge at 2019-03-31
 */
class MyApp : Application() {


    companion object {
        private var isStopLog = false

        @JvmStatic
        var myApp: MyApp by Delegates.notNull()
        @JvmStatic
        var appContext: Context by Delegates.notNull()
        var runtime = Runtime.getRuntime()
        private val sb = StringBuilder()

        fun appendLog(content: String) {
            if (isStopLog){
                return
            }
            sb.append(content)
        }

        fun getLog() = sb.toString()

        fun startLog(){
            isStopLog = false
        }

        fun stopLog(){
            sb.setLength(0)
            isStopLog = true
        }

    }

    override fun onCreate() {
        super.onCreate()
        myApp = this
        appContext = applicationContext
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}