package com.arsinedev.playlistgenerator.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.arsinedev.playlistgenerator.R
import com.arsinedev.playlistgenerator.auth.SpotifyPkceLoginActivity

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        btnConnect.setOnClickListener {
            startActivity(Intent(this, SpotifyPkceLoginActivity::class.java))
        }
    }
}