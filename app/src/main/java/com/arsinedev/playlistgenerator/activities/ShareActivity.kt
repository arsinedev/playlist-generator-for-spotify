package com.arsinedev.playlistgenerator.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.PlayableUri
import com.arsinedev.playlistgenerator.R
import com.arsinedev.playlistgenerator.data.Model
import com.arsinedev.playlistgenerator.utils.getPlaylistId
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class ShareActivity : BaseActivity() {
    private var action: Int = 0
    private var id: String? = null
    private var n: Int = 0
    private var description: String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        id = getPlaylistId(intent.getStringExtra(Intent.EXTRA_TEXT))
        if (id == null) {
            Toast.makeText(this, "Error! Invalid URL.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)

        editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

        val inputDialog = MaterialAlertDialogBuilder(this)
            .setTitle("How many songs to filter?")
            .setView(editText)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .create()

        inputDialog.setOnShowListener {
            editText.requestFocus()
            editText.postDelayed(200) {
                getSystemService(InputMethodManager::class.java)
                    .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        val items = arrayOf("Shuffle", "Filter + Shuffle", "Library + Filter + Shuffle")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select action")
            .setItems(items) { _, which ->
                action = which
                when (which) {
                    0 -> {
                        doAction()
                    }
                    1 -> {
                        inputDialog.setMessage("Enter positive number to filter by first, or negative number to filter by last.")
                        showInputDialog(inputDialog)
                    }
                    2 -> {
                        inputDialog.setMessage("Enter positive number to filter by newest, or negative number to filter by oldest.")
                        showInputDialog(inputDialog)
                    }
                }
            }
            .setOnCancelListener {
                finish()
            }
            .create()
            .show()
    }

    private fun showInputDialog(inputDialog: AlertDialog) {
        inputDialog.show()
        inputDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                n = Integer.parseInt(text)
            }
            if (n == 0) {
                editText.error = "Number cannot be blank or zero."
            } else {
                inputDialog.dismiss()
                doAction()
            }
        }
    }

    private fun doAction() {
        progressBar.visibility = ProgressBar.VISIBLE

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Toast.makeText(this, "Something went wrong! ${throwable.message}", Toast.LENGTH_LONG)
                .show()
            finish()
        }

        lifecycleScope.launch(exceptionHandler) {
            // Adding this delay seems to help in preventing UI lag when coroutine is launched.
            delay(200)

            val api = Model.credentialStore.getSpotifyClientPkceApi()
                ?: throw Exception("Couldn't get API.")
            val items = when (action) {
                2 -> getLibraryItems(api)
                else -> getPlaylistItems(api)
            }
            val outputPlaylistId = Model.getStringPreference("outputPlaylistId")
            setPlaylistItems(api, outputPlaylistId, items)

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("spotify://playlist/$outputPlaylistId")
                )
            )
            finish()
        }
    }

    /**
     * Return recent/oldest [n] tracks depending on if [n] is positive/negative.
     *
     * SpotifyClientApi.library.getSavedTracks() returns saved tracks in recently added order.
     */
    private suspend fun getLibraryItems(api: SpotifyClientApi): List<PlayableUri> {
        tvStatus.text = "Retrieving items from library"

        val total = api.library.getSavedTracks(1, 0).total
        val size = minOf(abs(n), total)

        description = (if (n > 0) "Recent" else "Oldest") + " $size songs from Library."

        progressBar.progress = 0
        progressBar.max = size

        val items = mutableListOf<PlayableUri>()

        var limit = minOf(50, size)
        var offset = if (n > 0) 0 else total - size

        while (items.size < size) {
            items += api.library.getSavedTracks(limit, offset).items.map { it.track.uri }
            offset += limit
            limit = minOf(50, size - items.size)
            progressBar.progress = items.size
        }

        return items.shuffled()
    }

    /**
     * If [action] is 0, returns entire playlist shuffled. If [action] is 1, returns first/last
     * [n] items depending on if [n] is positive/negative.
     *
     * SpotifyClientApi.playlists.getPlaylistTracks() returns playlist tracks in the custom order.
     */
    private suspend fun getPlaylistItems(api: SpotifyClientApi): List<PlayableUri> {
        tvStatus.text = "Retrieving items from playlist"

        val playlist = api.playlists.getPlaylist(id!!) ?: throw Exception("Playlist not found.")

        val total = playlist.tracks.total
        val size = if (action == 0) total else minOf(abs(n), total)

        val name = playlist.name
        description = if (action == 0) {
            "All songs from \"$name\"."
        } else {
            (if (n > 0) "First" else "Last") + " $size songs from \"$name\"."
        }

        progressBar.progress = 0
        progressBar.max = size

        val items = mutableListOf<PlayableUri>()

        var limit = minOf(50, size)
        var offset = if (n > 0) 0 else total - size

        while (items.size < size) {
            items += api.playlists.getPlaylistTracks(id!!, limit, offset).items.map {
                it.track?.uri ?: throw Exception("Playlist track uri is null.")
            }
            offset += limit
            limit = minOf(50, size - items.size)
            progressBar.progress = items.size
        }

        return items.shuffled()
    }

    private suspend fun setPlaylistItems(
        api: SpotifyClientApi,
        playlistId: String,
        items: List<PlayableUri>
    ) {
        tvStatus.text = "Putting items in output playlist"
        progressBar.progress = 0
        progressBar.max = items.size

        api.playlists.removeAllClientPlaylistPlayables(playlistId)
        api.playlists.changeClientPlaylistDetails(playlistId, description = description)

        var offset = 0
        val size = items.size
        while (size - offset > 0) {
            api.playlists.addPlayablesToClientPlaylist(
                playlistId,
                *items.subList(offset, minOf(offset + 100, size)).map { it }.toTypedArray()
            )
            offset = minOf(offset + 100, size)
            progressBar.progress = offset
        }
    }
}