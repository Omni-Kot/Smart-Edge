package com.imi.smartedge.sidebar.panel

import android.content.Context
import android.provider.Settings

object PanelCapabilities {
    fun accessibilityEnabled(context: Context): Boolean {
        if (PanelAccessibilityService.isRunning) return true
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val component = android.content.ComponentName(context, PanelAccessibilityService::class.java)
        return enabled?.split(':')?.any {
            android.content.ComponentName.unflattenFromString(it) == component
        } == true
    }
    fun hasEngine(context: Context): Boolean = accessibilityEnabled(context) ||
        (PanelPreferences(context).useAutomationForGestures && AutomationManager.isAutomationPossible())
    fun canStart(context: Context): Boolean = Settings.canDrawOverlays(context) && hasEngine(context)
}
