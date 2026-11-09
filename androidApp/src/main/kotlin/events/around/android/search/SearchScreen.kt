package events.around.android.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.eventDays
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.shared.presentation.feed.SearchViewModel
import kotlinx.coroutines.flow.drop

/** Search with Material's full-screen search bar: the field on top, results as event rows by day below. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navigateBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val text = rememberTextFieldState(state.query)
    val searchBar = rememberSearchBarState(initialValue = SearchBarValue.Expanded)
    val leave by rememberUpdatedState(navigateBack)

    LaunchedEffect(text) { snapshotFlow { text.text.toString() }.collect(viewModel::setQuery) }
    // Collapsing the bar (system back, the arrow) leaves the screen: there is nothing behind it here.
    LaunchedEffect(searchBar) {
        snapshotFlow { searchBar.currentValue }.drop(1).collect { if (it == SearchBarValue.Collapsed) leave() }
    }

    ExpandedFullScreenSearchBar(
        state = searchBar,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = text,
                searchBarState = searchBar,
                onSearch = {},
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                trailingIcon = {
                    if (text.text.isNotEmpty()) {
                        IconButton(
                            onClick = { text.clearText() },
                        ) { Icon(Icons.Filled.Close, contentDescription = strings.close) }
                    }
                },
            )
        },
        modifier = modifier,
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
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
    Text(text, color = AroundColors.secondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
}
