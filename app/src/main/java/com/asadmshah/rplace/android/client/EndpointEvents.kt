package com.asadmshah.rplace.android.client

import com.asadmshah.rplace.models.DrawEventsBatch
import okhttp3.WebSocket

sealed class EndpointEvents {
    data class OnInitial(val offset: Long, val bitmap: ByteArray?) : EndpointEvents()
    data class OnOpened(val webSocket: WebSocket) : EndpointEvents()
    data class OnDrawEvent(val webSocket: WebSocket, val batch: DrawEventsBatch) : EndpointEvents()
    data class OnClosed(val webSocket: WebSocket) : EndpointEvents()
}