package com.asadmshah.rplace.android.client

import com.asadmshah.rplace.android.preferences.Preferences
import com.asadmshah.rplace.android.storage.Storage
import com.asadmshah.rplace.client.DrawingSocketEvents
import com.asadmshah.rplace.client.PlaceClient
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

internal class EndpointsClientImpl(private val placeClient: PlaceClient,
                                   private val preferences: Preferences,
                                   private val storage: Storage,
                                   private val scheduler: Scheduler) : EndpointsClient {

    override fun connect(): Observable<EndpointEvents> {
        return Observable
                .defer { placeClient.stream(preferences.getOffset()) }
                .map {
                    when (it) {
                        is DrawingSocketEvents.OnOpened -> EndpointEvents.OnOpened(it.webSocket)
                        is DrawingSocketEvents.OnDrawEvent -> EndpointEvents.OnDrawEvent(it.webSocket, it.events)
                        is DrawingSocketEvents.OnClosed -> EndpointEvents.OnClosed(it.webSocket)
                    }
                }
                .startWith(getStarter())
                .subscribeOn(scheduler)
    }

    internal fun getStarter(): Observable<EndpointEvents.OnInitial> {
        return Observable
                .merge(getInitialLocal(), getInitialRemote(), Observable.never())
                .debounce(1, TimeUnit.SECONDS, scheduler)
                .take(2)
    }

    internal fun getInitialLocal(): Observable<EndpointEvents.OnInitial> {
        return storage
                .bitmapGet()
                .map { EndpointEvents.OnInitial(preferences.getOffset(), it) }
                .toObservable()
                .onErrorResumeNext(Observable.just(EndpointEvents.OnInitial(-1L, null)))
    }

    internal fun getInitialRemote(): Observable<EndpointEvents.OnInitial> {
        return placeClient
                .canvas()
                .map { EndpointEvents.OnInitial(it.first, it.second) }
                // Side effect :(
                .doOnSuccess { (offset, bitmap) ->
                    if (bitmap != null) {
                        preferences.setOffset(offset)
                        storage.bitmapCommit(bitmap).blockingAwait()
                    }
                }
                .toObservable()
    }

}