package com.arsinedev.playlistgenerator.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arsinedev.playlistgenerator.data.Model

class StartupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(
            this,
            if (Model.preferences.all.isNullOrEmpty()) LoginActivity::class.java else SettingsActivity::class.java
        )
        startActivity(intent)
        finish()
    }
}