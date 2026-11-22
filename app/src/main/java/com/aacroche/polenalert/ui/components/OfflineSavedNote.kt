package com.aacroche.polenalert.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import com.aacroche.polenalert.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun OfflineSavedNote(
    savedAt: LocalDateTime,
    savedHoursAgo: Long,
    modifier: Modifier = Modifier
) {
    val date = savedAt.format(
        DateTimeFormatter.ofPattern(
            "d MMM",
            LocalLocale.current.platformLocale
        )
    )
    val text = if (savedHoursAgo < 1) {
        stringResource(R.string.offline_saved_note_recent, date)
    } else {
        stringResource(R.string.offline_saved_note, date, savedHoursAgo)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
