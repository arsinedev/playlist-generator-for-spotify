package com.arsinedev.playlistgenerator.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.adamratzman.spotify.auth.SpotifyDefaultCredentialStore
import com.arsinedev.playlistgenerator.BuildConfig
import com.arsinedev.playlistgenerator.PlaylistGeneratorApplication

object Model {
    val credentialStore: SpotifyDefaultCredentialStore by lazy {
        SpotifyDefaultCredentialStore(
            clientId = BuildConfig.SPOTIFY_CLIENT_ID,
            redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI_PKCE,
            applicationContext = PlaylistGeneratorApplication.context
        )
    }
    val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(PlaylistGeneratorApplication.context)
    }

    fun getStringPreference(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    fun putStringPreference(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
}