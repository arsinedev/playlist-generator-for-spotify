package com.arsinedev.playlistgenerator.activities

import android.os.Bundle
import android.widget.TextView
import com.arsinedev.playlistgenerator.R
import com.arsinedev.playlistgenerator.data.Model

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val tvUserId = findViewById<TextView>(R.id.tvUserId)
        tvUserId.text = Model.getStringPreference("userId")
    }
}