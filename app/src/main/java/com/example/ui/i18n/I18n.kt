package com.example.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val label: String, val flag: String) {
    EN("en", "English", "🇺🇸"),
    RU("ru", "Русский", "🇷🇺");

    fun toggle(): AppLanguage = if (this == EN) RU else EN
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }

/**
 * Returns localized string based on the active Composable AppLanguage.
 * Default is English.
 */
@Composable
fun tr(en: String, ru: String): String {
    return if (LocalAppLanguage.current == AppLanguage.RU) ru else en
}

/**
 * Non-composable translation helper.
 */
fun str(en: String, ru: String, lang: AppLanguage): String {
    return if (lang == AppLanguage.RU) ru else en
}
