package com.arsinedev.playlistgenerator.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arsinedev.playlistgenerator.R
import com.arsinedev.playlistgenerator.utils.*
import com.arsinedev.playlistgenerator.data.Model
import kotlinx.coroutines.runBlocking

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        with(findPreference<EditTextPreference>("outputPlaylistId")!!) {
            setOnPreferenceChangeListener { _, newValue ->
                try {
                    savePlaylist(newValue.toString())
                } catch (exception: IllegalArgumentException) {
                    Toast.makeText(
                        this@SettingsFragment.context,
                        exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                false
            }

            /**
             * Show playlist name as summary.
             */
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { editTextPreference ->
                if (editTextPreference.text.isNullOrEmpty()) {
                    "Not set"
                } else {
                    Model.getStringPreference("outputPlaylistName")
                }
            }

            /**
             * If the preference is not empty, show the dialog with the full link populated and
             * selected.
             */
            setOnBindEditTextListener { editText ->
                val outputPlaylistId = Model.getStringPreference("outputPlaylistId")
                if (outputPlaylistId.isNotEmpty()) {
                    editText.setText("https://open.spotify.com/playlist/$outputPlaylistId")
                    editText.selectAll()
                }
            }
        }
    }

    /**
     *  If [url] is valid, save the playlist id and name.
     *
     *  @param url URL of the playlist.
     *  @throws IllegalArgumentException If the input is not valid.
     */
    private fun savePlaylist(url: String) {
        val id = getPlaylistId(url) ?: throw IllegalArgumentException("Error! Invalid URL.")
        val playlist = runBlocking {
            val api = Model.credentialStore.getSpotifyClientPkceApi()!!
            api.playlists.getClientPlaylist(id)
        }
        val playlistOwnerId = playlist?.owner?.id
            ?: throw IllegalArgumentException("Error! Playlist not found in your library.")
        if (playlistOwnerId != Model.getStringPreference("userId"))
            throw IllegalArgumentException("Error! You are not the playlist owner.")

        Model.putStringPreference("outputPlaylistName", playlist.name)
        findPreference<EditTextPreference>("outputPlaylistId")!!.text = id
    }
}