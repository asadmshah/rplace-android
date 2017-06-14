package com.asadmshah.rplace.android.screens.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.asadmshah.rplace.android.R
import com.asadmshah.rplace.models.DrawEvent

class MainActivity: AppCompatActivity(), MainContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun setBitmap(bytes: ByteArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPixels(drawEvents: List<DrawEvent>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showError(stringRes: Int, vararg args: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}