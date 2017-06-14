package com.asadmshah.rplace.android.preferences

import android.content.Context

interface Preferences {

    companion object {
        fun create(context: Context): Preferences {
            return PreferencesImpl(context)
        }
    }

    fun hasOffset(): Boolean

    fun getOffset(): Long

    fun setOffset(offset: Long)

}