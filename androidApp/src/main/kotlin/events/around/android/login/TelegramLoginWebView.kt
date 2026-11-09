package events.around.android.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import events.around.android.ui.strings.LocalAroundStrings

/**
 * Telegram's login page in a web view. It ends by navigating to the site with `#tgAuthResult=…`; that navigation
 * is caught here and never loaded. [onReturn] gets the URL and says whether it carried a login.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TelegramLoginWebView(
    loginUrl: String,
    isReturn: (String) -> Boolean,
    onReturn: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val currentIsReturn by rememberUpdatedState(isReturn)
    val currentOnReturn by rememberUpdatedState(onReturn)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(strings.signInTelegram) },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                    ) { Icon(Icons.Default.Close, contentDescription = strings.loginCancel) }
                },
            )
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        private var handled = false

                        private fun intercept(url: String): Boolean {
                            if (!currentIsReturn(url)) return false
                            if (!handled) {
                                handled = true
                                currentOnReturn(url)
                            }
                            return true
                        }

                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                            intercept(request.url.toString())

                        // A script-driven redirect may skip shouldOverrideUrlLoading; catch it as it starts.
                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            if (intercept(url)) view.stopLoading() else super.onPageStarted(view, url, favicon)
                        }
                    }
                    loadUrl(loginUrl)
                }
            },
        )
    }
}
