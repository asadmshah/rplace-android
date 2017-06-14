package com.asadmshah.rplace.android.client

import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

internal class NoopWebSocket: WebSocket {

    override fun queueSize(): Long {
        return 0L
    }

    override fun send(text: String?): Boolean {
        return false
    }

    override fun send(bytes: ByteString?): Boolean {
        return false
    }

    override fun close(code: Int, reason: String?): Boolean {
        return false
    }

    override fun cancel() {
    }

    override fun request(): Request {
        return Request.Builder().build()
    }
}