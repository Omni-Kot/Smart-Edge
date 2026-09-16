package com.imi.smartedge.sidebar.panel

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {
    fun onAttach(context: Context): Context {
        val language = PanelPreferences(context).appLanguage
        val configuration = Configuration(context.resources.configuration)
        val locale = if (language == "system") Resources.getSystem().configuration.locales[0] else Locale(language)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    fun setLocale(context: Context, language: String?): Context {
        PanelPreferences(context).appLanguage = language ?: "system"
        return onAttach(context)
    }
}
