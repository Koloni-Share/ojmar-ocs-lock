package com.t.ocslockapp

import android.app.Application
import android.content.Context

/**
 * Application class
 */
class KoloniApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }
}