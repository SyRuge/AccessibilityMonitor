package com.xcx.accessibilitymonitor.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.presenter.startCollectEnergy
import com.xcx.accessibilitymonitor.utils.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Create By Ruge at 2019-03-31
 */
class MyAccessibility : AccessibilityService() {

    private val TAG = "MyAccessibility"

    private var preTime = 0L
    private val scope = MainScope()

    val ALIPAY_PACKAGENAME = "com.eg.android.AlipayGphone"
    val ALIPAY_LOGIN = "com.eg.android.AlipayGphone.AlipayLogin"
    val ALIPAY_FOREST = "com.alipay.mobile.nebulax.integration.mpaas.activity.NebulaActivity\$Main"
    val UC_WEBKIT = "com.uc.webkit.be"
    val ALIPAY_WEBVIEW = "com.uc.webview.export.WebView"


    override fun onInterrupt() {
        logd(TAG, "onInterrupt: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            logd(TAG, "onStartCommand: $action")
            if (!action.isNullOrEmpty() && action == getString(R.string.collect_energy_action)) {
                var t = System.currentTimeMillis()
                if (t - preTime >= 15000) {
                    preTime = t
                    MyApp.startLog()
                    MyApp.appendLog("${DateUtils.getFormatDate()}  onStartCommand $action \r\n")
                    //解锁
                    startCollectEnergy(MyApp.appContext)
                } else {
                    MyApp.appendLog("${DateUtils.getFormatDate()}  onStartCommand receive repeat $action \r\n")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null || event.packageName == null || event.className == null) {
            return
        }

        val packageName = event.packageName.toString()
        val className = event.className.toString()


        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            if (hour !in getStartTimeHour()..getEndTimeHour()) {
                logd(TAG, "onAccessibilityEvent: alipay $packageName $className")
                MyApp.appendLog(
                    "${DateUtils.getFormatDate()}  TYPE_WINDOW_STATE_CHANGED or TYPE_WINDOW_CONTENT_CHANGED " +
                            "$packageName $className \r\n"
                )
                return
            }

            gotoH5Activity(packageName, className)

            if (packageName == ALIPAY_PACKAGENAME && (className == ALIPAY_FOREST || className == UC_WEBKIT)) {
                scope.launch {
                    delay(3 * 1000)
                    var nodeInfo = rootInActiveWindow

                    if (nodeInfo != null) {
                        MyApp.appendLog("${DateUtils.getFormatDate()}  nodeInfo!=null \r\n")
                        for (i in 0 until nodeInfo.childCount) {
                            val child = nodeInfo.getChild(i)
                            logd(TAG, "child.className: ${child.className}")
                            if (ALIPAY_WEBVIEW == child.className) {
                                loge(TAG, "找到蚂蚁森林的 webView count = " + child.childCount)
//                                        collectFriendEnergy(child)
                                findEveryViewNode(child)
                                break
                            }
                        }
                    } else {
                        logd(TAG, "alipayPolicy = null")
                        MyApp.appendLog("${DateUtils.getFormatDate()}  alipayPolicy=null \r\n")

                    }

                }

            }
        }
    }

    private fun findEveryViewNode(node: AccessibilityNodeInfo?) {

        if (node == null || node.childCount <= 0) {
            return
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            // 有时 child 为空


            val className = child.className.toString()
            if ("android.widget.Button" == className) {
                logd(
                    TAG, "Button 的节点数据 " +
                            "text = ${child.text}, " +
                            "descript = ${child.contentDescription}, " +
                            "className = ${child.className}, " +
                            "resId = ${child.viewIdResourceName}"
                )


                MyApp.appendLog(
                    "${DateUtils.getFormatDate()} Button 的节点数据: text = ${child.text}, " +
                            "descript = ${child.contentDescription}, className = ${child.className}, \r\n"
                )

                /**
                 * 好友的能量不能收取，因为支付宝在onTouch事件中return true,导致不会触发OnClick方法
                 * todo 第二版准备把偷能量加上
                 * 但是支付宝中的蚂蚁森林可以收取自己的能量
                 */

                var dec = child.contentDescription
                var text = child.text

                //关闭蒙层
                if (!dec.isNullOrEmpty() && dec.contains("关闭蒙层")
                    || !text.isNullOrEmpty() && text.contains("关闭蒙层")
                ) {
                    closeMongolia(child)
                    return
                }

                if (!dec.isNullOrEmpty() && dec.contains("收集能量")
                    || !text.isNullOrEmpty() && text.contains("收集能量")
                ) {
//                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    logd(TAG, "能量球 成功点击")
                    MyApp.appendLog("${DateUtils.getFormatDate()}  准备收集能量... \r\n")
                    collectEnergy(child)
                }

            }

            // 递归调用
            findEveryViewNode(child)
        }
    }

    /**
     * 关闭支付宝活动蒙层
     */
    private fun closeMongolia(info: AccessibilityNodeInfo) {
        val rect = Rect()
        info.getBoundsInScreen(rect)

        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            .build()

        handleGesture(gestureDescription) {
            onCompleted {
                loge(TAG, "close onCompleted")
                Toast.makeText(applicationContext, "need restart", Toast.LENGTH_SHORT).show()
                MyApp.appendLog("${DateUtils.getFormatDate()}  close mongolia... \r\n")
                restartCollect()
            }
            onCancelled {
                loge(TAG, "close onCancelled")
                Toast.makeText(applicationContext, "close 为啥取消了呢", Toast.LENGTH_SHORT).show()
                MyApp.appendLog("${DateUtils.getFormatDate()}  cancel mongolia... \r\n")
                restartCollect()
            }
        }
    }

    private fun restartCollect() {
        scope.launch {
            delay(1500)
            loge(TAG, "restartCollect: ")
            performGlobalAction(GLOBAL_ACTION_BACK)
            scope.cancel()
        }
    }

    private fun collectEnergy(info: AccessibilityNodeInfo) {
        val rect = Rect()
        info.getBoundsInScreen(rect)

        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            .build()

        handleGesture(gestureDescription) {
            onCompleted {
                loge(TAG, "onCompleted")
                Toast.makeText(applicationContext, "收能量了", Toast.LENGTH_SHORT).show()
                MyApp.appendLog("${DateUtils.getFormatDate()}  收集能量了... \r\n")
            }
            onCancelled {
                loge(TAG, "onCancelled")
                Toast.makeText(applicationContext, "为啥取消了呢", Toast.LENGTH_SHORT).show()
                MyApp.appendLog("${DateUtils.getFormatDate()}  为啥取消了呢... \r\n")
            }
        }
    }

    private fun gotoH5Activity(packageName: String, className: String) {

        if (packageName == ALIPAY_PACKAGENAME && className == ALIPAY_LOGIN) {
            MyApp.appendLog("${DateUtils.getFormatDate()}  gotoH5Activity \r\n")
            logd(TAG, "gotoH5Activity")
            rootInActiveWindow?.apply {
                val nodeInfos = findAccessibilityNodeInfosByText("蚂蚁森林")
                nodeInfos?.also {
                    for (node in it) {
                        var parent = node.parent
                        if (parent != null && parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            break
                        }
                    }
                }
            }
        }
    }
}