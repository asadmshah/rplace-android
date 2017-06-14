package com.asadmshah.rplace.android.screens.main

import android.graphics.Color
import com.asadmshah.rplace.android.Injector
import com.asadmshah.rplace.android.R
import com.asadmshah.rplace.android.client.EndpointEvents
import com.asadmshah.rplace.android.client.EndpointsClient
import com.asadmshah.rplace.android.schedulers.Schedulers
import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import okhttp3.WebSocket
import okio.ByteString
import org.junit.Before
import org.junit.Test

class MainPresenterTest {

    private lateinit var schedulers: Schedulers
    private lateinit var endpointsClient: EndpointsClient
    private lateinit var injector: Injector

    private lateinit var view: MainContract.View
    private lateinit var presenter: MainPresenter

    @Before
    fun setUp() {
        schedulers = mock()
        endpointsClient = mock()
        view = mock()

        injector = mock()
        whenever(injector.endpointsClient()).thenReturn(endpointsClient)
        whenever(injector.schedulers()).thenReturn(schedulers)

        presenter = MainPresenter(view, injector)
    }

    @Test
    fun onResumeConnectsToEndpoint() {
        whenever(schedulers.ui()).thenReturn(TestScheduler())
        whenever(endpointsClient.connect()).thenReturn(Observable.empty())

        presenter.onResume()

        verify(endpointsClient).connect()
    }

    @Test
    fun onPauseUnSubscribesFromDisposable() {
        presenter.socketDisposable = Observable.never<Any>().subscribe()

        presenter.onPause()

        assertThat(presenter.socketDisposable?.isDisposed ?: false).isTrue()
    }

    @Test
    fun touchNoopOnNoDisposable() {
        presenter.socket = mock()
        presenter.socketDisposable = null
        presenter.onPixelTouched(1, 1)
        verify(presenter.socket, never())?.send(any<ByteString>())
    }

    @Test
    fun touchNoopOnDisposed() {
        presenter.socket = mock()
        presenter.socketDisposable = mock {
            on { isDisposed } doReturn true
        }
        presenter.onPixelTouched(1, 1)
        verify(presenter.socket, never())?.send(any<ByteString>())
    }

    @Test
    fun touchSendOnConnected() {
        presenter.socket = mock {
            on { send(any<ByteString>()) } doReturn true
        }
        presenter.socketDisposable = mock {
            on { isDisposed } doReturn false
        }
        presenter.onColorSelected(Color.BLUE)
        presenter.onPixelTouched(1, 2)

        argumentCaptor<ByteString>().apply {
            verify(presenter.socket)?.send(capture())

            val event = DrawEvent.parseFrom(firstValue.toByteArray())
            assertThat(event.position.x).isEqualTo(1)
            assertThat(event.position.y).isEqualTo(2)
            assertThat(event.color).isEqualTo(Color.BLUE)
            assertThat(event.datetime).isIn(System.currentTimeMillis() - 1000..System.currentTimeMillis())
        }
    }

    @Test
    fun handlesInitialClientEvent() {
        presenter.onClientEvent(EndpointEvents.OnInitial(10L, byteArrayOf(1, 2, 3)))

        verify(view).setBitmap(byteArrayOf(1, 2, 3))
    }

    @Test
    fun handlesOnOpenedClientEvent() {
        val socket: WebSocket = mock()
        presenter.onClientEvent(EndpointEvents.OnOpened(socket))

        assertThat(presenter.socket).isEqualTo(socket)
    }

    @Test
    fun handlesOnDrawEventsClientEvent() {
        val socket: WebSocket = mock()
        val events = DrawEventsBatch.getDefaultInstance()
        presenter.onClientEvent(EndpointEvents.OnDrawEvent(socket, events))

        verify(view).setPixels(events.eventsList)
    }

    @Test
    fun handlesOnClosedEvent() {
        val socket: WebSocket = mock()
        presenter.socket = socket
        presenter.onClientEvent(EndpointEvents.OnClosed(socket))

        assertThat(presenter.socket).isNull()
    }

    @Test
    fun handlesSocketErrors() {
        presenter.onClientError(RuntimeException(""))

        verify(view).showError(R.string.an_error_occurred)
    }

}