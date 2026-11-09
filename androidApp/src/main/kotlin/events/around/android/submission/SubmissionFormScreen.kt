package events.around.android.submission

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.format.problem
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.domain.submission.FormField
import events.around.shared.presentation.submission.FormKind
import events.around.shared.presentation.submission.SubmissionFormActions
import events.around.shared.presentation.submission.SubmissionFormUiState
import events.around.shared.presentation.submission.SubmissionFormViewModel

@Composable
fun SubmissionFormScreen(
    viewModel: SubmissionFormViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val result = state.result
    if (result != null) {
        SubmissionResult(result = result, kind = state.kind, onDone = navigateBack, modifier = modifier)
    } else {
        // The sections only need what the form can do, not the view model behind it.
        val actions: SubmissionFormActions = viewModel
        SubmissionForm(state = state, actions = actions, navigateBack = navigateBack, modifier = modifier)
    }
}

/** Everything a form section needs: the state, the actions and where problems are shown. */
class FormScope(
    val state: SubmissionFormUiState,
    val actions: SubmissionFormActions,
    val requesters: Map<FormField, BringIntoViewRequester>,
    private val problemText: (FormField) -> String?,
) {
    fun problem(field: FormField): String? = problemText(field)

    fun requester(field: FormField): BringIntoViewRequester = requesters.getValue(field)
}

@Composable
private fun SubmissionForm(
    state: SubmissionFormUiState,
    actions: SubmissionFormActions,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var pickedUri by rememberSaveable { mutableStateOf<String?>(null) }

    val requesters = remember { FormField.entries.associateWith { BringIntoViewRequester() } }
    LaunchedEffect(state.problem) { state.problem?.let { requesters.getValue(it).bringIntoView() } }
    val scope = FormScope(state, actions, requesters) { field ->
        if (state.problem == field) state.serverMessage ?: field.problem(strings) else null
    }

    // A new submission is kept as a draft; anything else would be lost on leaving.
    val leave = { if (state.kind != FormKind.New && state.form.hasContent) confirmDiscard = true else navigateBack() }
    BackHandler(onBack = leave)

    val pickPoster = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.readPoster(uri)?.let { (bytes, name, mime) ->
            pickedUri = uri.toString()
            actions.pickPoster(bytes, name, mime)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (state.kind == FormKind.Edit) strings.formEditTitle else strings.formNewTitle) },
                navigationIcon = {
                    IconButton(onClick = leave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Banners(state, onStartOver = actions::startOver)
            EventSection(scope)
            PosterSection(
                scope = scope,
                pickedUri = pickedUri,
                onPick = {
                    pickPoster.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemove = {
                    pickedUri = null
                    actions.removePoster()
                },
            )
            WhenSection(scope)
            WhereSection(scope)
            DescriptionSection(scope)
            SendSection(scope)
            SubmitButton(state, actions::submit)
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(strings.formDiscardTitle) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    navigateBack()
                }) { Text(strings.formDiscard) }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(strings.formKeepEditing) } },
        )
    }
}

@Composable
private fun Banners(
    state: SubmissionFormUiState,
    onStartOver: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    when {
        state.sourceMissing -> ErrorBanner(strings.apiNotFound)
        state.kind == FormKind.Fix -> ErrorBanner(
            listOf(state.rejectionReason?.let(strings.reason) ?: strings.noReason, strings.rejectedFix)
                .joinToString(" "),
        )
        state.kind == FormKind.New && state.restoredDraft -> ErrorBanner(
            strings.restored,
            actionLabel = strings.startOver,
            onAction = onStartOver,
        )
        state.kind == FormKind.New -> FieldHint(strings.formIntro)
        else -> Unit
    }
}

@Composable
private fun SubmitButton(
    state: SubmissionFormUiState,
    onSubmit: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        state.error?.let { ErrorBanner(it.text(strings)) }
        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.kind == FormKind.Edit) strings.formSave else strings.formSend)
            }
        }
        if (state.kind != FormKind.Edit) FieldHint(strings.formSendHint)
    }
}

/** The picked image's bytes, file name and type; null when it can't be read. */
private fun Context.readPoster(uri: Uri): Triple<ByteArray, String, String>? {
    val mime = contentResolver.getType(uri) ?: "image/jpeg"
    val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: "poster.${mime.substringAfter('/')}"
    val bytes = runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return null
    return Triple(bytes, name, mime)
}
