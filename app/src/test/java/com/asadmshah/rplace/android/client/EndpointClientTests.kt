package com.asadmshah.rplace.android.client

import com.asadmshah.rplace.android.preferences.Preferences
import com.asadmshah.rplace.android.storage.Storage
import com.asadmshah.rplace.client.DrawingSocketEvents
import com.asadmshah.rplace.client.PlaceClient
import com.asadmshah.rplace.models.DrawEventsBatch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class EndpointClientTests {

    private lateinit var placeClient: PlaceClient
    private lateinit var preferences: Preferences
    private lateinit var storage: Storage

    @Before
    fun setUp() {
        placeClient = mock()
        placeClient = mock()
        preferences = mock()
        storage = mock()
    }

    @Test
    fun connectShouldWork() { // :)
        val scheduler = TestScheduler()
        val webSocket = org.mockito.Mockito.mock(WebSocket::class.java)
        val drawEventsBatch = DrawEventsBatch.getDefaultInstance()

        whenever(storage.bitmapGet()).thenReturn(Single.just(byteArrayOf(1, 2, 3)))
        whenever(storage.bitmapCommit(any())).thenReturn(Completable.complete())
        whenever(preferences.getOffset()).thenReturn(20L)
        whenever(placeClient.canvas()).thenReturn(Maybe.just(Pair(30L, byteArrayOf(4, 5, 6))).delay(1, TimeUnit.SECONDS, scheduler))
        whenever(placeClient.stream(anyLong()))
                .thenReturn(Observable
                        .just(
                                DrawingSocketEvents.OnOpened(webSocket),
                                DrawingSocketEvents.OnDrawEvent(webSocket, drawEventsBatch),
                                DrawingSocketEvents.OnClosed(webSocket)
                        )
                        .delay(1, TimeUnit.SECONDS, scheduler)
                )

        val client = EndpointsClientImpl(placeClient, preferences, storage, scheduler)
        val observer = TestObserver<EndpointEvents>()

        client.connect().subscribeOn(scheduler).subscribe(observer)

        // Check Debounce on Initial Works as Expected.
        scheduler.advanceTimeTo(999, TimeUnit.MILLISECONDS)
        observer.assertNoValues()

        // Checks latest offset value is being used.
        scheduler.advanceTimeTo(1001, TimeUnit.MILLISECONDS)
        whenever(preferences.getOffset()).thenReturn(30L)

        scheduler.advanceTimeTo(2000, TimeUnit.MILLISECONDS)
        observer.assertValueCount(2)
        verify(preferences).setOffset(30L)

        scheduler.advanceTimeTo(3000, TimeUnit.MILLISECONDS)
        observer.assertValueCount(5)
        assertThat(observer.values()[2]).isEqualTo(EndpointEvents.OnOpened(webSocket))
        assertThat(observer.values()[3]).isEqualTo(EndpointEvents.OnDrawEvent(webSocket, drawEventsBatch))
        assertThat(observer.values()[4]).isEqualTo(EndpointEvents.OnClosed(webSocket))
        verify(placeClient).stream(30L)
    }

    @Test
    fun starterShouldReturnLocalThenRemoteThrottled() {
        val scheduler = TestScheduler()

        whenever(storage.bitmapGet()).thenReturn(Single.just(byteArrayOf(1, 2, 3)))
        whenever(storage.bitmapCommit(any())).thenReturn(Completable.complete())
        whenever(preferences.getOffset()).thenReturn(10L)
        whenever(placeClient.canvas()).thenReturn(Maybe.just(Pair(20L, byteArrayOf(4, 5, 6))).delay(1, TimeUnit.SECONDS, scheduler))

        val client = EndpointsClientImpl(placeClient, preferences, storage, scheduler)
        val observer = TestObserver<EndpointEvents.OnInitial>()

        client.getStarter().subscribeOn(scheduler).subscribe(observer)

        scheduler.advanceTimeTo(999, TimeUnit.MILLISECONDS)
        observer.assertNoValues()

        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS)
        observer.assertNotComplete()
        observer.assertValueCount(1)
        val response1 = observer.values().last()
        assertThat(response1.offset).isEqualTo(10L)
        assertThat(response1.bitmap?.asList()).containsExactlyElementsIn(byteArrayOf(1, 2, 3).asList()).inOrder()

        scheduler.advanceTimeTo(2000, TimeUnit.MILLISECONDS)
        observer.assertComplete()
        observer.assertValueCount(2)
        val response2 = observer.values().last()
        assertThat(response2.offset).isEqualTo(20L)
        assertThat(response2.bitmap?.asList()).containsExactlyElementsIn(byteArrayOf(4, 5, 6).asList()).inOrder()
    }

    @Test
    fun initialLocalShouldQueryPreferencesAndStorage() {
        whenever(storage.bitmapGet()).thenReturn(Single.just(byteArrayOf(1, 2, 3)))
        whenever(preferences.getOffset()).thenReturn(10L)

        val scheduler = TestScheduler()
        val client = EndpointsClientImpl(placeClient, preferences, storage, scheduler)
        val observer = TestObserver<EndpointEvents.OnInitial>()

        client.getInitialLocal().subscribeOn(scheduler).subscribe(observer)
        scheduler.triggerActions()

        val (offset, bitmap) = observer.values().first()
        assertThat(offset).isEqualTo(10L)
        assertThat(bitmap?.asList()).containsExactlyElementsIn(byteArrayOf(1, 2, 3).asList()).inOrder()
    }

    @Test
    fun initialLocalShouldQueryDefault() {
        whenever(storage.bitmapGet()).thenReturn(Single.error { FileNotFoundException("File Not Found") })

        val scheduler = TestScheduler()
        val client = EndpointsClientImpl(placeClient, preferences, storage, scheduler)
        val observer = TestObserver<EndpointEvents.OnInitial>()

        client.getInitialLocal().subscribeOn(scheduler).subscribe(observer)
        scheduler.triggerActions()

        val (offset, bitmap) = observer.values().first()
        assertThat(offset).isEqualTo(-1L)
        assertThat(bitmap).isNull()
    }

    @Test
    fun initialRemoteShouldTransformEventAndCommitToDisk() {
        whenever(placeClient.canvas()).thenReturn(Maybe.just(Pair(10L, byteArrayOf(1, 2, 3))))
        whenever(storage.bitmapCommit(any())).thenReturn(Completable.complete())

        val scheduler = TestScheduler()
        val client = EndpointsClientImpl(placeClient, preferences, storage, scheduler)
        val observer = TestObserver<EndpointEvents.OnInitial>()

        client.getInitialRemote().subscribeOn(scheduler).subscribe(observer)
        scheduler.triggerActions()

        val (offset, bitmap) = observer.values().first()
        assertThat(offset).isEqualTo(10L)
        assertThat(bitmap?.asList()).containsExactlyElementsIn(byteArrayOf(1, 2, 3).asList()).inOrder()

        verify(storage).bitmapCommit(byteArrayOf(1, 2, 3))
        verify(preferences).setOffset(10L)
    }

}