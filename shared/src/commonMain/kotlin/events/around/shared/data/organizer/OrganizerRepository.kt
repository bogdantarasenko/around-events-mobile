package events.around.shared.data.organizer

import events.around.core.model.Organizer
import events.around.shared.data.auth.SignedInCalls
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.OrganizerInput

/** The organizer's public page: one per Telegram account, so creating and editing share one call. */
class OrganizerRepository(
    private val api: AroundApi,
    private val calls: SignedInCalls,
) {
    suspend fun load(): Organizer? = calls.run { api.organizer() }

    suspend fun save(input: OrganizerInput): Organizer = calls.run { api.saveOrganizer(input) }
}
