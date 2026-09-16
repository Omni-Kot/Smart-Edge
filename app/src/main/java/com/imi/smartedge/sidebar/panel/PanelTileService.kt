package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class PanelTileService : TileService() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
    
    companion object {
        private var isProcessingToggle = false
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
        if (PanelPreferences(this).useAutomationForGestures) AutomationManager.refreshRoot { updateTile() }
    }

    private fun updateTile() {
        if (qsTile == null) return
        val prefs = PanelPreferences(this)
        val isEnabled = prefs.serviceEnabled
        val isAccessibilityEnabled = PanelCapabilities.canStart(this)
        updateTileInternal(isEnabled, isAccessibilityEnabled)
    }

    private fun updateTileOptimistic(newState: Boolean) {
        val isAccessibilityEnabled = PanelCapabilities.canStart(this)
        updateTileInternal(newState, isAccessibilityEnabled)
    }

    private fun updateTileInternal(isEnabled: Boolean, isAccessibilityEnabled: Boolean) {
        val tile = qsTile ?: return
        if (isEnabled && isAccessibilityEnabled) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = LocaleHelper.onAttach(this).getString(R.string.shortcut_toggle_short)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = LocaleHelper.onAttach(this).getString(R.string.ui_service_active)
            }
        } else if (isEnabled && !isAccessibilityEnabled) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = LocaleHelper.onAttach(this).getString(R.string.shortcut_toggle_short)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = LocaleHelper.onAttach(this).getString(R.string.ui_accessibility_missing)
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = LocaleHelper.onAttach(this).getString(R.string.shortcut_toggle_short)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = LocaleHelper.onAttach(this).getString(R.string.ui_service_stopped)
            }
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        
        // 1. Debounce rapid clicks
        if (isProcessingToggle) return
        isProcessingToggle = true

        if (PanelPreferences(this).useAutomationForGestures) {
            AutomationManager.refreshRoot { handleClick() }
        } else handleClick()
    }

    private fun handleClick() {

        val prefs = PanelPreferences(this)
        val isEnabled = prefs.serviceEnabled
        
        // 2. Immediate Haptic Feedback
        triggerHapticFeedback()

        // 3. Permission Check (Instant)
        if (!isEnabled && !PanelCapabilities.canStart(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startAction(intent)
            isProcessingToggle = false
            return
        }

        // 4. Optimistic UI Update (Near Instant)
        val targetState = !isEnabled
        updateTileOptimistic(targetState)

        // 5. Background Execution to prevent blocking Tile UI
        Thread {
            try {
                // Centralized Toggle Logic with explicit target state
                prefs.toggleService(this@PanelTileService, forcedState = targetState)
                
                // Keep the debounce active for a short while to prevent system-retriggering 
                // and allow the service to actually start/stop.
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    updateTile()
                    isProcessingToggle = false
                }, 800)
            } catch (e: Exception) {
                e.printStackTrace()
                isProcessingToggle = false
            }
        }.start()
        
        // No shade collapse here, making the toggle seamless for the user!
    }

    // The Intent overload is required below API 34; the PendingIntent overload does not exist there.
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startAction(intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(android.app.PendingIntent.getActivity(this, 0, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        } finally { isProcessingToggle = false }
    }

    private fun triggerHapticFeedback() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Short light tap for tactile feel
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(10, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (e: Exception) {
            // Ignore if vibrator fails
        }
    }
}
