package com.jomebe.harmoniq.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jomebe.harmoniq.domain.Track

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerScreen(track: Track, onClose: () -> Unit) {
    val videoId = track.externalUrl.substringAfter("v=", "").substringBefore('&')
    var loadError by remember(videoId) { mutableStateOf(false) }
    val playerHtml = remember(videoId) {
        """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
          <style>
            html, body, iframe { width:100%; height:100%; margin:0; padding:0; border:0; background:#000; overflow:hidden; }
          </style>
        </head>
        <body>
          <iframe
            src="https://www.youtube.com/embed/$videoId?autoplay=1&amp;playsinline=1&amp;rel=0&amp;enablejsapi=1&amp;origin=https%3A%2F%2Fwww.youtube.com"
            title="YouTube video player"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            referrerpolicy="strict-origin-when-cross-origin"
            allowfullscreen>
          </iframe>
        </body>
        </html>
        """.trimIndent()
    }

    Column(Modifier.fillMaxSize().background(ComposeColor.Black)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "닫기") }
            Text(track.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(Color.BLACK)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                if (request.isForMainFrame) loadError = true
                            }
                        }
                        loadDataWithBaseURL("https://www.youtube.com/", playerHtml, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    if (webView.url == null) webView.loadDataWithBaseURL("https://www.youtube.com/", playerHtml, "text/html", "UTF-8", null)
                },
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.loadUrl("about:blank")
                    webView.destroy()
                }
            )
            if (loadError) {
                Text(
                    "YouTube 플레이어를 불러오지 못했습니다. 네트워크 연결과 Android System WebView 업데이트를 확인해 주세요.",
                    modifier = Modifier.padding(28.dp),
                    color = ComposeColor.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
