package com.aacroche.polenalert.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aacroche.polenalert.R

@Composable
fun MessageScreen(title: String, body: String, actionText: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (body.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionText != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionText) }
        }
    }
}

@Preview
@Composable
private fun MessageScreenPreview() {
    MessageScreen(
        title = stringResource(R.string.error_server_title),
        body = stringResource(R.string.error_server_message),
        actionText = stringResource(R.string.retry),
        onAction = {}
    )
}

@Composable
fun MessageScreenWithTwoButtons(
    title: String,
    body: String,
    actionText: String,
    actionText2: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (body.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (actionText != null && actionText2 != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDismiss, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(actionText) }
            Button(onClick = onAction) { Text(actionText2) }
        }
    }
}

@Preview
@Composable
private fun MessageScreenWithTwoButtonsPreview() {
    MessageScreenWithTwoButtons(
        title = stringResource(R.string.offline_title),
        body = stringResource(R.string.offline_message),
        actionText = stringResource(R.string.offline_text_button),
        actionText2 = stringResource(R.string.retry_connection),
        onDismiss = {},
        onAction = {}
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = 4.dp
        )
    }
}
