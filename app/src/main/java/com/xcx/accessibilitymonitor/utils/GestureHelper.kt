package com.xcx.accessibilitymonitor.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Handler

/**
 * Create By Ruge at 2019-09-26
 */


inline fun AccessibilityService.handleGesture(
    gesture: GestureDescription,
    handler: Handler? = null,
    block: GestureContainer.() -> Unit
) {
    val gc = GestureContainer().apply(block)
    dispatchGesture(gesture,object : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            gc.completed?.invoke(gestureDescription)
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            gc.cancelled?.invoke(gestureDescription)
        }

    },handler)
}
