package com.aacroche.polenalert.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aacroche.polenalert.ui.navigation.ForecastRoute
import com.aacroche.polenalert.ui.navigation.ProfileRoute
import com.aacroche.polenalert.ui.navigation.TodayRoute
import com.aacroche.polenalert.ui.navigation.TopLevelDestination
import com.aacroche.polenalert.ui.today.TodayDestination
import com.aacroche.polenalert.R
import com.aacroche.polenalert.ui.forecast.ForecastDestination

@Composable
fun PolenAlertAppContent() {
    val backStack = rememberNavBackStack(TodayRoute)
    val current = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = current == destination.route,
                        onClick = {
                            if (current != destination.route) {
                                backStack.retainAll { it == TodayRoute }
                                if (destination.route != TodayRoute) {
                                    backStack.add(destination.route)
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painterResource(destination.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(destination.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,   // E9F1E7
                            unselectedIconColor = MaterialTheme.colorScheme.outline,       // 82908B
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<TodayRoute> { TodayDestination() }
                entry<ForecastRoute> { ForecastDestination() }
                entry<ProfileRoute> { Text(stringResource(R.string.tab_profile)) }
            }
        )
    }
}

