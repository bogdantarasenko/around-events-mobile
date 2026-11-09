package events.around.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The organizer's public page, `around.events/o/{slug}` (`OrganizerResource` on the backend). */
@Serializable
data class Organizer(
    val slug: String,
    val name: String,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_public") val isPublic: Boolean = true,
    val url: String,
    val links: OrganizerLinks = OrganizerLinks(),
    @SerialName("link_urls") val linkUrls: Map<String, String> = emptyMap(),
)

/** As the organizer typed them: a handle, a `t.me` link or a full URL. */
@Serializable
data class OrganizerLinks(
    val instagram: String? = null,
    val telegram: String? = null,
    val website: String? = null,
)
