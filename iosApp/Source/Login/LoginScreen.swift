import AroundKit
import SwiftUI

struct LoginScreen: View {
    let state: SessionUiState
    let viewModel: SessionViewModel
    let languageTag: String
    @Environment(\.strings) private var strings
    @State private var loginURL: URL?

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "calendar")
                .font(.system(size: 52))
                .foregroundStyle(.tint)
            Text(strings.tabEvents)
                .font(.title2.weight(.semibold))
            Text(strings.loginIntro)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
            if state.isExpired {
                ErrorBanner(message: strings.loginExpired)
            }
            if state.canSignIn {
                Button {
                    loginURL = viewModel.loginUrl(languageTag: languageTag).flatMap(URL.init(string:))
                } label: {
                    Text(strings.signInTelegram).frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
            } else if state.preparationFailed {
                ErrorBanner(message: strings.loginUnavailable, actionLabel: strings.retry) {
                    viewModel.refreshSite()
                }
            } else {
                ProgressView()
            }
            Spacer()
        }
        .padding(24)
        .sheet(item: $loginURL) { url in
            NavigationStack {
                TelegramLoginView(
                    url: url,
                    isReturn: { viewModel.isLoginReturn(url: $0) },
                    onReturn: { returned in
                        _ = viewModel.onLoginReturn(url: returned)
                        loginURL = nil
                    }
                )
                .navigationTitle(strings.signInTelegram)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button(strings.loginCancel) { loginURL = nil }
                    }
                }
            }
        }
    }
}
