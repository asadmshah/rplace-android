package com.asadmshah.rplace.android.storage

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

internal class StorageImpl(private val context: Context, private val scheduler: Scheduler): Storage {

    companion object {
        private const val FILENAME = "bitmap.png"
    }

    private fun bitmapFile(): File {
        return File(context.filesDir, FILENAME)
    }

    override fun bitmapExists(): Boolean {
        return bitmapFile().exists()
    }

    override fun bitmapCommit(data: ByteArray): Completable {
        return Completable
                .fromCallable {
                    FileOutputStream(bitmapFile()).use {
                        it.write(data)
                    }
                }
                .subscribeOn(scheduler)
    }

    override fun bitmapGet(): Single<ByteArray> {
        return Single
                .fromCallable {
                    FileInputStream(bitmapFile()).use {
                        it.readBytes()
                    }
                }
    }

}