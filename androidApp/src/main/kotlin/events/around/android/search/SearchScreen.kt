package events.around.android.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.eventDays
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.shared.presentation.feed.SearchViewModel

/** Search as in the designs: a back circle and a white search pill on top, results as event rows below. */
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
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier.fillMaxSize().background(AroundColors.surface).statusBarsPadding(),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 6.dp),
            ) {
                IconButton(
                    onClick = navigateBack,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(AroundColors.tabSelected),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = AroundColors.ink,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = AroundColors.surface,
                    shadowElevation = 3.dp,
                    modifier = Modifier.weight(1f).height(44.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = AroundColors.secondary)
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.setQuery(it)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 17.sp, color = AroundColors.ink),
                            cursorBrush = SolidColor(AroundColors.ink),
                            decorationBox = { field ->
                                if (query.isEmpty()) {
                                    Text(
                                        strings.searchPlaceholder,
                                        fontSize = 17.sp,
                                        color = AroundColors.tertiary,
                                    )
                                }
                                field()
                            },
                            modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focus),
                        )
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                viewModel.setQuery("")
                            }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Filled.Cancel,
                                    contentDescription = strings.close,
                                    tint = AroundColors.chevron,
                                )
                            }
                        }
                    }
                }
            }
        }
        when {
            state.isBlank -> item { Hint(strings.searchHint) }
            state.days.isEmpty() -> item { Hint(strings.searchEmpty) }
        }
        eventDays(state.days, onOpenEvent)
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, color = AroundColors.secondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
}
