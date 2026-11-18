package com.aacroche.polenalert.ui.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aacroche.polenalert.R
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.ui.components.ChooseAllergiesCard
import com.aacroche.polenalert.ui.components.LoadingScreen
import com.aacroche.polenalert.ui.components.MessageScreen
import com.aacroche.polenalert.ui.components.MessageScreenWithTwoButtons
import com.aacroche.polenalert.ui.components.ScreenGreeting
import com.aacroche.polenalert.ui.components.TodayHeader
import com.aacroche.polenalert.ui.theme.PolenAlertTheme
import com.aacroche.polenalert.ui.theme.colors
import com.aacroche.polenalert.ui.theme.dominantRisk
import com.aacroche.polenalert.ui.theme.getImg
import com.aacroche.polenalert.ui.theme.labelRes
import com.aacroche.polenalert.ui.theme.nameRes
import com.aacroche.polenalert.ui.theme.scientificName

@Composable
fun TodayDestination(viewModel: TodayViewModel = hiltViewModel(), onOpenProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(state = state, onRetry = viewModel::retry, onOpenProfile = onOpenProfile)
}

@Composable
fun TodayScreen(state: TodayUiState, onRetry: () -> Unit, onOpenProfile: () -> Unit) {

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TodayHeader(stringResource(R.string.madrid_location))
        Box(Modifier.weight(1f)) {
            when (state) {
                is TodayUiState.Success -> {
                    var isDismissed by remember { mutableStateOf(false) }
                    if (state.isOffline && !isDismissed) {
                        MessageScreenWithTwoButtons(
                            title = stringResource(R.string.offline_title),
                            body = stringResource(R.string.offline_message),
                            actionText = stringResource(R.string.offline_text_button),
                            actionText2 = stringResource(R.string.retry_connection),
                            onDismiss = { isDismissed = true },
                            onAction = onRetry
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { ScreenGreeting(state.date, state.greeting, state.name) }
                            item { RiskHeroCard(state.dominant,  state.isOffline) }
                            item { SectionTitle(state.updatedAt, state.isOffline) }
                            items(
                                items = state.today.readings,
                                key = { it.type }
                            ) {
                                PollenCard(it)
                            }
                        }
                    }
                }

                TodayUiState.Loading -> {
                    LoadingScreen()
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
                    val (errorTitle, errorBody) = when (state.error) {
                        DataError.NO_CONNECTION -> stringResource(R.string.error_connection_title) to stringResource(
                            R.string.error_connection_message
                        )

                        DataError.SERVER -> stringResource(R.string.error_server_title) to stringResource(
                            R.string.error_server_message
                        )

                        DataError.UNKNOWN -> stringResource(R.string.error_unknown_title) to stringResource(
                            R.string.error_unknown_message
                        )
                    }
                    MessageScreen(
                        title = errorTitle,
                        body = errorBody,
                        actionText = stringResource(R.string.retry),
                        onAction = onRetry
                    )
                }

                is TodayUiState.NoAllergies -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { ScreenGreeting(state.date, state.greeting, state.name) }
                        item { ChooseAllergiesCard(onOpenProfile) }
                    }
                }
            }
        }
    }
}

@Composable
fun RiskHeroCard(dominant: PollenReading, isOffline: Boolean = false, modifier: Modifier = Modifier) {
    val (levelContent, _) = dominant.level.colors()
    val onCard = MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(levelContent, CircleShape)
                    )
                    Text(
                        text = if (isOffline) stringResource(R.string.offline_risk_data) else stringResource(dominant.level.dominantRisk()),
                        style = MaterialTheme.typography.labelMedium,
                        color = onCard
                    )
                }
                Text(
                    text = stringResource(
                        R.string.risk_title,
                        stringResource(dominant.type.nameRes())
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = onCard
                )
                Text(
                    text = stringResource(R.string.risk_subtitle, dominant.dailyMean),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(dominant.level.labelRes()),
                    style = MaterialTheme.typography.titleSmall,
                    color = onCard
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "es")
@Composable
private fun RiskHeroCardPreview() {
    PolenAlertTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RiskHeroCard(PollenReading(PollenType.MUGWORT, PollenLevel.LOW, 6.9))
            RiskHeroCard(PollenReading(PollenType.GRASS, PollenLevel.MEDIUM, 34.0))
            RiskHeroCard(PollenReading(PollenType.OLIVE, PollenLevel.HIGH, 210.0))
        }
    }
}

@Composable
fun SectionTitle(updatedAt: String, isOffline: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.section_title),
            style = MaterialTheme.typography.titleMedium
        )
        if(!isOffline) {
            Text(
                text = stringResource(R.string.updated_at, updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PollenLevelChip(level: PollenLevel, modifier: Modifier = Modifier) {
    val (content, container) = level.colors()
    Row(
        modifier = modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(content, CircleShape)
        )
        Text(
            text = stringResource(level.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PollenLevelChipPreview() {
    PolenAlertTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PollenLevel.entries.forEach { PollenLevelChip(it) }
        }
    }
}

@Composable
fun PollenCard(todayPollen: PollenReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(todayPollen.type.getImg()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(todayPollen.type.nameRes()),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = todayPollen.type.scientificName(),
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.grains_per_m3, todayPollen.dailyMean),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PollenLevelChip(todayPollen.level)
        }
    }
}
