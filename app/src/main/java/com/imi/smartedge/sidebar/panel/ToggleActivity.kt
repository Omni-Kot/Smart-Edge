package com.imi.smartedge.sidebar.panel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * A transparent activity that triggers the Side Panel to open.
 * Useful for mapping to hardware keys or 3rd party gesture apps.
 */
class ToggleActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    companion object {
        const val ACTION_TOGGLE = BuildConfig.APPLICATION_ID + ".TOGGLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Handle Shortcut Creation request from Launcher
        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            val shortcutIntent = Intent(this, ToggleActivity::class.java).apply {
                action = ACTION_TOGGLE
            }
            
            val resultIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.label_toggle_sidebar))
                val iconResource = Intent.ShortcutIconResource.fromContext(this@ToggleActivity, R.mipmap.ic_launcher)
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)
            }
            
            setResult(RESULT_OK, resultIntent)
            finish()
            return
        }

        // 2. Check basic permissions
        if (!PanelPreferences(this).serviceEnabled && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.ui_overlay_permission_required), Toast.LENGTH_SHORT).show()
            val pIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(pIntent)
            finish()
            return
        }

        fun toggle() {
            if (isFinishing || isDestroyed) return
            if (!PanelPreferences(this).toggleService(this)) {
                Toast.makeText(this, R.string.ui_accessibility_service_required, Toast.LENGTH_SHORT).show()
            }
            finishWithNoAnim()
        }
        if (PanelPreferences(this).useAutomationForGestures) AutomationManager.refreshRoot { toggle() }
        else toggle()
    }

    private fun finishWithNoAnim() {
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
