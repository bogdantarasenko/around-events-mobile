package events.around.android.submission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import events.around.android.ui.components.openUrl
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.submission.FormKind
import events.around.shared.presentation.submission.FormResult

@Composable
fun SubmissionResult(
    result: FormResult,
    kind: FormKind,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val title = when {
        result.saved -> strings.savedTitle
        result.isDuplicate -> strings.doneDuplicate
        else -> strings.doneAccepted
    }
    // The server explains a duplicate or an auto-published event in its own words; a review has a fixed path.
    val text = when {
        result.saved -> strings.savedReview
        else -> result.receipt?.message ?: strings.reviewTakes
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (!result.saved && result.receipt?.status == "review") {
            Steps()
        }
        result.receipt?.postUrl?.let { url ->
            OutlinedButton(
                onClick = { context.openUrl(url) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(strings.openPost) }
        }
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text(if (kind == FormKind.Edit) strings.back else strings.toMine)
        }
    }
}

/** What happens next to a submission in review. */
@Composable
private fun Steps() {
    val strings = LocalAroundStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(
            strings.timelineReview to strings.timelineReviewHint,
            strings.timelineAnswer to strings.timelineAnswerHint,
            strings.timelinePost to strings.timelinePostHint,
        ).forEachIndexed { index, (title, hint) ->
            Column {
                Text("${index + 1}. $title", style = MaterialTheme.typography.titleSmall)
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
