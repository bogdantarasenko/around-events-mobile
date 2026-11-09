package events.around.android.miniapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import events.around.android.ui.components.openUrl
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.domain.miniapp.MiniAppLinks
import events.around.shared.domain.miniapp.MiniAppScreen
import org.koin.compose.koinInject

/**
 * The around.events mini-app inside the app: submitting an event, or the organizer's own events. It is the same
 * page as on the open web, opened with the app's city, language and theme. The site and Telegram's login stay
 * in the WebView; other links open in the browser. Telegram's login opens a popup, which becomes a dialog.
 */
@Composable
fun MiniAppScreen(
    screen: MiniAppScreen,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    links: MiniAppLinks = koinInject(),
) {
    val strings = LocalAroundStrings.current
    var failed by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (screen == MiniAppScreen.Submit) strings.feedSubmit else strings.myEvents) },
                actions = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Filled.Close, contentDescription = strings.close) }
                },
            )
        },
    ) { padding ->
        if (failed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            ) {
                Text(strings.miniAppOffline)
                Button(onClick = {
                    failed = false
                    webView?.loadUrl(links.url(screen))
                }) { Text(strings.retry) }
            }
        } else {
            MiniAppWebView(
                url = links.url(screen),
                siteHost = links.host,
                onFailure = { failed = true },
                onCreated = { webView = it },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MiniAppWebView(
    url: String,
    siteHost: String,
    onFailure: () -> Unit,
    onCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // The poster picker: <input type="file"> asks the app to pick a file.
    var pendingFiles by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingFiles?.onReceiveValue(if (uri == null) emptyArray() else arrayOf(uri))
        pendingFiles = null
    }
    val view = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            webViewClient = SiteClient(siteHost, onFailure)
            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message,
                ): Boolean {
                    // A popup web view in a dialog, created from the page's message so window.opener works.
                    val popup = WebView(view.context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                    }
                    val dialog = Dialog(view.context).apply {
                        setContentView(
                            popup,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                        setOnDismissListener { popup.destroy() }
                    }
                    popup.webViewClient = SiteClient(siteHost, onFailure = {}, onExternal = { dialog.dismiss() })
                    popup.webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView) = dialog.dismiss()
                    }
                    (resultMsg.obj as android.webkit.WebView.WebViewTransport).webView = popup
                    resultMsg.sendToTarget()
                    dialog.show()
                    return true
                }

                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    pendingFiles?.onReceiveValue(emptyArray())
                    pendingFiles = filePathCallback
                    pickFile.launch(
                        fileChooserParams.acceptTypes.firstOrNull()?.takeIf { it.isNotBlank() } ?: "image/*",
                    )
                    return true
                }
            }
            loadUrl(url)
            onCreated(this)
        }
    }
    DisposableEffect(view) { onDispose { view.destroy() } }
    AndroidView(factory = { view }, modifier = modifier)
}

/** Keeps the site and Telegram inside; sends everything else to the browser. */
private class SiteClient(
    private val siteHost: String,
    private val onFailure: () -> Unit,
    private val onExternal: () -> Unit = {},
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        val host = uri.host?.lowercase().orEmpty()
        val inside = host == siteHost || host == "telegram.org" || host.endsWith(".telegram.org")
        val web = uri.scheme == "http" || uri.scheme == "https"
        if (web && inside) return false
        if (web) {
            view.context.openUrl(
                uri.toString(),
            )
        } else {
            runCatching { view.context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        }
        onExternal()
        return true
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (request.isForMainFrame) onFailure()
    }
}
