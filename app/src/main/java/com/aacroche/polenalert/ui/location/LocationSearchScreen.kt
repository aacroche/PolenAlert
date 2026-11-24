package com.aacroche.polenalert.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aacroche.polenalert.R
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.ui.theme.PolenAlertTheme

@Composable
fun LocationSearchDestination(
    onBack: () -> Unit,
    viewModel: LocationSearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val placeSaved by viewModel.placeSaved.collectAsStateWithLifecycle()

    LaunchedEffect(placeSaved) {
        if (placeSaved) onBack()
    }

    LocationSearchScreen(
        query = query,
        results = results,
        onQueryChange = viewModel::onQueryChange,
        onPlaceSelected = viewModel::onPlaceSelected,
        onUseCurrentLocation = viewModel::onUseCurrentLocation,
        onBack = onBack,
    )
}

@Composable
fun LocationSearchScreen(
    query: String,
    results: LocationSearchUiState,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (Place) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }      // el teclado se abre solo

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = stringResource(R.string.location_search_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.location_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )

        LocationRow(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.location_use_current),
            subtitle = null,
            onClick = onUseCurrentLocation,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (results) {
            LocationSearchUiState.Idle -> InfoText(stringResource(R.string.location_search_min_chars))

            LocationSearchUiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(28.dp))
            }

            LocationSearchUiState.NoResults -> InfoText(stringResource(R.string.location_search_no_results, query.trim()))

            is LocationSearchUiState.Error -> InfoText(
                stringResource(
                    if (results.error == DataError.NO_CONNECTION) R.string.location_search_error_connection
                    else R.string.location_search_error
                )
            )

            is LocationSearchUiState.Results -> LazyColumn(
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(results.places) { place ->
                    LocationRow(
                        icon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        title = place.name,
                        subtitle = listOfNotNull(place.region, place.country).joinToString(", ").ifEmpty { null },
                        onClick = { onPlaceSelected(place) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true, locale = "es")
@Composable
private fun LocationSearchScreenPreview() {
    PolenAlertTheme {
        LocationSearchScreen(
            query = "Sev",
            results = LocationSearchUiState.Results(
                listOf(
                    Place("Sevilla", "Andalucía", "España", Coordinates(37.3891, -5.9845)),
                    Place("Sevilla la Nueva", "Comunidad de Madrid", "España", Coordinates(40.3475, -4.0272)),
                )
            ),
            onQueryChange = {}, onPlaceSelected = {}, onUseCurrentLocation = {}, onBack = {},
        )
    }
}