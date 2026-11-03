package com.aacroche.polenalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aacroche.polenalert.ui.PolenAlertAppContent
import com.aacroche.polenalert.ui.theme.PolenAlertTheme
import com.aacroche.polenalert.ui.today.TodayDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolenAlertTheme {
                PolenAlertAppContent()
            }
        }
    }

}