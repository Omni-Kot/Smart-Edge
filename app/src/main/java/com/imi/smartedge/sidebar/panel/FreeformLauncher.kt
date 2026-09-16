package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.graphics.Rect
import android.util.Log

object FreeformLauncher {
    fun launch(context: android.content.Context, intent: Intent) {
        val panelPrefs = PanelPreferences(context)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        // Add intent extras that some OEMs/ROMs respect for freeform launching
        intent.putExtra("android.intent.extra.WINDOWING_MODE", 5)
        intent.putExtra("android.intent.extra.LAUNCH_WINDOWING_MODE", 5)

        try {
            val options = android.app.ActivityOptions.makeBasic()
            val displayMetrics = context.resources.displayMetrics
            val w = displayMetrics.widthPixels
            val h = displayMetrics.heightPixels
            val prefersLandscape = detectLandscapeOrientation(context, intent.`package`, intent)

            val bounds: Rect = when (panelPrefs.freeformWindowMode) {
                PanelPreferences.FREEFORM_MODE_PORTRAIT -> {
                    val left = w / 3
                    val top = h / 15
                    Rect(left, top, w - left, h - top)
                }
                PanelPreferences.FREEFORM_MODE_MAXIMIZED -> Rect(0, 0, w, h)
                PanelPreferences.FREEFORM_MODE_CUSTOM -> {
                    val winW = (w * panelPrefs.freeformCustomWidth / 100.0).toInt()
                    val winH = (h * panelPrefs.freeformCustomHeight / 100.0).toInt()
                    val left = (w - winW) / 2
                    val top = (h - winH) / 2
                    Rect(left, top, left + winW, top + winH)
                }
                else -> {
                    if (prefersLandscape) {
                        // 16:9 wide aspect for games/landscape apps
                        val targetW = if (w > h) (w * 0.80).toInt() else (w * 0.90).toInt()
                        val targetH = (targetW / 1.77).toInt().coerceAtMost((h * 0.85).toInt())
                        val left = (w - targetW) / 2
                        val top = (h - targetH) / 2
                        Rect(left, top, left + targetW, top + targetH)
                    } else {
                        // 9:16 portrait aspect for normal apps (fixed for landscape host)
                        val targetH = (h * 0.85).toInt()
                        val targetW = (targetH * 9 / 16).toInt().coerceAtMost((w * 0.85).toInt())
                        val left = (w - targetW) / 2
                        val top = (h - targetH) / 2
                        Rect(left, top, left + targetW, top + targetH)
                    }
                }
            }
            options.launchBounds = bounds
            Log.d("PanelAppsAdapter", "Launching Freeform: pkg=${intent.`package`}, bounds=$bounds")

            // Use HiddenApiBypass instead of direct reflection to avoid F-Droid lint errors
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    org.lsposed.hiddenapibypass.HiddenApiBypass.invoke(
                        android.app.ActivityOptions::class.java,
                        options,
                        "setLaunchWindowingMode",
                        5
                    )
                    Log.d("PanelAppsAdapter", "HiddenApiBypass: setLaunchWindowingMode(5) success")
                }
            } catch (e: Exception) {
                Log.e("PanelAppsAdapter", "HiddenApiBypass fail: ${e.message}")
            }
            context.startActivity(intent, options.toBundle())
            Log.d("PanelAppsAdapter", "startActivity called with options")
        } catch (e: Exception) {
            context.startActivity(intent)
        }
    }

    private fun detectLandscapeOrientation(context: android.content.Context, packageName: String?, intent: Intent? = null): Boolean {
        val pkg = packageName ?: intent?.`package` ?: intent?.component?.packageName ?: return false
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            val component = launchIntent?.component ?: return false
            val activityInfo = context.packageManager.getActivityInfo(component, android.content.pm.PackageManager.GET_META_DATA)
            when (activityInfo.screenOrientation) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE -> true
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}
