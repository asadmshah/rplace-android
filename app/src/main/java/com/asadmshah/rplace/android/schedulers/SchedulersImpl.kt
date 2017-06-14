package com.asadmshah.rplace.android.schedulers

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

internal class SchedulersImpl: Schedulers {

    override fun ui(): Scheduler = AndroidSchedulers.mainThread()

    override fun io(): Scheduler = io.reactivex.schedulers.Schedulers.io()

}