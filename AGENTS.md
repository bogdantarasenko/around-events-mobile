# AGENTS.md

## Project

Around is the mobile client for around.events: Android (Jetpack Compose) and iOS (SwiftUI) on top of
shared Kotlin Multiplatform logic. It started from FeedFlow; only the build setup and the KMP-to-Swift
bridge were kept (see `NOTICE`). The product scope is `../around-events-mobile-prd.md`; the backend
is `../around-events`.

The app is the reader side, built after the reader screens of the Figma file `QedJXtS24P3L60Vf2wLvrY`
(page `Luma Recreations`: Discover, category, organizer calendar, map, search, event detail). It uses the
public `GET /api/cities`, `/api/cities/{city}/events`, `/api/cities/{city}/organizers` and
`/api/organizers/{slug}`. It does not repeat what the Telegram mini-app already does — submitting, the
organizer cabinet, editing an organizer page — it opens the mini-app itself for those, in a WebView
(`MiniAppSheet` on iOS, `MiniAppScreen` on Android; the address comes from shared `MiniAppLinks`). The mini-app
reads `?lang=`, `?theme=` and signs in without a popup via `#tgAuthResult`, so keep both sides in step.

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
- Network: use the Koin `HttpClient`; it adds the base URL. Every endpoint the app calls is public.
  JSON uses `apiJson` (unknown keys ignored).
- Business logic in `shared` gets unit tests in `commonTest` (Turbine for flows, fakes over mocks,
  `MapSettings` for preferences, Ktor `MockEngine` for HTTP).

## Kotlin names in Swift

- A Kotlin enum entry `New` reaches Swift as `.theNew` (`new` is reserved in Objective-C).
- Anything named `description` collides with `NSObject.description` and comes out as `description_`
  (`Event.description_`). Don't name new enum entries `Description`.
- A `List<Pair<String, String>>` constant such as `EVENT_LANGUAGES` is `VocabularyKt.EVENT_LANGUAGES`, an array
  of `KotlinPair` whose `first`/`second` need `as String?`.

## Checking offline behaviour

Every answer is cached in settings: `cities`, `feed_<slug>`, `organizers_<slug>`, `organizer_<slug>`. To see the
offline state in the simulator, open the app once online, then rebuild against an unreachable server
(`xcodebuild … 'AROUND_BASE_URL=http://127.0.0.1:9' build`): the cached listing shows with the offline banner.

## Maps

Both platforms use MapLibre (iOS: SPM `maplibre-gl-native-distribution`; Android: `org.maplibre.gl:android-sdk`) on
the OpenFreeMap style `https://tiles.openfreemap.org/styles/liberty` — free, no key. The style URL is
`AroundMapStyleURL` in `iosApp/Info.plist` and `BuildConfig.MAP_STYLE_URL` on Android. Keep the OpenStreetMap
attribution button visible.

## Opening a screen in the simulator

Debug builds read two launch arguments, so screens can be checked without tapping:
`xcrun simctl launch booted events.around.app.debug -AroundTab discover` (or `map`), and
`-AroundRoute event:<id>`, `organizer:<slug>`, `events:<category>`, `city:<slug>`, `cities`, `search` or `settings`
to push a screen on the Home tab; `-AroundMapList YES` opens the map's event list; `-AroundMiniApp submit|me` opens the mini-app.

## Rules

- Comments only where the code isn't self-explanatory.
- try/catch at the top or bottom caller, not around every function.
- Swift: one type per file; `@Observable` over `ObservableObject` except `VMStoreOwner`; no force unwraps.
- Commit messages: one line, imperative.
