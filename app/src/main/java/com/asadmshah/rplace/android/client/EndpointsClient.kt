package com.asadmshah.rplace.android.client

import android.content.Context
import com.asadmshah.rplace.android.preferences.Preferences
import com.asadmshah.rplace.android.storage.Storage
import com.asadmshah.rplace.client.PlaceClient
import io.reactivex.Observable
import io.reactivex.Scheduler

interface EndpointsClient {

    companion object {
        fun create(client: PlaceClient, preferences: Preferences, storage: Storage, scheduler: Scheduler): EndpointsClient {
            return EndpointsClientImpl(client, preferences, storage, scheduler)
        }

        fun create(context: Context, scheduler: Scheduler): EndpointsClient {
            return OfflineEndpointsClient(context, scheduler)
        }
    }

    fun connect(): Observable<EndpointEvents>

}