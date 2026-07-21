package com.jomebe.harmoniq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.Ink
import com.jomebe.harmoniq.ui.theme.TextSecondary
import com.jomebe.harmoniq.ui.theme.Violet

@Composable
fun PlayerScreen(
    track: Track,
    isSaved: Boolean,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTogglePlayback: () -> Unit,
    onToggleSaved: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1F1832), Ink)))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.ExpandMore, "닫기", Modifier.size(34.dp)) }
                Text("지금 재생 중", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onToggleSaved) {
                    Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "저장", tint = if (isSaved) Color(0xFFFB7185) else Color.White)
                }
            }
            Spacer(Modifier.height(20.dp))
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(32.dp))
            )
            Spacer(Modifier.height(30.dp))
            Text(track.title, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(track.artist, color = TextSecondary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(34.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Default.SkipPrevious, "이전 곡", Modifier.size(42.dp))
                }
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier.size(82.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Violet, Cyan)))
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isPlaying) "일시정지" else "재생",
                        Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Default.SkipNext, "다음 곡", Modifier.size(42.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("내 기기 음악 · 백그라운드 재생 · 자동 다음 곡", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        }
    }
}
