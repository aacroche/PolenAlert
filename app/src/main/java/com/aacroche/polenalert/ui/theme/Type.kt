package com.aacroche.polenalert.ui.theme

import com.aacroche.polenalert.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_italic, FontWeight.Normal, FontStyle.Italic),
)

val Typography = Typography(
    headlineMedium = TextStyle(fontFamily = Inter, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 19.sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    titleSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Inter, fontSize = 11.sp)
)