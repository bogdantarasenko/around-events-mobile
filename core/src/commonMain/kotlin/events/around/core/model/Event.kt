package events.around.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An event in a city's listing, as `GET /api/cities/{city}/events` returns it (`PublicEventResource` on the
 * backend). Dates are `yyyy-MM-dd`, times `HH:mm`, both in the city's local time.
 */
@Serializable
data class Event(
    val id: Long,
    val title: String,
    val description: String? = null,
    val city: City,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val venue: String? = null,
    val address: String? = null,
    /** Found by the backend from the place text; null until then, or when it found nothing. */
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("price_from") val priceFrom: String? = null,
    val currency: String? = null,
    val languages: List<String> = emptyList(),
    @SerialName("with_kids") val withKids: Boolean? = null,
    val recurrence: Recurrence? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("event_url") val eventUrl: String? = null,
    @SerialName("tickets_url") val ticketsUrl: String? = null,
    /** The post in the city's Telegram channel, when the event went out there. */
    @SerialName("post_url") val postUrl: String? = null,
    /** Our key for a submission, the source's own word for an import (`games`, `creative`…). */
    val category: String? = null,
    /** Only when the organizer's public page is switched on. */
    val organizer: EventOrganizer? = null,
) {
    /** The last day the event runs: a multi-day event lives until its end date. */
    val lastDay: String? get() = if (endDate != null && startDate != null && endDate >= startDate) {
        endDate
    } else {
        startDate
    }

    val hasLocation: Boolean get() = latitude != null && longitude != null
}

@Serializable
data class City(
    val slug: String,
    val name: String,
    @SerialName("upcoming_count") val upcomingCount: Int = 0,
)

@Serializable
data class Recurrence(
    val freq: String,
    val until: String,
    val first: String? = null,
)

@Serializable
data class EventOrganizer(
    val slug: String,
    val name: String,
    val url: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
