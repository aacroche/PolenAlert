package com.aacroche.polenalert.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aacroche.polenalert.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScreenGreeting(date: LocalDate, @androidx.annotation.StringRes greeting: Int, name: String) {
    val pattern = stringResource(R.string.date_pattern)
    val locale = LocalConfiguration.current.locales[0]
    val dateText = date.format(DateTimeFormatter.ofPattern(pattern, locale)).uppercase(locale)

    val greetingText = stringResource(greeting)
    val text = if (name.isBlank()) greetingText
    else stringResource(R.string.greeting_with_name, greetingText, name)

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(R.string.today_date, dateText),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}