# Around

Mobile client for [around.events](https://around.events): what's on in the city, by day, with a category
filter and offline cache. Submitting events stays in the around.events Telegram mini-app, which the app links to.
Android and iOS, shared logic in Kotlin Multiplatform.

Product scope lives in the PRD: `../around-events-mobile-prd.md`. The backend is `../around-events` (Laravel).

## Modules

| Module | What it is |
|---|---|
| `core` | Domain models shared by every layer |
| `i18n` | Strings in en / uk / ru / be, generated into Kotlin by Lyricist |
| `shared` | Networking (Ktor), preferences, ViewModels, DI (Koin); exported to iOS as `AroundKit` |
| `androidApp` | Jetpack Compose UI |
| `iosApp` | SwiftUI UI; Xcode project generated from `project.yml` |
| `build-logic` | Gradle convention plugins |

## Requirements

- JDK 21 or newer to run Gradle (Gradle downloads JDK 21 for compilation by itself)
- Android SDK; put its path in `local.properties` as `sdk.dir=...`
- Xcode and [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`)

## Build and run

```sh
./gradlew :shared:jvmTest                 # shared unit tests (fastest)
./gradlew :androidApp:assembleDebug       # Android debug APK
.scripts/run-android.sh                   # install and launch on a connected device

iosApp/.scripts/generate-project.sh       # generate iosApp/Around.xcodeproj
.scripts/run-ios-iphone-17-pro-simulator.sh
```

The backend address comes from `around.baseUrl` in `gradle.properties` (Android) and `AROUND_BASE_URL`
in `iosApp/Config/Shared.xcconfig` (iOS). To sign iOS builds for a device, put
`AROUND_DEVELOPMENT_TEAM = <team id>` into `iosApp/Config/Local.xcconfig`, which is not committed.

## Strings

Add a key to `i18n/src/commonMain/resources/locale/values/strings.xml` and to each `values-uk`, `values-ru`
and `values-be`, then run `.scripts/refresh-translations.sh`. Compose reads them through
`LocalAroundStrings.current`, SwiftUI through `@Environment(\.strings)`.

## License

Apache License 2.0. Based on [FeedFlow](https://github.com/prof18/feed-flow) by Marco Gomiero — see `NOTICE`.
