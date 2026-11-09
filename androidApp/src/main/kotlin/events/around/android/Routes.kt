package events.around.android

import androidx.navigation3.runtime.NavKey
import events.around.shared.presentation.submission.FormKind
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object TelegramLogin : NavKey

@Serializable
data class EventDetail(val eventId: Long) : NavKey

@Serializable
data class Submission(val kind: FormKind, val eventId: Long? = null) : NavKey

@Serializable
data object OrganizerEdit : NavKey
