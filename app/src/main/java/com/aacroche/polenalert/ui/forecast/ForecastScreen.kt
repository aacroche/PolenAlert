package com.aacroche.polenalert.ui.forecast

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aacroche.polenalert.R
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.PollenForecast
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.Trend
import com.aacroche.polenalert.ui.components.ChooseAllergiesCard
import com.aacroche.polenalert.ui.components.LoadingScreen
import com.aacroche.polenalert.ui.components.MessageScreen
import com.aacroche.polenalert.ui.components.MessageScreenWithTwoButtons
import com.aacroche.polenalert.ui.components.OfflineSavedNote
import com.aacroche.polenalert.ui.components.ScreenGreeting
import com.aacroche.polenalert.ui.components.TodayHeader
import com.aacroche.polenalert.ui.theme.Inter
import com.aacroche.polenalert.ui.theme.PolenAlertTheme
import com.aacroche.polenalert.ui.theme.colors
import com.aacroche.polenalert.ui.theme.getImg
import com.aacroche.polenalert.ui.theme.labelRes
import com.aacroche.polenalert.ui.theme.nameRes
import com.aacroche.polenalert.ui.theme.scientificName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ForecastDestination(viewModel: ForecastViewModel = hiltViewModel(), onOpenProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.onLocationPermissionChange()
        onPauseOrDispose { }
    }

    ForecastScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpenProfile = onOpenProfile,
        onLocationPermission = viewModel::onLocationPermissionChange
    )
}

@Composable
fun ForecastScreen(
    state: ForecastUiState,
    onRetry: () -> Unit,
    onOpenProfile: () -> Unit,
    onLocationPermission: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> onLocationPermission() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TodayHeader(location = stringResource(R.string.madrid_location))
        Box(modifier = Modifier.weight(1f)) {
            when (state) {
                is ForecastUiState.Success -> {
                    var isDismissed by rememberSaveable(state.isOffline) { mutableStateOf(false) }
                    if (state.isOffline && !isDismissed) {
                        MessageScreenWithTwoButtons(
                            title = stringResource(R.string.offline_title),
                            body = stringResource(R.string.offline_message),
                            actionText = stringResource(R.string.offline_text_button),
                            actionText2 = stringResource(R.string.retry_connection),
                            onAction1 = { isDismissed = true },
                            onAction2 = onRetry
                        )
                    } else {
                        ForecastContent(
                            days = state.days,
                            forecasts = state.forecasts,
                            isOffline = state.isOffline,
                            savedAt = state.savedAt,
                            savedHoursAgo = state.savedHoursAgo
                        )
                    }
                }

                ForecastUiState.Loading -> {
                    LoadingScreen()
                }

                ForecastUiState.Empty -> {
                    MessageScreen(
                        title = stringResource(R.string.out_of_season),
                        body = "",
                        actionText = null,
                        onAction = {}
                    )
                }

                ForecastUiState.OutOfCoverage -> {
                    MessageScreen(
                        title = stringResource(R.string.out_of_coverage),
                        body = "",
                        actionText = null,
                        onAction = {}
                    )
                }

                is ForecastUiState.Error -> {
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

                is ForecastUiState.NoAllergies -> {
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

                is ForecastUiState.NoLocation -> {
                    MessageScreenWithTwoButtons(
                        title = stringResource(R.string.location_permission_title),
                        body = stringResource(R.string.location_description),
                        actionText = stringResource(R.string.location_permission_text_button),
                        actionText2 = stringResource(R.string.location_manual_text_button),
                        onAction1 = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                        onAction2 = {}
                    )
                }

                ForecastUiState.LocationUnavailable -> {
                    MessageScreen(
                        title = stringResource(R.string.location_unavailable),
                        body = "",
                        actionText = null,
                        onAction = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastContent(
    days: List<LocalDate>,
    forecasts: List<PollenForecast>,
    isOffline: Boolean,
    savedAt: LocalDateTime?,
    savedHoursAgo: Long?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ForecastHeader()
        }

        item {
            ForecastSectionTitle(isOffline = isOffline, savedAt = savedAt)
        }

        item {
            DaysHeaderRow(days = days)
        }

        items(
            items = forecasts,
            key = { it.type }
        ) { forecast ->
            ForecastCard(forecast = forecast)
        }

        item {
            ForecastLegend()
        }

        item {
            if (isOffline && savedAt != null && savedHoursAgo != null) {
                OfflineSavedNote(
                    savedAt = savedAt,
                    savedHoursAgo = savedHoursAgo
                )
            }
        }
    }
}

@Composable
fun ForecastHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(R.string.forecast_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.forecast_subtitle),
            style = TextStyle(fontFamily = Inter, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ForecastSectionTitle(
    isOffline: Boolean,
    savedAt: LocalDateTime? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.section_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!isOffline) {
            savedAt?.let {
                val time = savedAt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                Text(
                    text = stringResource(R.string.updated_at, time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DaysHeaderRow(days: List<LocalDate>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { date ->
            DayHeaderItem(
                date = date,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DayHeaderItem(date: LocalDate, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE", locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    val dayAndMonth =
        date.format(DateTimeFormatter.ofPattern("d MMM", locale)).lowercase(locale)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = dayAndMonth,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ForecastCard(forecast: PollenForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(forecast.type.getImg()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(forecast.type.nameRes()),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = forecast.type.scientificName(),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontStyle = FontStyle.Italic,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(forecast.trend.labelRes()),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                forecast.levels.forEach { level ->
                    DayLevelChip(
                        level = level,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DayLevelChip(level: PollenLevel, modifier: Modifier = Modifier) {
    val (contentColor, containerColor) = level.colors()
    Box(
        modifier = modifier
            .height(22.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(level.labelRes()),
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ForecastLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.legend_levels),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(level = PollenLevel.LOW)
            LegendItem(level = PollenLevel.MEDIUM)
            LegendItem(level = PollenLevel.HIGH)
        }
    }
}

@Composable
private fun LegendItem(level: PollenLevel) {
    val (contentColor, _) = level.colors()
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(contentColor, CircleShape)
        )
        Text(
            text = stringResource(level.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    PolenAlertTheme {
        ForecastScreen(
            state = ForecastUiState.Success(
                days = listOf(
                    LocalDate.now(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(2),
                    LocalDate.now().plusDays(3)
                ),
                forecasts = listOf(
                    PollenForecast(
                        type = PollenType.GRASS,
                        levels = listOf(
                            PollenLevel.LOW,
                            PollenLevel.MEDIUM,
                            PollenLevel.HIGH,
                            PollenLevel.HIGH
                        ),
                        trend = Trend.RISING
                    ),
                    PollenForecast(
                        type = PollenType.OLIVE,
                        levels = listOf(
                            PollenLevel.MEDIUM,
                            PollenLevel.LOW,
                            PollenLevel.LOW,
                            PollenLevel.NONE
                        ),
                        trend = Trend.FALLING
                    ),
                    PollenForecast(
                        type = PollenType.MUGWORT,
                        levels = listOf(
                            PollenLevel.LOW,
                            PollenLevel.LOW,
                            PollenLevel.LOW,
                            PollenLevel.LOW
                        ),
                        trend = Trend.STABLE
                    )
                ),
                isOffline = false,
                savedAt = LocalDateTime.now()
            ),
            onRetry = {},
            onOpenProfile = {},
            onLocationPermission = {}
        )
    }
}
