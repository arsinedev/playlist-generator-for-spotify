package com.arsinedev.playlistgenerator.auth

import android.content.Intent
import android.widget.Toast
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.auth.pkce.AbstractSpotifyPkceLoginActivity
import com.arsinedev.playlistgenerator.BuildConfig
import com.arsinedev.playlistgenerator.PlaylistGeneratorApplication
import com.arsinedev.playlistgenerator.activities.SettingsActivity
import com.arsinedev.playlistgenerator.data.Model
import kotlinx.coroutines.runBlocking

class SpotifyPkceLoginActivity : AbstractSpotifyPkceLoginActivity() {
    override val clientId = BuildConfig.SPOTIFY_CLIENT_ID
    override val redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI_PKCE
    override val scopes = listOf(
        SpotifyScope.USER_LIBRARY_READ,
        SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
        SpotifyScope.PLAYLIST_READ_COLLABORATIVE,
        SpotifyScope.PLAYLIST_READ_PRIVATE
    )
    override val pkceCodeVerifier = PlaylistGeneratorApplication.pkceCodeVerifier

    override fun onSuccess(api: SpotifyClientApi) {
        val model = (application as PlaylistGeneratorApplication).model
        model.credentialStore.setSpotifyApi(api)

        val userInformation = runBlocking {
            api.users.getClientProfile()
        }

        with(Model.preferences.edit()) {
            putString("userId", userInformation.id)
            putString("outputPlaylistId", "")
            putString("outputPlaylistName", "")
            apply()
        }

        Toast.makeText(this, "Authentication was successful.", Toast.LENGTH_SHORT)
            .show()

        val intent = Intent(this, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onFailure(exception: Exception) {
        exception.printStackTrace()
        Toast.makeText(this, "Authentication failed: ${exception.message}", Toast.LENGTH_LONG)
            .show()
    }
}