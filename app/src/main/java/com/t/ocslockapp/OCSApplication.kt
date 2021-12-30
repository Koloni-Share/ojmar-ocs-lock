package com.t.ocslockapp

import android.app.Application
import android.content.Context

/**
 * Application class
 */
class OCSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }
}