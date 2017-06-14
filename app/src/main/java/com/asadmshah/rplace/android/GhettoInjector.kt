package com.asadmshah.rplace.android

import com.asadmshah.rplace.android.client.EndpointsClient
import com.asadmshah.rplace.android.preferences.Preferences
import com.asadmshah.rplace.android.schedulers.Schedulers
import com.asadmshah.rplace.android.storage.Storage
import com.asadmshah.rplace.client.PlaceClientFactory

internal class GhettoInjector(private val application: BaseApplication): Injector {

    private val schedulers_ by lazy { Schedulers.create() }
    private val storage_ by lazy { Storage.create(application, schedulers_.io()) }
    private val preferences_ by lazy { Preferences.create(application) }
    private val placesClient_ by lazy {
        PlaceClientFactory.create(BuildConfig.SERVER_HOST, BuildConfig.SERVER_PORT)
    }
    private val endpointsClient_ by lazy {
//        EndpointsClient.create(placesClient_, preferences_, storage_, schedulers_.io())
        EndpointsClient.create(application, schedulers_.io())
    }

    override fun schedulers(): Schedulers = schedulers_

    override fun storage(): Storage = storage_

    override fun preferences(): Preferences = preferences_

    override fun endpointsClient(): EndpointsClient = endpointsClient_

}