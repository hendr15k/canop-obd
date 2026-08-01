package com.canopobd.data.locale

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val displayName: String, val tag: String?) {
    SYSTEM("System", null),
    GERMAN("Deutsch", "de"),
    ENGLISH("English", "en");

    companion object {
        fun fromName(name: String): AppLanguage =
            entries.find { it.name == name } ?: SYSTEM
    }
}

object LocaleManager {

    private const val PREFS_NAME = "app_language"
    private const val KEY_LANGUAGE = "language"

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguage(context: Context): AppLanguage =
        AppLanguage.fromName(prefs(context).getString(KEY_LANGUAGE, AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name)

    fun setLanguage(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    fun wrapContext(context: Context): Context {
        val language = getLanguage(context)
        if (language.tag == null) return context

        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
