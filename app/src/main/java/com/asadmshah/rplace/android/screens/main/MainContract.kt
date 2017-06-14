package com.asadmshah.rplace.android.screens.main

import android.os.Bundle
import android.support.annotation.StringRes
import com.asadmshah.rplace.models.DrawEvent

interface MainContract {

    interface View {

        fun setBitmap(bytes: ByteArray)

        fun setPixels(drawEvents: List<DrawEvent>)

        fun showError(@StringRes stringRes: Int, vararg args: Any)
    }

    interface Presenter {

        fun onCreate(savedInstanceState: Bundle?)

        fun onResume()

        fun onPause()

        fun onSaveInstanceState(outState: Bundle)

        fun onDestroy()

        fun onColorSelected(color: Int)

        fun onPixelTouched(x: Int, y: Int)
    }

}