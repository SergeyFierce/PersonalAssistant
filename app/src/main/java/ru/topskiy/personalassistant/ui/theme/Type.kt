package ru.topskiy.personalassistant.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

// Базовый шрифт «как в Телеграм» (системный сан‑сериф / Roboto на Android)
private val TelegramFontFamily = FontFamily.SansSerif

// Берём дефолтную типографику Material3 и везде подставляем один и тот же fontFamily
private val DefaultTypography = Typography()

val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = TelegramFontFamily),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = TelegramFontFamily),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = TelegramFontFamily),

    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = TelegramFontFamily),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = TelegramFontFamily),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = TelegramFontFamily),

    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = TelegramFontFamily),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = TelegramFontFamily),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = TelegramFontFamily),

    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = TelegramFontFamily),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = TelegramFontFamily),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = TelegramFontFamily),

    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = TelegramFontFamily),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = TelegramFontFamily),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = TelegramFontFamily)
)