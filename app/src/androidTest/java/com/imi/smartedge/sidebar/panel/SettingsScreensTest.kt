package com.imi.smartedge.sidebar.panel

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreensTest {
    @Test fun settingsOpenInEverySupportedLocale() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val prefs = PanelPreferences(context)
        val savedLanguage = prefs.appLanguage
        val classes = listOf(SettingsMainActivity::class.java, AppearanceSettingsActivity::class.java,
            InteractionSettingsActivity::class.java, HandleSettingsActivity::class.java,
            ToolsSettingsActivity::class.java, MiscellaneousSettingsActivity::class.java)
        try {
            for (language in listOf("ru", "en", "es")) {
                prefs.appLanguage = language
                for (screen in classes) {
                    val activity = instrumentation.startActivitySync(Intent(context, screen).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    try {
                        instrumentation.waitForIdleSync()
                        assertEquals(language, activity.resources.configuration.locales[0].language)
                        if (screen == MiscellaneousSettingsActivity::class.java) {
                            instrumentation.runOnMainSync {
                                assertNotNull(activity.findViewById<android.view.View>(R.id.btnExportSettings))
                                assertNotNull(activity.findViewById<android.view.View>(R.id.btnImportSettings))
                            }
                        }
                    } finally { instrumentation.runOnMainSync { activity.finish() } }
                }
            }
        } finally { prefs.appLanguage = savedLanguage }
    }
}
