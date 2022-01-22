package com.arsinedev.playlistgenerator.utils

fun getPlaylistId(input: String?): String? {
    input ?: return null
    val pattern = Regex("""https?://open\.spotify\.com/playlist/([a-zA-Z0-9]+).*""")
    val matchResult = pattern.matchEntire(input) ?: return null
    return matchResult.groupValues[1]
}