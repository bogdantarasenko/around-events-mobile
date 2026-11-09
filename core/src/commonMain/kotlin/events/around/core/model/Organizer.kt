package events.around.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An organizer's public page — `GET /api/organizers/{slug}`, or a row of `GET /api/cities/{city}/organizers`
 * without [events], [followUrl] and [pastCount].
 */
@Serializable
data class Organizer(
    val slug: String,
    val name: String,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    /** around.events/o/slug. */
    val url: String,
    @SerialName("link_urls") val links: OrganizerLinks = OrganizerLinks(),
    @SerialName("upcoming_count") val upcomingCount: Int = 0,
    /** Opens the bot, which follows the organizer: following lives in Telegram, not in the app. */
    @SerialName("follow_url") val followUrl: String? = null,
    @SerialName("past_count") val pastCount: Int = 0,
    val events: List<Event> = emptyList(),
)

@Serializable
data class OrganizerLinks(
    val instagram: String? = null,
    val telegram: String? = null,
    val website: String? = null,
)
