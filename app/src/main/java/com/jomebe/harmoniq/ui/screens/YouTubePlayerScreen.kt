package com.jomebe.harmoniq.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jomebe.harmoniq.domain.Track
import com.jomebe.harmoniq.ui.theme.Cyan
import com.jomebe.harmoniq.ui.theme.TextSecondary
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayerScreen(
    track: Track,
    isPipMode: Boolean = false,
    onEnterPip: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoId = remember(track) {
        val fromUrl = track.externalUrl.substringAfter("v=", "").substringBefore('&')
        if (fromUrl.isNotBlank()) fromUrl else track.id.removePrefix("youtube:")
    }

    var isLoaded by remember(videoId) { mutableStateOf(false) }
    var errorCode by remember(videoId) { mutableIntStateOf(0) }

    val openInYouTubeApp = {
        openYouTubeExternal(context, videoId, preferApp = true)
    }

    val openInBrowser = {
        openYouTubeExternal(context, videoId, preferApp = false)
    }

    Column(Modifier.fillMaxSize().background(ComposeColor.Black)) {
        // PiP 모드가 아닐 때만 상단 툴바 표시
        if (!isPipMode) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = ComposeColor.White)
                }
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(
                        text = track.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        color = ComposeColor.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = track.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                onEnterPip?.let { enterPipAction ->
                    IconButton(onClick = enterPipAction) {
                        Icon(Icons.Default.PictureInPictureAlt, contentDescription = "팝업(PiP) 모드", tint = Cyan)
                    }
                }
                IconButton(onClick = openInYouTubeApp) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "YouTube 앱에서 열기", tint = Cyan)
                }
                IconButton(onClick = openInBrowser) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = "브라우저에서 열기", tint = TextSecondary)
                }
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        enableAutomaticInitialization = false
                        lifecycleOwner.lifecycle.addObserver(this)
                        val options = IFramePlayerOptions.Builder(ctx)
                            .controls(1)
                            .autoplay(1)
                            .build()
                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                isLoaded = true
                                youTubePlayer.loadVideo(videoId, 0f)
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                errorCode = error.ordinal + 1
                                isLoaded = true
                            }
                        }, options)
                    }
                },
                onRelease = { playerView ->
                    lifecycleOwner.lifecycle.removeObserver(playerView)
                    playerView.release()
                }
            )

            // 로딩 중 표시
            if (!isLoaded && !isPipMode) {
                CircularProgressIndicator(
                    color = Cyan,
                    modifier = Modifier.size(48.dp)
                )
            }

            // 에러 또는 저작권 임베드 제한(101, 150 등) 발생 시 안내 오버레이
            if (errorCode != 0 && !isPipMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor(0xEE0A0A10))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (errorCode == 101 || errorCode == 150) {
                            "저작권 정책으로 인해 앱 내 임베드 재생이 제한된 음원입니다."
                        } else {
                            "플레이어를 불러오는 중 문제가 발생했습니다."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = ComposeColor.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "공식 YouTube 앱 또는 브라우저에서 바로 무료로 감상하실 수 있습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = openInYouTubeApp,
                        colors = ButtonDefaults.buttonColors(containerColor = ComposeColor.White, contentColor = ComposeColor.Black),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("YouTube 앱에서 바로 보기", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = openInBrowser,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Cyan)
                        Spacer(Modifier.width(8.dp))
                        Text("웹 브라우저에서 열기", color = Cyan)
                    }
                }
            }
        }
    }
}

private fun openYouTubeExternal(context: Context, videoId: String, preferApp: Boolean) {
    if (preferApp) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val resolve = context.packageManager.queryIntentActivities(appIntent, 0)
        if (resolve.isNotEmpty()) {
            context.startActivity(appIntent)
            return
        }
    }
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    runCatching { context.startActivity(webIntent) }
}
