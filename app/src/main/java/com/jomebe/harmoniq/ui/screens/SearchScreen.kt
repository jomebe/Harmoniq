package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.domain.Artist
import com.jomebe.harmoniq.ui.components.EmptyState
import com.jomebe.harmoniq.ui.components.TrackRow
import com.jomebe.harmoniq.ui.theme.Cyan

@Composable
fun SearchScreen(
    query: String,
    results: List<Track>,
    artists: List<Artist>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onArtist: (Artist) -> Unit,
    onPlay: (Track, List<Track>) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "음악 검색",
            Modifier.padding(start = 20.dp, top = 18.dp, bottom = 14.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            placeholder = { Text("곡, 아티스트, 앨범 검색") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Cyan) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, "지우기") }
            },
            shape = RoundedCornerShape(22.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        if (results.isEmpty() && artists.isEmpty()) {
            EmptyState("음악과 아티스트를 찾아보세요", if (query.isBlank()) "YouTube 음악과 내 기기 음악을 함께 검색합니다" else "검색 결과가 없습니다. 내 기기 음악도 확인해 보세요.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
            ) {
                if (artists.isNotEmpty()) {
                    item { Text("아티스트", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(artists, key = Artist::id) { artist ->
                        Text(artist.name, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).clickable { onArtist(artist) }, style = MaterialTheme.typography.titleMedium, color = Cyan)
                    }
                    item { Text("YouTube 곡 · 내 기기 음악", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                }
                items(results, key = Track::id) { track ->
                    TrackRow(track, { onPlay(track, results) })
                }
            }
        }
    }
}
