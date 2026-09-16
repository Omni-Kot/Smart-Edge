package com.imi.smartedge.sidebar.panel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rikka.shizuku.Shizuku
import java.util.concurrent.atomic.AtomicBoolean

object AutomationManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val main = Handler(Looper.getMainLooper())
    private val probing = AtomicBoolean(false)
    private val commandMutex = Mutex()
    @Volatile private var rootAvailable = false

    fun isShizukuAvailable(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) { false }

    fun isRootAvailable(): Boolean = rootAvailable

    fun refreshRoot(onResult: ((Boolean) -> Unit)? = null) {
        scope.launch {
            if (probing.compareAndSet(false, true)) {
                try {
                    val result = ProcessRunner.run { Runtime.getRuntime().exec(arrayOf("su", "-c", "id")) }
                    rootAvailable = result.success && result.output.contains("uid=0")
                } finally { probing.set(false) }
            } else {
                while (probing.get()) delay(20)
            }
            onResult?.let { callback -> main.post { callback(rootAvailable) } }
        }
    }

    fun requestRootPermission(onResult: (Boolean) -> Unit) {
        scope.launch {
            val result = ProcessRunner.run(30_000) { Runtime.getRuntime().exec(arrayOf("su", "-c", "id")) }
            rootAvailable = result.success && result.output.contains("uid=0")
            main.post { onResult(rootAvailable) }
        }
    }

    fun isAutomationPossible() = isShizukuAvailable() || isRootAvailable()

    fun checkRootAndRequestPermission(context: Context, onResult: (Boolean) -> Unit) {
        if (rootAvailable) { onResult(true); return }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle(R.string.ui_root_access)
            .setMessage(R.string.ui_direct_system_grant)
            .setPositiveButton(R.string.ui_grant_permission) { _, _ -> requestRootPermission(onResult) }
            .setNegativeButton(R.string.ui_not_now) { _, _ -> onResult(false) }
            .setOnCancelListener { onResult(false) }
            .show()
    }

    fun executeAsync(command: String, onResult: (Boolean) -> Unit = {}) {
        scope.launch {
            val success = commandMutex.withLock { if (isShizukuAvailable()) {
                ProcessRunner.run { Shizuku.newProcess(arrayOf("sh", "-c", command), null, null) }.success
            } else if (rootAvailable) {
                ProcessRunner.run { Runtime.getRuntime().exec(arrayOf("su", "-c", command)) }.success
                    .also { if (!it) rootAvailable = false }
            } else false }
            main.post { onResult(success) }
        }
    }

    fun performSystemAction(action: String, onResult: (Boolean) -> Unit) {
        val command = when (action) {
            PanelAccessibilityService.ACTION_BACK -> "input keyevent 4"
            PanelAccessibilityService.ACTION_HOME -> "input keyevent 3"
            PanelAccessibilityService.ACTION_RECENTS -> "input keyevent 187"
            PanelAccessibilityService.ACTION_NOTIFICATIONS -> "cmd statusbar expand-notifications"
            PanelAccessibilityService.ACTION_QUICK_SETTINGS -> "cmd statusbar expand-settings"
            PanelAccessibilityService.ACTION_SPLIT_SCREEN -> "cmd statusbar toggle-split-screen"
            PanelAccessibilityService.ACTION_LOCK_SCREEN -> "input keyevent 223"
            PanelAccessibilityService.ACTION_SHOW_POWER_MENU -> "input keyevent --longpress 26"
            PanelAccessibilityService.ACTION_TAKE_SCREENSHOT -> "input keyevent 120"
            PanelAccessibilityService.ACTION_PREVIOUS_APP -> "input keyevent 187; sleep 0.2; input keyevent 187"
            else -> null
        }
        if (command == null) onResult(false) else executeAsync(command, onResult)
    }

}
