import AroundKit
import SwiftUI

@main
struct AroundApp: App {
    init() {
        let info = Bundle.main.infoDictionary ?? [:]
        let baseURL = info["AroundBaseURL"] as? String ?? "https://around.events"
        let isDebug = (info["AroundIsDebug"] as? String) == "YES"
        let version = info["CFBundleShortVersionString"] as? String ?? "0"
        DepsKt.startAround(baseUrl: baseURL, versionName: version, isDebug: isDebug)
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
