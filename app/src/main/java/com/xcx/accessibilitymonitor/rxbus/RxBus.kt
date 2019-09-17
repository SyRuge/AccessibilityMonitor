package com.xcx.accessibilitymonitor.rxbus

import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * Create By Ruge at 2019-03-31
 */
class RxBus private constructor() {

    companion object {
        private val subject = PublishSubject.create<String>().toSerialized()

        fun getInstance(): RxBus {
            return SingletonHolder.instance
        }

    }

    fun onNext(action: String) {
        subject.onNext(action)
    }

    fun getObservable(): Subject<String> {
        return subject
    }

    private object SingletonHolder {
        val instance = RxBus()
    }

}