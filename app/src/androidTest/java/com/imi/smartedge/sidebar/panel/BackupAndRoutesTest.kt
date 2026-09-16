package com.imi.smartedge.sidebar.panel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupAndRoutesTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun completeBackupRoundTripAndRejectedFileIsAtomic() {
        val prefs = PanelPreferences(context)
        val initial = prefs.exportToJson()
        try {
            prefs.tapAction = PanelPreferences.ACTION_BACK
            prefs.notchDoubleTapAction = PanelPreferences.ACTION_HOME
            prefs.appLanguage = "ru"
            prefs.onlyOnHome = true
            prefs.favoriteAppPackage = "example.app"
            prefs.showScreenshotTool = false
            prefs.setFullscreenWhitelist(listOf("example.app"))
            val configured = prefs.exportToJson()
            prefs.resetToDefaults()
            assertTrue(prefs.importFromJson(configured))
            assertEquals(JSONObject(configured).toString(), JSONObject(prefs.exportToJson()).toString())
            assertFalse(prefs.importFromJson("{}"))
            assertFalse(prefs.importFromJson(JSONObject(configured).put("_app", "OtherApp").toString()))
            assertFalse(prefs.importFromJson(JSONObject(configured).put("_version", 999).toString()))
            assertFalse(prefs.importFromJson(JSONObject(configured).put("panel_columns", 0).toString()))
            assertFalse(prefs.importFromJson(JSONObject(configured).put("tap_action", "4").toString()))
            assertFalse(prefs.importFromJson(JSONObject(configured).apply { remove("favorite_app_package") }.toString()))
            assertEquals(JSONObject(configured).toString(), JSONObject(prefs.exportToJson()).toString())
        } finally { assertTrue(prefs.importFromJson(initial)) }
    }
    @Test fun pickerRouteIsRegistered() {
        val component = android.content.ComponentName(context, AppPickerActivity::class.java)
        assertNotNull(context.packageManager.getActivityInfo(component, 0))
    }
    @Test fun resourceCoverageIncludesAllLocales() {
        for (language in listOf("en", "ru", "es")) {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(java.util.Locale(language))
            val localized = context.createConfigurationContext(config)
            assertTrue(localized.getString(R.string.backup_saved_document).isNotBlank())
            assertTrue(localized.getString(R.string.notch_unavailable).isNotBlank())
        }
    }
}
