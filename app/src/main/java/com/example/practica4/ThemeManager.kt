package com.example.practica4

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_NIGHT_MODE = "night_mode"

    const val THEME_IPN = "ipn"
    const val THEME_ESCOM = "escom"

    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"
    const val MODE_SYSTEM = "system"

    fun getThemePreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCurrentTheme(context: Context): String {
        return getThemePreferences(context).getString(KEY_THEME, THEME_IPN) ?: THEME_IPN
    }

    fun getCurrentNightMode(context: Context): String {
        return getThemePreferences(context).getString(KEY_NIGHT_MODE, MODE_SYSTEM) ?: MODE_SYSTEM
    }

    fun setTheme(context: Context, theme: String) {
        getThemePreferences(context).edit().putString(KEY_THEME, theme).apply()
        applyTheme(context)
    }

    fun setNightMode(context: Context, mode: String) {
        getThemePreferences(context).edit().putString(KEY_NIGHT_MODE, mode).apply()
        applyTheme(context)
    }

    fun applyTheme(context: Context) {
        val theme = getCurrentTheme(context)
        val nightMode = getCurrentNightMode(context)

        when (nightMode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MODE_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        val themeRes = when (theme) {
            THEME_ESCOM -> R.style.Theme_Practica4_ESCOM
            else -> R.style.Theme_Practica4_IPN
        }
        context.setTheme(themeRes)
    }
}