package com.asadmshah.rplace.android.client

import android.content.Context
import android.graphics.Color
import com.asadmshah.rplace.models.DrawEvent
import com.asadmshah.rplace.models.DrawEventsBatch
import com.asadmshah.rplace.models.Position
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.io.BufferedInputStream
import java.util.*
import java.util.concurrent.TimeUnit

internal class OfflineEndpointsClient(private val context: Context, private val scheduler: Scheduler) : EndpointsClient {

    private val random = Random()
    private val colors = intArrayOf(
            Color.BLACK,
            Color.DKGRAY,
            Color.GRAY,
            Color.LTGRAY,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA
    )
    private val socket = NoopWebSocket()

    override fun connect(): Observable<EndpointEvents> {
        return Observable
                .interval(1000, TimeUnit.MILLISECONDS, scheduler)
                .flatMap {
                    getDrawEventsBatch()
                }
                .startWith(EndpointEvents.OnOpened(socket))
                .startWith(getBitmap())
    }

    fun getBitmap(): Observable<EndpointEvents> {
        return Observable
                .fromCallable {
                    context.assets.open("dart.png").use {
                        BufferedInputStream(it).use {
                            val bitmap = ByteArray(it.available())
                            it.read(bitmap)
                            EndpointEvents.OnInitial(0L, bitmap)
                        }
                    }
                }
    }

    fun getDrawEventsBatch(): Observable<EndpointEvents> {
        return Observable
                .fromCallable {
                    val batch = DrawEventsBatch.newBuilder()
                    for (i in 0 until Math.abs(random.nextInt()) % 500) {
                        val x = Math.abs(random.nextInt()) % 1024
                        val y = Math.abs(random.nextInt()) % 1024
                        val c = colors[Math.abs(random.nextInt()) % colors.size]
                        val d = System.currentTimeMillis()
                        val event = DrawEvent
                                .newBuilder()
                                .setPosition(Position.newBuilder().setX(x).setY(y))
                                .setColor(c)
                                .setDatetime(d)
                                .build()
                        batch.addEvents(event)
                    }
                    EndpointEvents.OnDrawEvent(NoopWebSocket(), batch.build())
                }
    }

}