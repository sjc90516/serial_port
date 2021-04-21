package com.serial.test

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

class App : Application() {


    companion object {
        var instance: App by Delegates.notNull()
    }


    override fun onCreate() {
        super.onCreate()


    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

    }
}