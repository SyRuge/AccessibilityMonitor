package com.xcx.accessibilitymonitor.utils

import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Create By Ruge at 2019-04-13
 */

fun writeLogToFile(fileName: String, content: String) {
    Observable.just(content)
        .map {
            val sb = StringBuilder()
            sb.append("\r\n")
                .append("\r\n")
                .append("\r\n")
                .append("本次log开始时间: ${DateUtils.getFormatDate()}\r\n")
                .append(it)
            execWrite(sb.toString(), fileName)
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            if (it) {
                if (fileName.contains("unlock")) {
                    MyApp.unlockLog.setLength(0)
                } else {
                    MyApp.sb.setLength(0)
                }
                Toast.makeText(MyApp.appContext, "success!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(MyApp.appContext, "failed!", Toast.LENGTH_SHORT).show()
            }
        }
}

private fun execWrite(it: String, fileName: String): Boolean {
    var path = Environment.getExternalStorageDirectory().absolutePath + "/xcx"

    var out: FileOutputStream? = null
    try {
        var fileDir = File(path)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
//            var file = File(path + "xcx_log.txt")
        val f = "$path/$fileName.txt"

        out = FileOutputStream(f, true)
        out.write(it.toByteArray())
//            out.flush()
        out.close()
        Log.d("xcx", "realWrite: success")
        return true

    } catch (e: IOException) {
        Log.e("xcx", "realWrite: ", e)
        return false
    } finally {
        try {
            out?.close()
        } catch (e: IOException) {
            Log.e("xcx", "close: ", e)
        }
    }
}
