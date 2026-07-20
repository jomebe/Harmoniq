package com.jomebe.harmoniq.player

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

private class PlayerBridge(
    private val onEnded: () -> Unit,
    private val onError: (String) -> Unit
) {
    @JavascriptInterface fun onEnded() = onEnded.invoke()
    @JavascriptInterface fun onError(code: String) = onError.invoke(code)
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    onEnded: () -> Unit,
    onError: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val bridge = remember(onEnded, onError) { PlayerBridge(onEnded, onError) }
    val webViewRef = remember { arrayOfNulls<WebView>(1) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()
            WebView(context).apply {
                webViewRef[0] = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                addJavascriptInterface(bridge, "PlayerBridge")
                webViewClient = object : WebViewClientCompat() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                        assetLoader.shouldInterceptRequest(request.url)

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        if (!request.isForMainFrame) return false
                        val host = request.url.host.orEmpty()
                        if (host == "appassets.androidplatform.net") return false
                        context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        return true
                    }
                }
                loadUrl("https://appassets.androidplatform.net/assets/player.html?videoId=${Uri.encode(videoId)}")
            }
        },
        update = { view ->
            val expected = "videoId=${Uri.encode(videoId)}"
            if (view.url?.contains(expected) != true) {
                view.loadUrl("https://appassets.androidplatform.net/assets/player.html?$expected")
            }
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> webViewRef[0]?.evaluateJavascript("pausePlayback()", null)
                Lifecycle.Event.ON_DESTROY -> webViewRef[0]?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webViewRef[0]?.removeJavascriptInterface("PlayerBridge")
            webViewRef[0]?.destroy()
            webViewRef[0] = null
        }
    }
}
