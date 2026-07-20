package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.layout.Column
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
import com.jomebe.harmoniq.ui.components.EmptyState
import com.jomebe.harmoniq.ui.components.TrackRow
import com.jomebe.harmoniq.ui.theme.Cyan

@Composable
fun SearchScreen(
    query: String,
    results: List<Track>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
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

        if (results.isEmpty()) {
            EmptyState("듣고 싶은 음악을 찾아보세요", "Audius의 공개 음악 카탈로그를 검색할 수 있어요")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
            ) {
                items(results, key = Track::id) { track ->
                    TrackRow(track, { onPlay(track, results) })
                }
            }
        }
    }
}
