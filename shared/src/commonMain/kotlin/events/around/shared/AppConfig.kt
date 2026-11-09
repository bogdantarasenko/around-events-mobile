package events.around.shared

/** What a platform tells the shared code at start-up. */
data class AppConfig(
    val baseUrl: String,
    val versionName: String,
    val isDebug: Boolean,
)
