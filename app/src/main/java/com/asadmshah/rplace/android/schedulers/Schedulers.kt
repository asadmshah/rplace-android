package com.asadmshah.rplace.android.schedulers

import io.reactivex.Scheduler

interface Schedulers {

    companion object {
        fun create(): Schedulers {
            return SchedulersImpl()
        }
    }

    fun ui(): Scheduler

    fun io(): Scheduler

}