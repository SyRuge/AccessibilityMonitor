package com.xcx.accessibilitymonitor.utils

import android.os.Environment
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Create By Ruge at 2019-04-13
 */

private val TAG = "FileUtils"

fun writeLogToFile(fileName: String, content: String) {

    val scope = MainScope()
    scope.launch {
        val job = async(Dispatchers.IO) {
            val sb = StringBuilder()
            sb.append("\r\n")
                .append("\r\n")
                .append("\r\n")
                .append("本次log开始时间: ${DateUtils.getFormatDate()}\r\n")
                .append(content)
            execWrite(sb.toString(), fileName)
        }
        val isWrite = job.await()
        MyApp.stopLog()
        if (isWrite) {
            Toast.makeText(MyApp.appContext, "success!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(MyApp.appContext, "failed!", Toast.LENGTH_SHORT).show()
        }
    }

}

private fun execWrite(content: String, fileName: String): Boolean {
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
        out.write(content.toByteArray())
//            out.flush()
        out.close()
        logd(TAG, "realWrite: success")
        return true

    } catch (e: IOException) {
        loge(TAG, "realWrite: ", e)
        return false
    } finally {
        try {
            out?.close()
        } catch (e: IOException) {
            loge(TAG, "close: ", e)
        }
    }
}
