# AGENTS.md

## Project

Around is the mobile client for around.events: Android (Jetpack Compose) and iOS (SwiftUI) on top of
shared Kotlin Multiplatform logic. It started from FeedFlow; only the build setup and the KMP-to-Swift
bridge were kept (see `NOTICE`). The product scope is `../around-events-mobile-prd.md`; the backend
is `../around-events`. Build only on endpoints that backend already has.

## Modules

- `core/` — domain models, no platform code
- `i18n/` — strings (en default, uk, ru, be) in `src/commonMain/resources/locale/values-*/strings.xml`
- `shared/` — repositories, network, ViewModels, Koin DI; iOS framework `AroundKit`
- `androidApp/` — Compose UI, Navigation 3
- `iosApp/` — SwiftUI; `Around.xcodeproj` is generated from `project.yml` and not committed
- `build-logic/` — convention plugins `events.around.library`, `.detekt`, `.versioning`

## Commands

Run Gradle with `--quiet --console=plain`. Gradle needs `JAVA_HOME` pointing at a JDK 21+.

- `./gradlew :shared:jvmTest` — shared tests, fastest loop
- `./gradlew :shared:compileKotlinJvm` — quick compile check of shared code
- `./gradlew :androidApp:assembleDebug` — Android debug APK
- `./gradlew detekt` — static analysis
- `.scripts/refresh-translations.sh` — regenerate Kotlin after editing strings
- `iosApp/.scripts/generate-project.sh` — regenerate the Xcode project after changing `project.yml` or adding Swift files
- `xcodebuild -project iosApp/Around.xcodeproj -scheme Around -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build -quiet`

KMP modules build `iosX64` as well as the arm64 targets: the simulator on Intel Macs needs it. Keep
dependencies of `shared` on versions that still publish `iosX64` (this is why the shared ViewModel
library is pinned to lifecycle 2.10.0).

## Adding a feature

- ViewModel in `shared/src/commonMain/kotlin/events/around/shared/presentation/<feature>/`, registered
  in `shared/.../di/Koin.kt`. Expose a `StateFlow<…UiState>`; UI state classes sit at the bottom of the file.
- iOS access: add `fun getXxxViewModel()` to `object Deps` in `shared/src/iosMain/.../di/Deps.kt`.
  Hold it with `@StateObject VMStoreOwner<XxxViewModel>(…)` and read state with `for await` in `.task`.
- Android: screen under `androidApp/src/main/kotlin/events/around/android/<feature>/`, route in
  `Routes.kt`, `entry<Route>` in `MainActivity`.
- Strings: never hardcode user-visible text. Add the key to all four `strings.xml` files.
  Compose: `LocalAroundStrings.current`; SwiftUI: `@Environment(\.strings)`.
- Network: use the Koin `HttpClient`; it adds the base URL and the `Authorization` header from
  `AuthorizationProvider`. JSON uses `apiJson` (unknown keys ignored).
- Business logic in `shared` gets unit tests in `commonTest` (Turbine for flows, fakes over mocks,
  `MapSettings` for preferences, Ktor `MockEngine` for HTTP).

## Rules

- Comments only where the code isn't self-explanatory.
- try/catch at the top or bottom caller, not around every function.
- Swift: one type per file; `@Observable` over `ObservableObject` except `VMStoreOwner`; no force unwraps.
- Commit messages: one line, imperative.
