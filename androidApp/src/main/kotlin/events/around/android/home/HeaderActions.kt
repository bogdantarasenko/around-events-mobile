package events.around.android.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import events.around.android.ui.theme.AroundColors

/** One icon of [HeaderActions]: what it shows, what it says to a screen reader, what it does. */
class HeaderAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

/** The white pill with two icons at the top right of Home and Discover: 88 × 44, a soft shadow. */
@Composable
fun HeaderActions(
    first: HeaderAction,
    second: HeaderAction,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = AroundColors.surface,
        shadowElevation = 3.dp,
        modifier = modifier.size(88.dp, 44.dp),
    ) {
        Row {
            listOf(first, second).forEach { action ->
                IconButton(onClick = action.onClick, modifier = Modifier.size(44.dp)) {
                    Icon(action.icon, contentDescription = action.label, tint = AroundColors.ink)
                }
            }
        }
    }
}
