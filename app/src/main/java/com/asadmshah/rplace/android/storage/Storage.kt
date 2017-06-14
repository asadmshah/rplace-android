package com.asadmshah.rplace.android.storage

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single

interface Storage {

    companion object {
        fun create(context: Context, scheduler: Scheduler): Storage {
            return StorageImpl(context, scheduler)
        }
    }

    fun bitmapExists(): Boolean

    fun bitmapCommit(data: ByteArray): Completable

    fun bitmapGet(): Single<ByteArray>
}