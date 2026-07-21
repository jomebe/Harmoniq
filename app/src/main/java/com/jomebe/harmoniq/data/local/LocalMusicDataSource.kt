package com.jomebe.harmoniq.data.local

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.jomebe.harmoniq.domain.Track

class LocalMusicDataSource(private val context: Context) {
    fun search(query: String, limit: Int = 100): List<Track> {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
        val needle = "%${query.replace("%", "\\%").replace("_", "\\_")}%"
        return runCatching {
            context.contentResolver.query(collection, projection, selection, arrayOf(needle, needle), "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
                buildList {
                    while (cursor.moveToNext() && size < limit) {
                        val id = cursor.getLong(0)
                        add(Track("local:$id", cursor.getString(1).orEmpty(), cursor.getString(2) ?: "알 수 없는 아티스트", "", ContentUris.withAppendedId(collection, id).toString(), formatDuration(cursor.getLong(3) / 1000), tags = listOf("내 기기")))
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun formatDuration(seconds: Long) = "%d:%02d".format(seconds / 60, seconds % 60)
}
