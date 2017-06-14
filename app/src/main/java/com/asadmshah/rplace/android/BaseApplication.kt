package com.asadmshah.rplace.android

import android.app.Application

class BaseApplication: Application() {

    private val injector: Injector by lazy { InjectorImpl(BaseApplication@this) }

}