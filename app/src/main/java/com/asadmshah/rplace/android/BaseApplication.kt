package com.asadmshah.rplace.android

import android.app.Application
import android.content.Context

class BaseApplication: Application() {

    companion object {
        fun injector(context: Context): Injector {
            return (context.applicationContext as BaseApplication).injector
        }
    }

    private val injector: Injector by lazy { GhettoInjector(BaseApplication@this) }

}