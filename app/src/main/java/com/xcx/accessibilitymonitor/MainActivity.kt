package com.xcx.accessibilitymonitor

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.xcx.accessibilitymonitor.activity.StartAbsActivity
import com.xcx.accessibilitymonitor.presenter.isNeedToGoAliPay
import com.xcx.accessibilitymonitor.presenter.startFirstAlarm
import com.xcx.accessibilitymonitor.presenter.startTestAlarm
import com.xcx.accessibilitymonitor.service.MyAccessibility
import com.xcx.accessibilitymonitor.utils.enableMyAccessibility
import com.xcx.accessibilitymonitor.utils.logd
import com.xcx.accessibilitymonitor.utils.writeLogToFile
import com.xcx.rxpermission.RxPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initListener()
    }

    private fun initView() {

    }

    private fun initData() {
        createCollectEnergyChannel()
    }

    private fun createCollectEnergyChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val preCl = notifyManager.getNotificationChannel("com.xcx")

            if (preCl == null) {
                val cl = NotificationChannel("com.xcx", "偷能量", NotificationManager.IMPORTANCE_HIGH)
                notifyManager.createNotificationChannel(cl)
            }

        }
    }

    private fun initListener() {
        tv_set_collect.setOnClickListener(this)
        tv_up_log.setOnClickListener(this)
        tv_test_other.setOnClickListener(this)
        tv_start_collect.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_set_collect -> {
                startActivity(Intent(this, StartAbsActivity::class.java))
            }
            R.id.tv_up_log -> {
                RxPermission.with(this)
                        .requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe {
                            for (p in it) {
                                if (p == PackageManager.PERMISSION_GRANTED) {
                                    writeLogToFile(getString(R.string.log_name), MyApp.getLog())
                                } else {
                                    Toast.makeText(applicationContext, "权限拒绝呢", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
            }
            R.id.tv_test_other -> {
//                Toast.makeText(applicationContext, "尽请期待呢~", Toast.LENGTH_SHORT).show()
                isNeedToGoAliPay()
            }
            R.id.tv_start_collect -> {
                if (!accessibilityServiceEnable()){
                    enableMyAccessibility()
                    showCheckNotification()
                }
                startFirstAlarm()
            }
        }
    }

    private fun showCheckNotification() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi =
            PendingIntent.getActivity(this, 10086, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, "com.xcx")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setTicker("检查无障碍是否开启")
            .setContentTitle("检查无障碍是否开启")
            .setContentText("检查自动收能量脚本是否开启")
            .setContentIntent(pi)
            .setAutoCancel(true)
        val id = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this@MainActivity).notify(id, builder.build())
    }

    private fun accessibilityServiceEnable(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(contentResolver,Settings.Secure.ACCESSIBILITY_ENABLED,0)

        if (accessibilityEnabled == 0) {
            return false
        }
        val service = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        // 开启并且弹出通知
        if (service.contains(MyAccessibility::class.java.simpleName)) {
            return true
        }
        return false
}

    private fun testJobIsOk() {
        val c = Calendar.getInstance()
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        var t = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            Toast.makeText(applicationContext, "$hourOfDay $minute", Toast.LENGTH_SHORT).show()
            startDelayedJob(hourOfDay, minute)
        }, h, m + 1, true)
        t.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which ->
            Toast.makeText(applicationContext, "取消~", Toast.LENGTH_SHORT).show()
        }
        t.show()
    }

    private fun startDelayedJob(hourOfDay: Int, minute: Int) {
        startTestAlarm(this, hourOfDay, minute)
        logd("MainActivity", "test show notify")
    }

}
