package events.around.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An organizer's own event, as `GET /api/me/events` returns it (`MyEventResource` on the backend).
 * Dates are `yyyy-MM-dd`, times `HH:mm`, both in the city's local time.
 */
@Serializable
data class MyEvent(
    val id: Long,
    @SerialName("status") val statusName: String,
    @SerialName("skip_reason") val skipReason: String? = null,
    val editable: Boolean = false,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    val title: String,
    val description: String? = null,
    val city: City,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val venue: String? = null,
    val address: String? = null,
    @SerialName("price_from") val priceFrom: String? = null,
    @SerialName("event_url") val eventUrl: String? = null,
    @SerialName("tickets_url") val ticketsUrl: String? = null,
    val languages: List<String> = emptyList(),
    @SerialName("with_kids") val withKids: Boolean? = null,
    val recurrence: Recurrence? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    val category: String? = null,
    @SerialName("category_suggestion") val categorySuggestion: String? = null,
    @SerialName("price_note") val priceNote: String? = null,
    val organization: String? = null,
    @SerialName("submitted_at") val submittedAt: String? = null,
    val posts: List<PublishedPost> = emptyList(),
    @SerialName("duplicate_of") val duplicateOf: DuplicateOf? = null,
) {
    val status: EventStatus get() = EventStatus.fromApi(statusName)

    /** The last day the event runs: a multi-day event lives until its end date. */
    val lastDay: String? get() = if (endDate != null && startDate != null && endDate >= startDate) endDate else startDate

    val telegramPost: PublishedPost? get() = posts.firstOrNull { it.target == PublishedPost.TELEGRAM }
    val threadsPost: PublishedPost? get() = posts.firstOrNull { it.target == PublishedPost.THREADS }

    /** Edits apply only while the moderator hasn't decided: a published post isn't rewritten from here. */
    val canEdit: Boolean get() = editable && status == EventStatus.Review
    val canCancel: Boolean get() = editable
    val canFix: Boolean get() = status == EventStatus.Rejected
    val canRepeat: Boolean get() = status != EventStatus.Rejected
}

/** Where the event is now, from the organizer's point of view. */
enum class EventStatus {
    Review,
    Queued,
    Published,
    Rejected,
    Duplicate,
    Cancelled,
    ;

    companion object {
        // An unknown status from a newer backend reads as "in review" rather than failing the whole list.
        fun fromApi(value: String): EventStatus = when (value) {
            "queued" -> Queued
            "published" -> Published
            "rejected" -> Rejected
            "duplicate" -> Duplicate
            "cancelled" -> Cancelled
            else -> Review
        }
    }
}

@Serializable
data class City(
    val slug: String,
    val name: String,
)

@Serializable
data class Recurrence(
    val freq: String,
    val until: String,
    val first: String? = null,
)

@Serializable
data class PublishedPost(
    val target: String,
    val url: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
) {
    companion object {
        const val TELEGRAM = "telegram"
        const val THREADS = "threads"
    }
}

@Serializable
data class DuplicateOf(
    val id: Long,
    val title: String,
    val posts: List<PublishedPost> = emptyList(),
) {
    val telegramPost: PublishedPost? get() = posts.firstOrNull { it.target == PublishedPost.TELEGRAM }
}
