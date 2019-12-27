package com.xcx.accessibilitymonitor.activity

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.presenter.startTestAlarm
import com.xcx.accessibilitymonitor.utils.*
import com.xcx.accessibilitymonitor.view.dialog.CustomDialog
import kotlinx.android.synthetic.main.activity_start_abs.*
import java.util.*


class StartAbsActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var time: TimePickerDialog
    lateinit var pvCustomOptions: OptionsPickerView<Int>
    private val normalList = mutableListOf<Int>()
    private val specialList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_abs)
        initView()
        initData()
        initListener()
    }

    private fun initView() {
        tv_start_time.text = getFormatTime(getStartTimeHour(), getStartTimeMinute())
        tv_end_time.text = getFormatTime(getEndTimeHour(), getEndTimeMinute())
        tv_normal_repeat.text = "${getNormalRepeat()}分钟"
        tv_special_repeat.text = "${getSpecialRepeat()}分钟"
    }

    private fun initData() {

        normalList.add(10)
        normalList.add(15)
        normalList.add(20)
        normalList.add(25)
        normalList.add(23)

        specialList.add(1)
        specialList.add(2)
        specialList.add(3)
        specialList.add(4)
        specialList.add(5)
        pvCustomOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->

            setNormalRepeat(normalList[options1])
            setSpecialRepeat(specialList[options2])

        })
                .setSelectOptions(0, 4)
                .setContentTextSize(23)
                .build()

        pvCustomOptions.setNPicker(normalList, specialList, null)//添加数据
    }

    private fun initListener() {
        tv_start_abs.setOnClickListener(this)
        tv_start_time.setOnClickListener(this)
        tv_end_time.setOnClickListener(this)
        tv_normal_repeat.setOnClickListener(this)
        tv_input_passwd.setOnClickListener(this)
    }

    private fun setAlarmRepeatJob(isStartTime: Boolean, h: Int = 5, m: Int = 30) {
        time = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            Toast.makeText(applicationContext, "$hourOfDay $minute", Toast.LENGTH_SHORT).show()
            setHourAndMinute(isStartTime, hourOfDay, minute)
        }, h, m, true)
        time.show()

    }

    private fun getFormatTime(hour: Int, minute: Int): String {
        var h = "$hour"
        var m = "$minute"
        if (hour < 10) {
            h = "0$hour"
        }
        if (minute < 10) {
            m = "0$minute"
        }
        return "$h:$m"
    }

    private fun setHourAndMinute(isStartTime: Boolean, hourOfDay: Int, minute: Int) {

        if (isStartTime) {
            setStartTimeHour(hourOfDay)
            setStartTimeMinute(minute)
            tv_start_time.text = getFormatTime(hourOfDay, minute)
        } else {
            setEndTimeHour(hourOfDay)
            setEndTimeMinute(minute)
            tv_end_time.text = getFormatTime(hourOfDay, minute)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_start_abs -> {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            R.id.tv_start_time -> {
                setAlarmRepeatJob(true)
            }
            R.id.tv_end_time -> {
                setAlarmRepeatJob(false, 8, 0)
            }
            R.id.tv_normal_repeat -> {
                pvCustomOptions.show()
            }
            R.id.tv_input_passwd -> {
                CustomDialog().show(supportFragmentManager, "xyjk")
            }
        }
    }

    private fun testJobIsOk() {
        val c = Calendar.getInstance()
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        var t = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            Toast.makeText(applicationContext, "$hourOfDay $minute", Toast.LENGTH_SHORT).show()
            startDelayedJob(hourOfDay, minute)
            finish()
        }, h, m + 1, true)
        t.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which ->
            Toast.makeText(applicationContext, "取消~", Toast.LENGTH_SHORT).show()
        }
        t.show()
    }

    private fun startDelayedJob(hourOfDay: Int, minute: Int) {
        startTestAlarm(this, hourOfDay, minute)
        logd("StartAbsActivity", "test show notify")
    }

}
