package com.asadmshah.rplace.android.screens.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.asadmshah.rplace.android.BaseApplication
import com.asadmshah.rplace.android.R
import com.asadmshah.rplace.android.views.PlaceView
import com.asadmshah.rplace.models.DrawEvent

class MainActivity: AppCompatActivity(), MainContract.View {

    private lateinit var presenter: MainContract.Presenter

    private lateinit var viewPlace: PlaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPlace = findViewById(R.id.place) as PlaceView

        presenter = MainPresenter(this, BaseApplication.injector(this))
        presenter.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()

        presenter.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        presenter.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }

    override fun setBitmap(bytes: ByteArray) {
        viewPlace.setBitmap(bytes)
    }

    override fun setPixels(drawEvents: List<DrawEvent>) {
        viewPlace.drawPoints(drawEvents)
    }

    override fun showError(stringRes: Int, vararg args: Any) {
        val message = getString(stringRes, args)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}