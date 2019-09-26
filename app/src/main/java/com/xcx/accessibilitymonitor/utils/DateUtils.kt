package com.xcx.accessibilitymonitor.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Create By Ruge at 2019-03-31
 */
class DateUtils {

    companion object {

        fun getFormatDate(): String {
            val format=SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
            return format.format(Date())
        }

        val startDate="5:00"
        val endDate="8:00"

        fun compareCurrentTime(){
            val format=SimpleDateFormat("HH:mm:ss")
            val format1 = format.format(Date())

            val calendar=Calendar.getInstance()
            calendar.get(Calendar.HOUR_OF_DAY)
            calendar.get(Calendar.HOUR)

        }

        fun strToDate(date: String): Long {

            val format=SimpleDateFormat("HH:mm:ss")

            val parse = format.parse("8:00:00")

            val calendar=Calendar.getInstance()
            val hd = calendar.get(Calendar.HOUR_OF_DAY)
            val h = calendar.get(Calendar.HOUR)

            logd("xcx", "strToDate: $hd $h")

            return parse.time

        }

        fun getCurrentHour(): Int {
            val calendar=Calendar.getInstance()
            return calendar.get(Calendar.HOUR_OF_DAY)
        }



    }
}