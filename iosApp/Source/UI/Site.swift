import Foundation

/// around.events, or the server the build points at (`AroundBaseURL` in Info.plist).
enum Site {
    static var url: URL {
        let configured = Bundle.main.infoDictionary?["AroundBaseURL"] as? String
        return configured.flatMap { URL(string: $0) } ?? URL(string: "https://around.events")
            ?? URL(fileURLWithPath: "/")
    }

    /// The mini-app opened in a browser: it takes submissions outside Telegram too.
    static var submit: URL { url }

    static var rules: URL { url.appending(path: "rules") }
}
