package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A bottom sheet dialog that shows OEM-specific step-by-step guidance
 * for enabling the SidePanel accessibility service.
 */
class AccessibilityGuideDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AccessibilityGuideDialog"

        fun newInstance() = AccessibilityGuideDialog()

        // OEM detection helpers
        private fun manufacturer() = Build.MANUFACTURER.lowercase()
        private fun brand() = Build.BRAND.lowercase()

        private fun isMiui(): Boolean {
            return try {
                val clazz = Class.forName("android.os.SystemProperties")
                val getMethod = clazz.getMethod("get", String::class.java)
                val propValue = getMethod.invoke(null, "ro.miui.ui.version.name") as? String
                val isMiuiDevice = propValue != null && propValue.isNotBlank()
                isMiuiDevice
            } catch (e: Exception) {
                val m = manufacturer(); val b = brand()
                m.contains("xiaomi") || b.contains("xiaomi") ||
                        b.contains("redmi") || b.contains("poco")
            }
        }

        private fun isSamsung() = manufacturer().contains("samsung")
        private fun isOppoColorOS(): Boolean {
            val m = manufacturer(); val b = brand()
            return m.contains("oppo") || b.contains("oppo") ||
                    b.contains("realme") || b.contains("oneplus")
        }
        private fun isVivo(): Boolean {
            val m = manufacturer(); val b = brand()
            return m.contains("vivo") || b.contains("vivo")
        }
        private fun isHuawei(): Boolean {
            val m = manufacturer(); val b = brand()
            return m.contains("huawei") || b.contains("huawei") || b.contains("honor")
        }
    }

    // Data class for each step
    private data class OemGuide(
        val title: String,
        val subtitle: String,
        val icon: Int,            // drawable res (android system icon)
        val steps: List<String>
    )

    private fun detectGuide(): OemGuide {
        return when {
            isMiui() -> OemGuide(
                title = "MIUI / HyperOS",
                subtitle = "Xiaomi · POCO · Redmi",
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_scroll_down_and_tap_installed_apps),
                    getString(R.string.ui_find_smart_edge_and_tap_it),
                    getString(R.string.ui_toggle_the_switch_to_on),
                    getString(R.string.ui_tap_allow_on_the_confirmation_dialog)
                )
            )
            isSamsung() -> OemGuide(
                title = "One UI",
                subtitle = "Samsung Galaxy",
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_scroll_down_to_installed_apps),
                    getString(R.string.ui_tap_smart_edge),
                    getString(R.string.ui_toggle_the_switch_to_on),
                    getString(R.string.ui_tap_allow_on_the_prompt)
                )
            )
            isOppoColorOS() -> OemGuide(
                title = "ColorOS / Realme UI",
                subtitle = "OPPO · Realme · OnePlus",
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_tap_downloaded_apps_or_installed_services),
                    getString(R.string.ui_find_and_tap_smart_edge),
                    getString(R.string.ui_toggle_the_switch_to_on),
                    getString(R.string.ui_confirm_with_allow)
                )
            )
            isVivo() -> OemGuide(
                title = "FunTouch OS / OriginOS",
                subtitle = "Vivo",
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_scroll_to_installed_apps_section),
                    getString(R.string.ui_select_smart_edge),
                    getString(R.string.ui_turn_the_toggle_on),
                    getString(R.string.ui_tap_ok_to_confirm)
                )
            )
            isHuawei() -> OemGuide(
                title = "EMUI / HarmonyOS",
                subtitle = "Huawei · Honor",
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_go_to_installed_apps),
                    getString(R.string.ui_find_smart_edge_in_the_list),
                    getString(R.string.ui_enable_the_toggle),
                    getString(R.string.ui_tap_ok_to_confirm)
                )
            )
            else -> OemGuide(
                title = "Android",
                subtitle = getString(R.string.ui_stock_pixel_other),
                icon = android.R.drawable.ic_menu_manage,
                steps = listOf(
                    getString(R.string.ui_tap_open_settings_below),
                    getString(R.string.ui_go_to_downloaded_apps_or_installed_services),
                    getString(R.string.ui_find_smart_edge_and_tap_it),
                    getString(R.string.ui_toggle_the_switch_to_on),
                    getString(R.string.ui_tap_allow_on_the_confirmation)
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Build the entire UI programmatically for zero layout dependency
        val ctx = requireContext()
        val density = ctx.resources.displayMetrics.density

        val guide = detectGuide()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (24 * density).toInt(),
                (20 * density).toInt(),
                (24 * density).toInt(),
                (28 * density).toInt()
            )
        }

        // Drag handle
        val handle = View(ctx).apply {
            val lp = LinearLayout.LayoutParams((40 * density).toInt(), (4 * density).toInt())
            lp.gravity = android.view.Gravity.CENTER_HORIZONTAL
            lp.bottomMargin = (20 * density).toInt()
            layoutParams = lp
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 8 * density
                setColor(android.graphics.Color.parseColor("#33FFFFFF"))
            }
        }
        root.addView(handle)

        // Header row: icon + title/subtitle
        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (20 * density).toInt()
            layoutParams = lp
        }

        val iconBg = android.widget.FrameLayout(ctx).apply {
            val size = (48 * density).toInt()
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = (16 * density).toInt()
            layoutParams = lp
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setColor(android.graphics.Color.parseColor("#1A4A9EFF"))
            }
        }
        val iconView = ImageView(ctx).apply {
            setImageResource(android.R.drawable.ic_menu_manage)
            imageTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#4A9EFF")
            )
            val pad = (10 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        iconBg.addView(iconView)
        headerRow.addView(iconBg)

        val titleCol = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvTitle = TextView(ctx).apply {
            text = getString(R.string.ui_enable_accessibility)
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tvSubtitle = TextView(ctx).apply {
            text = getString(R.string.accessibility_guide_device, guide.title, guide.subtitle)
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#99FFFFFF"))
        }
        titleCol.addView(tvTitle)
        titleCol.addView(tvSubtitle)
        headerRow.addView(titleCol)
        root.addView(headerRow)

        // Divider
        val divider = View(ctx).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (1 * density).toInt()
            )
            lp.bottomMargin = (20 * density).toInt()
            layoutParams = lp
            setBackgroundColor(android.graphics.Color.parseColor("#1AFFFFFF"))
        }
        root.addView(divider)

        // Step list
        guide.steps.forEachIndexed { index, stepText ->
            val stepRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = (14 * density).toInt()
                layoutParams = lp
            }

            // Number bubble
            val numBubble = TextView(ctx).apply {
                text = context.getString(R.string.value_number, index + 1)
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#4A9EFF"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = android.view.Gravity.CENTER
                val size = (28 * density).toInt()
                val lp = LinearLayout.LayoutParams(size, size)
                lp.marginEnd = (14 * density).toInt()
                layoutParams = lp
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(android.graphics.Color.parseColor("#1A4A9EFF"))
                }
            }

            val tvStep = TextView(ctx).apply {
                text = stepText
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#E6FFFFFF"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            stepRow.addView(numBubble)
            stepRow.addView(tvStep)
            root.addView(stepRow)
        }

        // Spacer
        root.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (8 * density).toInt()
            )
        })

        // "Open Settings" button
        val btnOpen = Button(ctx).apply {
            text = getString(R.string.ui_open_accessibility_settings)
            setTextColor(android.graphics.Color.WHITE)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setColor(android.graphics.Color.parseColor("#4A9EFF"))
            }
            setPadding(0, (14 * density).toInt(), 0, (14 * density).toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                context.openAccessibilitySettings()
                dismiss()
            }
        }
        root.addView(btnOpen)

        // "Use System Automation instead" secondary button
        val btnAutomation = Button(ctx).apply {
            text = getString(R.string.ui_use_system_automation_instead)
            setTextColor(android.graphics.Color.parseColor("#4A9EFF"))
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setStroke((1 * density).toInt(), android.graphics.Color.parseColor("#4A9EFF"))
                setColor(android.graphics.Color.TRANSPARENT)
            }
            setPadding(0, (10 * density).toInt(), 0, (10 * density).toInt())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = (12 * density).toInt()
            layoutParams = lp
            setOnClickListener {
                SecureSettingsDialog.show(ctx) {
                    // Update preference if automation becomes possible
                    if (AutomationManager.isAutomationPossible()) {
                        PanelPreferences(ctx).useAutomationForGestures = true
                    }
                }
                dismiss()
            }
        }
        root.addView(btnAutomation)

        val tvNote = TextView(ctx).apply {
            text = getString(R.string.ui_automation_root_shizuku_can_handle_gestures_without_using_the_accessibility_service_s)
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#66FFFFFF"))
            gravity = android.view.Gravity.CENTER
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = (12 * density).toInt()
            layoutParams = lp
        }
        root.addView(tvNote)

        // Style the bottom sheet itself with dark background
        dialog?.setOnShowListener {
            val bottomSheet = (dialog as? com.google.android.material.bottomsheet.BottomSheetDialog)
                ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    24 * density, 24 * density,
                    24 * density, 24 * density,
                    0f, 0f, 0f, 0f
                )
                setColor(android.graphics.Color.parseColor("#1F2732"))
            }
        }

        return root
    }
}
