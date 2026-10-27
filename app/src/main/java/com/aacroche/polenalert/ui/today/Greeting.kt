package com.aacroche.polenalert.ui.today

import androidx.annotation.StringRes
import com.aacroche.polenalert.R

@StringRes
fun greetingFor(hour: Int): Int = when (hour) {
    in 6..13 -> R.string.greeting_morning
    in 14..20 -> R.string.greeting_afternoon
    else -> R.string.greeting_night

}