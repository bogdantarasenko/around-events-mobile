package events.around.android.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import events.around.android.R
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.City

/**
 * The logo with the city next to it, as the brand next to the avatar in the designs; a menu switches the city.
 * Both tabs with a city's content put it in their top bar's title.
 */
@Composable
fun CityTitle(
    city: City?,
    cities: List<City>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(enabled = cities.size > 1, onClickLabel = strings.feedCity) { open = true },
        ) {
            Image(painterResource(R.drawable.around_logo), contentDescription = null, modifier = Modifier.size(36.dp))
            Text(city?.name ?: strings.tabHome, fontWeight = FontWeight.Bold)
            if (cities.size > 1) Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            cities.forEach { option ->
                DropdownMenuItem(text = { Text(option.name) }, onClick = {
                    open = false
                    onSelect(option.slug)
                })
            }
        }
    }
}
