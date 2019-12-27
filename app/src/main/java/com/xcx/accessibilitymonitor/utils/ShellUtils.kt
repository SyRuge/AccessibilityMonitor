package com.xcx.accessibilitymonitor.utils

import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.service.MyAccessibility
import java.io.DataOutputStream

/**
 * Create By Ruge at 2019-10-26
 */

private const val TAG = "ShellUtils"

/**
 * root权限开启辅助
 */
fun enableMyAccessibility() {
    val cmd1 =
        "settings put secure enabled_accessibility_services  " +
                "${MyApp.myApp.packageName}/${MyAccessibility::class.java.name}"
    val cmd2 = "settings put secure accessibility_enabled 1"
    val cmds = mutableListOf<String>()
    cmds.add(cmd1)
    cmds.add(cmd2)
    execShell(cmds)
}

fun execShell(commands: MutableList<String>) {
    // 获取Runtime对象
    var os: DataOutputStream? = null
    try {
        logd(TAG, "execShell")
        MyApp.appendLog("${DateUtils.getFormatDate()}  execShell \r\n")
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
        loge(TAG, "execShell: error", e)
        MyApp.appendLog("${DateUtils.getFormatDate()} execShell error \r\n")
        MyApp.appendLog("${DateUtils.getFormatDate()} $e \r\n")
    } finally {
        try {
            os?.close()
//                MyApp.process.outputStream.close()
        } catch (e: Exception) {
            loge(TAG, "close stream error", e)
            MyApp.appendLog("${DateUtils.getFormatDate()}  close stream error \r\n")
            MyApp.appendLog("${DateUtils.getFormatDate()}  $e \r\n")

        }
    }

}
