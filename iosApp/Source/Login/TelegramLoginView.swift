import SwiftUI
import WebKit

/// Telegram's login page. It ends by navigating to the site with `#tgAuthResult=…`; that navigation is
/// caught here and never loaded.
struct TelegramLoginView: UIViewRepresentable {
    let url: URL
    let isReturn: (String) -> Bool
    let onReturn: (String) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(isReturn: isReturn, onReturn: onReturn)
    }

    func makeUIView(context: Context) -> WKWebView {
        // A non-persistent store: the Telegram session isn't kept in the app between sign-ins.
        let configuration = WKWebViewConfiguration()
        configuration.websiteDataStore = .nonPersistent()
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_: WKWebView, context _: Context) {}

    final class Coordinator: NSObject, WKNavigationDelegate {
        private let isReturn: (String) -> Bool
        private let onReturn: (String) -> Void
        private var handled = false

        init(isReturn: @escaping (String) -> Bool, onReturn: @escaping (String) -> Void) {
            self.isReturn = isReturn
            self.onReturn = onReturn
        }

        func webView(
            _: WKWebView,
            decidePolicyFor navigationAction: WKNavigationAction,
            decisionHandler: @escaping @MainActor @Sendable (WKNavigationActionPolicy) -> Void
        ) {
            guard let url = navigationAction.request.url?.absoluteString, isReturn(url) else {
                decisionHandler(.allow)
                return
            }
            decisionHandler(.cancel)
            if !handled {
                handled = true
                onReturn(url)
            }
        }
    }
}
