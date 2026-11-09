package events.around.android.city

import androidx.compose.ui.graphics.Color

private val Gradients = listOf(
    listOf(Color(0xFF5856D6), Color(0xFFAF52DE)),
    listOf(Color(0xFF30B0C7), Color(0xFF007AFF)),
    listOf(Color(0xFFFF9500), Color(0xFFFF2D55)),
    listOf(Color(0xFF34C759), Color(0xFF30B0C7)),
)

/** A gradient per city, by its place in the list: the backend has no city photos, so colours tell them apart. */
fun cityGradient(index: Int): List<Color> = Gradients[Math.floorMod(index, Gradients.size)]

/** The same for a city known only by its slug, stable across launches. */
fun cityGradient(slug: String): List<Color> = cityGradient(slug.sumOf { it.code })
