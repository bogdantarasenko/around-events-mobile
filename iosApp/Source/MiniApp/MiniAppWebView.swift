import SwiftUI
import WebKit

/// A WKWebView for the mini-app. The site and Telegram's login stay inside; other links open in the browser.
/// Telegram's login opens a popup window, which becomes a sheet with a second web view over this one.
struct MiniAppWebView: UIViewRepresentable {
    let url: String
    let siteHost: String
    /// Changing it reloads the page after a failure.
    var attempt = 0
    let onFailure: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    /// The mini-app's own page background (`BACKGROUND` in its scheme.ts), shown before the page draws.
    static func background(dark: Bool) -> UIColor {
        dark ? .black : UIColor(red: 0xF4 / 255, green: 0xF5 / 255, blue: 0xF7 / 255, alpha: 1)
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> WKWebView {
        // The default, persistent store: the mini-app keeps the Telegram sign-in in localStorage for a day.
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator
        webView.allowsBackForwardNavigationGestures = true
        webView.scrollView.contentInsetAdjustmentBehavior = .never
        // No white flash while the page loads: the view is the mini-app's colour from the first frame.
        webView.isOpaque = false
        webView.backgroundColor = Self.background(dark: colorScheme == .dark)
        webView.scrollView.backgroundColor = webView.backgroundColor
        webView.overrideUserInterfaceStyle = colorScheme == .dark ? .dark : .light
        if let url = URL(string: url) { webView.load(URLRequest(url: url)) }
        context.coordinator.attempt = attempt
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        context.coordinator.parent = self
        webView.backgroundColor = Self.background(dark: colorScheme == .dark)
        webView.scrollView.backgroundColor = webView.backgroundColor
        webView.overrideUserInterfaceStyle = colorScheme == .dark ? .dark : .light
        if attempt != context.coordinator.attempt {
            context.coordinator.attempt = attempt
            if let url = URL(string: url) { webView.load(URLRequest(url: url)) }
        }
    }

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        var parent: MiniAppWebView
        var attempt = 0
        private var popup: PopupController?

        init(_ parent: MiniAppWebView) {
            self.parent = parent
        }

        // Telegram's own pages (the login, its redirect) belong to the flow; anything else is a link out.
        private func staysInside(_ url: URL) -> Bool {
            guard let host = url.host?.lowercased() else { return true }
            return host == parent.siteHost || host == "telegram.org" || host.hasSuffix(".telegram.org")
        }

        func webView(
            _ webView: WKWebView,
            decidePolicyFor action: WKNavigationAction,
            decisionHandler: @escaping @MainActor @Sendable (WKNavigationActionPolicy) -> Void
        ) {
            guard let url = action.request.url, action.targetFrame?.isMainFrame != false else {
                decisionHandler(.allow)
                return
            }
            if ["http", "https"].contains(url.scheme ?? "") && !staysInside(url) {
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
            } else if !["http", "https", "about", "blob"].contains(url.scheme ?? "") {
                // tg://, mailto:, tel: and the like.
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
            } else {
                decisionHandler(.allow)
            }
        }

        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            if (error as NSError).code != NSURLErrorCancelled { parent.onFailure() }
        }

        // window.open: the Telegram login window as a sheet; an outside link in the browser.
        func webView(
            _ webView: WKWebView,
            createWebViewWith configuration: WKWebViewConfiguration,
            for action: WKNavigationAction,
            windowFeatures: WKWindowFeatures
        ) -> WKWebView? {
            guard let url = action.request.url else { return nil }
            if !staysInside(url) {
                UIApplication.shared.open(url)
                return nil
            }
            let controller = PopupController(configuration: configuration)
            controller.onClose = { [weak self] in self?.popup = nil }
            popup = controller
            webView.window?.rootViewController?.topMost.present(controller, animated: true)
            return controller.webView
        }

        func webViewDidClose(_ webView: WKWebView) {
            if webView === popup?.webView {
                popup?.dismiss(animated: true)
                popup = nil
            }
        }
    }
}

/// The sheet holding a popup web view. Its web view is created with the configuration the page asked for, so
/// `window.opener` works and the login result reaches the mini-app.
final class PopupController: UIViewController {
    let webView: WKWebView
    var onClose: (() -> Void)?

    init(configuration: WKWebViewConfiguration) {
        webView = WKWebView(frame: .zero, configuration: configuration)
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .pageSheet
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { nil }

    override func loadView() {
        view = webView
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        onClose?()
    }
}

private extension UIViewController {
    var topMost: UIViewController {
        presentedViewController?.topMost ?? self
    }
}
