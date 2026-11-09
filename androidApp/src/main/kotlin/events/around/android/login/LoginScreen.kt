package events.around.android.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.session.SessionUiState

@Composable
fun LoginScreen(
    state: SessionUiState,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Outlined.Event,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
            Text(strings.tabEvents, style = MaterialTheme.typography.headlineSmall)
            Text(
                strings.loginIntro,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.isExpired) {
                ErrorBanner(strings.loginExpired)
            }
            when {
                state.canSignIn -> Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.signInTelegram)
                }
                state.preparationFailed -> ErrorBanner(
                    strings.loginUnavailable,
                    actionLabel = strings.retry,
                    onAction = onRetry,
                )
                else -> CircularProgressIndicator()
            }
        }
    }
}
