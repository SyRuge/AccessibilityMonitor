package com.xcx.accessibilitymonitor.utils

import android.accessibilityservice.GestureDescription

/**
 * Create By Ruge at 2019-09-26
 */
class GestureContainer {

    var completed: ((GestureDescription?) -> Unit)? = null
    var cancelled: ((GestureDescription?) -> Unit)? = null


    fun onCompleted(action: (GestureDescription?) -> Unit){
        completed = action
    }

    fun onCancelled(action: (GestureDescription?) -> Unit){
        cancelled = action
    }


}