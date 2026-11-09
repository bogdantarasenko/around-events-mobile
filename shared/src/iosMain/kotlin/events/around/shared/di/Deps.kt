package events.around.shared.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogWriter
import events.around.i18n.AroundStrings
import events.around.i18n.aroundStringsFor
import events.around.shared.AppConfig
import events.around.shared.presentation.feed.DiscoverViewModel
import events.around.shared.presentation.feed.EventDetailViewModel
import events.around.shared.presentation.feed.EventsViewModel
import events.around.shared.presentation.feed.MapViewModel
import events.around.shared.presentation.feed.OrganizerViewModel
import events.around.shared.presentation.feed.SearchViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

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

    fun getDiscoverViewModel(): DiscoverViewModel = get()

    fun getMapViewModel(): MapViewModel = get()

    fun getSearchViewModel(): SearchViewModel = get()

    /** [category] null is the whole listing; [citySlug] null is the chosen city. */
    fun getEventsViewModel(category: String?, citySlug: String?): EventsViewModel =
        get { parametersOf(category, citySlug) }

    fun getOrganizerViewModel(slug: String): OrganizerViewModel = get { parametersOf(slug) }

    fun getEventDetailViewModel(eventId: Long): EventDetailViewModel = get { parametersOf(eventId) }

    fun strings(languageTag: String): AroundStrings = aroundStringsFor(languageTag)
}
