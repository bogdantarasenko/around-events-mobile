package events.around.android.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import events.around.android.ui.strings.LocalAroundStrings

/** A pushed screen: a back arrow, an optional title, and the content under it. */
@Composable
fun BackScaffold(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalAroundStrings.current.back)
                    }
                },
            )
        },
        content = content,
    )
}
