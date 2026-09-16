package com.imi.smartedge.sidebar.panel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Listens for BOOT_COMPLETED and auto-starts FloatingPanelService
 * if the user has enabled "Auto-start on boot" in settings.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON") return

        val prefs = PanelPreferences(context)
        if (!prefs.autoStart) return
        
        if (PanelCapabilities.accessibilityEnabled(context) || !prefs.useAutomationForGestures) {
            prefs.toggleService(context, true)
            return
        }
        val pending = goAsync()
        AutomationManager.refreshRoot {
            try { prefs.toggleService(context, true) } finally { pending.finish() }
        }
    }
}
