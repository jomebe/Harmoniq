package com.jomebe.harmoniq.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.InkRaised
import com.jomebe.harmoniq.ui.theme.TextSecondary
import com.jomebe.harmoniq.ui.theme.Violet

@Composable
fun AuroraBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x443B1D79), Color.Transparent),
                    radius = 950f
                )
            )
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D0E17), Color(0xFF08090F)))
            )
    ) { content() }
}

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = track.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                listOf(track.artist, track.durationText).filter(String::isNotBlank).joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        trailing?.invoke() ?: IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = TextSecondary)
        }
    }
}

@Composable
fun ArtworkCard(track: Track, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(164.dp).clickable(onClick = onClick)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Violet, spotColor = Cyan)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xDDFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.Black)
            }
        }
        Spacer(Modifier.height(11.dp))
        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MiniPlayer(track: Track, onOpen: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF242036), InkRaised)))
            .clickable(onClick = onOpen)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(13.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onOpen) { Icon(Icons.Default.PlayArrow, "플레이어 열기") }
        IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "다음 곡") }
    }
}

@Composable
fun EmptyState(title: String, description: String) {
    Column(
        Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(66.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Violet.copy(alpha = .45f), Cyan.copy(alpha = .35f)))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.PlayArrow, null, Modifier.size(34.dp)) }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(description, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}
