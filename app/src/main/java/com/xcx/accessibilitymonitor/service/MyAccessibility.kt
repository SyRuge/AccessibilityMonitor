package com.xcx.accessibilitymonitor.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.presenter.startCollectEnergy
import com.xcx.accessibilitymonitor.rxbus.RxBus
import com.xcx.accessibilitymonitor.utils.DateUtils
import com.xcx.accessibilitymonitor.utils.getEndTimeHour
import com.xcx.accessibilitymonitor.utils.getScreenHeight
import com.xcx.accessibilitymonitor.utils.getStartTimeHour
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.DataOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Create By Ruge at 2019-03-31
 */
class MyAccessibility : AccessibilityService() {

    private var disposable: Disposable? = null
    private var collectDis: Disposable? = null
    private var preTime = 0L


    override fun onInterrupt() {
        Log.d("xcx", "onInterrupt: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            Log.d("xyjk", "onStartCommand: $action")
            if (!action.isNullOrEmpty() && action == getString(R.string.collect_energy_action)) {
                var t = System.currentTimeMillis()
                if (t - preTime >= 15000) {
                    preTime = t
                    MyApp.sb.append("${DateUtils.getFormatDate()}  onStartCommand $action \r\n")
                    RxBus.getInstance().onNext(action)
                    //解锁
                    startCollectEnergy(MyApp.appContext)
                } else {
                    MyApp.sb.append("${DateUtils.getFormatDate()}  onStartCommand receive repeat $action \r\n")
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

        Log.i("xcx", "type: ${event.eventType}")

        if (packageName == "com.eg.android.AlipayGphone") {
            Log.d("xcx", "onAccessibilityEvent: ${event.eventType}")
        }

        /**
         * 收到自己的通知消息准备解锁并收能量
         */
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//            unlockScreen(event)
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {

            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            if (hour in getStartTimeHour()..getEndTimeHour()) {
                Log.i("xcx", "onAccessibilityEvent: alipay $packageName $className")
                MyApp.sb.append(
                    "${DateUtils.getFormatDate()}  TYPE_WINDOW_STATE_CHANGED or TYPE_WINDOW_CONTENT_CHANGED " +
                            "$packageName $className \r\n"
                )

                gotoH5Activity(event, packageName, className)

                if (packageName == "com.eg.android.AlipayGphone" &&
                    (className == "com.alipay.mobile.nebulacore.ui.H5Activity" ||
                            className == "com.uc.webkit.be")
                ) {

                    if (disposable != null && !disposable!!.isDisposed) {
                        disposable!!.dispose()
                        disposable = null
                    }

                    disposable = Observable.timer(3 * 1000, TimeUnit.MILLISECONDS, Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            var nodeInfo = rootInActiveWindow

                            if (nodeInfo != null) {
                                MyApp.sb.append("${DateUtils.getFormatDate()}  nodeInfo!=null \r\n")
                                for (i in 0 until nodeInfo.childCount) {
                                    val child = nodeInfo.getChild(i)
                                    Log.d("xyjk", "child.className: ${child.className}")
                                    if ("com.uc.webview.export.WebView" == child.className) {
                                        Log.e("xyjk", "找到蚂蚁森林的 webView count = " + child.childCount)
//                                        collectFriendEnergy(child)
                                        findEveryViewNode(child)
                                        break
                                    }
                                }
                            } else {
                                Log.d("xyjk", "alipayPolicy = null")
                                MyApp.sb.append("${DateUtils.getFormatDate()}  alipayPolicy=null \r\n")

                            }
                        }

                }
            }
        }
    }

    private fun findEveryViewNode(node: AccessibilityNodeInfo?) {
        if (null != node && node.childCount > 0) {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                // 有时 child 为空

//                Log.e("xcx", "findEveryViewNode = " + child.toString())

                val className = child.className.toString()
                if ("android.widget.Button" == className) {
                    Log.e(
                        "xyjk", "Button 的节点数据 " +
                                "text = ${child.text}, " +
                                "descript = ${child.contentDescription}, " +
                                "className = ${child.className}, " +
                                "resId = ${child.viewIdResourceName}"
                    )


                    MyApp.sb.append(
                        "${DateUtils.getFormatDate()} Button 的节点数据: text = ${child.text}, " +
                                "descript = ${child.contentDescription}, className = ${child.className}, \r\n"
                    )


                    val isClickable = child.isClickable
                    val isResIdNull = child.viewIdResourceName == null

                    /**
                     * 好友的能量不能收取，因为支付宝在onTouch事件中return true,导致不会触发OnClick方法
                     * todo 第二版准备把偷能量加上
                     * 但是支付宝中的蚂蚁森林可以收取自己的能量
                     */
                    Log.e("xcx", "isClickable=$isClickable isResIdNull=$isResIdNull")

                    var dec = child.contentDescription
                    var text = child.text

                    //关闭蒙层
                    if (!dec.isNullOrEmpty() && dec.contains("关闭蒙层") || !text.isNullOrEmpty() && text.contains("关闭蒙层")) {
                        closeMongolia(child)
                        return
                    }


                    if (!dec.isNullOrEmpty() && dec.contains("收集能量") || !text.isNullOrEmpty() && text.contains("收集能量")) {
//                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.e("xcx", "能量球 成功点击")
                        MyApp.sb.append("${DateUtils.getFormatDate()}  准备收集能量... \r\n")
                        collectEnergy(child)

                    }

                }

                // 递归调用
                findEveryViewNode(child)
            }
        }
    }

    private fun collectFriendEnergy(info: AccessibilityNodeInfo) {


        val rect = Rect()
        info.getBoundsInScreen(rect)

        Log.e("xyjk", "collectFriendEnergy: ${getScreenHeight()}")

        Log.e("xyjk", "${rect.toShortString()}")
    }

    /**
     * 关闭支付宝活动蒙层
     */
    private fun closeMongolia(info: AccessibilityNodeInfo) {
        val rect = Rect()
        info.getBoundsInScreen(rect)

        Log.e("xcx", "${rect.toShortString()}")

        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            .build()
        dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                Log.e("xcx", "close onCompleted")
                Toast.makeText(applicationContext, "need restart", Toast.LENGTH_SHORT).show()
                MyApp.sb.append("${DateUtils.getFormatDate()}  close mongolia... \r\n")
                restartCollect()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.e("xcx", "close onCancelled")
                Toast.makeText(applicationContext, "close 为啥取消了呢", Toast.LENGTH_SHORT).show()
                MyApp.sb.append("${DateUtils.getFormatDate()}  cancel mongolia... \r\n")
                restartCollect()
            }
        }, null)
    }

    private fun restartCollect() {
        if (collectDis != null && !collectDis!!.isDisposed) {
            collectDis!!.dispose()
        }
        collectDis = Observable.timer(1500, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //                findEveryViewNode(node)

                Log.e("xcx", "restartCollect: ")
                if (collectDis != null && !collectDis!!.isDisposed) {
                    collectDis!!.dispose()
                }
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
    }

    private fun collectEnergy(info: AccessibilityNodeInfo) {
        val rect = Rect()
        info.getBoundsInScreen(rect)

        Log.e("xcx", "${rect.toShortString()}")

        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 100, 50))
            .build()
        dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                Log.e("xcx", "onCompleted")
                Toast.makeText(applicationContext, "收能量了", Toast.LENGTH_SHORT).show()
                MyApp.sb.append("${DateUtils.getFormatDate()}  收集能量了... \r\n")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.e("xcx", "onCancelled")
                Toast.makeText(applicationContext, "为啥取消了呢", Toast.LENGTH_SHORT).show()
                MyApp.sb.append("${DateUtils.getFormatDate()}  为啥取消了呢... \r\n")
            }
        }, null)
    }

    private fun gotoH5Activity(event: AccessibilityEvent, packageName: String, className: String) {

        if (packageName == "com.eg.android.AlipayGphone" &&
            (className != "com.alipay.mobile.nebulacore.ui.H5Activity" ||
                    !className.contains("com.uc.webkit"))
        ) {
            MyApp.sb.append("${DateUtils.getFormatDate()}  gotoH5Activity \r\n")
            Log.d("xyjk", "gotoH5Activity")
            rootInActiveWindow?.apply {
                val nodeInfos = findAccessibilityNodeInfosByText("蚂蚁森林")
                nodeInfos?.also {
                    for (node in it) {
//                        Log.e("xcx", "rootInActiveWindow: ${node.text}")
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


    private fun isNeedUnLocked(event: AccessibilityEvent): Boolean {
        val parcelableData = event.parcelableData

        if (parcelableData == null) {
            return false
        }

        if (parcelableData !is Notification) {
            return false
        }

        Log.e("xyjk", "isNeedUnLocked: ${parcelableData.tickerText}")
        MyApp.sb.append("${DateUtils.getFormatDate()}  needUnlocked \r\n")


        val tickerText = parcelableData.tickerText
        return !tickerText.isNullOrEmpty() && tickerText.contains("ruge_ya")
    }

    private fun execShell(
        commands: MutableList<String>,
        child: AccessibilityNodeInfo
    ) {
        // 获取Runtime对象
        var os: DataOutputStream? = null
        try {
            Log.e("xyjk", "execShell")
            MyApp.sb.append("${DateUtils.getFormatDate()}  execShell \r\n")
            // 获取root权限，这里大量申请root权限会导致应用卡死，可以把Runtime和Process放在Application中初始化
            val process = MyApp.runtime.exec("su")

            os = DataOutputStream(process.outputStream)

            for (command in commands) {
                if (command.isNullOrEmpty()) {
                    continue
                }
                // donnot use os.writeBytes(commmand), avoid chinese charset
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
}