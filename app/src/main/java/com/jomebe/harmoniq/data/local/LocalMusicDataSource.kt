package com.jomebe.harmoniq.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.jomebe.harmoniq.domain.Track

class LocalMusicDataSource(private val context: Context) {
    fun search(query: String, limit: Int = 100): List<Track> {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val trimmed = query.trim()
        val (selection, selectionArgs) = if (trimmed.isEmpty()) {
            "${MediaStore.Audio.Media.IS_MUSIC} != 0" to null
        } else {
            val needle = "%${trimmed.replace("%", "\\%").replace("_", "\\_")}%"
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)" to arrayOf(needle, needle)
        }

        return runCatching {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                buildList {
                    while (cursor.moveToNext() && size < limit) {
                        val id = cursor.getLong(idCol)
                        val title = cursor.getString(titleCol).orEmpty()
                        val artist = cursor.getString(artistCol).takeIf { !it.isNullOrBlank() } ?: "알 수 없는 아티스트"
                        val durationSec = cursor.getLong(durCol) / 1000
                        val albumId = cursor.getLong(albumCol)
                        val artworkUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                        val streamUri = ContentUris.withAppendedId(collection, id).toString()

                        add(
                            Track(
                                id = "local:$id",
                                title = title,
                                artist = artist,
                                thumbnailUrl = artworkUri,
                                streamUrl = streamUri,
                                externalUrl = "",
                                durationText = formatDuration(durationSec),
                                tags = listOf("내 기기")
                            )
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun formatDuration(seconds: Long) = "%d:%02d".format(seconds / 60, seconds % 60)
}

