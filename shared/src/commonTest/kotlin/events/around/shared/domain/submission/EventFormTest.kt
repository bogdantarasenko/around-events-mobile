package events.around.shared.domain.submission

import events.around.core.model.City
import events.around.core.model.MAX_POSTER_BYTES
import events.around.core.model.MyEvent
import events.around.core.model.Recurrence
import events.around.core.model.Repeat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EventFormTest {
    private val complete = EventForm(
        city = "krakow",
        title = "Jazz night",
        category = "concert",
        startDate = "2026-12-01",
        venue = "Klub",
        description = "Ten words about the evening.",
        pricing = Pricing.Free,
        acceptedRules = true,
    )

    @Test
    fun `parts match what the web form sends`() {
        val parts = complete.copy(
            languages = listOf("uk", "en"),
            organization = " Fund ",
            ticketsUrl = "https://t.example",
        ).toParts()

        assertEquals(listOf("uk", "en"), parts.filter { it.first == "languages[]" }.map { it.second })
        val single = parts.toMap()
        assertEquals("1", single["is_free"])
        assertEquals("0", single["with_kids"])
        assertEquals("", single["price_from"])
        assertEquals("Fund", single["organizer[organization]"])
        assertEquals("https://t.example", single["tickets_url"])
        assertEquals("1", single["accepted_rules"])
        assertEquals("", single["repeat"])
        assertEquals("", single["category_suggestion"])
    }

    @Test
    fun `kids category always sends with_kids and other keeps its suggestion`() {
        assertEquals("1", complete.copy(category = "kids").toParts().toMap()["with_kids"])
        assertEquals(
            "Board games",
            complete.copy(
                category = "other",
                categorySuggestion = " Board games ",
            ).toParts().toMap()["category_suggestion"],
        )
    }

    @Test
    fun `only the chosen day mode sends its fields`() {
        val form = complete.copy(endDate = "2026-12-03", repeatUntil = "2027-01-01", repeat = Repeat.Biweekly)

        val oneDay = form.toParts().toMap()
        assertEquals("", oneDay["end_date"])
        assertEquals("", oneDay["repeat_until"])

        val multi = form.copy(dayMode = DayMode.MultiDay).toParts().toMap()
        assertEquals("2026-12-03", multi["end_date"])
        assertEquals("", multi["repeat"])

        val repeats = form.copy(dayMode = DayMode.Repeats).toParts().toMap()
        assertEquals("biweekly", repeats["repeat"])
        assertEquals("2027-01-01", repeats["repeat_until"])
        assertEquals("", repeats["end_date"])
    }

    @Test
    fun `unknown price clears tickets and paid needs a price or a note`() {
        val unknown = complete.copy(
            pricing = Pricing.Unknown,
            ticketsUrl = "https://t",
            priceNote = "x",
        ).toParts().toMap()
        assertEquals("", unknown["tickets_url"])
        assertEquals("", unknown["price_note"])

        assertEquals(FormField.PriceFrom, complete.copy(pricing = Pricing.Paid).firstProblem(null))
        assertNull(complete.copy(pricing = Pricing.Paid, priceNote = "60 zł a day").firstProblem(null))
    }

    @Test
    fun `problems come in the order the form reads`() {
        assertEquals(FormField.Title, EventForm().firstProblem(null))
        assertEquals(FormField.Category, complete.copy(category = "").firstProblem(null))
        assertEquals(FormField.Poster, complete.firstProblem(MAX_POSTER_BYTES + 1))
        assertEquals(FormField.RepeatUntil, complete.copy(dayMode = DayMode.Repeats).firstProblem(null))
        assertEquals(
            FormField.EndDate,
            complete.copy(dayMode = DayMode.MultiDay, endDate = "2026-11-30").firstProblem(null),
        )
        assertEquals(FormField.Description, complete.copy(description = "short").firstProblem(null))
        assertEquals(FormField.Pricing, complete.copy(pricing = null).firstProblem(null))
        assertEquals(FormField.AcceptedRules, complete.copy(acceptedRules = false).firstProblem(null))
        assertNull(complete.firstProblem(null))
    }

    @Test
    fun `repeat drops the dates, fix keeps them, the rules are accepted anew`() {
        val event = MyEvent(
            id = 5,
            statusName = "rejected",
            title = "Club",
            city = City("warsaw", "Warszawa"),
            startDate = "2026-12-01",
            endDate = "2026-12-01",
            recurrence = Recurrence(freq = "weekly", until = "2027-02-01"),
            priceFrom = "80.00",
            category = "lecture",
        )

        val repeat = event.toForm(keepDates = false)
        assertEquals("", repeat.startDate)
        assertEquals(DayMode.OneDay, repeat.dayMode)
        assertEquals("", repeat.repeatUntil)
        assertEquals(Pricing.Paid, repeat.pricing)
        assertEquals("80", repeat.priceFrom)
        assertEquals(false, repeat.acceptedRules)

        val fix = event.toForm(keepDates = true)
        assertEquals("2026-12-01", fix.startDate)
        assertEquals(DayMode.Repeats, fix.dayMode)
        assertEquals("2027-02-01", fix.repeatUntil)

        assertEquals(Pricing.Free, event.copy(priceFrom = "0.00").toForm(true).pricing)
        assertEquals(Pricing.Unknown, event.copy(priceFrom = null).toForm(true).pricing)
        assertEquals("", event.copy(category = "retired").toForm(true).category)
    }

    @Test
    fun `server fields map onto form fields`() {
        assertEquals(FormField.Venue, serverField("address"))
        assertEquals(FormField.RepeatUntil, serverField("repeat"))
        assertNull(serverField("organizer.email"))
    }
}
