package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.ui.components.ArtworkCard
import com.jomebe.harmoniq.ui.components.TrackRow
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    personalized: List<Track>,
    popular: List<Track>,
    onPlay: (Track, List<Track>) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("HARMONIQ", color = Cyan, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    Text("오늘은 뭘 들어볼까요?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, "알림") }
            }
        }

        popular.firstOrNull()?.let { hero ->
            item {
                Box(
                    Modifier.fillMaxWidth().height(260.dp).padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(30.dp)).clickable { onPlay(hero, popular) }
                ) {
                    AsyncImage(hero.thumbnailUrl, hero.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE08090F)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(16.dp))
                            Text("  지금 인기 급상승", color = Cyan, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(hero.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text(hero.artist, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { onPlay(hero, popular) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) { Icon(Icons.Default.PlayArrow, null); Text("바로 재생") }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }

        item { SectionTitle("취향을 반영한 믹스", "앱에서 들은 아티스트와 장르를 바탕으로 골랐어요") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(personalized.ifEmpty { popular.take(12) }, key = Track::id) { track ->
                    ArtworkCard(track, { onPlay(track, personalized.ifEmpty { popular }) })
                }
            }
            Spacer(Modifier.height(30.dp))
        }

        item { SectionTitle("Jamendo 인기 음악", "무료로 들을 수 있는 Jamendo 음악을 넉넉하게 모았어요") }
        items(popular.take(40), key = Track::id) { track ->
            TrackRow(track, { onPlay(track, popular) }, Modifier.padding(horizontal = 12.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
