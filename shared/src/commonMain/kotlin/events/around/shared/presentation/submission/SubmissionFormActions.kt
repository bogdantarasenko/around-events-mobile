package events.around.shared.presentation.submission

import events.around.core.model.Repeat
import events.around.shared.domain.submission.DayMode
import events.around.shared.domain.submission.Pricing

/** What the form screen can do, so its sections take these instead of the view model itself. */
interface SubmissionFormActions {
    fun setCity(slug: String)
    fun setTitle(value: String)
    fun setCategory(value: String)
    fun setCategorySuggestion(value: String)
    fun toggleLanguage(code: String)
    fun setWithKids(value: Boolean)
    fun setDayMode(value: DayMode)
    fun setStartDate(value: String)
    fun setStartTime(value: String)
    fun setEndTime(value: String)
    fun setEndDate(value: String)
    fun setRepeat(value: Repeat)
    fun setRepeatUntil(value: String)
    fun setVenue(value: String)
    fun setAddress(value: String)
    fun setPricing(value: Pricing)
    fun setPriceFrom(value: String)
    fun setPriceNote(value: String)
    fun setEventUrl(value: String)
    fun setTicketsUrl(value: String)
    fun setDescription(value: String)
    fun setOrganization(value: String)
    fun setAcceptedRules(value: Boolean)
    fun pickPoster(bytes: ByteArray, fileName: String, mimeType: String)
    fun removePoster()
    fun startOver()
    fun submit()
}
