package events.around.android.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.BuildConfig
import events.around.android.submission.FieldHint
import events.around.android.submission.FormTextField
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.organizer.OrganizerField
import events.around.shared.presentation.organizer.OrganizerViewModel
import kotlinx.coroutines.flow.first

@Composable
fun OrganizerEditScreen(
    viewModel: OrganizerViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editor = state.editor

    // Saving or cancelling closes the editor in the view model; the screen leaves with it.
    val currentNavigateBack by rememberUpdatedState(navigateBack)
    LaunchedEffect(viewModel) {
        viewModel.state.first { it.editor == null }
        currentNavigateBack()
    }
    if (editor == null) return

    fun error(field: OrganizerField) = if (editor.fieldError == field) editor.fieldMessage else null
    val close = {
        viewModel.cancelEditing()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(strings.pageTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = close,
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FieldHint(strings.pageHint)
            FormTextField(
                editor.name,
                viewModel::setName,
                strings.pageName,
                placeholder = strings.pageNamePlaceholder,
                error = error(OrganizerField.Name),
            )
            FormTextField(
                value = editor.slug,
                onValueChange = viewModel::setSlug,
                label = strings.pageSlug,
                error = error(OrganizerField.Slug),
                hint = "${BuildConfig.BASE_URL.toUri().host}/o/${editor.slug}",
                keyboardType = KeyboardType.Uri,
            )
            FormTextField(
                value = editor.bio,
                onValueChange = viewModel::setBio,
                label = strings.pageBio,
                placeholder = strings.pageBioPlaceholder,
                error = error(OrganizerField.Bio),
                singleLine = false,
                minLines = 3,
            )
            FormTextField(
                editor.instagram,
                viewModel::setInstagram,
                strings.pageInstagram,
                keyboardType = KeyboardType.Uri,
            )
            FormTextField(
                editor.telegram,
                viewModel::setTelegram,
                strings.pageTelegram,
                keyboardType = KeyboardType.Uri,
            )
            FormTextField(editor.website, viewModel::setWebsite, strings.pageWebsite, keyboardType = KeyboardType.Uri)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(strings.pagePublic, style = MaterialTheme.typography.bodyLarge)
                    FieldHint(strings.pagePublicHint)
                }
                Switch(checked = editor.isPublic, onCheckedChange = viewModel::setPublic)
            }
            editor.error?.let { ErrorBanner(it.text(strings)) }
            Button(onClick = viewModel::save, enabled = !editor.isSaving, modifier = Modifier.fillMaxWidth()) {
                if (editor.isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(strings.pageSave)
                }
            }
        }
    }
}
