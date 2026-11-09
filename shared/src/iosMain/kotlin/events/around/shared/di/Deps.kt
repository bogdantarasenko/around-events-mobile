package events.around.shared.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogWriter
import events.around.i18n.AroundStrings
import events.around.i18n.aroundStringsFor
import events.around.shared.AppConfig
import events.around.shared.presentation.events.EventDetailViewModel
import events.around.shared.presentation.events.MyEventsViewModel
import events.around.shared.presentation.organizer.OrganizerViewModel
import events.around.shared.presentation.session.SessionViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import events.around.shared.presentation.submission.FormKind
import events.around.shared.presentation.submission.FormMode
import events.around.shared.presentation.submission.SubmissionFormViewModel
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import platform.Foundation.NSData
import platform.posix.memcpy

private lateinit var koin: Koin

/** Called once from the SwiftUI `App` initializer. */
fun startAround(baseUrl: String, versionName: String, isDebug: Boolean) {
    Logger.setLogWriters(NSLogWriter())
    koin = initKoin(AppConfig(baseUrl = baseUrl, versionName = versionName, isDebug = isDebug)).koin
}

/** Entry points for Swift, which cannot call Koin's reified functions. */
object Deps : KoinComponent {
    override fun getKoin(): Koin = koin

    fun getSettingsViewModel(): SettingsViewModel = get()

    fun getSessionViewModel(): SessionViewModel = get()

    fun getMyEventsViewModel(): MyEventsViewModel = get()

    fun getOrganizerViewModel(): OrganizerViewModel = get()

    fun getEventDetailViewModel(eventId: Long): EventDetailViewModel = get { parametersOf(eventId) }

    /** [eventId] is ignored for [FormKind.New]. */
    fun getSubmissionFormViewModel(kind: FormKind, eventId: Long): SubmissionFormViewModel =
        get { parametersOf(FormMode(kind, eventId.takeUnless { kind == FormKind.New })) }

    fun strings(languageTag: String): AroundStrings = aroundStringsFor(languageTag)

    /** Copies a picked image out of Swift's `Data` for the poster upload. */
    fun bytes(data: NSData): ByteArray {
        val size = data.length.toInt()
        val bytes = ByteArray(size)
        if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return bytes
    }
}
