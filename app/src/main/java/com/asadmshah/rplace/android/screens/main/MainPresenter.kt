package com.asadmshah.rplace.android.screens.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.asadmshah.rplace.android.Injector
import com.asadmshah.rplace.android.R
import com.asadmshah.rplace.android.client.EndpointEvents
import com.asadmshah.rplace.android.client.EndpointsClient
import com.asadmshah.rplace.android.schedulers.Schedulers
import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.Position
import io.reactivex.disposables.Disposable
import okhttp3.WebSocket
import okio.ByteString

class MainPresenter(private val view: MainContract.View, injector: Injector) : MainContract.Presenter {

    val schedulers: Schedulers = injector.schedulers()
    val client: EndpointsClient = injector.endpointsClient()

    var socketDisposable: Disposable? = null
    var socket: WebSocket? = null

    var colorPicked: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        socketDisposable = client
                .connect()
                .observeOn(schedulers.ui())
                .subscribe(this::onClientEvent, this::onClientError)
    }

    override fun onPause() {
        socketDisposable?.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {

    }

    override fun onDestroy() {

    }

    override fun onColorSelected(color: Int) {
        colorPicked = color
    }

    override fun onPixelTouched(x: Int, y: Int) {
        socketDisposable?.let {
            if (!it.isDisposed) socket?.let { socket ->
                val data = DrawEvent
                        .newBuilder()
                        .setPosition(Position.newBuilder().setX(x).setY(y))
                        .setColor(colorPicked)
                        .setDatetime(System.currentTimeMillis())
                        .build()
                        .toByteArray()
                socket.send(ByteString.of(data, 0, data.size))
            }
        }
    }

    fun onClientEvent(event: EndpointEvents) {
        when (event) {
            is EndpointEvents.OnInitial -> {
                Log.d("MainPresenter", "onInitial")
                event.bitmap?.let { view.setBitmap(it) }
            }
            is EndpointEvents.OnOpened -> {
                Log.d("MainPresenter", "onOpened")
                socket = event.webSocket
            }
            is EndpointEvents.OnDrawEvent -> {
                Log.d("MainPresenter", "onDrawEvent")
                view.setPixels(event.batch.eventsList)
            }
            is EndpointEvents.OnClosed -> {
                Log.d("MainPresenter", "onClosed")
                socket = null
            }
        }
    }

    fun onClientError(throwable: Throwable) {
        throwable.printStackTrace()

        view.showError(R.string.an_error_occurred)
    }
}