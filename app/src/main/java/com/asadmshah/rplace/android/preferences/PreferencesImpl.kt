package com.asadmshah.rplace.android.preferences

import android.content.Context
import android.preference.PreferenceManager

internal class PreferencesImpl(context: Context) : Preferences {

    companion object {
        private const val KEY_OFFSET = "offset"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun hasOffset(): Boolean {
        return sharedPreferences.contains(KEY_OFFSET)
    }

    override fun getOffset(): Long {
        return sharedPreferences.getLong(KEY_OFFSET, 0)
    }

    override fun setOffset(offset: Long) {
        sharedPreferences
                .edit()
                .putLong(KEY_OFFSET, offset)
                .apply()
    }

}