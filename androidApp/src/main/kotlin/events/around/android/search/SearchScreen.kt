package events.around.android.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.BackScaffold
import events.around.android.ui.components.eventDays
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.feed.SearchViewModel

/** Search in the city's listing, results by day. */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navigateBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val focus = remember { FocusRequester() }
    // Local copy: the field must not wait for the view model's round trip on every keystroke.
    var query by rememberSaveable { mutableStateOf(state.query) }
    LaunchedEffect(focus) { focus.requestFocus() }
    BackScaffold(navigateBack, modifier, title = state.city?.name.orEmpty()) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.setQuery(it)
                    },
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
            }
            when {
                state.isBlank -> item { Hint(strings.searchHint) }
                state.days.isEmpty() -> item { Hint(strings.searchEmpty) }
            }
            eventDays(state.days, onOpenEvent)
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
}
