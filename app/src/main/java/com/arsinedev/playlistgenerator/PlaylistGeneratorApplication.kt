package com.arsinedev.playlistgenerator

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.arsinedev.playlistgenerator.data.Model

class PlaylistGeneratorApplication : Application() {
    lateinit var model: Model

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        model = Model
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
        val pkceCodeVerifier = (0..96).joinToString("") {
            (('a'..'z') + ('A'..'Z') + ('0'..'9')).random().toString()
        }
    }
}