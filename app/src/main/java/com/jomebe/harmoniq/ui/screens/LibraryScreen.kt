package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.ui.components.EmptyState
import com.jomebe.harmoniq.ui.components.TrackRow

@Composable
fun LibraryScreen(history: List<Track>, saved: List<Track>, onPlay: (Track, List<Track>) -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    val tracks = if (selected == 0) saved else history
    Column(Modifier.fillMaxSize()) {
        Text(
            "내 라이브러리",
            Modifier.padding(start = 20.dp, top = 18.dp, bottom = 12.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            FilterChip(selected == 0, { selected = 0 }, { Text("저장한 곡 ${saved.size}") })
            FilterChip(selected == 1, { selected = 1 }, { Text("최근 재생 ${history.size}") }, modifier = Modifier.padding(start = 8.dp))
        }
        if (tracks.isEmpty()) {
            EmptyState(if (selected == 0) "아직 저장한 곡이 없어요" else "재생 기록이 없어요", "음악을 재생하면 여기에 차곡차곡 모여요")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                items(tracks, key = Track::id) { track -> TrackRow(track, { onPlay(track, tracks) }) }
            }
        }
    }
}
