package com.aacroche.polenalert.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aacroche.polenalert.R
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.ui.theme.colors

@Composable
fun TodayRoute(viewModel: TodayViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(state = state, onRetry = viewModel::retry)
}

@Composable
fun TodayScreen(state: TodayUiState, onRetry: () -> Unit) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                TodayUiState.Loading -> {
                    LoadingScreen()
                }

                is TodayUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.today.readings) {
                            PollenCard(it)
                        }
                    }
                }

                TodayUiState.OutOfSeason -> {
                    MessageScreen(
                        title = stringResource(R.string.out_of_season),
                        body = "",
                        actionText = null,
                        onAction = {}
                    )
                }

                is TodayUiState.Error -> {
                    val errorTitle = when (state.error) {
                        DataError.NO_CONNECTION -> stringResource(R.string.error_connection_title)
                        DataError.SERVER -> stringResource(R.string.error_server_title)
                        DataError.UNKNOWN -> stringResource(R.string.error_unknown_title)
                    }
                    MessageScreen(
                        title = errorTitle,
                        body = state.error.toString(),
                        actionText = stringResource(R.string.retry),
                        onAction = onRetry
                    )
                }
            }
        }
    }
}

@Composable
fun PollenCard(todayPollen: PollenReading) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Título más destacado
                Text(
                    text = todayPollen.type.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${todayPollen.dailyMean} granos/m³",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Nivel de polen alineado horizontalmente a la derecha
            OvalTagWithIcon(todayPollen.level)
        }
    }
}

@Composable
fun OvalTagWithIcon(pollenlevel: PollenLevel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = pollenlevel.colors().second, shape = CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.size(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_circle),
                contentDescription = null,
                tint = pollenlevel.colors().first
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = pollenlevel.displayName(),
            color = pollenlevel.colors().first,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PollenType.displayName(): String = when (this) {
    PollenType.ALDER -> stringResource(R.string.pollen_type_alder)
    PollenType.BIRCH -> stringResource(R.string.pollen_type_birch)
    PollenType.GRASS -> stringResource(R.string.pollen_type_grass)
    PollenType.MUGWORT -> stringResource(R.string.pollen_type_mugwort)
    PollenType.OLIVE -> stringResource(R.string.pollen_type_olive)
    PollenType.RAGWEED -> stringResource(R.string.pollen_type_ragweed)
}

@Composable
fun PollenLevel.displayName(): String = when (this) {
    PollenLevel.NONE -> stringResource(R.string.pollen_level_none)
    PollenLevel.LOW -> stringResource(R.string.pollen_level_low)
    PollenLevel.MEDIUM -> stringResource(R.string.pollen_level_medium)
    PollenLevel.HIGH -> stringResource(R.string.pollen_level_high)
}

@Composable
fun MessageScreen(title: String, body: String, actionText: String?, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Alignment.CenterHorizontally.let { Arrangement.Center },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (body.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (actionText != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionText) }
        }
    }
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
