package com.asadmshah.rplace.android

import com.asadmshah.rplace.android.client.EndpointsClient
import com.asadmshah.rplace.android.preferences.Preferences
import com.asadmshah.rplace.android.schedulers.Schedulers
import com.asadmshah.rplace.android.storage.Storage

interface Injector {

    fun schedulers(): Schedulers

    fun storage(): Storage

    fun preferences(): Preferences

    fun endpointsClient(): EndpointsClient

}