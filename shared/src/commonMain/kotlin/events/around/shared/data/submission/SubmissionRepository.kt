package events.around.shared.data.submission

import com.russhwolf.settings.Settings
import events.around.shared.data.auth.SignedInCalls
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.PosterUpload
import events.around.shared.data.network.SubmissionReceipt
import events.around.shared.data.network.apiJson
import events.around.shared.domain.submission.EventForm

/** Sends new submissions and keeps the unfinished one on the device between launches. */
class SubmissionRepository(
    private val api: AroundApi,
    private val calls: SignedInCalls,
    private val settings: Settings,
) {
    /** [posterFrom]: the past event whose poster to reuse when no new one is attached (repeat and fix). */
    suspend fun submit(parts: List<Pair<String, String>>, poster: PosterUpload?, posterFrom: Long?): SubmissionReceipt {
        val all = if (posterFrom != null && poster == null) parts + ("poster_from" to posterFrom.toString()) else parts
        return calls.run { api.submit(all, poster) }
    }

    fun draft(): EventForm? = settings.getStringOrNull(DRAFT_KEY)
        ?.let { runCatching { apiJson.decodeFromString(EventForm.serializer(), it) }.getOrNull() }

    fun saveDraft(form: EventForm) {
        if (form.hasContent) {
            settings.putString(DRAFT_KEY, apiJson.encodeToString(EventForm.serializer(), form))
        } else {
            clearDraft()
        }
    }

    fun clearDraft() = settings.remove(DRAFT_KEY)

    private companion object {
        const val DRAFT_KEY = "submission_draft"
    }
}
